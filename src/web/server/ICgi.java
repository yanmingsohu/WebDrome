package web.server;
// CatfoOD 2008.3.23

import java.io.File;

/** CGI 实现接口 */
public interface ICgi extends IRequest {
	/**脚本文件使用的扩展名*/
	public String[] getExpandName();
	/**检查指定的文件是否能被当前的cgi处理*/
	public boolean canDisposal(File f);
	/**注册默认的主页文件*/
	public String[] registerIndexFile();
}
