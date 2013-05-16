/**
 * 读取、解析任务XML文件，调用相关功能，保存结果文件
 */
package ext.fast.capp.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ext.fast.util.Tools;

/**
 * Created on 2005-7-17
 * @author liuld
 */
public class Worker implements CAPPConstants {
	private Logger logger = Logger.getLogger();

	private HashMap<String, String> webInfo = new HashMap<String, String>();
	private String userName = null;
	private String password = null;
	private String serviceUrl = null;

	/**
	 * 构造函数
	 * @param user Windchill用户登录ID
	 * @param pass Windchill用户登录口令
	 */
	public Worker(String user, String pass) {
		userName = user;
		password = pass;
	}

	/**
	 * 执行由任务描述XML文件规定的任务
	 * 
	 * @param nameTask 任务描述XML文件的完整路径
	 */
	public int performTask(String nameTask) {
		HashMap<String, Object> task = null;
		HashMap<String, Object> result = null;
		String resultFileName = null;
		try {
			// 检查任务描述文件是否存在
			File fileTask = new File(nameTask);
			if (!fileTask.exists())
				throw new Exception("指定任务XML文件未找到: " + nameTask);

			// 读入任务描述信息
			String taskFileName = fileTask.getAbsolutePath();
			InputStream is = new BufferedInputStream(new FileInputStream(fileTask));
			resultFileName = getResultFileName(taskFileName);
			task = CAPPTaskHandler.readTask(is);
			result = performTask(task);
		} catch (Exception e) {
			Logger.log(e);
			if (result == null)
				result = new HashMap<String, Object>();

			HashMap<String, Object> message = new HashMap<String, Object>();
			message.put(TYPE, FAILURE);
			message.put(MESSAGE, Tools.getErrorMessage(e));
			result.put(MESSAGE, message);
			result.put(CAPP, task == null ? new LinkedHashMap<String, Object>() : task.get(CAPP));
			result.put(EXCEPTION, e);
		}

		boolean success = false;
		if (resultFileName != null)
			success = saveResult(resultFileName, result);

		Exception ee = (Exception) result.get(EXCEPTION);
		if (ee != null) {
			JOptionPane.showMessageDialog(null, Tools.getErrorMessage(ee), "警告", JOptionPane.ERROR_MESSAGE);
			// 这里的ee应该已经在前面log过
			Tools.printError(ee, logger);
		}

		// 未完成任务返回异常状态值1，直接退出
		if (!success) {
			if (result != task)
				return 1;
			else
				// result == task 表示用户选择了取消操作
				return 2;
		} else
			return 0;
	}

