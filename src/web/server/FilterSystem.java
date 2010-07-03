package web.server;
// CatfoOD 2008.3.25

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * 文件过滤器,根据 CommonInfo.exclude 域指定的文件加载过滤器,
 * 过滤器只对普通的文件请求有过滤作用,并不过滤对cgi文件的请求.
 * <pre>
 * 过滤文件的格式:
 *	注释行以:"# | // | ;"开始
 *	每行包含要滤除的扩展名,不区分大小写
 * <pre>
 */
public final class FilterSystem {
	private static String[] exclude = new String[0];
	
	public final static void init() {
		if (!CommonInfo.filterEnable) return;
		ConfArray[] ca = ReadConfig.readToConfArray(CommonInfo.exclude);

		exclude = new String[ca.length];
		for (int i=0; i<exclude.length; ++i) {
			exclude[i] = ca[i].getName();
		}
	}
	
	/**
	 * 过滤字符串
	 * @param s - 要过滤的字符串
	 * @throws FilterException - 当过滤关键字在字符串中出现,抛出这个异常
	 */
	public final static void exclude(String s) throws FilterException {
		if (!CommonInfo.filterEnable) return;
		for (int i=0; i<exclude.length; ++i) {
			if (s.toLowerCase().endsWith( exclude[i].toLowerCase() )) {
				throw new FilterException(s+" "+Language.fileinFilterListRebut+".");
			}
		}
	}
	
	/**
	 * 过滤文件
	 * @param s - 要过滤的文件
	 * @throws Exception - 当过滤关键字在文件名中出现,抛出这个异常
	 */
	public final static void exclude(File f) throws FilterException {
		if (!CommonInfo.filterEnable) return;
		exclude(f.getName());
	}

	/**
	 * 字符串是否是注释行
	 * @param s - 要测试的字符串
	 * @return 是注释行返回true
	 */
	private final static boolean isCommentary(String s) {
		final String[] comm = {"//", "#", ";"};
		return (s.startsWith(comm[0])||
				s.startsWith(comm[1])||
				s.startsWith(comm[2]) );
	}
}
