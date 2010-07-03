package web.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// CatfoOD 2008.11.1

public final class FileClassLoader extends ClassLoader {
	public FileClassLoader() {}
	
	/**
	 * 加载本地文件class的 类加载器
	 * @throws ClassNotFoundException 
	 */
	public Class<?> loadClass(String fileName) throws ClassNotFoundException {		
		try {
			Class <?> c = super.loadClass(fileName);
			if (c!=null) {
				return c;
			}
		} catch(Exception e) {
		}
		
		return loadClass(new File(fileName));
	}
	
	/**
	 * 加载本地文件class
	 * @throws ClassNotFoundException 
	 */
	public Class<?> loadClass(File f) throws ClassNotFoundException {
		Class<?> c = readForCache(f);
		if (c!=null) return c;

		byte[] cb = readForFile(f);
		if (cb==null || cb.length<1) {
			throw new ClassNotFoundException(f.getPath());
		}
		c = defineClass(getClassName(f), cb, 0, cb.length);
		cacheClass(f, c);
		return c;
	}
	
	private static byte[] readForFile(File f) throws ClassNotFoundException {
		long fileLength = f.length();
		long readLength = 0;
		
		if (fileLength>=Integer.MAX_VALUE) {
			return null;
		}
		
		byte[] readbuffer = new byte[(int) fileLength];
		try {
			FileInputStream in = new FileInputStream(f);
			readLength = in.read(readbuffer);
			if (readLength!=fileLength) {
				throw new ClassNotFoundException(
						"file read error: [fileLen:"+fileLength+"] [readLen:"+readLength+"]");
			}
			return readbuffer;
		} catch (IOException e) {
			throw new ClassNotFoundException(e.getMessage());
		}
	}
	
	private static Class<?> readForCache(File f) {
		if (f!=null) {
			return classcaches.get(f.getPath());
		}
		return null;
	}
	
	private static void cacheClass(File f, Class<?> c) {
		if (f!=null && c!=null) {
			classcaches.put(f.getPath(), c);
		}
	}
	
	private static String getClassName(File f) {
		String name = f.getName();
		int end = name.lastIndexOf('.');
		if (end==-1) {
			return null;
		}
		return name.substring(0, end);
	}
	
	private static Map<String, Class<?>> classcaches = new HashMap<String, Class<?>>();
}

