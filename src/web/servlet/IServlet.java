// CatfoOD 2008-10-31 下午08:13:08

package web.servlet;

/**
 * 服务端脚本实现的接口
 */
public interface IServlet {
	/**
	 * GET 请求，这个方法被调用
	 */
	void doGet(IServletRequest req, IServletResponse resp);
	
	/**
	 * POST 请求，这个方法被调用
	 */
	void doPost(IServletRequest req, IServletResponse resp);
}
