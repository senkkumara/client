/**
 * Created on 2004-10-14
 * @author lld
 */
package ext.fast.capp.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import ext.fast.util.Base64;
import ext.fast.util.Tools;

public class ServerHelper implements CAPPConstants {

	/**
	 * 回调对象接口
	 */
	public interface HTTPCallback {
		/**
		 * 通知调用者进度
		 * @param total 总量
		 * @param progress 当前进度
		 */
		void setProgress(long total, long progress);
	}

	/**
	 * 回调适配器
	 */
	public static class HTTPCallAdapter {
		public void onInit() {
		}

		public void onComplete(HashMap<String, Object> result) {
		}

		public void onError(Exception e) {
		}

		public void onEnd(HashMap<String, Object> result) {
		}
	}

	/**
	 * Calculate the HTTP authorization field from userName and password
	 * @param user
	 * @param pass
	 * @return http basic authentication string
	 */
	public static String getBasicAuthorization(String user, String pass) {
		byte[] userPass = (user + ":" + pass).getBytes();
		return "Basic " + Base64.encodeBytes(userPass);
	}

	/**
	 * 启动一个服务调用线程
	 * @param title 进度窗口标题
	 * @param user 用户名
	 * @param pass 用户登录口令
	 * @param serviceUrl 服务URL
	 * @param task 任务参数
	 * @param files 上传／下载文件
	 * @param adapter 回调对象
	 */
	public static HashMap<String, Object> callServer(String title,
			HashMap<String, String> webInfo, HashMap<String, Object> task,
			ArrayList<HashMap<String, Object>> files, HTTPCallAdapter adapter) {
		if (adapter != null)
			adapter.onInit();

		ProgressWindow progWin = new ProgressWindow(title);
		HashMap<String, Object> result = new HashMap<String, Object>();
		new Thread() {
			ProgressWindow progWin;
			HashMap<String, String> webInfo;
			HashMap<String, Object> task;
			ArrayList<HashMap<String, Object>> files;
			HTTPCallAdapter adapter = null;
			HashMap<String, Object> _result;

			public void start(ProgressWindow progWin,
					HashMap<String, String> webInfo,
					HashMap<String, Object> task,
					ArrayList<HashMap<String, Object>> files,
					HTTPCallAdapter adapter, HashMap<String, Object> result) {
				this.progWin = progWin;
				this.webInfo = webInfo;
				this.task = task;
				this.files = files;
				this.adapter = adapter;
				this._result = result;
				start();
			}

			public void run() {
				HashMap<String, Object> result = null;
				try {
					String user = (String) webInfo.get(USER);
					String pass = (String) webInfo.get(PASS);
					String serviceUrl = (String) webInfo.get(SERVER_URL);
					result = callServer(user, pass, serviceUrl, task, files,
							progWin);
					_result.put(RESULT, result);
					if (adapter != null) {
						SwingUtilities.invokeLater(new Runnable() {
							HashMap<String, Object> result;

							public void run() {
								adapter.onComplete(result);
							}

							public Runnable get(HashMap<String, Object> result) {
								this.result = result;
								return this;
							}
						}.get(result));
					}
				} catch (Exception e) {
					SwingUtilities.invokeLater(new Runnable() {
						Exception e;

						public void run() {
							if (adapter != null)
								adapter.onError(e);
							else if (progWin.isWorking()) {
								String errMsg = Tools.getErrorMessage(e);
								JOptionPane.showMessageDialog(null, errMsg);
							}
						}

						public Runnable get(Exception e) {
							this.e = e;
							return this;
						}
					}.get(e));
				} finally {
					SwingUtilities.invokeLater(new Runnable() {
						HashMap<String, Object> result;

						public void run() {
							if (adapter != null)
								adapter.onEnd(result);
							progWin.close();
						}

						public Runnable get(HashMap<String, Object> result) {
							this.result = result;
							return this;
						}
					}.get(result));
				}
			}
		}.start(progWin, webInfo, task, files, adapter, result);
		progWin.show();
		return (HashMap<String, Object>) result.get(RESULT);
	}

