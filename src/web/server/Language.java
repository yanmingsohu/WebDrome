package web.server;
import java.io.File;

// CatfoOD 2008-4-2

/**
 * 存储本地字符串
 */
public final class Language {
	private Language() {}
	
	public static String parameterError = "参数错误";
	public static String second = "秒";
	public static String hour	= "小时";
	public static String error	= "错误";
	public static String request= "请求";
	
	// AboutDialog	--------
	public static String about 		= "关于"; 
	public static String closeButton= "关闭";
	public static String explainS1 	= "程序为免费软件,可以任意分发使用";
	public static String explainS2 	= "对于因使用本程序造成的任何损失,作者不负有任何责任";
	
	// Cgi_Manage	--------
	public static String cgiRequestError = "Cgi_Manage未知的请求";
	
	// CgiBase		--------
	public static String cgiSevereError			= "cgi严重错误";
	public static String cgiSupportClosed		= "(CGI)的支持被关闭";
	public static String cgiError 				= "cgi请求错误";
	public static String cgiErrorFileNotFound	= "请求的文件不存在";
	
	// CharBuffer	--------
	public static String arrayException = "源数组长度太短";
	
	// FileCache	--------
	public static String fileRecaches 			= "文件被更改需重新缓冲";
	public static String recacheOver 			= "文件缓冲完成";
	public static String canNotRecacheMessage 	= "不能重缓冲,有另一个线程正在访问这个缓冲";
	public static String fileChangedRangefall 	= "文件被更改,范围失效,请求需重新递交";
	public static String rangeFormatError 		= "范围参数不合法";
	
	// FileCacheManage	--------
	public static String freeMemory 			= "可用内存";
	public static String usedMemory 			= "内存用量";
	public static String fileCachedCount 		= "文件缓存数量";
	public static String MemoryFullFreeCache 	= "内存不足释放内存";
	public static String timeUPfreeCache 		= "小时,清理缓存";
	public static String clientClose 			= "客户端关闭了链接";
	public static String unknowError 			= "未知的错误";
	
	// FileCacheMonitorWindow	--------
	public static String reBrushList 		= "刷新列表";
	public static String table_filename 	= "文件名";
	public static String table_cachetime 	= "缓存时间(秒)";
	public static String table_usecount 	= "使用次数";
	public static String table_refcount 	= "引用计数";
	public static String table_useMem 		= "占用内存(字节)";
	public static String table_state 		= "状态";
	
	// FilterSystem	--------
	public static String fileinFilterListRebut = "在过滤列表中,请求驳回";
	
	// HttpHeadAnalyse	--------
	public static String URLDecoderException	= "URL解码错误"; 
	public static String unableCRLF 			= "发送的消息中不能有CR,LF";
	public static String lineEndnotisCRLF 		= "当前行不以CRLF结尾";
	public static String outputMessageException = "消息体已经发送,不能继续写入消息头";
	
	// LinkedIPArray	--------
	public static String atLinkLimit 	= "达到链接上限";
	public static String UPException 	= "使用次数已经达到上限";
	public static String DownException	= "使用次数已经为0,不能继续释放";
	
	// LogSystem	--------
	public static String startAt 			= "start At";
	public static String writeLogfileError 	= "不能写入日志文件";
	public static String dialog_error 		= "错误消息";
	public static String dialog_link 		= "链接消息";
	public static String dialog_httphead 	= "无效的Http数据请求";
	public static String dialog_state 		= "系统状态";
	public static String dialog_cache 		= "文件缓存状态";
	
	// ReadConfig	--------
	public static String unsupportConfigCommand = "文件中错误的配置,不支持的命令";
	public static String unsupporttype 			= "can not find type";
	
	// ServerMachine	--------
	public static String portinUseError 	= "端口已经被占用,请退出相关程序再重试";
	public static String serverAddress		= "服务器地址";
	public static String serverPort			= "服务器端口";
	public static String serverBeginAt		= "开始运行于";
	public static String closedWinExitpro	= "关闭这个窗口服务器会退出";
	public static String serverAcceptError	= "服务器连接侦听错误";
	public static String cannotChangePort 	= "服务器正在运行,不能改变端口";
	public static String cannotChangeMode 	= "服务器正在运行,不能改变显示模式";
	public static String ITI_linkcount 	= "当前链接数";
	public static String ITI_totallink 	= "总共接受的请求";
	public static String ITI_runtime 	= "系统运行的时间";
	public static String ITI_threadcont = "使用的线程";
	
	// SocketLink	--------
	public static String socketLinked 		= "已连接";
	public static String socketTimeout 		= "连接超时";
	public static String finishListen		= "结束对客户端的监听"; 
	public static String requestError 		= "请求错误";
	public static String cannotFindFile		= "找不到文件";
	public static String unknowRequest 		= "未知的请求";
	public static String readHttpHeadError 	= "读取HTTP头数据出错";
	public static String socketClosed 		= "关闭了连接";
	public static String sendFileError 		= "发送文件失败";
	public static String sendFile 			= "发送文件";
	
	// SystemIcon	--------
	public static String exitWebServer 		= "退出Web服务器";
	public static String systemIconinitError= "系统图标不能载入";
	public static String systemOnExit 		= "系统正在退出";
	public static String plaseUseTaskManageOnExit 	= "退出请用系统的进程管理器";
	
	// TableMessageWindow	--------
	public static String tablecol_statename = "服务器属性";
	public static String tablecol_state 	= "状态";
	
	/** 初始化本地语言设置 */
	public static void init() {
		String localLanguagefile = 	CommonInfo.languagePath + 
									File.separatorChar +
									CommonInfo.language;
		try {
			ReadConfig rc = new ReadConfig("web.server.Language");
			rc.readFromfile(localLanguagefile, false, true);
		} catch (Exception e) {
			System.out.println("读取语言配置文件出错:"+e);
		}
	}
}
