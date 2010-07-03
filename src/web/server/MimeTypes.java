package web.server;
// CatfoOD 2008-3-28

public class MimeTypes {
	private MimeTypes() {}
	
	private static ConfArray[] ca = new ConfArray[0];
	
	public final static void init() {
		ca = ReadConfig.readToConfArray(CommonInfo.miniTypeConf);
	}
	
	/**
	 * 寻找指定文件的Mini类型
	 * @param hha - 消息头包装类
	 * @return - Mini类型字符串,找不到返回null
	 */
	public final static String getMimeName(HttpHeadAnalyse hha) {
		String s = hha.getRequestFile().getName();
		int begin = s.lastIndexOf('.');
		if (begin>=0 && begin<s.length()-1) {
			s = s.substring(begin+1);
			for (int i=0; i<ca.length; ++i) {
				if (ca[i].findSub(s)) {
					return ca[i].getName();
				}
			}
		}
		return null;
	}
}
