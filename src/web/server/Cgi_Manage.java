package web.server;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

// CatfoOD 2008.3.23

public final class Cgi_Manage implements IRequest {
	private final static Cgi_Manage cm = new Cgi_Manage();
	
	
	/** 返回Cgi_Manage的实例 */
	public static final Cgi_Manage get() {
		return cm;
	}	
	
	/** 让Cgi_Manage做好准备 */
	public static void Init() {}
	
	// ---------------------------------------------------------
	private final boolean cgiEnable;
	private final ICgi[] cgis;
	private int count = 0;
	
	private Cgi_Manage() {
		cgiEnable = CommonInfo.cgiEnable;
		if (!cgiEnable) {
			cgis = new ICgi[0];
			return;
		}
		
		ConfArray[] cas = ReadConfig.readToConfArray(CommonInfo.cgiConf);
		ArrayList cgilist = new ArrayList();
		
		for (int i=0; i<cas.length; ++i) {
			File f = new File(	CommonInfo.systemPath +
								File.separatorChar+ 
								cas[i].getName() );
			
			if (f.isFile()) {
				ConfArray[] cgiconf = ReadConfig.readToConfArray(f);
				String[] expNames = null;
				String[] indexFiles = null;
				String cgipath = null;
				String cginame = null;
				boolean enable = false;
				
				for (int x=0; x<cgiconf.length; ++x) {
					String main = cgiconf[x].getName();
					if (equal("expandName", main)) {
						expNames = cgiconf[x].getSubs();
					} 
					else if (equal("indexFile", main)) {
						indexFiles = cgiconf[x].getSubs();
					}
					else if (equal("cgiPath", main)) {
						cgipath = cgiconf[x].getSub();
					}
					else if (equal("name", main)) {
						cginame = cgiconf[x].getSub();
					}
					else if (equal("support", main)) {
						enable = Boolean.parseBoolean(cgiconf[x].getSub());
					}
					else {
						System.out.println( Language.unsupportConfigCommand+":"+
											cgiconf[x].getName());
					}
				}
				
				if (enable) {
					try {
					CgiBase cgi = new CgiBase(	cginame,
												cgipath,
												expNames,
												indexFiles,
												enable
											  ) {};
					cgilist.add(cgi);
					LogSystem.addToState(cgi);
					CommonInfo.registerIndexType(cgi.indexFile);
					} catch(Exception e) {
						// donothing
						// e.printStackTrace();
					}
				}
			}
		}
		cgis = new ICgi[cgilist.size()];
		for (int i=0; i<cgilist.size(); ++i) {
			cgis[i] = (ICgi)cgilist.get(i);
		}
	}

	/** 比较两个字符串是否相等,忽略大小写 */
	private static final boolean equal(final String a, final String b) {
		return a.compareToIgnoreCase(b)==0;
	}
	
	public void request(Object o, IResponsion ir) 
	throws CgiRequestException, CgiCannotSupport, Exception
	{
		if (!cgiEnable) throw new CgiCannotSupport();
		
		++count;
		if (o instanceof HttpHeadAnalyse) {
			HttpHeadAnalyse hha = (HttpHeadAnalyse)o;
			for (int i=0; i<cgis.length; ++i) {
				if (cgis[i].canDisposal(hha.getRequestFile())) {
					cgis[i].request(o, ir);
					return;
				}
			}
		}
		throw new CgiCannotSupport(Language.cgiRequestError+":"+o);
	}
	
	public boolean isCgi(HttpHeadAnalyse hha) {
		if (!cgiEnable) return false;

		for (int i=0; i<cgis.length; ++i) {
			if (cgis[i].canDisposal(hha.getRequestFile())) {
				return true;
			}
		}
		return false;
	}
}
