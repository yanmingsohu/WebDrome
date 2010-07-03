package web.server;
// CatfoOD 2008.3.24

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * 实现CgiBase的类属于过程化的类,每个cgi只需要一个实现,否则没有意义.
 */
public abstract class CgiBase implements ICgi, ITableItem {
	/** 回车 */
	public static final char cr = '\r';
	/** 换行 */
	public static final char lf = '\n';
	
	private int count = 0;
	
	/** 脚本支持的扩展名数组 */
	protected String[] expandName;
	/** 脚本可执行程序的完整路径 */
	protected String cgiPath;
	/** 脚本支持的默认主页文件 */
	protected String[] indexFile;
	/** 是否启用支持 */
	protected boolean support;
	/** 脚本的标识名字 */
	protected String name;
	
	public CgiBase(	String cginame,
					String path,
					String[] expname,
					String[] indexfile,
					boolean enable ) 
	{
		if (cginame==null || path==null || expname==null || indexfile==null) {
			throw new NullPointerException();
		}
		expandName = expname;
		cgiPath = path;
		indexFile = indexfile;
		support = enable;
		name = cginame;
	}
	
	public final boolean canDisposal(File f) {
		if (support) {
			if (f.isFile()) {
				// 把文件名子变为小写,防止因大小写不同字符相同源代码被盗窃
				String name = f.getName().toLowerCase();
				for (int i=0; i<expandName.length; ++i) {
					if (name.endsWith(expandName[i])) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public final String[] getExpandName() {
		return expandName;
	}
	
	public final void request(Object o, IResponsion ir) 
	throws CgiRequestException, Exception {
		HttpHeadAnalyse hha = (HttpHeadAnalyse)o;
		File f = hha.getRequestFile();
		if (f!=null) {
			++count;
			String[] env = getENV(hha);
			//if (canDisposal(f)) {
			try {
				File runPath = new File(cgiPath);
				Process cgi = Runtime.getRuntime().
						exec(cgiPath, env, runPath.getParentFile());
								
				writePostMessage(cgi.getOutputStream(), hha);
				
				// -- 防止CGI无限制等待 --
				readCgiOutput rco = new readCgiOutput(cgi);
				rco.start();
				try {
					rco.join(CommonInfo.socketReadOuttime);
				}catch(InterruptedException ie) {}
				if (!rco.complete) {
					cgi.destroy();
					throw new CgiTimeOutException("CGI "+Language.socketTimeout);
				}
				Object[] heads = rco.heads;
				byte[]   data  = rco.data;
				// -- --
				
				hha.println("HTTP/1.1 200 OK");
				for (int i=0; i<heads.length; ++i) {
					hha.println(heads[i].toString());
				}
				hha.setContentLength(data.length);
				hha.printEnd();

				ir.responsion(creatInput(data));
				return;
				
			} catch (IOException e) {
				throw e;
				
			} catch(CgiTimeOutException e) {
				throw new CgiTimeOutException(name+" "+Language.cgiError+":"+e);
			
			} catch (Exception e) {
				support = false;
				throw new CgiRequestException(Language.cgiSevereError+e+'\n'+
						name+Language.cgiSupportClosed);
			}
			//}
		} else {
			throw new CgiRequestException(name+Language.cgiErrorFileNotFound+".");
		}
	}
	
	private void writePostMessage(OutputStream out, HttpHeadAnalyse hha) 
	throws IOException
	{
		out.write(hha.getMessageBody());
		out.flush();
	}
	
	/** 初始化CGI环境参数 */
	private String[] getENV(HttpHeadAnalyse hha) {
		String[] env = new String[22];
		String scrname = sf(hha.getRequestFile().getName());
		String abspath = sf(hha.getRequestFile().getAbsolutePath());
		
		env[0] = "CONTENT_TYPE="	+	sf(hha.get("Content-Type"));
		env[1] = "PATH_TRANSLATED="	+	abspath;
		env[6] = "SCRIPT_NAME="		+	scrname;
		env[12]= "PATH_INFO="		+	
			sf( abspath.substring(0, abspath.length() - scrname.length()) );
		
		env[2] = "QUERY_STRING="	+	sf(hha.getArguments());
		env[3] = "REMOTE_ADDR="		+	sf(hha.getRemoteAddress().getHostAddress());
		env[4] = "REMOTE_HOST="		+	sf(hha.getRemoteAddress().getHostName());
		env[5] = "REQUEST_METHOD="	+	sf(hha.getMethod());
		env[7] = "SERVER_NAME="		+	sf(hha.getHost());
		env[8] = "SERVER_PORT="		+	CommonInfo.serverPort;
		env[9] = "SERVER_SOFTWARE="	+	VersionControl.programName+' '+
										VersionControl.version;
		
		env[10]= "SERVER_PROTOCOL=HTTP/1.1";
		env[11]= "GATEWAY_INTERFACE=CGI/1.1";
		env[13]= "REMOTE_IDENT=";
		env[14]= "REMOTE_USER=";
		env[15]= "AUTH_TYPE=";
		env[16]= "CONTENT_LENGTH="	+	(hha.getMessageBody().length>0 ?
										 hha.getMessageBody().length : "");
		
		env[17]= "ACCEPT=" 			+	sf(hha.get("Accept"));
		env[18]= "ACCEPT_ENCODING="	+	sf(hha.get("Accept-Encoding"));
		env[19]= "ACCEPT_LANGUAGE="	+	sf(hha.get("Accept-Language"));
		env[20]= "REFFERER="		+	sf(hha.get("Referer"));
		env[21]= "USER_AGENT="		+	sf(hha.get("User-Agent"));
		
		return env;
	}
	
	private final String sf(String s) {
		return s!=null ? s : "" ;
	}
	
	/** 读取CGI输出的头数据,一直读取到以一个空行结束并返回 */
	private Object[] readHead(InputStream in) throws IOException {
		ArrayList headlist = new ArrayList();
		String r = readLine(in);
		while (r!=null && r.trim().length()>0) {
			headlist.add(r);
			r = readLine(in);
		}
		return headlist.toArray();
	}
	
	/** 读取数据到字符串 */
	private byte[] readData(InputStream in) throws IOException {
		ByteArrayOutputStream array = new ByteArrayOutputStream(2000);
		byte[] buff = new byte[255];
		int readc = in.read(buff);
		while (readc>0) {
			array.write(buff, 0, readc);
			readc = in.read(buff);
		}
		return array.toByteArray();
	}
	
	/** 
	 * 从inputStream读取一行字符串,以cr&lf&crlf结尾 
	 * @throws IOException 
	 */
	public static String readLine(InputStream in) throws IOException {
		CharBuffer buff = new CharBuffer(150);
		int read = in.read();
		while (read>=0 && read!=cr && read!=lf) {
			buff.append(read);
			read = in.read();
		}
		if (read==cr) {
			read = in.read();
			if (read==lf) {
				return buff.toString();
			}
		}
		return buff.toString();
	}
	
	private InputStream creatInput(byte[] data) {
		return new cgiStream(data);
	}
	
	private class cgiStream extends InputStream {
		private byte[] data;
		private int p;
		private cgiStream(byte[] d) {
			data = d;
			p = 0;
		}

		public int read() throws IOException {
			if (data.length==0) 
				throw new IOException("is Closed.");
			
			if (p<data.length) {
				return toInt(data[p++]);
			} else {
				return -1;
			}
		}
		
		public void close() {
			data = new byte[0];
		}
	}

	protected final static int toInt(byte b){
		int r = 0;
		if (b<0) r = 256 + b;
		else r = (int)b;
		return r;
	}

	public final String[] registerIndexFile() {
		if (support) {
			return indexFile;
		} else {
			return null;
		}
	}

	public String getName() {
		return name+' '+Language.request;
	}

	public Object getVolume() {
		return count;
	}
	

	private class readCgiOutput extends Thread {
		Process cgi = null;
		public Object[] heads = null;
		public byte[] data = null;
		public boolean complete;
		
		public readCgiOutput(Process c) {
			cgi = c;
			complete = false;
		}
		
		public void run() {
			complete = false;
			try {
				heads = readHead(cgi.getInputStream());
				data  = readData(cgi.getInputStream());
				complete = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};
}
