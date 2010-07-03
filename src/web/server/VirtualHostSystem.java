package web.server;
import java.io.File;

// CatfoOD 2008-3-28

public class VirtualHostSystem {
	private VirtualHostSystem() {}
	
	private static vhost[] hosts = new vhost[0];
	
	private static final String PATH = CommonInfo.webRootPath + File.separatorChar;
	private static final String defaultpath = PATH + CommonInfo.defaultRootPath;
	
	public final static void init() {
		if (!CommonInfo.vHostEnable) return;
		ConfArray[] ca = ReadConfig.readToConfArray(CommonInfo.virtualHost);
		hosts = new vhost[ca.length];
		int hInx = 0;
		for (int i=0; i<ca.length; ++i) {
			String sFile = PATH+ca[i].getSub(0);
			if (sFile!=null) {
				File f = new File(sFile);
				if (f.isDirectory()) {
					hosts[hInx] = new vhost();
					hosts[hInx].host = ca[i].getName();
					hosts[hInx].path = f;
					++hInx;
				}
			}
		}
	}
	
	/**
	 * 根据请求头域的host域获得本地文件夹的映射,一定会返回有效的路径
	 * @param hha - 请求头
	 * @return - 本地路径String;
	 */
	public final static String getVhost(HttpHeadAnalyse hha) {
		if (!CommonInfo.vHostEnable) return defaultpath;
		String host = hha.getHost();
		if (host!=null) {
			for (int i=0; i<hosts.length; ++i) {
				if (hosts[i]==null) break;
				if (hosts[i].host.compareToIgnoreCase(host)==0) {
					return hosts[i].path.getPath();
				}
			}
		}
		return defaultpath;
	}
}

/** 虚拟主机数据封装 */
class vhost {
	public String host;
	public File path;
}
