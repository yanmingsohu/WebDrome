package web.server;
/*
 * Created on 2008-3-11
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * 应答接口
 */
public interface IResponsion {
	/**
	 * 回应对请求的操作,实现这个方法的对象应该让该方法立即返回
	 * @param o - 操作的数据一般为InputStream;
	 */
	public void responsion(Object o);
}