	/**
	 * 按task指定的任务描述执行服务调用任务，执行结果以HashMap方式保存
	 * @param task 任务描述
	 * @return 执行结果
	 */
	public HashMap<String, Object> performTask(HashMap<String, Object> task) {
		HashMap<String, Object> result = null;
		HashMap<String, Object> capp = null;

		try {
			capp = (HashMap<String, Object>) task.get(CAPP);
			if (capp == null)
				throw new Exception("未指定任务参数: <CAPP TYPE=\"...\">...</CAPP>");
			String type = (String) task.get(TYPE);
			String serverUrl = (String) capp.get(SERVER_URL);
			if (type == null)
				throw new Exception("未指定任务类型: <CAPP TYPE='?'>");
			if (serverUrl == null)
				throw new Exception("未指定Windchill服务器URL.");

			serviceUrl = getServiceUrl(serverUrl);
			webInfo.put(USER, userName);
			webInfo.put(PASS, password);
			webInfo.put(SERVER_URL, serviceUrl);

			// 根据任务描述信息调用相应方法

			// 4.1 查询并获取一个或多个零部件信息
			if (type.equals(SEARCH_PART)) { // 1, 零部件查询界面
				result = doWork_SearchPart(type, task, webInfo);
			}
			// 4.2 查询并获取一个文档信息
			// 4.3 查询并检出一个工艺文档
			// 4.4 查询并修订一个工艺文档
			// 4.5 查询并删除一个工艺文档
			else if (type.equals(SEARCH_DOCUMENT)
					|| type.equals(CHECKOUT_DOCUMENT)
					|| type.equals(REVISE_DOCUMENT)
					|| type.equals(DELETE_DOCUMENT)) { // 4, 文档查询界面
				result = doWork_SearchDocument(type, task, webInfo);
			}
			// 4.6 获取指定零部件信息
			// 4.7 获取指定文档信息
			// 4.8 检出指定工艺文档
			// 4.9 删除指定工艺文档
			else if (type.equals(QUICK_GET_PART)
					|| type.equals(QUICK_GET_DOCUMENT)
					|| type.equals(QUICK_CHECKOUT_DOCUMENT)
					|| type.equals(QUICK_REVISE_DOCUMENT)
					|| type.equals(QUICK_DELETE_DOCUMENT)) {
				result = doWork_DirectCall(type, task);
			}
			// 4.10 检入工艺文档
			else if (type.equals(CHECKIN_DOCUMENT)) { // 1, 文档检入界面
				result = doWork_CheckinDocument(type, task, webInfo);
			}
			// 4.11/4.13/4.14 获取文档流程签署信息/列出批注信息/下载指定名称批注信息
			else if (type.equals(GET_DOCUMENT_SIGNATURES)
					|| type.equals(LIST_MARKUP) || type.equals(GET_MARKUP)) { // 4,
																				// 无用户交互界面
				result = doWork_DirectCall(type, task);
			}
			// 4.12 保存批注信息
			else if (type.equals(SAVE_MARKUP)) { // 1, 批注保存界面
				String prompt = (String) capp.get(PROMPT);
				if (prompt == null || !prompt.equalsIgnoreCase("false"))
					result = doWork_SaveMarkup(type, task, webInfo);
				else
					result = doWork_DirectCall(type, task);
			}
			// 4.15 验证用户登录信息
			else if (type.equals(VERIFY_AUTHORIZATION)) {
				try {
					result = doWork_DirectCall(type, task);
				} catch (Exception e) {
					if (USER_AUTHORIZATION_FAIL.equals(e.getMessage())) {
						result = new HashMap();
						HashMap message = new HashMap();
						message.put(TYPE, FAILURE);
						message.put(MESSAGE, USER_AUTHORIZATION_FAIL);
						result.put(MESSAGE, message);
					} else
						throw e;
				}
			}
			// 4.16 获取虚拟目录列表
			// 4.17 获取流程任务信息
			else if (type.equals(LIST_DIR) || type.equals(LIST_WORKITEMS)
					|| type.equals(LIST_PART_BASELINES)
					|| type.equals(LIST_PART_DOCUMENTS)
					|| type.equals(SET_DOCUMENT_IN_CAPP_TASK)) {
				result = doWork_DirectCall(type, task);
			}
			// 4.18 打开流程任务界面
			// 4.19 打开零部件属性页面
			// 4.20 打开工艺文档属性页面
			else if (type.equals(OPEN_WORKITEM_PAGE)
					|| type.equals(OPEN_PART_PAGE)
					|| type.equals(OPEN_DOCUMENT_PAGE)) {
				result = doWork_DirectCall(type, task);

				String getUrlOnly = (String) capp.get(GET_URL_ONLY);
				ArrayList objList = (ArrayList) result.get(RESULT);
				if ((getUrlOnly == null || !getUrlOnly.equalsIgnoreCase(TRUE))
						&& objList != null && objList.size() > 0) {
					HashMap obj = (HashMap) objList.get(0);
					String url = (String) obj.get(OBJECT_URL);
					if (url != null) {
						String cmdLine = "explorer.exe \"" + url + "\"";
						Runtime.getRuntime().exec(cmdLine);
					}
				}
			}
			// 4.21 获取一个工装申请卡编号
			else if (type.equals(GET_TOOLING_NUMBER)) {
				result = FrameGetToolingNumber.getToolingNumber(task, webInfo);
			} else {
				throw new Exception("未知的任务类型: " + type);
			}

			// 返回值为null表示用户在用户界面选择了取消
			if (result != null) {
				result.put(CAPP, task.get(CAPP));
			} else {
				HashMap message = new HashMap();
				message.put(TYPE, FAILURE);
				message.put(MESSAGE, "用户取消操作");
				task.put(MESSAGE, message);
				result = task;
			}
		} catch (Exception e) {
			Logger.log(e);
			if (result == null)
				result = new HashMap();
			if (task.get(CAPP) != null)
				result.put(CAPP, task.get(CAPP));

			HashMap message = new HashMap();
			message.put(TYPE, FAILURE);
			message.put(MESSAGE, Tools.getErrorMessage(e));
			result.put(MESSAGE, message);
			result.put(EXCEPTION, e);
		}

		return result;
	}

