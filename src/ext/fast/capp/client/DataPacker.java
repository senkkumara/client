/**
 * Generic data pack utility
 */
package ext.fast.capp.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import ext.fast.util.Base64;

/**
 * Created on 2005-7-13
 * @author lld
 */
public class DataPacker {
	private static File tmpDir = null;
	public static boolean deleteTempFileOnExit = true;

	public static final String NAME = "NAME";
	public static final String PATH = "PATH";
	public static final String SIZE = "SIZE";
	public static final String FILE = "FILE";

	/**
	 * Encode an object to String
	 * 
	 * @return String result
	 * @throws Exception
	 */
	public static String encode(Object data) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.useProtocolVersion(ObjectOutputStream.PROTOCOL_VERSION_2);
		oos.writeObject(data);
		oos.flush();
		oos.close();

		return Base64.encodeBytes(bos.toByteArray());
	}

	/**
	 * Decode an object from an encoded String
	 * 
	 * @param encoded
	 * @return
	 * @throws Exception
	 */
	public static Object decode(String encoded) throws Exception {
		byte[] bytes = Base64.decode(encoded);
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bis);

		return ois.readObject();
	}

	/**
	 * 完全克隆一个Serializable对象
	 * 
	 * @param o 要克隆的源对象
	 * @return 克隆生成的对象
	 * @throws Exception
	 */
	public static Object clone(Object o) throws Exception {
		return decode(encode(o));
	}

	/**
	 * 设定数据对象/文件列表包装和解包的临时文件目录,可以不必设定,主要用于测试
	 * @param tempDir 临时文件目录位置
	 */
	public static void setTempDir(String tempDir) {
		File dir = new File(tempDir);
		if (dir.exists())
			tmpDir = dir;
		else
			tmpDir = null;
	}

	/**
	 * 包装一个数据对象和一个文件列表
	 * 
	 * fileList的每个结点都是一个HashMap,包含以下信息: <br>
	 * PATH: 文件完整路径/文件File对象/输入流 NAME: 文件名, 不含路径 SIZE: 文件大小, 当PATH为无法取得剩余数的输入流时,
	 * 必需指定SIZE
	 * 
	 * @param data 数据对象
	 * @param fileList 文件列表
	 * @return 包装好的文件(可供发送)
	 * @throws IOException
	 */
	public static File encode(Object data, ArrayList<HashMap<String, Object>> fileList)
			throws IOException {
		File file = File.createTempFile("TMP", ".PAK", tmpDir);
		if (deleteTempFileOnExit)
			file.deleteOnExit();

		ObjectOutputStream oos = new ObjectOutputStream(
				new DeflaterOutputStream(new BufferedOutputStream(new FileOutputStream(file))));

		oos.useProtocolVersion(ObjectOutputStream.PROTOCOL_VERSION_2);
		oos.writeObject(data);

		if (fileList != null && fileList.size() > 0) {
			ArrayList<InputStream> iss = new ArrayList<InputStream>();
			for (int i = 0; i < fileList.size(); i++) {
				// fileInfo可以使用的键: NAME, PATH, SIZE
				HashMap<String, Object> fileInfo = fileList.get(i);
				if (fileInfo == null)
					continue;

				Object fileObj = fileInfo.get("PATH");
				if (fileObj instanceof InputStream) {
					InputStream is = (InputStream) fileObj;
					fileInfo.put(PATH, "");
					if (fileInfo.get(SIZE) == null)
						fileInfo.put(SIZE, new Long(is.available()));
					iss.add(is);
				} else if (fileObj instanceof File) {
					File f = (File) fileObj;
					InputStream is = new BufferedInputStream(
							new FileInputStream(f));
					fileInfo.put(PATH, f.getAbsolutePath());
					fileInfo.put(SIZE, new Long(((File) fileObj).length()));
					if (fileInfo.get(NAME) == null)
						fileInfo.put(NAME, f.getName());
					iss.add(is);
				} else {
					File f = new File(String.valueOf(fileObj));
					InputStream is = new BufferedInputStream(
							new FileInputStream(f));
					fileInfo.put(SIZE, new Long(f.length()));
					if (fileInfo.get(NAME) == null)
						fileInfo.put(NAME, f.getName());
					iss.add(is);
				}
			}

			oos.writeObject(fileList);

			for (int i = 0; i < iss.size(); i++) {
				InputStream is = (InputStream) iss.get(i);
				try {
					byte[] buf = new byte[8192];
					int len = 0;
					while ((len = is.read(buf)) >= 0) {
						oos.write(buf, 0, len);
					}
				} finally {
					is.close();
				}
			}
		} else {
			oos.writeObject(null);
		}

		oos.flush();
		oos.close();

		return file;
	}

	/**
	 * 解开一个数据对象和文件列表的包装包
	 * 
	 * @param is  包装输入流
	 * @param fileList 文件列表, 如需文件列表则需提供一个空列表
	 * @return 解包的数据对象
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object decode(InputStream is, ArrayList<HashMap<String, Object>> fileList)
			throws IOException, ClassNotFoundException {
		// 测试是否是空输入流
		PushbackInputStream pis = new PushbackInputStream(is);
		byte[] bytes = new byte[1];
		int bsize = 0;
		if ((bsize = pis.read(bytes)) < 0)
			return null;
		pis.unread(bytes, 0, bsize);

		ObjectInputStream ois = new ObjectInputStream(new InflaterInputStream(
				pis));
		Object data = ois.readObject();
		ArrayList<HashMap<String, Object>> fileList1 = (ArrayList<HashMap<String, Object>>) ois.readObject();

		if (fileList != null && fileList1 != null) {
			fileList.addAll(fileList1);

			for (int i = 0; i < fileList1.size(); i++) {
				HashMap<String, Object> fileInfo = fileList1.get(i);
				if (fileInfo != null) {
					File ff = File.createTempFile("TMP", ".UPK", tmpDir);
					fileInfo.put(PATH, ff.getAbsolutePath());
					fileInfo.put(FILE, ff);

					OutputStream os = new BufferedOutputStream(
							new FileOutputStream(ff));

					long length = ((Long) fileInfo.get(SIZE)).longValue();
					byte[] buffer = new byte[8192];
					int len = length < 8192 ? (int) length : 8192;
					while ((len = ois.read(buffer, 0, len)) >= 0) {
						os.write(buffer, 0, len);
						length -= len;
						len = length < 8192 ? (int) length : 8192;
						if (length <= 0)
							break;
					}

					os.flush();
					os.close();
				}
			}

			// !!! 这里不能关闭输入流, 输入流不应归本方法管理
			// 当ois为ServletInputStream时,关闭该流导致连接断开
			// ois.close();
		}

		return data;
	}
}