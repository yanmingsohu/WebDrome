package web.server;
// CatfoOD 2008.3.30

import java.util.ArrayList;
import java.net.InetAddress;
	
/**
 * 关于链接限制.<br>
 * <pre>
	HTTP/1.1 [8.2.4] 服务器过早关闭连接时客户端的行为:
	
	如果HTTP/1.1 客户端发送一条含有消息主体的请求消息，但不含值为"100-continue"
	的Expect请求头域,并且客户端直接与HTTP/1.1源服务器相连，并且客户端在接收到服务
	器的状态响应之前看到了连接的关闭，那么客户端应该重试此请求。在重试时，客户端可以
	利用下面的算法来获得可靠的响应。
	
	1． 向服务器发起一新连接。 
	2． 发送请请求头域。
	3． 初始化变量R，使R的值为通往服务器的往返时间的估计值（比如基于建立连接的时间），
	    或在无法估计往返时间时设为一常数值5秒。
	4． 计算T=R*（2**N），N为此前重试请求的次数。
	5． 等待服务器出错响应，或是等待T秒（两者中时间较短的）。
	6． 若没等到出错响应，T秒后发送请求的消息主体。 
	7． 若客户端发现连接被提前关闭，转到第1步，直到请求被接受，接收到出错响应，
	    或是用户因不耐烦而终止了重试过程。
	    
	在任意点上，客户端如果接收到服务器的出错响应，客户端
		--- 不应再继续发送请求， 并且 
		--- 应该关闭连接如果客户端没有完成发送请求消息。 
   </pre>
   利用这个机制,来实现ip连接数限制
 */
public final class LinkedIPArray {
	private ArrayList list;
	private int connectLimit;
	
	/**
	 * 新建链接数组
	 * @param limit - 允许每个IP最大的链接数
	 */
	public LinkedIPArray(int limit) {
		list = new ArrayList();
		connectLimit = limit;
	}
	
	/**
	 * 增加对ip的引用计数
	 * @return boolean - 未超过连接限制，返回true并将对ip的引用计数+1
	 */
	public synchronized boolean addALink(InetAddress ip) {
		if (ip!=null) {
			IPUse uip = findIP(ip);
			if (uip!=null) {
				if  ( !uip.isLimit() ) {
					uip.add();
					if (uip.usedCount>=connectLimit) {
						LogSystem.error(ip+" "+Language.atLinkLimit+".");
					}
					return true;
				}
				// else return false 在最后
			} else {
				list.add(new IPUse(ip));
				return true;
			}	
		}
		return false;
	}
	
	/**
	 * 指定的连接已经结束，释放这个ip
	 */
	public synchronized void relaseALink(InetAddress ip) {
		if (ip!=null) {
			IPUse uip = findIP(ip);
			if (uip!=null) {
				uip.release();
				if (uip.notUsed()) {
					list.remove(uip);
				}	
			}
			// else donothing
		} else {
			throw new NullPointerException();
		}
	}
	
	private IPUse findIP(InetAddress ia) {
		for (int i=0; i<list.size(); ++i) {
			if ( list.get(i).equals(ia) ) {
				return (IPUse)list.get(i);
			}
		}
		return null;
	}
	
	// inner class;
	private class IPUse {
		private InetAddress iadd;
		private int usedCount;
		
		public IPUse(InetAddress i) {
			iadd = i;
			usedCount = 1;
		}
		
		public int getUseCount() {
			return usedCount;
		}
		public InetAddress getInetAddress() {
			return iadd;
		}	
		public void add() {
			if (usedCount<connectLimit) {
				++usedCount;
			} else {
				throw new IllegalStateException(Language.UPException);
			}
		}
		public void release() {
			if (usedCount>0) {
				--usedCount;
			} else {
				throw new IllegalStateException(Language.DownException);
			}
		}	
		public boolean notUsed() {
			return usedCount==0;
		}
		public boolean equals(Object o) {
			return iadd.equals(o);
		}
		public boolean isLimit() {
			return usedCount>=connectLimit;
		}
	}
}