	/**
	 * 根据任务描述文件名获取结果XML文件名
	 * 
	 * @param taskFileName 任务描述XML文件名(完整路径)
	 * @return 结果XML文件名(完整路径)
	 */
	public static String getResultFileName(String taskFileName) {
		String tmpName = taskFileName.toUpperCase();
		String resultFileName = null;
		if (tmpName.endsWith(".XML")) {
			tmpName = taskFileName.substring(0, taskFileName.length() - 4);
			resultFileName = tmpName + "_RESULT" + taskFileName.substring(taskFileName.length() - 4);
		} else {
			resultFileName = taskFileName + "_RESULT.XML";
		}

		return resultFileName;
	}

	/**
	 * 将任务执行结果保存到XML文件中
	 * 
	 * @param fileName
	 *            XML文件名
	 * @param result
	 *            服务调用结果HashMap
	 */
	public boolean saveResult(String fileName, HashMap result) {
		boolean success = true;

		try {
			File file = new File(fileName);
			OutputStream os = new BufferedOutputStream(new FileOutputStream(
					file));
			success = saveResult(os, result);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.log(e);
			success = false;
			JOptionPane.showMessageDialog(null, Tools.getErrorMessage(e), "警告",
					JOptionPane.ERROR_MESSAGE);
		}

		return success;
	}

	/**
	 * 将结果XML文件内容写入到输出流
	 * 
	 * @param os
	 *            输出流
	 * @param result
	 *            执行结果HashMap
	 * @return 服务调用是否成功
	 */
	public boolean saveResult(OutputStream os, HashMap result) {
		PrintStream ps = null;
		try {
			if (os instanceof PrintStream)
				ps = (PrintStream) os;
			else
				ps = new PrintStream(os, true, "GB2312");
		} catch (UnsupportedEncodingException e) {
			Logger.log(e);
			ps = new PrintStream(os, true);
		}
		ps.println("<?xml version=\"1.0\" encoding=\"gb2312\"?>");
		HashMap capp = (HashMap) result.get(CAPP);
		if (capp != null) {
			Object type = result.get(TYPE);
			result.remove(TYPE);
			if (capp.get(TYPE) == null)
				capp.put(TYPE, type);
		}

		ps.println("<cappdata>");
		writeXMLElement(ps, CAPP, capp, 1);

		LinkedHashMap sMap = null;
		if (result instanceof LinkedHashMap)
			sMap = (LinkedHashMap) result;

		ArrayList keyList = new ArrayList(result.keySet());
		for (int i = 0; i < result.size(); i++) {
			String key = (String) keyList.get(i);
			if (key.equalsIgnoreCase(CAPP))
				continue;
			if (key.equalsIgnoreCase(MESSAGE))
				continue;
			if (key.equalsIgnoreCase("SERVER_ID"))
				continue;
			writeXMLElement(ps, key, result.get(key), 1);
		}

		// 输出执行状态
		String msgType = null;
		String msgText = null;
		HashMap message = (HashMap) result.get(MESSAGE);
		if (message != null) {
			msgType = (String) message.get(TYPE);
			msgText = (String) message.get(MESSAGE);
		}
		if (msgType == null)
			msgType = SUCCESS;
		if (msgText == null)
			msgText = ""; // "EMPTY MESSAGE";

		msgText = Tools.xmlEscape(msgText);
		ps.println(getIndent(1) + "<message type=\"" + msgType + "\">"
				+ msgText + "</message>");
		ps.println("</cappdata>");

		// 获取功能调用执行状态
		return msgType.equalsIgnoreCase(SUCCESS);
	}

