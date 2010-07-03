package web.server;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import web.servlet.ServletManager;

/**
 * @author CatfoOD
 */
public class SocketLink implements Runnable, IResponsion {
	
	private Socket soket;
	private volatile boolean stop = false;
	
	private InputStream in;
	private OutputStream out;
	private File sendfile;
		
	public SocketLink(Socket s) throws IOException {
		soket = s;
		soket.setSoTimeout(CommonInfo.socketReadOuttime);
		in  = s.getInputStream();
		out = s.getOutputStream();
		
		new Thread(this).start();
	}
	
	/** 一个web链接的主进程 */
	public void run() {
		out(f(soket)+Language.socketLinked);
		HttpHeadAnalyse hha = null;
		/** 如果客户端发出关闭消息为true */
		boolean clientisClosed = false;
		
		// 进入循环前设置connect++
		++connect;
		do { // 消息循环的开始, 直到客户端发送关闭链接的头域,或没有其他的消息时,循环才退出.
			
			try {
				hha = null;
				hha = new HttpHeadAnalyse(soket);
			} catch (SocketTimeoutException e) {
				LogSystem.error(f(soket)+
						Language.socketTimeout+','+Language.finishListen+".");
				break;
			} catch (IOException e) {
				LogSystem.error(f(soket)+
						Language.clientClose+','+Language.finishListen+".");
				break;
			}
			
			if (hha!=null) {
				if (!clientisClosed) clientisClosed = hha.isCloseConnect();
				
				hha.setBasePath( VirtualHostSystem.getVhost(hha) );
				
				if (ServletManager.request(hha)) {
					continue;
				}
				
				if (hha.isGET()) {
					sendfile = hha.getRequestFile();
					if ( sendfile!=null ) {
						try {
							if (Cgi_Manage.get().isCgi(hha)) {
								// 对脚本文件的请求
								Cgi_Manage.get().request(hha, this);
							} else {
								// 对普通文件的请求,先过滤
								FilterSystem.exclude(sendfile);
								FileCacheManage.get().request(hha, this);
							}
							// 下面的代码在请求成功后执行
							++connect;
							// 请求成功继续循环并等待回调,
							continue;
							
						} catch(HTTPException e) {
							error(f(soket)+e);
							hha.error(e.getHttpErrorCode(), e.toString());
							break;
							
						} catch(Exception e) {
							error(Language.requestError+":"+e);
							// 会转向下面的代码--应答一个404错误
						}
					}
					
					hha.error(RequestErrCode.E404, hha.getRequestURI());
					
					error(f(soket)+Language.requestError+","+
							Language.cannotFindFile+":"+hha.getRequestURI());
					
					LogSystem.httpHead(hha);
					break;
					
				} else if (hha.isPOST()) {
					try {
						Cgi_Manage.get().request(hha, this);
						++connect;
						continue;
						
					} catch(HTTPException e) {
						error(f(soket)+e);
						hha.error(e.getHttpErrorCode(), e.toString());
						break;
					
					} catch (Exception e) {
						error(Language.requestError+":"+e);
						// 其他的未知错误会应答下面的代码--404错误
					}
					hha.error(RequestErrCode.E404, hha.getRequestURI());
					LogSystem.httpHead(hha);
				} else {
					error(Language.unknowRequest+":"+f(soket));
				}
			} else {
				error(f(soket)+Language.readHttpHeadError);
			}
			// ----- 下面的代码在出错时执行,
			// 出错立即退出
			break;
			// -----
		} while( (!clientisClosed) );
		// 退出循环时设置 connect--;
		--connect;
		closeConnect();
	}
	
	/** 
	 * 尝试关闭连接,如果消息队列中为空,且处于超时状态<br>
	 * closeConnect()通过检测链接线程的数量(connect变量)
	 * 来判断是否应该关闭套接字
	 */
	private void closeConnect() {
		if (connect<=0) {
			out(f(soket)+Language.socketClosed);
			try {
				in.close();
				out.close();
				soket.close();
			}catch(Exception ee){}
			stop = true;
		}
	}
	/** 
	 * 这是很关键的终结变量,小心的设置它!!! 
	 * 设置他的方法必须'成对'出现
	 * 
	 * 每当一个新的<b>链接线程</b>被建立,connect加一,
	 * 当<b>链接线程</b>退出,connect减一
	 */
	private volatile int connect = 0;
	
	/** 检查这次握手是否已经结束 */
	public boolean isDisconnect() {
		return stop;
	}
	
	/** 文件缓存对请求的回调方法 */
	public void responsion(Object o) {
		if ( (o!=null) && (!stop) ) {
			if (o instanceof InputStream) {
				sendThread.send( (InputStream)o );
				// 回调成功,立即返回
				return;
			} else {
				throw new IllegalArgumentException("responsion unsupport class:"+o);
			}
		}
		--connect;
		closeConnect();
	}
	private SendThread sendThread = new SendThread();
	
	/** 开始发送文件 */
	private class SendThread extends Thread {
		private List inQueue;

		private SendThread() {
			inQueue = new LinkedList();
			this.start();
		}
		
		public void send(InputStream in) {
			inQueue.add(in);
		}
		
		public boolean isEmpty() {
			return inQueue.isEmpty();
		}
		
		public void run() {
			// 流量控制需要的变量
			final boolean SPEEDLIMIT = !(CommonInfo.downSpeedLimit<=0);
			final int waitTime = (int)(SPEEDLIMIT ?
				(float)CommonInfo.writeBufferSize/
				(CommonInfo.downSpeedLimit*1024)*1000 : 0);
			long useTime = 0;
			
			while (!stop) {
				while (inQueue.isEmpty()) {
					try {
						if (stop) return;
						sleep(50);
					} catch (InterruptedException e) {}
				}
				InputStream fin = (InputStream)inQueue.get(0);
				inQueue.remove(0); // 模仿队列
	
				byte[] buffer = new byte[CommonInfo.writeBufferSize];
				try {
					out(f(soket)+Language.sendFile+":"+sendfile);
					int len = fin.read(buffer);
					while (len>0) {
						out.write(buffer, 0, len);
						len = fin.read(buffer);
						
						if (SPEEDLIMIT) {
							useTime = System.currentTimeMillis() - useTime;
							if (useTime<waitTime) {
								try {
									Thread.sleep(waitTime-useTime);
								} catch (InterruptedException e) {}
							}
							useTime = System.currentTimeMillis();
						}
					}
					
				} catch (IOException e) {
					error(f(soket)+Language.sendFileError+":"+
							sendfile+" "+Language.clientClose+".");
				//	error("详细信息:"+e.getLocalizedMessage());
				} finally {
					try {
						fin.close();
						--connect;
						closeConnect();
					} catch (IOException e) {}
				}
			}
			return;
		}
	}
	
	public InetAddress getInetAddress() {
		return soket.getInetAddress();
	}
	
	/** 通过LogSystem打印信息 */
	private final void out(Object o) {
		LogSystem.message(o.toString());
	}
	/** 通过LogSystem打印错误信息 */
	private final void error(Object o) {
		LogSystem.error(o.toString());
	}
	/** 格式化Socket的输出 */
	private final String f(Socket s) {
		return s.getRemoteSocketAddress()+" ";
	}
}
