/**
 * Created on 2004-10-22
 * @author lld
 */
package ext.fast.capp.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.SoftBevelBorder;

import ext.fast.util.Tools;

public class FrameCheckinDocument extends JDialog implements CAPPConstants,
		ServerHelper.HTTPCallback {

	private static final long serialVersionUID = 1L;
	private FrameCheckinDocument frame = this;
	private HashMap<String, Object> task;
	private HashMap<String, Object> webInfo;
	private HashMap diCheckin = null;
	private HashMap diPrepare = null;
	private ArrayList fileList = null;
	private boolean newDocument = false;
	private boolean moreIBANeeded = false;
	private boolean docNumbered = false;

	// --------------------------------------------------------------------------
	private javax.swing.JPanel jContentPane = null;

	private JLabel labNumber = null;
	private JLabel labName = null;
	private JLabel labPartName = null;
	private JLabel labCIComments = null;
	private JLabel labFolder = null;
	private JTextField txtNumber = null;
	private JTextField txtName = null;
	private JTextField txtRelatedObj = null;
	private JTextArea memoCIComments = null;
	private JComboBox comboDocFolder = null;
	private JButton btnOk = null;
	private JButton btnCancel = null;
	private JScrollPane jScrollPane = null;
	private JLabel labStatus = null;
	private JLabel labDocType = null;
	private JTextField txtDocType = null;
	private JLabel labIBAPanel = null;
	private JProgressBar progressBar = null;
	private JLabel jLabel = null;
	private JPanel panelIBA = null;
	private JPanel panelIBALabel = null;
	private JPanel panelIBAValue = null;

	private FrameCheckinDocument() {
		super();
		initialize();
	}

	public static HashMap checkinDocument(HashMap task, HashMap webInfo)
			throws Exception {
		return new FrameCheckinDocument()._checkinDocument(task, webInfo);
	}

	private HashMap _checkinDocument(HashMap task, HashMap webInfo)
			throws Exception {
		// 获取功能调用信息
		HashMap capp = (HashMap) task.get(CAPP);
		if (capp == null)
			throw new Exception("内部错误，未找到任务描述参数！");

		String primary = (String) capp.get(PRIMARY_FILE);
		if (primary == null || primary.equals(""))
			throw new Exception("请指定要检入文档的主文件!");
		File primaryFile = new File(primary);
		if (!primaryFile.exists())
			throw new Exception("指定文档主文件不存在: " + primary);
		fileList = Worker.getFileList(task);
		if (fileList == null || fileList.size() <= 0 || fileList.get(0) == null)
			throw new Exception("CAPP系统没有指定要检入文档的主文件!");

		this.webInfo = webInfo;
		this.task = task;

		// 获取基本调用参数
		String windowCaption = (String) capp.get(WINDOW_CAPTION);
		String docNumber = (String) capp.get(DOC_NUMBER);
		//处理文档编号
		if(getCharSize(docNumber, '-') < 1)
			throw new Exception("请输入正确的工艺编号!");
		docNumber = getNumberFromStr(docNumber);//截取工艺编号
		String docName = (String) capp.get(DOC_NAME);
		String docType = (String) capp.get(DOC_TYPE);
		String newDocStr = (String) capp.get(DOC_NEW);
		String relPartNumber = (String) capp.get(RELATED_PART_NUMBER);
		docNumbered = docNumber != null && !docNumber.trim().equals("");
		newDocument = newDocStr != null && newDocStr.equalsIgnoreCase("true");
		if (docNumber == null || docNumber.trim().equals(""))
			throw new Exception("检入文档必需指定文档编号！");
		if (docName == null || docName.trim().length() <= 0)
			docName = relPartNumber + "-" + docType;

		// 提取相关调用参数
		if (windowCaption == null || windowCaption.trim().equals(""))
			windowCaption = "检入工艺文档";

		// 初始化各组件
		ScreenUtil.centerWindow(frame = this);
		setTitle(windowCaption);
		txtDocType.setEditable(false);
		txtRelatedObj.setEditable(false);
		txtNumber.setEditable(false);
		txtNumber.setText(docNumber);
		if (newDocument)
			txtDocType.setText(docType);
		progressBar.setVisible(false);
		txtName.setText(docName);

		labFolder.setVisible(true);
		comboDocFolder.setVisible(true);

		// 读取上次查询参数
		try {
			HashMap<String, Object> vars = Settings.getVariables();
			Rectangle r = (Rectangle) vars.get(CHECKIN_DOCUMENT + "BOUNDS");
			setBounds(r);
		} catch (Exception e) {
			Logger.log(e);
		}

		// 准备检入线程
		new SearchThread().start();

		// 显示窗口
		setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
		setVisible(true);
		setAlwaysOnTop(true);
		dispose();

		HashMap<String, Object> vars = Settings.getVariables();
		vars.put(CHECKIN_DOCUMENT + "BOUNDS", getBounds());
		Settings.saveVariables();

		// 判断用户是否选择取消
		if (diCheckin == null)
			return null;

		// 组装返回数据并返回
		ArrayList documents = new ArrayList();
		task.put(DOCUMENT, documents);
		documents.add(diCheckin);

		return task;
	}

	public void setProgress(long total, long progress) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				long _total;
				long _progress;

				public void run() {
					while (_total > Integer.MAX_VALUE) {
						_total /= 10;
						_progress /= 10;
					}
					progressBar.setMinimum(0);
					progressBar.setMaximum((int) _total);
					progressBar.setValue((int) _progress);
				}

				public Runnable get(long total, long progress) {
					_total = total;
					_progress = progress;
					return this;
				}
			}.get(total, progress));
		} catch (Exception e) {
			Logger.log(e);
			e.printStackTrace();
		}
	}

	/**
	 * 远程调用线程开始处理
	 * @param status 显示开始状态文字
	 * @throws Exception
	 */
	private void threadStart(String status) throws Exception {
		SwingUtilities.invokeAndWait(new Runnable() {
			String status;

			public void run() {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

				labStatus.setText(status);

				txtNumber.setEnabled(false);
				txtName.setEnabled(false);
				comboDocFolder.setEnabled(false);

				memoCIComments.setEnabled(false);
				progressBar.setVisible(true);
				btnOk.setEnabled(false);
			}

			public Runnable get(String status) {
				this.status = status;
				return this;
			}
		}.get(status));
	}

	/**
	 * 远程调用线程完成处理
	 * @param thread 远程调用线程对象
	 */
	private void threadEnd(Thread thread) {
		SwingUtilities.invokeLater(new Runnable() {
			Thread thread;

			public void run() {
				if (thread instanceof SearchThread && diPrepare != null)
					setPrepareInfo();

				if (newDocument) {
					txtNumber.setEnabled(!docNumbered);
					txtName.setEnabled(true);
					comboDocFolder.setEnabled(true);
					memoCIComments.setText("(新建文档不能设定检入日志)");

					if (diPrepare != null)
						labStatus.setText("请输入并确认相关信息后按检入按钮");
				} else if (diPrepare != null)
					labStatus.setText("请确认相关信息后按检入按钮");

				memoCIComments.setEnabled(!newDocument);
				progressBar.setVisible(false);
				btnOk.setEnabled(true);

				setCursor(null);
			}

			public Runnable get(Thread thread) {
				this.thread = thread;
				return this;
			}
		}.get(thread));
	}

	/**
	 * 远程调用线程错误处理
	 * @param title 错误窗标题
	 * @param e 异常对象
	 */
	void threadError(String title, final Exception e) {
		SwingUtilities.invokeLater(new Runnable() {
			String title;

			public void run() {
				e.printStackTrace();
				labStatus.setText(title + ": " + e.getMessage());
				JOptionPane.showMessageDialog(frame, e.getMessage(), title,	JOptionPane.ERROR_MESSAGE);
			}

			public Runnable get(String title) {
				this.title = title;
				return this;
			}
		}.get(title));
	}

	/**
	 * 从diPrepare-检入准备返回的文档信息中提取相关信息显示道窗口上.
	 */
	void setPrepareInfo() {
		/*设定检入准备信息*/
		// 清理选项
		comboDocFolder.removeAllItems();

		// 文档基本属性
		if (!newDocument) {
			String name = (String) diPrepare.get(NAME);
			txtName.setText(name);
		} else {
			// 新文档的默认名称
			String pName = (String) diPrepare.get(RELATED_PART_NAME);
			String pNumber = (String) diPrepare.get(RELATED_PART_NUMBER);
			String docType = txtDocType.getText();
//			txtName.setText(pNumber + "-" + pName + "-" + docType);
		}

		// 关联对象或产品信息
		String root = "/Default";
		int rootLength = root.length();
		txtRelatedObj.setText(String.valueOf(diPrepare.get(DESCRIBES)));

		// 文档的必需属性信息
		moreIBANeeded = false;
		HashMap capp = (HashMap) task.get(CAPP);
		ArrayList<String> typeIBAList = (ArrayList) diPrepare.get(TYPE_IBA_LIST);
		HashMap ibaNameDispMap = (HashMap) diPrepare.get(IBA_NAME_DISP_MAP);
		Map<String, Vector<String>> iba_options = (Map<String, Vector<String>>) diPrepare.get(TYPE_IBA_OPTIONS);
		if (typeIBAList.size() > 0) {
			labIBAPanel.setVisible(true);
			panelIBA.setVisible(true);
		}
		Collections.sort(typeIBAList);
		for (int i = 0; typeIBAList != null && i < typeIBAList.size(); i++) {
			String ibaName = typeIBAList.get(i);
			String ibaDisp = ibaNameDispMap == null ? ibaName : (String) ibaNameDispMap.get(ibaName);
			String ibaValue = (String) capp.get(ibaName.toUpperCase());

			// 转换为大小写精确的属性名称
			if (ibaName != null && ibaValue != null) {
				capp.remove(ibaName.toUpperCase());
				capp.put(ibaName, ibaValue);
			}

			Font f = new Font("DialogInput", Font.PLAIN, 12);
			// 创建标签和输入字段
			JLabel labDisp = new JLabel(" " + ibaDisp + " ");
			Vector<String> options = null;
			if((options = iba_options.get(ibaName)) != null && options.size() > 0){
				JComboBox comboBox = new JComboBox();
				comboBox.setName(ibaName);
				for (String option : options) {
					comboBox.addItem(option);
					comboBox.setFont(f);
					comboBox.setEditable(true);
					comboBox.setMaximumRowCount(20);
					if(option.equalsIgnoreCase(ibaValue)){
						comboBox.setSelectedItem(option);
						comboBox.setEnabled(false);
					}
				}
				panelIBAValue.add(comboBox);
			}else{
				if(ibaName.equalsIgnoreCase("SaveTime"))
					ibaValue = "2012/12/27 17:55:00";
				JTextField txtValue = new JTextField();
				txtValue.setName(ibaName);
				if (ibaValue == null || ibaValue.equals("")) {
					txtValue.setForeground(Color.BLUE);
					moreIBANeeded = true;
				} else {
					txtValue.setText(ibaValue);
					txtValue.setEditable(false);
				}
				txtValue.setFont(f);
				txtValue.setToolTipText(ibaName);
				panelIBAValue.add(txtValue);
			}

			labDisp.setFont(f);
			/* labDisp.setPreferredSize(new Dimension(labDisp.getPreferredSize().width, txtValue.getPreferredSize().height));*/
			labDisp.setHorizontalAlignment(JLabel.RIGHT);
			labDisp.setToolTipText(ibaName);

			// 加到界面中
			panelIBALabel.add(labDisp);
		}
		panelIBA.updateUI();

		// 新建文档的选项信息
		if (newDocument) {
			// 保存路径信息
			ArrayList<Object> ll = (ArrayList) diPrepare.get(LOCATION);
			if (ll != null) {
				Collections.sort(ll, new Comparator<Object>() {
					public int compare(Object o1, Object o2) {
						Collator collator = Collator.getInstance(Locale.CHINA);
						String s1 = o1 == null ? "" : String.valueOf(o1);
						String s2 = o2 == null ? "" : String.valueOf(o2);
						return collator.compare(s1, s2);
					}
				});
			}
			for (int i = 0; ll != null && i < ll.size(); i++) {
				String loc = (String) ll.get(i);
				if (loc != null && loc.startsWith("/Default")) {
					if (loc.length() == rootLength)
						loc = "/";
					else
						loc = loc.substring(rootLength);
					comboDocFolder.addItem(loc);
				}
			}
			if (comboDocFolder.getItemCount() > 0)
				comboDocFolder.setSelectedIndex(0);
		} else {// 原检出文档的文档信息
			// 文档类型信息
			String docType = (String) diPrepare.get(DOC_TYPE);
			docType = docType == null ? "(未定义)" : docType;
			txtDocType.setText(docType);
			capp.put(DOC_TYPE, docType);

			// 路径信息
			String loc = (String) diPrepare.get(LOCATION);
			if (loc.startsWith(root)) {
				if (loc.length() == rootLength)
					loc = "/";
				else
					loc = loc.substring(rootLength);
			}
			comboDocFolder.addItem(loc);
			comboDocFolder.setSelectedIndex(0);
		}
	}

	/**
	 * 检入准备线程: 从远程服务器获取检入文档所需的必要信息-原检出文档的相关属性,或检入新文档时可以选择 的各种属性选项和默认值.
	 * Created on 2005-8-3
	 * @author liuld
	 */
	class SearchThread extends Thread {
		public void run() {
			try {
				threadStart("正在从服务器获取检入准备资料......");

				HashMap task1 = (HashMap) DataPacker.clone(task);
				HashMap capp = (HashMap) task1.get(CAPP);
				task1.put(TYPE, PREPARE_CHECKIN_DOCUMENT);
				capp.put(DOC_NEW, newDocument ? TRUE : FALSE);

				HashMap result = ServerHelper.callServer(
						(String) webInfo.get(USER), (String) webInfo.get(PASS),
						(String) webInfo.get(SERVER_URL), task1, null, frame);

				if (result == null)
					throw new Exception("准备检入文档时发生未知异常!");

				ArrayList docs = (ArrayList) result.get(DOCUMENT);
				diPrepare = (HashMap) docs.get(0);
			} catch (Exception e) {
				Logger.log(e);
				threadError("文档检入准备出错", e);
			} finally {
				threadEnd(this);
			}
		}
	}

	/**
	 * 文档检入线程: 执行远程调用检入文档
	 * Created on 2005-8-3
	 * @author liuld
	 */
	class CheckinThread extends Thread {
		HashMap taskData;

		CheckinThread(HashMap taskData) {
			this.taskData = taskData;
		}

		public void run() {
			try {
				threadStart("正在检入......");
				HashMap capp = (HashMap) taskData.get(CAPP);
				capp.put(DOC_NEW, newDocument ? TRUE : FALSE);
				ArrayList list = (ArrayList) DataPacker.clone(fileList);

				HashMap result = ServerHelper
						.callServer((String) webInfo.get(USER),
								(String) webInfo.get(PASS),
								(String) webInfo.get(SERVER_URL), taskData,
								list, frame);

				if (result == null)
					throw new Exception("检入出错,检入过程中出现未知异常情况.");

				ArrayList docs = (ArrayList) result.get(DOCUMENT);
				diCheckin = (HashMap) docs.get(0);

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog(FrameCheckinDocument.this, "文档检入成功!");
						setVisible(false);
					}
				});
			} catch (Exception e) {
				Logger.log(e);
				threadError("文档检入失败", e);
			} finally {
				threadEnd(this);
			}
		}
	}

	/**
	 * 检出用户界面输入, 如果没有错误, 则生成检入调用所需的task HashMap返回, 否则返回null
	 * @return 检入调用task HashMap
	 */
	private HashMap checkUIFields() {
		HashMap task1 = null;
		HashMap capp = null;
		String err = null;

		try {
			// 准备数据结构
			task1 = (HashMap) DataPacker.clone(task);
			capp = (HashMap) task1.get(CAPP);

			// 辨认是否新建文档
			String docNumber = txtNumber.getText().trim();
			if (docNumber.equals(""))
				err = "请输入文档编号!";
			else if (moreIBANeeded) {
				Component[] txtFields = panelIBAValue.getComponents();
				for (int i = 0; i < txtFields.length; i++) {
					String txt = "";
					if (txtFields[i] instanceof JTextField){
						JTextField txtField = (JTextField) txtFields[i];
//						if (!txtField.isEditable())
//							continue;
						txt = txtField.getText();
						if (txt.equals("")) {
							err = "CAPP系统未提供必需的文档属性!";
							break;
						}
					}else if(txtFields[i] instanceof JComboBox){
						JComboBox comboBox = (JComboBox) txtFields[i];
						txt = comboBox.getSelectedItem().toString();
					}else
						continue;

					capp.put(txtFields[i].getName(), txt);
				}
			}

			// 设定检入日志
			String ciComments = memoCIComments.getText();
			ciComments = ciComments.replaceAll("\n", "<br>");
			capp.put(CHECKIN_COMMENTS, ciComments);

			// 设定新建文档的属性
			if (newDocument && err == null) {
				// 提取CAPP系统调用输入
				String docType = txtDocType.getText().trim();
				String relatedInfo = txtRelatedObj.getText().trim();

				// 提取用户界面输入
				String docName = txtName.getText().trim();
				String location = (String) comboDocFolder.getSelectedItem();

				// 检出必填项目:
				// CAPP软件设定-文档类型, 关联对象
				// 用户交换设定 -文档编号, 名称, 密级, 适用机型, 有效期限, 编制单位, 存储位置
				if (docType.equals(""))
					err = "CAPP系统没有指定检入文档类型!";
				else if (relatedInfo.equals(""))
					err = "CAPP系统没有指定关联对象信息或产品名称!";
				else if (docName.equals(""))
					err = "请指定文件名称";
				else if (location == null || location.trim().equals(""))
					err = "请指定文档存储路径!";
				else if (!location.startsWith("/")	&& !location.startsWith("/Default/")
						&& !location.equals("/Default"))
					err = "非法的文档存储路径!";

				// 设定IBA属性HashMap, 如果CAPP系统提供了部分要设定的属性HashMap, 则沿用
				if (err == null) {
					capp.put(DOC_NAME, docName);
					capp.put(DOC_NUMBER, docNumber);

					location = location.trim();
					String root = "/Default";
					if (!location.startsWith(root + "/") && !location.equals(root))
						location = location.equals("/") ? root : root + location;
					capp.put(LOCATION, location);

					HashMap ibaMap = (HashMap) capp.get(IBA);
					if (ibaMap == null)
						ibaMap = new LinkedHashMap();
					capp.put(IBA, ibaMap);
				}
			}
		} catch (Exception e) {
			Logger.log(e);
			err = Tools.getErrorMessage(e);
		}

		// 用户仅能新建文档时设定文档属性
		if (err != null) {
			JOptionPane.showMessageDialog(this, err, "输入错误", JOptionPane.INFORMATION_MESSAGE);
			return null;
		}

		return task1;
	}

	/**
	 * This method initializes this
	 * @return void
	 */
	private void initialize() {
		this.setContentPane(getJContentPane());
		this.setSize(480, 600);
		this.setTitle("检入工艺文档");
	}

	/**
	 * This method initializes jContentPane
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.gridx = 1;
			gridBagConstraints14.fill = GridBagConstraints.BOTH;
			gridBagConstraints14.gridwidth = 3;
			gridBagConstraints14.insets = new Insets(18, 0, 0, 8);
			gridBagConstraints14.gridy = 6;
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			gridBagConstraints16.gridx = 0;
			gridBagConstraints16.gridy = 13;
			jLabel = new JLabel();
			jLabel.setText("　");
			GridBagConstraints gridBagConstraints111 = new GridBagConstraints();
			gridBagConstraints111.gridx = 1;
			gridBagConstraints111.gridwidth = 3;
			gridBagConstraints111.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints111.insets = new Insets(0, 0, 0, 8);
			gridBagConstraints111.gridy = 13;
			GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
			gridBagConstraints22.gridx = 0;
			gridBagConstraints22.anchor = GridBagConstraints.NORTHEAST;
			gridBagConstraints22.insets = new Insets(22, 0, 0, 4);
			gridBagConstraints22.gridy = 6;
			labIBAPanel = new JLabel();
			labIBAPanel.setText("*文档属性:");
			labIBAPanel.setFont(new Font("DialogInput", Font.PLAIN, 12));
			labIBAPanel.setVisible(false);
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints6.gridy = 0;
			gridBagConstraints6.weightx = 1.0;
			gridBagConstraints6.gridwidth = 3;
			gridBagConstraints6.insets = new Insets(8, 0, 0, 8);
			gridBagConstraints6.gridx = 1;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.anchor = GridBagConstraints.EAST;
			gridBagConstraints.insets = new Insets(8, 16, 0, 4);
			gridBagConstraints.gridy = 0;
			labDocType = new JLabel();
			labDocType.setText("*文档类型:");
			labDocType.setFont(new Font("DialogInput", Font.PLAIN, 12));
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();

			jContentPane = new JPanel();
			jContentPane.setLayout(new java.awt.GridBagLayout());

			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 2;
			gridBagConstraints1.anchor = GridBagConstraints.EAST;
			gridBagConstraints1.insets = new Insets(8, 0, 0, 4);
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.anchor = GridBagConstraints.EAST;
			gridBagConstraints2.insets = new Insets(8, 0, 0, 4);
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.gridy = 3;
			gridBagConstraints3.anchor = GridBagConstraints.EAST;
			gridBagConstraints3.insets = new Insets(8, 0, 0, 4);
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.gridy = 4;
			gridBagConstraints5.anchor = GridBagConstraints.NORTHEAST;
			gridBagConstraints5.insets = new Insets(8, 0, 0, 4);
			gridBagConstraints9.gridx = 0;
			gridBagConstraints9.gridy = 11;
			gridBagConstraints9.anchor = GridBagConstraints.EAST;
			gridBagConstraints9.insets = new Insets(22, 0, 0, 4);
			gridBagConstraints10.gridx = 0;
			gridBagConstraints10.gridy = 14;
			gridBagConstraints10.anchor = GridBagConstraints.CENTER;
			gridBagConstraints10.insets = new Insets(4, 4, 4, 4);

			gridBagConstraints11.gridx = 1;
			gridBagConstraints11.gridy = 2;
			gridBagConstraints11.weightx = 1.0;
			gridBagConstraints11.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints11.gridwidth = 3;
			gridBagConstraints11.insets = new Insets(8, 0, 0, 8);
			gridBagConstraints12.gridx = 1;
			gridBagConstraints12.gridy = 1;
			gridBagConstraints12.weightx = 1.0;
			gridBagConstraints12.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints12.gridwidth = 3;
			gridBagConstraints12.insets = new Insets(8, 0, 0, 8);
			gridBagConstraints13.gridx = 1;
			gridBagConstraints13.gridy = 3;
			gridBagConstraints13.weightx = 1.0;
			gridBagConstraints13.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints13.gridwidth = 3;
			gridBagConstraints13.insets = new Insets(8, 0, 0, 8);
			gridBagConstraints15.gridx = 1;
			gridBagConstraints15.gridy = 4;
			gridBagConstraints15.weightx = 1.0;
			gridBagConstraints15.weighty = 1.0;
			gridBagConstraints15.fill = GridBagConstraints.BOTH;
			gridBagConstraints15.gridwidth = 3;
			gridBagConstraints15.insets = new Insets(4, 0, 0, 8);
			gridBagConstraints19.gridx = 1;
			gridBagConstraints19.gridy = 11;
			gridBagConstraints19.weightx = 1.0;
			gridBagConstraints19.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints19.gridwidth = 3;
			gridBagConstraints19.insets = new Insets(18, 0, 0, 8);
			gridBagConstraints20.gridx = 1;
			gridBagConstraints20.anchor = GridBagConstraints.EAST;
			gridBagConstraints20.gridy = 12;
			gridBagConstraints20.weightx = 1.0;
			gridBagConstraints20.fill = GridBagConstraints.NONE;
			gridBagConstraints20.gridwidth = 1;
			gridBagConstraints20.insets = new Insets(30, 0, 15, 8);
			gridBagConstraints21.gridx = 3;
			gridBagConstraints21.anchor = GridBagConstraints.WEST;
			gridBagConstraints21.gridy = 12;
			gridBagConstraints21.weightx = 1.0;
			gridBagConstraints21.fill = GridBagConstraints.NONE;
			gridBagConstraints21.gridwidth = 1;
			gridBagConstraints21.insets = new Insets(30, 8, 15, 0);
			labNumber = new JLabel();
			labPartName = new JLabel();
			labName = new JLabel();
			labCIComments = new JLabel();
			labFolder = new JLabel();
			labStatus = new JLabel();

			labNumber.setText("*文档编号:");
			labNumber.setFont(new Font("DialogInput", Font.PLAIN, 12));
			labPartName.setText("*相关对象:");
			labPartName.setFont(new Font("DialogInput", Font.PLAIN, 12));
			labName.setText("*文档名称:");
			labName.setFont(new Font("DialogInput", Font.PLAIN, 12));
			labCIComments.setText("检入日志:");
			labCIComments.setFont(new Font("DialogInput", Font.PLAIN, 12));
			jContentPane.add(labCIComments, gridBagConstraints5);
			jContentPane.add(labIBAPanel, gridBagConstraints22);
			jContentPane.add(getTxtDocType(), gridBagConstraints6);
			jContentPane.add(getTxtRelatedObj(), gridBagConstraints12);
			labFolder.setText("*存储位置:");
			labFolder.setFont(new Font("DialogInput", Font.PLAIN, 12));
			labStatus.setText(" ");
			labStatus.setFont(new Font("DialogInput", Font.PLAIN, 12));
			labStatus.setName("labStatus");
			gridBagConstraints10.fill = GridBagConstraints.BOTH;
			gridBagConstraints10.gridwidth = 4;
			gridBagConstraints15.anchor = GridBagConstraints.CENTER;
			jContentPane.add(labDocType, gridBagConstraints);
			jContentPane.add(labPartName, gridBagConstraints2);
			jContentPane.add(labNumber, gridBagConstraints1);
			jContentPane.add(labName, gridBagConstraints3);
			jContentPane.add(labFolder, gridBagConstraints9);
			jContentPane.add(labStatus, gridBagConstraints10);
			jContentPane.add(getTxtNumber(), gridBagConstraints11);
			jContentPane.add(getTxtName(), gridBagConstraints13);
			jContentPane.add(getJScrollPane(), gridBagConstraints15);
			jContentPane.add(getComboDocFolder(), gridBagConstraints19);
			jContentPane.add(getBtnOk(), gridBagConstraints20);
			jContentPane.add(getBtnCancel(), gridBagConstraints21);
			jContentPane.add(getProgressBar(), gridBagConstraints111);
			jContentPane.add(jLabel, gridBagConstraints16);
			jContentPane.add(getIbaPanel(), gridBagConstraints14);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jTextField
	 * @return javax.swing.JTextField
	 */
	private JTextField getTxtNumber() {
		if (txtNumber == null) {
			txtNumber = new JTextField();
			txtNumber.setFont(new Font("DialogInput", Font.PLAIN, 12));
			txtNumber.setName("txtNumber");
			txtNumber.setEditable(true);
			txtNumber.setEnabled(true);
			txtNumber.setPreferredSize(new Dimension(100, 20));
		}
		return txtNumber;
	}

	/**
	 * This method initializes jTextField1
	 * @return javax.swing.JTextField
	 */
	private JTextField getTxtName() {
		if (txtName == null) {
			txtName = new JTextField();
			txtName.setFont(new Font("DialogInput", Font.PLAIN, 12));
			txtName.setText("");
			txtName.setName("txtName");
			txtName.setPreferredSize(new Dimension(150, 20));
		}
		return txtName;
	}

	/**
	 * This method initializes jTextField3
	 * @return javax.swing.JTextField
	 */
	private JTextField getTxtRelatedObj() {
		if (txtRelatedObj == null) {
			txtRelatedObj = new JTextField();
			txtRelatedObj.setFont(new Font("DialogInput", Font.PLAIN, 12));
			txtRelatedObj.setText("");
			txtRelatedObj.setName("txtPartName");
			txtRelatedObj.setEnabled(false);
			txtRelatedObj.setPreferredSize(new Dimension(25, 20));
		}
		return txtRelatedObj;
	}

	/**
	 * This method initializes jTextArea
	 * @return javax.swing.JTextArea
	 */
	private JTextArea getMemoCIComments() {
		if (memoCIComments == null) {
			memoCIComments = new JTextArea();
			memoCIComments.setFont(new Font("DialogInput", Font.PLAIN, 12));
			memoCIComments.setText("");
			memoCIComments.setPreferredSize(new Dimension(0, 50));
			memoCIComments.setName("memoDescription");
		}
		return memoCIComments;
	}

	/**
	 * This method initializes jComboBox2
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getComboDocFolder() {
		if (comboDocFolder == null) {
			comboDocFolder = new JComboBox();
			comboDocFolder.setFont(new Font("DialogInput", Font.PLAIN, 12));
			comboDocFolder.setEditable(true);
			comboDocFolder.setPreferredSize(new Dimension(119, 20));
			comboDocFolder.setMaximumRowCount(20);
		}
		return comboDocFolder;
	}

	/**
	 * This method initializes jButton
	 * @return javax.swing.JButton
	 */
	private JButton getBtnOk() {
		if (btnOk == null) {
			btnOk = new JButton();
			btnOk.setText("检入");
			btnOk.setFont(new Font("DialogInput", Font.PLAIN, 12));
			btnOk.setPreferredSize(new Dimension(80, 24));
			btnOk.setName("btnOk");
			btnOk.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					HashMap task1 = checkUIFields();
					if (task1 == null)
						return;

					btnOk.setEnabled(false);
					new CheckinThread(task1).start();
				}
			});
		}
		return btnOk;
	}

	/**
	 * This method initializes jButton1
	 * @return javax.swing.JButton
	 */
	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setText("取消");
			btnCancel.setFont(new Font("DialogInput", Font.PLAIN, 12));
			btnCancel.setPreferredSize(new Dimension(80, 24));
			btnCancel.setName("btnCancel");
			btnCancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					diCheckin = null;
					frame.setVisible(false);
				}
			});
		}
		return btnCancel;
	}

	/**
	 * This method initializes jScrollPane
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getMemoCIComments());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes txtDocType
	 * @return javax.swing.JTextField
	 */
	private JTextField getTxtDocType() {
		if (txtDocType == null) {
			txtDocType = new JTextField();
			txtDocType.setEnabled(false);
			txtDocType.setPreferredSize(new Dimension(25, 20));
			txtDocType.setFont(new Font("DialogInput", Font.PLAIN, 12));
		}
		return txtDocType;
	}

	/**
	 * This method initializes jProgressBar
	 * @return javax.swing.JProgressBar
	 */
	private JProgressBar getProgressBar() {
		if (progressBar == null) {
			progressBar = new JProgressBar();
			progressBar.setVisible(true);
		}
		return progressBar;
	}

	/**
	 * This method initializes ibaPanel
	 * @return javax.swing.JPanel
	 */
	private JPanel getIbaPanel() {
		if (panelIBA == null) {
			panelIBA = new JPanel();
			panelIBA.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
			panelIBA.setLayout(new BorderLayout());
			panelIBA.add(getJPanel(), BorderLayout.WEST);
			panelIBA.add(getJPanel1(), BorderLayout.CENTER);
			panelIBA.setVisible(false);
		}
		return panelIBA;
	}

	/**
	 * This method initializes jPanel
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (panelIBALabel == null) {
			panelIBALabel = new JPanel();
			panelIBALabel.setLayout(new GridLayout(0, 1));
		}
		return panelIBALabel;
	}

	/**
	 * This method initializes jPanel1
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel1() {
		if (panelIBAValue == null) {
			panelIBAValue = new JPanel();
			panelIBAValue.setLayout(new GridLayout(0, 1));
		}
		return panelIBAValue;
	}

	/**
	 * 获取str中c的个数 
	 */
	private int getCharSize(String str, char c){
		int size = 0;
		for (char ch : str.toCharArray()) {
			if(ch == c)
				size++;
		}
		return size;
	}

	/**
	 * 截取工艺编号 
	 */
	private String getNumberFromStr(String number){
		String temp = number, result = "";
		int end = temp.substring(0, temp.lastIndexOf('-')).lastIndexOf('-');
		result = number.substring(end + 1, number.length());
		return result;
	}

	public static void main(String[] args) throws Exception {
		//		test(args);
//		testGetDocInfo();
	}

	public static void test(String[] args) throws Exception {
		HashMap<String, Object> task = new HashMap<String, Object>();
		HashMap<String, Object> capp = new LinkedHashMap<String, Object>();
		HashMap<String, Object> wi = new HashMap<String, Object>();
		HashMap<String, Object> iba = new LinkedHashMap<String, Object>();
		task.put(CAPP, capp);
		capp.put(IBA, iba);

		String serverUrl = "http://pdm.fastgroup.cn/Windchill/";
		task.put(TYPE, "CHECKIN_DOCUMENT");
		capp.put(SERVER_URL, serverUrl);
		capp.put(DOC_NUMBER, "TEST-DOC-005");
		capp.put(DOC_NUMBER, "");
		capp.put(PRODUCT_NAME, "F系列全功能数控车HTM_F250");
		capp.put(DOC_TYPE, "机械设计图样");
		capp.put(PRIMARY_FILE, "c:\\china-xp.rar");
		ArrayList<String> ff = new ArrayList<String>();
		ff.add("c:\\atlog1.txt");
		ff.add("c:\\atlog2.txt");
		capp.put(SECONDARY_FILE, ff);

		iba.put("CAC_SecretLevel", "机密1");
		iba.put("CAC_Substitute", "---");

		wi.put(USER, "wcadmin");
		wi.put(PASS, "winadmin");
		wi.put(SERVER_URL, Worker.getServiceUrl(serverUrl));

		HashMap result = checkinDocument(task, wi);
		if (result != null)
			new Worker("wcadmin", "winadmin").saveResult(System.out, result);
		System.exit(0);
	}

	public static void testGetDocInfo() throws Exception {
		HashMap<String, Object> task = new HashMap<String, Object>();
		HashMap<String, Object> capp = new LinkedHashMap<String, Object>();
		String serverUrl = "http://pdm.fastgroup.cn/Windchill/";

		task.put(CAPP, capp);
		task.put(TYPE, CHECKOUT_DOCUMENT);
//		task.put(TYPE, PREPARE_CHECKIN_DOCUMENT);
//		task.put(TYPE, CHECKIN_DOCUMENT);
		capp.put(SERVER_URL, serverUrl);
		capp.put(WINDOW_CAPTION, "查找工艺文档");
		capp.put(DOC_TYPE, "*");
		capp.put(IBA_LIST, "*");
		capp.put(NUMBER, "12345");
		capp.put(DOC_NUMBER, "12345");
		capp.put(DOC_VERSION, "A.1");
//		capp.put(RELATED_DOC_NUMBER, "TEST-DOC-002");
		// capp.put(IBA_LIST, "htDocumentSubClass");

		String serviceUrl = Worker.getServiceUrl(serverUrl);
		HashMap<String, Object> result = ServerHelper.callServer("wcadmin", "winadmin", serviceUrl, task, null, null);

		if (result != null)
			new Worker("wcadmin", "winadmin").saveResult(System.out, result);
	}

	public static void testCheckOut() throws Exception {
		HashMap<String, Object> task = new HashMap<String, Object>();
		HashMap<String, Object> capp = new LinkedHashMap<String, Object>();
		String serverUrl = "http://pdm.fastgroup.cn/Windchill/";

		task.put(CAPP, capp);
		task.put(TYPE, CHECKOUT_DOCUMENT);
		capp.put(SERVER_URL, serverUrl);
		capp.put(OID, "");
		capp.put(NUMBER, "12345");
		capp.put(VERSION, "A.1");

		String serviceUrl = Worker.getServiceUrl(serverUrl);
		HashMap<String, Object> result = ServerHelper.callServer("wcadmin", "winadmin", serviceUrl, task, null, null);

		if (result != null)
			new Worker("wcadmin", "winadmin").saveResult(System.out, result);
	}
}