	/**
	 * 显示界面输入零部件搜索条件，执行零部件搜索功能
	 * @param cappType
	 * @param task 任务描述参数Map
	 * @return 任务描述参数Map+选中的一个或多个零部件数据
	 * @throws Exception
	 */
	private HashMap<String, Object> doWork_SearchPart(String cappType, HashMap<String, Object> task,
			HashMap<String, String> webInfo) throws Exception {
		return FrameSearchPart.searchPart(task, webInfo);
	}

	/**
	 * 显示界面输入文档搜索条件，执行文档搜索功能, 并按需要对选中文档做相应操作，用户选择取消时，
	 * 设定退出值2直接退出，当操作为检出、修订、删除、预先更改时，在用户选中后做相应远程调用
	 * 
	 * @param cappType
	 * @param task 任务描述参数Map
	 * @return 任务描述参数Map + 选中的一个文档数据和相关操作结果
	 * @throws Exception
	 */
	private HashMap<String, Object> doWork_SearchDocument(String cappType, HashMap<String, Object> task,
			HashMap<String, String> webInfo) throws Exception {
		return FrameSearchDocument.searchDocument(task, webInfo);
	}

	/**
	 * 显示界面供用户输入文档检入参数，执行文档检入功能，用户选择取消时，设定退出值2直接退出
	 * 
	 * @param cappType
	 *            *
	 * @param task
	 *            任务描述参数Map
	 * @return 任务描述参数Map + 文档检入后的文档基本信息
	 * @throws Exception
	 */
	private HashMap doWork_CheckinDocument(String cappType, HashMap task,
			HashMap webInfo) throws Exception {
		return FrameCheckinDocument.checkinDocument(task, webInfo);
	}

	/**
	 * 显示界面供用户输入批注保存信息，执行批注保存功能
	 * 
	 * @param cappType
	 * @param task
	 * @return
	 * @throws Exception
	 */
	private HashMap doWork_SaveMarkup(String cappType, HashMap task,
			HashMap webInfo) throws Exception {
		return FrameSaveMarkup.saveMarkup(task, webInfo);
	}

	/**
	 * 不显示用户交互界面，直接做远程调用
	 * 
	 * @param cappType
	 * @param task
	 * @return
	 * @throws Exception
	 */
	private HashMap<String, Object> doWork_DirectCall(String cappType, HashMap<String, Object> task)
			throws Exception {
		HashMap<String, Object> data = new HashMap<String, Object>();
		ArrayList<HashMap<String, Object>> files = getFileList(task);
		if (files == null)
			files = new ArrayList();

		ServerHelper.callServer(null, webInfo, task, files,
				new ServerHelper.HTTPCallAdapter() {
					HashMap data;

					public void onComplete(HashMap result) {
						data.put(RESULT, result);
					}

					public void onError(Exception e) {
						data.put(EXCEPTION, e);
					}

					public void onEnd(HashMap result) {
						data.put("FINISHED", "true");
					}

					public ServerHelper.HTTPCallAdapter get(HashMap data) {
						this.data = data;
						return this;
					}
				}.get(data));

		if (data.get(EXCEPTION) != null)
			throw (Exception) data.get(EXCEPTION);

		HashMap result = (HashMap) data.get(RESULT);

		if (cappType.equalsIgnoreCase(QUICK_GET_PART))
			fixPartInfo(task, result);

		fillFileParams(result, files);

		if (result != null)
			result.put(TYPE, cappType);

		return result;
	}

	public void fixPartInfo(HashMap task, HashMap result) throws Exception {
		HashMap capp = (HashMap) task.get(CAPP);
		String ibaCSV = (String) capp.get(IBA_LIST);
		Object[] ibas = null;
		if (ibaCSV != null && !ibaCSV.equals("")) {
			if (!ibaCSV.equals("*"))
				ibas = ibaCSV.split(",");
		}

		HashMap pi = (HashMap) ((ArrayList) result.get(PART)).get(0);
		HashMap pp = new LinkedHashMap();
		((ArrayList) result.get(PART)).set(0, pp);

		pp.put(NAME, pi.get(NAME));
		pp.put(NUMBER, pi.get(NUMBER));
		pp.put(VERSION, pi.get(VERSION));
		pp.put(PRODUCT_NAME, pi.get(PRODUCT_NAME));
		pp.put(PRODUCT_NUMBER, pi.get(PRODUCT_NUMBER));
		pp.put(PARENT_NUMBER, pi.get(PARENT_NUMBER));
		pp.put(VIEW, pi.get(VIEW));

		Object[] _ibas = ibas;
		if (ibas == null) {
			ArrayList ibaNameList = (ArrayList) pi.get(TYPE_IBA_LIST);
			if (ibaNameList != null)
				_ibas = ibaNameList.toArray();
		}
		for (int j = 0; _ibas != null && j < _ibas.length; j++) {
			if (_ibas[j] == null)
				continue;
			String ibaName = String.valueOf(_ibas[j]).trim();
			if (!ibaName.equals(""))
				pp.put(ibaName, pi.get(ibaName));
		}
	}

