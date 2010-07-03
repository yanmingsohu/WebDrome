package web.server;
// CatfoOD 2008.3.11

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 文件缓存管理器从程序运行时,就会一直存在,直至程序结束
 * 该类的实例只有一个,通过全局方法可以获得这个实例
 */
public class FileCacheManage implements IRequest,ITableItem,IFileCacheList {
	private static FileCacheManage instance = new FileCacheManage();
	
	/** 返回FileCacheManage类的唯一实列 */
	public static IRequest get() {
		return instance;
	}
	/** 让FileCacheManage做好准备 */
	public static void Init() {}
	
	//-----------------------------------------------------------------
	
	private ITableItem memory = new ITableItem() {
		public String getName() {
			return Language.freeMemory;
		}
		public Object getVolume() {
			Runtime r  = Runtime.getRuntime();
			usedMemory = r.totalMemory()-r.freeMemory();
			freeRatio  = r.freeMemory()*100/r.totalMemory();
			return freeRatio+ "%   ("+Language.usedMemory+":" +(usedMemory)/1024+"KB)";
		}
	};
	public volatile long usedMemory = 0;
	public volatile long freeRatio  = 100;
	
	/** 缓存文件组 */
	private List list;
	/** 请求队列 */
	private List requestQueue;
	
	private FileCacheManage() {
		list = new ArrayList();
		requestQueue = new LinkedList();
		new RequestQueueProcessor().start(); 
		
		LogSystem.addToState(this);
		LogSystem.addToState(memory);
		LogSystem.showCacheMonitor(this);
	}
	
	/* 所有的请求必须通过ir回调!否则会出现空的线程 */
	public synchronized void request(Object o, IResponsion ir) 
	throws FileNotFoundException 
	{
		requestQueue.add( new RequestData(o,ir) );
	}
	
	/**
	 * 返回一个文件的缓冲对象,如果文件没有缓冲,就建立他
	 * @param f - 有效的文件名,
	 * @return 一定会返回一个FileCache
	 * @throws FileNotFoundException - 
	 * 			cached内部会建立FileCache如果文件非法,会抛出这个异常
	 */
	private FileCache cached(File f) throws FileNotFoundException  {
		int cont = list.size();
		FileCache fc;
		for (int i=0; i<cont; ++i) {
			fc = (FileCache)list.get(i);
			if (fc.isCreated(f)) {
				if (fc.currentState()!=FileCache.BRUSHOFF) {
					return fc;
				} else {
					list.remove(fc);
				}
			}
		}
		fc = new FileCache(f);
		fc.beginCacheFile();
		list.add(fc);
		return fc;
	}

	/**
	 * 移除过期的文件缓存, 策略:
	 * 1.当内存不足时 - 可用的内存是总内存的10%
	 *   移除占用内存最大的5个文件缓存
	 *  
	 * 2.内存足够,时间达到2小时时;
	 *   移除 1小时的文件使用的次数低于1的文件
	 */
	private void removeCachedFile() {
		// 计算内存用量
		memory.getVolume();
		if (usedMemory>CommonInfo.maxMemoryUse || freeRatio<10) {
			out(Language.fileCachedCount+":"+list.size()+"."+Language.MemoryFullFreeCache);
			int[] max = {0,0,0,0,0};
			int index = 0;
			for (int x=0; x<list.size(); ++x) {
				int currt = ((FileCache)list.get(x)).useMemory();
				if ( max[index]<currt ) {
					max[index++] = x;
					if (index>=max.length) {
						index = 0;
					}
				}
			}
			removesFile(max, max.length);
		} else {
			if (lapseTime()>=CommonInfo.clearCacheTime) {
				out(CommonInfo.clearCacheTime+Language.timeUPfreeCache+"..");
				resetTime();
				int[] tolong = new int[list.size()];
				int index = 0;
				for (int i=0; i<list.size(); ++i) {
					int time = ((FileCache)list.get(i)).ontHourUseCount();
					if (time<CommonInfo.clearCacheTime) {
						tolong[index++] = i;
						if (index>=tolong.length) break;
					}
				}
				removesFile(tolong, index);
			}
		}
	}
	
	private void removesFile(int[] index, int count) {
		Object[] o = new Object[count];
		for (int i=0; i<o.length; ++i) {
			o[i] = list.get(index[i]);
		}
		for (int i=0; i<o.length; ++i) {
			if ( ((FileCache)o[i]).release() ) {
				list.remove(o[i]);
			}
		}
	}
	
	private long starttime = System.currentTimeMillis();
	/**
	 * 返回上次调用resetTime()经过的小时数
	 * @return - 返回一个int的小时数
	 */
	private long lapseTime() {
		long currenttime = System.currentTimeMillis();
		int passtime = (int)((currenttime-starttime)/1000/60/60);
		return passtime;
	}
	/**
	 * 重置时间,与lapseTime()配合使用
	 */
	private void resetTime() {
		starttime = System.currentTimeMillis();
	}
	
	/** 消息队列处理 */
	private class RequestQueueProcessor extends Thread {
		/** 没有"Range"域 */
		final Object NORANGE = null;
		public void run() {
			int delayremoveaction = 0;
			while (true) {
				if (requestQueue.size()>0) {
					RequestData rd = (RequestData)requestQueue.get(0);
					//--------
					HttpHeadAnalyse hha = rd.hha;
					InputStream in	= null;
					try {
						FileCache fc	= cached(rd.o);
						Range r 		= hha.getRange();
						
						if (r==NORANGE) {
							hha.println("HTTP/1.1 200 OK");
							in = fc.getInputStream();
							hha.setContentLength(fc.getFileLength());
						} else {
							hha.println("HTTP/1.1 206");
							r.setLastPos(fc.getFileLength()-1);
							in = cached(rd.o).getInputStream(r);
							hha.setContentRange(
									r.getFirstPos(), 
									r.getLastPos(), 
									fc.getFileLength() );
							hha.setContentLength(r.getLastPos()-r.getFirstPos()+1);
						}
						hha.setMimeType( MimeTypes.getMimeName(hha) );
						hha.printEnd();
						
					} catch(HTTPException e) {
						hha.error(e.getHttpErrorCode(), e.toString());
						
					} catch(IOException e) {
						// LogSystem.error(Language.clientClose+".");
						// donothing..
					} catch(Exception e) {
						LogSystem.error(Language.unknowError+":"+e);
						
					} finally {
						rd.ir.responsion( in );
						requestQueue.remove(0);
					}
				} else {
					if (++delayremoveaction>100) {
						delayremoveaction = 0;
						removeCachedFile();
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {}
				}
			}
		}
	}
	
	/** 请求包装类 */
	private class RequestData {
		File o;
		IResponsion ir;
		HttpHeadAnalyse hha;
		
		private RequestData(Object obj, IResponsion ir) throws FileNotFoundException {
			hha = (HttpHeadAnalyse)obj;
			o = hha.getRequestFile();
			if (!o.isFile()) throw new FileNotFoundException();
			this.ir= ir;
		}
	}
	
	private void out(Object o) {
		LogSystem.message(o);
	}

	public String getName() {
		return Language.fileCachedCount;
	}

	public Object getVolume() {
		return list.size();
	}

	public Object[] getFileList() {
		return list.toArray();
	}
}