	/**
	 * 调用服务器功能
	 * @param user 登录名
	 * @param pass 口令
	 * @param serviceUrl 服务器服务URL
	 * @param task 任务HashMap
	 * @param files 要上传的文件列表,也接收返回的文件列表
	 * @param callback 回调对象
	 * @return 返回结果HashMap
	 * @throws Exception
	 */
	public static HashMap<String, Object> callServer(String user, String pass,
			String serviceUrl, HashMap<String, Object> task, ArrayList<HashMap<String, Object>> files,
			Object progObj) throws Exception {
		HashMap<String, Object> result = null;
		HttpURLConnection conn = null;

		ProgressWindow progWin = null;
		HTTPCallback progCall = null;

		if (progObj instanceof ProgressWindow)
			progWin = (ProgressWindow) progObj;
		else if (progObj instanceof HTTPCallback)
			progCall = (HTTPCallback) progObj;

		try {
			// 加入客户端发布时的服务器端Id
			task.put("SERVER_ID", getServerId());

			// 包装数据
			File file1 = DataPacker.encode(task, files);
			File file2 = File.createTempFile("TMP", ".TMP");
			file1.deleteOnExit();
			file2.deleteOnExit();

			// 准备HTTP连接
			URL url = new URL(serviceUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/octet-stream");
			conn.addRequestProperty("AUTHORIZATION", getBasicAuthorization(user, pass));
			conn.setRequestProperty("Content-Length", Long.toString(file1.length()));

			// 准备发送数据
			OutputStream os = conn.getOutputStream();
			InputStream is = null;
			InputStream fis = new BufferedInputStream(new FileInputStream(file1));

			final int BUFFER_LENGTH = 1024;
			byte[] buffer = new byte[BUFFER_LENGTH];
			int len = 0;
			int total = fis.available();
			int progress = 0;

			try {
				// 发送数据
				if (progCall != null)
					progCall.setProgress(total, progress);
				else if (progWin != null)
					progWin.setProgress(total, progress);
				while ((len = fis.read(buffer)) >= 0) {
					os.write(buffer, 0, len);
					os.flush();
					progress += len;
					if (progCall != null)
						progCall.setProgress(total, progress);
					else if (progWin != null) {
						if (!progWin.isWorking())
							throw new Exception("用户中止了操作！");
						progWin.setProgress(total, progress);
					}
				}
				fis.close();

				if (progWin != null && !progWin.isWorking())
					throw new Exception("用户中止了操作！");

				// 接收数据
				total = conn.getContentLength();
				OutputStream fos = new BufferedOutputStream(
						new FileOutputStream(file2));

				try {
					is = conn.getInputStream();

					progress = 0;
					if (progCall != null)
						progCall.setProgress(total, progress);
					else if (progWin != null)
						progWin.setProgress(total, progress);
					while ((len = is.read(buffer)) >= 0) {
						fos.write(buffer, 0, len);
						progress += len;
						if (progCall != null)
							progCall.setProgress(total, progress);
						else if (progWin != null) {
							if (!progWin.isWorking())
								throw new Exception("用户中止了操作！");
							progWin.setProgress(total, progress);
						}
					}
					fos.flush();
				} finally {
					fos.close();
				}

				fis = new BufferedInputStream(new FileInputStream(file2));
				if (files != null)
					files.clear();
				result = (HashMap<String, Object>) DataPacker.decode(fis, files);
				fis.close();
			} finally {
				os.flush();
				os.close();
				if (is != null)
					is.close();
				fis.close();
				file1.delete();
				file2.delete();
			}
		} catch (Exception e) {
			if (e instanceof IOException && conn != null) {
				if (conn.getResponseCode() != HttpURLConnection.HTTP_UNAUTHORIZED) // 权限错误
					throw e;
				else
					throw new Exception(USER_AUTHORIZATION_FAIL);
			} else {
				while (e.getCause() != null)
					e = (Exception) e.getCause();
				throw e;
			}
		}

		if (result == null)
			throw new Exception("服务器端异常, 未返回执行结果!");

		HashMap<String, Object> m = (HashMap<String, Object>) result.get(MESSAGE);
		if (m != null) {
			String type = (String) m.get(TYPE);
			if (!SUCCESS.equalsIgnoreCase(type)) {// 功能错误
				String message = (String) m.get(MESSAGE);

				// 需更新客户端时，自动弹出窗口下载
				if (serviceUrl != null && message != null
						&& message.indexOf("请重新下载") > 0) {

					int pos = serviceUrl.indexOf("servlet");
					File jarFile = Tools.getJarFile(ServerHelper.class);
					System.err.println("jar file: " + jarFile);
					if (pos > 0
							&& jarFile != null
							&& jarFile.toString().toUpperCase()
									.endsWith(".EXE")) {
						String url = serviceUrl.substring(0, pos);
						url += "ext/capp/capp.exe";
						try {
							Runtime.getRuntime().exec("explorer.exe \"" + url + "\"");
						} catch (Throwable t) {
							t.printStackTrace();
							Logger.log(t);
						}
					}
				}
				throw new Exception(message);
			}
		} else {
			HashMap<String, Object> message = new HashMap<String, Object>();
			message.put(TYPE, SUCCESS);
		}

		return result;
	}

	private static String getServerId() {
		return "要求客户端版本-20070618";
	}
}