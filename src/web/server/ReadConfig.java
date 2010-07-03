package web.server;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

// CatfoOD 2008.3.28

/**
 * 读取配置文件到类的公有域
 * 
 * 配置文件中的格式:(BNF)
 * 		注释行	= "# | // | ;" CHAR
 * 		每个有效行= 配置名 *SP 数值
 * 		配置名	= *CHAR
 * 		数值		= *CHAR
 */
public class ReadConfig {

	private final static String[] comm = {"//", "#", ";"};
	private final static String PATH = CommonInfo.systemPath + File.separatorChar;
	private Class c;
	private Object ref;
	
	/**
	 * 被配置的类的域被认为是静态共有域
	 * @param cla - 要被配置的类
	 */
	public ReadConfig(Class cla) {
		c = cla;
		ref = null;
	}
	
	/**
	 * 被配置的类的域被认为是实例共有域
	 * @param o - 要被配置的实例
	 */
	public ReadConfig(Object o) {
		c = o.getClass();
		ref = o;
	}
	
	/**
	 * 被配置的类的域被认为是静态共有域
	 * @param cla - 要被配置类的类名
	 */
	public ReadConfig(String s) throws ClassNotFoundException {
		c = Class.forName(s);
		ref = null;
	}
	
	/**
	 * 读取配置文件到配置数组,并使用默认的系统文件路径,参考默认的系统路径CommonInfo.systemPath
	 * @param filename 配置文件名
	 */
	public static final ConfArray[] readToConfArray(String filename) {
		return readToConfArray(filename, true);
	}
	
	/** 
	 * 读取配置文件到配置数组
	 * @param defaultpath - 如果为true,使用默认的系统配置路径参考CommonInfo.systemPath<br>
	 *						否则,路径从安装文件夹开始计算
	 */
	public static final ConfArray[] 
	readToConfArray(String filename, boolean defaultpath) 
	{
		String finalFile;
		if (defaultpath) {
			finalFile = PATH+filename;
		} else {
			finalFile = filename;
		}
		return readToConfArray(new File(finalFile));
	}
	
