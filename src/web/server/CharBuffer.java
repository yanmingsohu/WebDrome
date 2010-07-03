package web.server;
// CatfoOD 2008.3.26

/**
 * 字符缓冲
 */
public class CharBuffer {
	private int DEFAULTLEN = 30;
	private byte[] buff = new byte[0];
	private int point = 0;
	
	/**
	 * 建立默认的缓冲区长度:30 
	 */
	public CharBuffer() {
		this(30);
	}
	
	/**
	 * 建立一个字符缓冲区,长度为len
	 * @param len - 缓冲区的长度
	 */
	public CharBuffer(int len) {
		buff = new byte[len];
		DEFAULTLEN = len;
	}
	
	/**
	 * 将字符添加到末尾
	 * @param c - 要添加的字符
	 */
	public void append(byte c) {
		if (point>=buff.length) {
			reAllotArray();
		}
		buff[point++] = c;
	}
	
	/**
	 * 将字符添加到末尾
	 */
	public void append(char c) {
		append((byte)c);
	}
	
	/**
	 * 将字符添加到缓冲区末尾
	 */
	public void append(int c) {
		append((byte)c);
	}
	
	private void reAllotArray() {
		byte[] newbuff = new byte[buff.length*2];
		copy(newbuff, buff);
		buff = newbuff;
		// 垃圾回收!!
		System.gc();
	}
	
	private void copy(byte[] src, final byte[] dec) {
		if (src.length<dec.length) 
			throw new IllegalArgumentException(Language.arrayException+".");
		
		for (int i=0; i<dec.length; ++i) {
			src[i] = dec[i];
		}
	}
	
	/**
	 * 字符缓冲区的字符串表示
	 */
	public String toString() {
		return new String(buff, 0, point);
	}
	
	/**
	 * 清空缓冲区,循环利用???
	 */
	public void delete() {
		buff = new byte[DEFAULTLEN];
		point = 0;
		System.gc();
	}
}