	/**
	 * 根据fileList生成result结果中的PRIMARY_FILE, SECONDARY_FILE参数
	 * @param result *
	 * @param fileList  *
	 * @throws Exception
	 */
	public static void fillFileParams(HashMap<String, Object> result, ArrayList fileList)
			throws Exception {
		if (result == null || fileList == null)
			return;

		HashMap mm = null;
		for (Iterator it = result.keySet().iterator(); it.hasNext();) {
			Object val = result.get(it.next());
			if (val instanceof ArrayList) {
				ArrayList valList = (ArrayList) val;
				if (valList.size() > 0 && valList.get(0) instanceof HashMap) {
					HashMap valEle = (HashMap) valList.get(0);
					if (valEle.get(PRIMARY_FILE) != null) {
						mm = valEle;
						break;
					}
				}
			}
		}

		for (int i = 0; i < fileList.size(); i++) {
			String FILE = i == 0 ? PRIMARY_FILE : SECONDARY_FILE;
			HashMap fi = (HashMap) fileList.get(i);
			if (fi == null) {
				mm.put(FILE, "");
			} else {
				File file = (File) fi.get(DataPacker.FILE);
				String name = (String) fi.get(DataPacker.NAME);

				// 文件更改为原名
				File newFile = new File(file.getParent() + File.separator
						+ name);
				if (!file.equals(newFile)) {
					Tools.copyFile(file, newFile);
					file.delete();
				}
				Object existing = mm.get(FILE);
				String path = newFile.getAbsolutePath();
				if (existing == null)
					mm.put(FILE, path);
				else if (existing instanceof List)
					((List) existing).add(path);
				else {
					if (existing.equals(""))
						mm.put(FILE, path);
					else {
						List files = new ArrayList();
						files.add(existing);
						files.add(path);
						mm.put(FILE, files);
					}
				}
			}
		}
	}

	/**
	 * 根据fileList生成结果中文档属性继对象的PRIMARY_FILE, SECONDARY_FILE参数
	 * @param docInfo
	 * @param fileList
	 * @throws Exception
	 */
	public static void fillDocFileParams(HashMap docInfo, ArrayList fileList)
			throws Exception {
		if (docInfo == null || fileList == null)
			return;

		for (int i = 0; i < fileList.size(); i++) {
			String FILE = i == 0 ? PRIMARY_FILE : SECONDARY_FILE;
			HashMap fi = (HashMap) fileList.get(i);
			if (fi == null) {
				docInfo.put(FILE, "");
			} else {
				File file = (File) fi.get(DataPacker.FILE);
				String name = (String) fi.get(DataPacker.NAME);

				// 文件更改为原名
				File newFile = new File(file.getParent() + File.separator
						+ name);
				if (!file.equals(newFile)) {
					Tools.copyFile(file, newFile);
					file.delete();
				}
				Object existing = docInfo.get(FILE);
				String path = newFile.getAbsolutePath();
				if (existing == null)
					docInfo.put(FILE, path);
				else if (existing instanceof List)
					((List) docInfo.get(FILE)).add(path);
				else {
					if (existing.equals(""))
						docInfo.put(FILE, path);
					else {
						List files = new ArrayList();
						files.add(existing);
						files.add(path);
						docInfo.put(FILE, files);
					}
				}
			}
		}
	}

	/**
	 * 根据Windchill的URL获取CAPPProcessor的远程调用URL
	 * @param serverUrl  Windchill URL
	 * @return CAPPProcessor远程调用URL
	 */
	public static String getServiceUrl(String serverUrl) {
		if (!serverUrl.endsWith("/"))
			serverUrl += "/";
		serverUrl += "servlet/WindchillAuthGW/ext.fast.capp.CAPPProcessor/processRequest";
		return serverUrl;
	}