	/** 读取配置文件到配置数组 */
	public static final ConfArray[] readToConfArray(File conffile) {
		try{
			ArrayList list = new ArrayList();
			
			BufferedReader in = new BufferedReader(
								new FileReader( conffile ) );
			String s = in.readLine();
			while (s!=null) {
				String[] conf = s.split("\t| ");
				if (conf.length>0 && 
					(!isCommentary(conf[0])) && conf[0].trim().length()>0) 
				{
					ConfArray ca = new ConfArray(conf[0]);
					int next = 1;
					while (next<conf.length) {
						if (conf[next].length()!=0) {
							ca.add(conf[next]);
						}
						++next;
					}
					list.add(ca);
				}
				s = in.readLine();
			}
			// 复制数组
			ConfArray[] confarr = new ConfArray[list.size()];
			for (int i=0; i<confarr.length; ++i) {
				confarr[i] = (ConfArray)list.get(i);
			}
			list.clear();
			return confarr;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return new ConfArray[0];
	}
	
	/**
	 * 从指定的文件读取配置到共有域,配置文件的路径根据CommonInfo.systemPath定义
	 * @param s - 文件名文件不存在抛出这个异常
	 * @throws FileNotFoundException
	 */
	public void readFromfile(String s) throws FileNotFoundException 
	{
		readFromfile(s, true, false);
	}
	
	/**
	 * 从指定的文件读取配置到共有域,配置文件的路径根据CommonInfo.systemPath定义
	 * @param s - 文件名
	 * @param defaultFile - 如果为false,不使用默认的配置文件路径
	 * @param unitePare - 是否把每一行除主键以外的参数合并为一个参数
	 * @throws FileNotFoundException - 文件不存在抛出这个异常
	 */
	public void readFromfile(String s, boolean defaultFile, boolean unitePare) 
	throws FileNotFoundException 
	{
		File f;
		if (defaultFile) {
			f = new File(PATH+s);
		} else {
			f = new File(s);
		}
		if (!f.isFile()) throw new FileNotFoundException(f.toString());
		
		read(f, (unitePare? 2 : 0));
	}
	
	private void read(File confFile, int splitLimit) {
		try {
		BufferedReader in = new BufferedReader(
							new FileReader( confFile ) );
		String s = in.readLine();
		while (s!=null) {
			String[] conf = s.split("\t| ",splitLimit);
			if (conf.length>1 && (!isCommentary(conf[0])) ) {
				try {
					int next = 0;
					while (conf[++next].length()==0);
					set(conf[0], conf[next].trim());
				}catch(Exception e) {
					LogSystem.error(confFile.getName()+
							Language.unsupportConfigCommand+":"+
							conf[0]+"\n"+e);
				}
			}
			s = in.readLine();
		}
		} catch(Exception ee) {
			System.out.println(ee);
		}
	}
	
	/**
	 * 将指定的域设置为指定的值
	 * @param parmname - 域名
	 * @param value - 值
	 * @throws Exception - 不存在的域,会抛出这个异常
	 */
	private final void set(String parmname, String value ) throws Exception {
		Field fs = c.getDeclaredField(parmname.trim());
		set(fs, value);
	}
	
	private final void set(Field f, String s) throws Exception {
		Object o = null;
		switch ( typeMap(f) ) {
		case 0:
			o = Boolean.valueOf(s); break;
		case 1:
			o = Byte.valueOf(s); break;
		case 2:
		case 3:
			// 暂不支持 char short
			break;
		case 4:
			o = Integer.valueOf(s); break;
		case 5:
			o = Long.valueOf(s); break;
		case 6:
			o = Float.valueOf(s); break;
		case 7:
			o = Double.valueOf(s); break;
		default:
			o = s;
		}
		if (o==null) throw new Exception();
		f.set(ref, o);
	}

	private final int typeMap(Field f) throws Exception {
		String s = f.getType().toString();
		final String[] type = { "boolean", "byte", "char",  "short", 
								"int",     "long", "float", "double", 
								"class java.lang.String"};
		for (int i=0; i<type.length; ++i) {
			if (s.equalsIgnoreCase(type[i])) return i;
		}
		throw new Exception(Language.unsupporttype+":"+s);
	}
	
	/**
	 * 字符串是否是注释行
	 * @param s - 要测试的字符串
	 * @return 是注释行返回true
	 */
	private final static boolean isCommentary(String s) {
		return (s.startsWith(comm[0])||
				s.startsWith(comm[1])||
				s.startsWith(comm[2]) );
	}
}

/**
 * 配置数据包装类
 */
final class ConfArray {
	/** 配置的名字 */
	private String name;
	/** 配置的内容,小写 */
	private List sub;
	
	public ConfArray(String mainName) {
		sub = new ArrayList();
		name = mainName;
	}
	/** 找到指定的内容返回true */
	public boolean findSub(String key) {
		return sub.contains(key.toLowerCase());
	}
	/** 添加一个子项并转换为小写 */
	public void add(String key) {
		sub.add(key.toLowerCase());
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * 得到指定位置的子键
	 * @param index - 索引
	 * @return 索引超出范围返回null,否则返回索引处的项目
	 */
	public String getSub(int index) {
		if (index<sub.size() && index>=0) {
			return (String)sub.get(index);
		}else{
			return null;
		}
	}
	
	/** 返回第一个参数 */
	public String getSub() {
		return getSub(0);
	}
	
	public int getSize() {
		return sub.size();
	}
	
	public String[] getSubs() {
		Object[] os = sub.toArray();
		String[] s = new String[os.length];
		for (int i=0; i<s.length; ++i) {
			s[i] = os[i].toString();
		}
		return s;
	}
	
	public String toString() {
		return name + sub.toString();
	}
}
