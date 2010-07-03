// CatfoOD 2008-10-31 下午07:40:58

package web.servlet;

import java.io.File;

import web.server.HttpHeadAnalyse;
import web.server.RequestErrCode;

public final class ServletManager {
	private ServletManager() {}
	
	/**
	 * jsc:
	 *   java servlet class
	 */
	private static final String classExName = ".class";
	private static final FileClassLoader cloader = new FileClassLoader();
	
	/**
	 * 立即处理对servlet的请求
	 * @param hha - HttpHeadAnalyse
	 * @return 可以处理返回true, 否则返回false
	 */
	public static boolean request(HttpHeadAnalyse hha) {
		File f = hha.getRequestFile();
		if (f!=null) {
			String name = f.getName();
			if (name.endsWith(classExName)) {
				DO(hha);
				return true;
			}
		}
		return false;
	}
	
	private static void DO(HttpHeadAnalyse hha) {
		try {
			Class<?> servletclass = cloader.loadClass(hha.getRequestFile());
			IServlet servlet = (IServlet) servletclass.newInstance();
			if (hha.isGET()) {
				ServletWrapper sw = new ServletWrapper(hha);
				servlet.doGet(sw, sw);
			}
			else if (hha.isPOST()) {
				ServletWrapper sw = new ServletWrapper(hha);
				servlet.doPost(sw, sw);
			}
			else {
				hha.error(RequestErrCode.E400, "must GET or POST.");
			}
		} catch (Exception e) {
			hha.error(RequestErrCode.E503, e.getMessage());
		}
	}
}
