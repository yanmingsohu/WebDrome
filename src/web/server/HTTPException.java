package web.server;
// CatfoOD 2008-3-30

public abstract class HTTPException extends Exception {
	private String errorMessage;
	
	public HTTPException(String mess) {
		errorMessage = mess;
	}
	public HTTPException() {
		errorMessage = "HTTP Error";
	}
	public String toString() {
		return errorMessage;
	}
	/**
	 * 每个继承的错误类必须实现这个方法,以确定错误的原因
	 * @return Http的错误代码,参考<b>RequestErrCode</b>
	 */
	public abstract int getHttpErrorCode();
}

/** 错误的Range格式 */
class BadRangeException extends HTTPException {
	public BadRangeException() {}
	public BadRangeException(String s) {
		super(s);
	}

	public int getHttpErrorCode() {
		return RequestErrCode.E416;
	}
}

/** Post方法错误 */
class CgiCannotSupport extends HTTPException {
	public CgiCannotSupport() {}
	public CgiCannotSupport(String s) {
		super(s);
	}

	public int getHttpErrorCode() {
		return RequestErrCode.E406;
	}
}

/** 过滤错误 */
class FilterException extends HTTPException {
	public FilterException() {}
	public FilterException(String s) {
		super(s);
	}

	public int getHttpErrorCode() {
		return RequestErrCode.E403;
	}
}

/** 当CGI请求错误时抛出这个异常 */
class CgiRequestException extends HTTPException {
	public CgiRequestException() {}
	public CgiRequestException(String s) {
		super(s);
	}

	public int getHttpErrorCode() {
		return RequestErrCode.E400;
	}
}

class CgiTimeOutException extends HTTPException {
	public CgiTimeOutException() {}
	public CgiTimeOutException(String s) {
		super(s);
	}

	public int getHttpErrorCode() {
		return RequestErrCode.E504;
	}
}