	/**
	 * 根据任务HashMap, 提取主文件和附件清单
	 * @param task 任务HashMap
	 * @return 可以直接调用DataPacker的文件清单ArrayList
	 */
	public static ArrayList getFileList(HashMap task) throws Exception {
		HashMap capp = (HashMap) task.get(CAPP);
		ArrayList<File> fileList = new ArrayList<File>();
		ArrayList<HashMap<String, Object>> infoList = new ArrayList<HashMap<String, Object>>();
		String primaryPath = (String) capp.get(PRIMARY_FILE);
		ArrayList secondaryList = (ArrayList) capp.get(SECONDARY_FILE);
		if (primaryPath == null)
			return fileList;

		fileList.add(new File(primaryPath));
		for (int i = 0; secondaryList != null && i < secondaryList.size(); i++)
			fileList.add(new File((String) secondaryList.get(i)));

		for (int i = 0; i < fileList.size(); i++) {
			HashMap<String, Object> fileInfo = new HashMap<String, Object>();
			infoList.add(fileInfo);

			File file = (File) fileList.get(i);
			if (!file.exists()) {
				String ftype = i == 0 ? "主文件" : "附件文件";
				throw new Exception(ftype + "未找到错误: <" + file.toString() + ">");
			}

			fileInfo.put(DataPacker.NAME, file.getName());
			fileInfo.put(DataPacker.SIZE, new Long(file.length()));
			fileInfo.put(DataPacker.PATH, file);
		}

		return infoList;
	}

	/**
	 * 输出一个XML单元
	 * @param w
	 * @param key
	 * @param val
	 * @param indent
	 */
	public void writeXMLElement(PrintStream w, String key, Object val, int indent) {
		if (indent <= 1 && key != null && key.equalsIgnoreCase(EXCEPTION)) {
			return;
		} else if (val instanceof ArrayList) {
			ArrayList valList = (ArrayList) val;
			if (valList.size() <= 0)
				return;

			for (int i = 0; i < valList.size(); i++)
				writeXMLElement(w, key, valList.get(i), indent);
		} else if (val instanceof HashMap) {
			HashMap valMap = (HashMap) val;
			ArrayList keyList = null;
			LinkedHashMap sMap = null;

			if (key.equalsIgnoreCase(PART) || key.equalsIgnoreCase(DOCUMENT))
				valMap.remove(TYPE_IBA_LIST);

			if (val instanceof LinkedHashMap)
				sMap = (LinkedHashMap) valMap;

			keyList = new ArrayList(sMap.keySet());

			if (indent <= 1) {
				if (key.equalsIgnoreCase(MESSAGE)) {
					String type = (String) valMap.get(TYPE);
					String message = (String) valMap.get(MESSAGE);
					w.println(getIndent(indent) + "<message type=\""
							+ Tools.xmlEscape(type) + "\">"
							+ Tools.xmlEscape(message) + "</message>");
					return;
				}

				String vk = toLower(key);
				Object vt = valMap.get(TYPE);

				if (vk.equalsIgnoreCase(CAPP) && vt != null
						&& (vt instanceof String))
					vk += " type=\"" + Tools.xmlEscape((String) vt) + "\"";
				w.println(getIndent(indent) + "<" + vk + ">");
			} else
				w.print(getIndent(indent) + "<" + toLower(key));

			for (int i = 0; i < valMap.size(); i++) {
				String eleKey = (String) keyList.get(i);

				Object eleVal = valMap.get(eleKey);

				if (indent <= 1) {
					if (!key.equalsIgnoreCase(CAPP) || eleKey == null
							|| !eleKey.equals(TYPE))
						writeXMLElement(w, eleKey, eleVal, indent + 1);
				} else {
					String v = eleVal == null ? "" : String.valueOf(eleVal);
					w.print(" " + toLower(Tools.xmlEscape(eleKey)) + "=\""
							+ Tools.xmlEscape(v) + "\"");
				}
			}

			if (indent <= 1)
				w.println(getIndent(indent) + "</" + toLower(key) + ">");
			else
				w.println("/>");
		} else {
			String value = val == null ? "" : String.valueOf(val);
			w.println(getIndent(indent) + "<metadata name=\""
					+ Tools.xmlEscape(key) + "\" value=\""
					+ Tools.xmlEscape(value) + "\"/>");
		}
	}

