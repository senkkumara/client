/*
 * Created on 2005-3-27 by lld
 */
package ext.fast.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Tools: created mainly for copyFile function
 */
public class Tools {

	public static void copyFile(File srcFile, File dstFile, boolean failIfExists)
			throws Exception {
		if (!srcFile.exists())
			throw new Exception("CopyFile Error: source file not exists.");

		if (dstFile.exists() && failIfExists)
			throw new Exception("CopyFile Error: destination file exists.");

		if (!dstFile.exists()) {
			// force create parent directors
			new File(dstFile.getParent()).mkdirs();
		} else
			dstFile.delete(); // readonly files must be deleted before open for
								// write

		FileInputStream is = new FileInputStream(srcFile);
		FileOutputStream os = new FileOutputStream(dstFile);
		try {
			os.getChannel().transferFrom(is.getChannel(), 0, srcFile.length());
		} finally {
			os.close();
			is.close();
		}
		dstFile.setLastModified(srcFile.lastModified());
	}

	public static void copyFile(File srcFile, File dstFile) throws Exception {
		copyFile(srcFile, dstFile, false);
	}

	public static void copyFile(String srcPath, String dstPath,
			boolean failIfExists) throws Exception {
		copyFile(new File(srcPath), new File(dstPath), failIfExists);
	}

	public static void copyFile(String srcPath, String dstPath)
			throws Exception {
		copyFile(new File(srcPath), new File(dstPath));
	}

	public static BufferedReader getBufferedReader(InputStream is,
			String encoding) throws UnsupportedEncodingException {
		return new BufferedReader(new InputStreamReader(is, encoding));
	}

	public static BufferedReader getBufferedReader(File file, String encoding)
			throws UnsupportedEncodingException, FileNotFoundException {
		return getBufferedReader(new FileInputStream(file), encoding);
	}

	public static PrintWriter getPrintWriter(OutputStream os, String encoding)
			throws UnsupportedEncodingException {
		return new PrintWriter(new OutputStreamWriter(os, encoding));
	}

	public static PrintWriter getPrintWriter(File file, String encoding)
			throws UnsupportedEncodingException, FileNotFoundException {
		return getPrintWriter(new FileOutputStream(file), encoding);
	}

	public static String getTime() {
		return getTime(new Date(), false);
	}

	public static String getTimeWithMils() {
		return getTime(new Date(), true);
	}

	public static String getTime(Date theDate) {
		return getTime(theDate, false);
	}

	public static String getTimeWithMils(Date theDate) {
		return getTime(theDate, true);
	}

	static String getTime(Date date, boolean withMils) {
		String pattern = "yyyyMMdd HH:mm:ss";
		if (withMils)
			pattern += ".SSS";
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		return sdf.format(date);
	}

	public static InetAddress parseIPv4Addr(String host) {
		host = host.trim();
		InetAddress inetAddr;
		try {
			inetAddr = InetAddress.getByName(host);
			return inetAddr;
		} catch (UnknownHostException e) {
			System.out
					.println("cannot find address for specified name, trying ip address string.");
		}

		int[] ipSeg = { 0, 0, 0, 0 };
		int j = 0;
		try {
			for (int i = 0; i < host.length(); i++) {
				if (j >= 4)
					throw new Exception("ip addr string too long: " + (j + 1));
				char c = host.charAt(i);
				if (c >= '0' && c <= '9') {
					ipSeg[j] = ipSeg[j] * 10 + c - '0';
					if (ipSeg[j] > 255)
						throw new Exception("ip segment too big: " + ipSeg[j]);
				} else if (c == '.')
					j++;
				else
					throw new Exception("illegal char in ip address: " + c);
			}
			byte[] ipBytes = new byte[4];
			for (int i = 0; i < 4; i++)
				ipBytes[i] = (byte) ipSeg[i];
			return InetAddress.getByAddress(ipBytes);
		} catch (Exception e1) {
			System.out.println(e1.getMessage());
			System.out.println("not an legal ip address string.");
		}

		return null;
	}

	public static String getClassDir(Class<?> klass) {
		URL url = Tools.class.getResource('/'
				+ klass.getName().replace('.', '/') + ".class");
		return new File(url.getFile()).getParent();
	}

	public static File getClassDirFile(Class<?> klass) {
		URL url = Tools.class.getResource('/'
				+ klass.getName().replace('.', '/') + ".class");
		return new File(url.getFile()).getParentFile();
	}

	public static File getClassFile(Class<?> klass) {
		URL url = Tools.class.getResource('/'
				+ klass.getName().replace('.', '/') + ".class");
		return new File(url.getFile());
	}

	public static File getJarFile(Class<?> klass) {
		String classPath = "/" + klass.getName().replace('.', '/') + ".class";
		String urlStr = Tools.class.getResource(classPath).toString();
		String head = "jar:file:/";
		String tail = "!" + classPath;
		if (urlStr.startsWith(head) && urlStr.endsWith(tail)) {
			String path = urlStr.substring(head.length(), urlStr.length()
					- tail.length());
			return new File(path);
		}

		return null;
	}

