package web.server;
// CatfoOD 2008.3.13

public interface ICacheState {
	/** 文件名*/
	public String getFilename();
	/** 缓存时间(秒)*/
	public int cacheTime();
	/** 使用次数*/
	public int getUseCount();
	/** 当前引用次数*/
	public int referenceCount();
	/** 占用内存(字节)*/
	public int useMemory();
	/** 状态*/
	public String state();
}
