package web.server;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

// CatfoOD 2008.3.13

/**
 * 打印日志系统
 */
public final class LogSystem {
	public static final int lineLength = 79;
	public static final String line;
	public static final String filename = "";
	public static final FileOutputStream logout;
		
	static {
		// init FileOutputStream
		if (CommonInfo.printLogFile) {
			Calendar c = Calendar.getInstance();
			int y = c.get(Calendar.YEAR);
			int m = c.get(Calendar.MONTH)+1;
			int d = c.get(Calendar.DAY_OF_MONTH);
			String sd = y+""+(m<10?"0"+m:m)+""+(d<10?"0"+d:d)+".txt";
			
			File logf  = new File(CommonInfo.logPath+File.separatorChar+sd);
			FileOutputStream t_logout = null;
			try {
				t_logout = new FileOutputStream(logf, true);
				t_logout.write( ("\r\n#\r\n#   "+
						Language.startAt+":"+getDate()+"\r\n#\r\n").getBytes() );
				
			} catch(Exception o) {
				System.out.println(Language.writeLogfileError+":"+logf);
				o.printStackTrace();
			}
			logout = t_logout;
		} else {
			logout = null;
		}
		// init line;
		String t = "";
		for (int i=0; i<lineLength; ++i) {
			t += '-';  
		}
		line = t;
	}
	
	/** 不允许取得实例 */
	private LogSystem() {}
	
	/**
	 * 打印正常消息
	 */
	public final static void message(Object o) {
		String s = getDate() + o;
		if (messageWindow!=null) {
			messageWindow.append(s);
		} else {
			System.out.println("[Info] "+s);
		}
		printtoFile("[Info] "+s);
	}
	
	/**
	 * 打印错误消息
	 */
	public final static void error(Object o) {
		String s = getDate() + o;
		if (errorMesageWindow!=null) {
			errorMesageWindow.append(s);
		} else {
			System.out.println("[Err ] "+s);
		}
		printtoFile("[Err ] "+s);
	}
	
	/**
	 * 打印Http请求的头
	 */
	public final static void httpHead(HttpHeadAnalyse o) {
		StringBuffer sb = new StringBuffer();
		sb.append(getDate()+"HTTP:\r\n");
		sb.append(line+"\r\n");
		sb.append(o.toString()+"\r\n");
		sb.append(new String(o.getMessageBody())+"\r\n");
		sb.append(line+"\r\n");
		
		if (httpErrorWindow!=null) {
			httpErrorWindow.append(sb);
		} else {
			System.out.println(sb);
		}
		printtoFile(sb);
	}
	
	
	/**
	 * 添加一个状态项目到系统状态窗口
	 *  GUIMessage() 未被调用过,此方法不做任何相应
	 */
	public final static void addToState(ITableItem i) {
		if (stateWindow!=null) {
			stateWindow.add(i);
		}
	}
	
	/**
	 * 显示文件缓存监视器
	 * @param fcl - 需要一个文件列表源
	 */
	public final static void showCacheMonitor(IFileCacheList fcl) {
		if (cacheMonitor!=null) {
			cacheMonitor.show(fcl);
		}
	}
	
	/**
	 * 如果调用这个方法,后续的所有消息全部以非命令行的方式打印
	 */
	public static void GUIMessage() {
		if (windowCount==0) {
			errorMesageWindow = new MessageWindow(Language.dialog_error, computRect());
			messageWindow = new MessageWindow(Language.dialog_link, computRect());
			httpErrorWindow = new MessageWindow(Language.dialog_httphead, computRect());
			stateWindow = new TableMessageWindow(Language.dialog_state, computRect());
			
			Rectangle r = new Rectangle(0, h*2, w*2, h-startupHeight);
			cacheMonitor = new FileCacheMonitorWindow(Language.dialog_cache, r);
		}
	}
	
	/**
	 * 如果现在处于GUI模式返回真
	 */
	public static boolean isGUIMode() {
		return windowCount!=0;
	}
	
	private static Rectangle computRect() {
		int x = windowCount%2;
		int y = windowCount/2;
		Rectangle r = new Rectangle(x*w, y*h, w, h);
		++windowCount;
		return r;
	}
	
	public static final String getDate() {
		return new Date().toLocaleString()+' ';
	}
	
	/**
	 * 写入日志文件,
	 * @param o - 写入的内容
	 */
	public static final void printtoFile(Object o) {
		if (CommonInfo.printLogFile && logout!=null) {
			try {
				logout.write( (o.toString()+"\r\n").getBytes());
			} catch (IOException e) {
				System.out.println(Language.writeLogfileError+".");
				e.printStackTrace();
			}
		}
	}
	
	private static int windowCount = 0;
	private static final int startupHeight = 30;
	private static final int w = Toolkit.getDefaultToolkit().getScreenSize().width/2;
	private static final int h = Toolkit.getDefaultToolkit().getScreenSize().height/3;
	
	private static MessageWindow errorMesageWindow = null;
	private static MessageWindow messageWindow = null;
	private static MessageWindow httpErrorWindow = null;
	
	private static TableMessageWindow stateWindow = null;
	private static FileCacheMonitorWindow cacheMonitor = null;
}
