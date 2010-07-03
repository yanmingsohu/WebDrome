package web.server;


import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

/**
 * @author CatfoOD
 */
public class ServerMachine implements Runnable,ITableItem {
	
	private ServerSocket server;
	private boolean stop = true;
	private boolean guiMode = false;
	private List socketlist;
	private Listener listener;
	private int port;
	private long totalLink = 0;
	
	private final boolean NoIPConnectLimit;
	private LinkedIPArray iplimit;

	public ServerMachine() {
		this(-1);
	}

	public ServerMachine(int port) {
		// 初始化配置文件
		CommonInfo.init();
		// 初始化语言配置
		Language.init();
		
		if (port>0) {
			CommonInfo.serverPort = port;
		}
		this.port = CommonInfo.serverPort;
		
		// 初始化IP链接限制
		NoIPConnectLimit = CommonInfo.ipConnectLimit==0;
		if (!NoIPConnectLimit) {
			iplimit = new LinkedIPArray(CommonInfo.ipConnectLimit);
		}
		// 初始化服务器状态
		stop = true;
		socketlist = new LinkedList();
		listener = new Listener();
		printWelcome();
	}
	
	public final void printWelcome() {
		print(VersionControl.programName+" "+
			  VersionControl.version+" Copyright CatfoOD 2008\n");
		print("Debug reporting: yanming-sohu@sohu.com QQ:412475540");
		print("Choice parameter: [-GUI|quiet] [-p:PORT]");
		print(LogSystem.line);
	}
	
	/** 服务器主线程 */
	public void run() {
		try {
			server = new ServerSocket(port);
		} catch(IOException e) {
			print("["+Language.error+"] "+port+" "+Language.portinUseError+".\n\n");
			JOptionPane.showMessageDialog(	null, 
											port+" "+Language.portinUseError+".", 
											Language.error, 
											JOptionPane.ERROR_MESSAGE, 
											null);
			// 如果出错应该立即退出系统
			System.exit(1);
			return;
		}
		
		try {
			print(	Language.serverAddress+":\t"+
					InetAddress.getLocalHost().getHostAddress()+"/" +
					InetAddress.getLocalHost().getHostName() );
		}catch(Exception e) {
			print(e);
		}
		print(Language.serverPort+":\t"+port);
		print(Language.serverBeginAt+":\t"+LogSystem.getDate());
		
		if (guiMode) {
			print("\n"+Language.closedWinExitpro+"..");
			LogSystem.GUIMessage();
			LogSystem.addToState(runtime);
			LogSystem.addToState(this);
			LogSystem.addToState(totalLinked);
			LogSystem.addToState(threadCount);
		}

		// 初始化过滤系统
		FilterSystem.init();
		// 初始化cgi管理器
		Cgi_Manage.Init();
		// 初始化文件管理器
		FileCacheManage.Init();
		// 初始化文件类型
		MimeTypes.init();
		// 初始化虚拟主机配置
		VirtualHostSystem.init();
		
		while (!stop) {
			while (socketlist.size()>=CommonInfo.maxConnect) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
			}
			try {
				Socket soket = server.accept();
				if (!NoIPConnectLimit) {
					// 如果指定的IP已经超过了链接上限,立即关闭链接并返回
					if ( !iplimit.addALink(soket.getInetAddress()) ) {
						soket.close();
						continue;
					}
				}
				SocketLink sl = new SocketLink(soket);
				socketlist.add(sl);
				++totalLink;
			} catch (IOException e) {
				out(Language.serverAcceptError+":["+e+"]");
			}
		}
	}

	/** 维护链接列表的线程 */
	private class Listener implements Runnable {
		public void run() {
			while (!stop || socketlist.size()!=0) {
				
				for (int i=0; i<socketlist.size(); ++i) {
					SocketLink is = (SocketLink)socketlist.get(i);
					if (is.isDisconnect()) {
						socketlist.remove(is);
						
						if (!NoIPConnectLimit) {
							iplimit.relaseALink(is.getInetAddress());
						}
					}
				}
				
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {}
			}
		}
	}
	
	/** 改变端口 */
	public void port(int p) {
		if (stop) {
			port = p;
		} else {
			throw new IllegalStateException(Language.cannotChangePort+".");
		}
	}
	
	public void GUIMode() {
		if (stop) {
			guiMode = true;
		} else {
			throw new IllegalStateException(Language.cannotChangeMode+".");
		}
	}
	
	public void start() {
		new Thread(this).start();
		new Thread(listener).start();
		stop = false;
	}
	
	public void stop() {
		stop = true;
	}

	private void out(Object out) {
		LogSystem.message(out.toString());
	}
	
	/** 直接向命令行窗口输出数据+cr */
	private void print(Object o) {
		System.out.println(o);
	}
	
	/** 直接向命令行窗口输出数据结尾不加cr */
	private void prind(Object o) {
		System.out.print(o);
	}

	public String getName() {
		return Language.ITI_linkcount;
	}

	/** 当前连接数 */
	public Object getVolume() {
		return socketlist.size();
	}
	
	public int getLinkConut() {
		return socketlist.size();
	}
	
	// ------------------------- 系统状态类 ---------------------------
	private ITableItem totalLinked = new ITableItem() {
		public String getName() {
			return Language.ITI_totallink;
		}
		public Object getVolume() {
			return totalLink;
		}
	};
	
	private ITableItem runtime = new ITableItem() {
		private long stime = System.currentTimeMillis();
		
		public String getName() {
			return Language.ITI_runtime;
		}
		public Object getVolume() {
			int i = (int)((System.currentTimeMillis()-stime)/1000);
			if (i>3600) {
				return i+Language.second+"("+(i/3600)+Language.hour+")";
			} else {
				return i+Language.second;
			}
		}
	};
	
	private ITableItem threadCount = new ITableItem() {
		public String getName() {
			return Language.ITI_threadcont;
		}

		public Object getVolume() {
			return Thread.activeCount();
		}
	};
}