	private static final String indent0 = "";
	private static final String indent1 = "\t";
	private static final String indent2 = "\t\t";
	private static final String indent3 = "\t\t\t";
	private static final String indent4 = "\t\t\t\t";
	private static final String indent5 = "\t\t\t\t\t";
	private static final String indent6 = "\t\t\t\t\t\t";
	private static final String indent7 = "\t\t\t\t\t\t\t";
	private static final String indent8 = "\t\t\t\t\t\t\t\t";
	private static final String indent9 = "\t\t\t\t\t\t\t\t\t";
	private static final String INDENTS[] = { indent0, indent1, indent2,
			indent3, indent4, indent5, indent6, indent7, indent8, indent9, };

	private String getIndent(int indent) {
		if (indent >= 0 && indent <= 9)
			return INDENTS[indent];

		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < indent; i++)
			buf.append("\t");
		return buf.toString();
	}

	private String toLower(String s) {
		if (s == null)
			return "";
		else
			return s.toLowerCase();
	}

	/**
	 * CAPP任务描述XML处理器类
	 * Created on 2005-7-23
	 * @author liuld
	 */
	static class CAPPTaskHandler extends DefaultHandler {
		private List<String> ctxList = new LinkedList<String>();
		private String type = null;

		private LinkedHashMap<String ,Object> params = new LinkedHashMap<String ,Object>();
		private LinkedHashMap<String, Object> capp = new LinkedHashMap<String, Object>();

		/**
		 * 解析任务描述XML流
		 * 
		 * @param is 任务描述XML输入流
		 * @return 解析了的任务描述参数
		 * @throws Exception
		 */
		public static LinkedHashMap<String ,Object> readTask(InputStream is) throws Exception {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			CAPPTaskHandler taskHandler = new CAPPTaskHandler();
			taskHandler.params = new LinkedHashMap();
			parser.parse(is, taskHandler);

			taskHandler.params.put(CAPP, taskHandler.capp);
			taskHandler.params.put(TYPE, taskHandler.type);

			return taskHandler.params;
		}

		public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
			String QName = qName.toUpperCase();

			// CAPP元素
			if (QName.equals(CAPP)) {
				type = getAttr(attrs, TYPE);
			}
			// CAPP下所有METADATA元素
			else if (ctxList.size() == 2 && QName.equals(METADATA)
					&& CAPP.equals(ctxList.get(0))) {
				String name = getAttr(attrs, NAME);
				String value = getAttr(attrs, VALUE);
				if (name != null && value != null) {
					name = name.toUpperCase();

					// 允许有多个元素共存的元素种类: SECONDARY_FILE
					if (name.equalsIgnoreCase(SECONDARY_FILE)) {
						if (!value.trim().equals("")) {
							ArrayList secondaryFiles = (ArrayList) capp.get(SECONDARY_FILE);
							if (secondaryFiles == null) {
								secondaryFiles = new ArrayList();
								capp.put(SECONDARY_FILE, secondaryFiles);
							}
							secondaryFiles.add(value);
						}
					} else
						capp.put(name, value);
				} else {
					String err = "METADATA ERROR: " + name + "=" + value;
					System.out.println(err);
					Logger.log(err);
				}
			}

			ctxList.add(0, QName);
		}

		public void endElement(String uri, String localName, String qName) throws SAXException {
			ctxList.remove(0);
		}

		/**
		 * 取指定名称的属性值(属性名称不区分大小写的方式)
		 * @param attrs 属性集
		 * @param attrName 属性名称(不分大小写)
		 * @return 属性值
		 */
		protected String getAttr(Attributes attrs, String attrName) {
			if (attrName == null)
				return null;

			String result = null;
			for (int i = 0; i < attrs.getLength(); i++) {
				if (attrName.equalsIgnoreCase(attrs.getQName(i))) {
					result = attrs.getValue(i);
					break;
				}
			}
			return result;
		}
	}
}