	public static File downloadFile(URL url) throws IOException {
		File temp = File.createTempFile("tmp", ".tmp");
		OutputStream os = null;
		InputStream is = null;

		try {
			os = new FileOutputStream(temp);
			is = url.openStream();
			byte[] buffer = new byte[8192];
			int len = 0;
			while ((len = is.read(buffer)) >= 0)
				os.write(buffer, 0, len);
		} finally {
			if (os != null)
				try {
					os.close();
				} catch (Exception e) {
				}
			if (is != null)
				try {
					is.close();
				} catch (Exception e) {
				}
		}

		return temp;
	}

	public static String ascii(String s) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c < 0xff)
				sb.append(c);
			else
				sb.append("\\u" + Integer.toHexString((int) c));
		}
		return sb.toString();
	}

	public static String jspRepair(String s) {
		return jspRepair(s, "UTF-8");
	}

	public static String jspRepair(String s, String realEncoding) {
		try {
			if (s == null)
				return null;
			return new String(s.getBytes("ISO-8859-1"), realEncoding);
		} catch (Exception ee) {
			ee.printStackTrace();
			return s;
		}
	}

	public static String changeEncoding(String s, String fromEncoding,
			String toEncoding) {
		try {
			return s == null ? null : new String(s.getBytes(fromEncoding),
					toEncoding);
		} catch (Exception ee) {
			ee.printStackTrace();
			return s;
		}
	}

	public static String getErrorMessage(Throwable t) {
		String msg = null;
		Throwable tt = t;
		Throwable tt_prev = t;
		for (int i = 1; tt != null && i < 10; i++) {
			msg = tt.getLocalizedMessage();
			if (msg == null)
				msg = tt.getMessage();
			tt_prev = tt;
			tt = tt.getCause();
			if (msg != null || tt == tt_prev)
				break;
		}

		if (msg == null || msg.equals(""))
			msg = t.toString();

		if (msg == null || msg.equals(""))
			msg = "(无错误信息!)";
		return msg;
	}

	public static void printError(Throwable t) {
		StackTraceElement ste = t.getStackTrace()[0];
		String fileTime = null;
		try {
			File classFile = getClassFile(Class.forName(ste.getClassName()));
			fileTime = " [MODIFIED@"
					+ getTime(new Date(classFile.lastModified())) + ']';
		} catch (Throwable e) {
		}
		System.err
				.println("\n-------------------------------------------------------------"
						+ "\n"
						+ getTime()
						+ " "
						+ ste.getFileName()
						+ fileTime
						+ "\n"
						+ "File line "
						+ ste.getLineNumber()
						+ ", in "
						+ ste.getClassName()
						+ "."
						+ ste.getMethodName()
						+ "(...):  " + "\n");
		t.printStackTrace(System.err);
		System.err
				.println("\n-------------------------------------------------------------\n");
	}

	public static void printError(Throwable t, PrintStream ps) {
		StackTraceElement ste = t.getStackTrace()[0];
		String fileTime = null;
		try {
			File classFile = getClassFile(Class.forName(ste.getClassName()));
			fileTime = " [MODIFIED@"
					+ getTime(new Date(classFile.lastModified())) + ']';
		} catch (Throwable e) {
		}
		ps.println("\n-------------------------------------------------------------"
				+ "\n"
				+ getTime()
				+ " "
				+ ste.getFileName()
				+ fileTime
				+ "\n"
				+ "File line "
				+ ste.getLineNumber()
				+ ", in "
				+ ste.getClassName()
				+ "."
				+ ste.getMethodName()
				+ "(...):  "
				+ "\n");
		t.printStackTrace(ps);
		ps.println("\n-------------------------------------------------------------\n");
	}

	public static String xmlEscape(String s) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; s != null && i < s.length(); i++) {
			char c;
			switch (c = s.charAt(i)) {
			case '&':
				buf.append("&amp;");
				break;
			case '<':
				buf.append("&lt;");
				break;
			case '>':
				buf.append("&gt;");
				break;
			case '"':
				buf.append("&quot;");
				break;
			case '\'':
				buf.append("&apos;");
				break;
			default:
				buf.append(c);
				break;
			}
		}

		return buf.toString();
	}

	public static String breakLine(String s, int lineLength) {
		if (s == null || s.length() <= lineLength)
			return s;

		StringBuffer buf = new StringBuffer();
		int iTotal = 0;
		int iLine = 0;
		int len = s.length();
		char ch;
		while (iTotal < len) {
			buf.append(ch = s.charAt(iTotal));

			iLine++;
			iTotal++;
			if (ch > 255)
				iLine++;
			else if (ch == '\n')
				iLine = 0;

			if (iLine >= lineLength) {
				iLine = 0;
				if (iTotal < len)
					buf.append('\n');
			}
		}

		return buf.toString();
	}

	public static void main(String[] args) {
		System.out.println(getClassDir(Tools.class));
		System.out.println(getClassDirFile(Tools.class));
		System.out.println(ascii("中文abcd"));
		try {
//			Integer a = null;
//			int b = a.intValue();
		} catch (Throwable t) {
			printError(t);
		}

		printError(new Throwable("TEST THROWABLE"));
	}
}
