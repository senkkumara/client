/**
 * Created on 2004-10-22
 * @author lld
 */
package ext.fast.capp.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.SoftBevelBorder;

import ext.fast.util.Tools;

public class TestApp extends JFrame implements CAPPConstants {

	private static final long serialVersionUID = 1L;
	private static final String K_NAME = "NAME";
	// private static final String K_LABEL = "LABEL";
	private static final String K_VALUE = "VALUE";
	private static final String K_KLASS = "KLASS";
	private static final String K_HINT = "HINT";
	private static final String K_READONLY = "READONLY";

	private static TestApp frame = null;
	private HashMap<Object, Object> params = new HashMap<Object, Object>();
	private HashMap textFieldMap = new HashMap();

	private HashMap ibaFieldMap = new HashMap();
	private ArrayList ibaNameList = new ArrayList();
	private ArrayList ibaComponents = new ArrayList();

	private HashMap fileChooserMap = new HashMap();
	private HashMap buttonFieldMap = new HashMap();

	private Object fileChooser = null;
	private Object activeTestButton = null;
	private String activeTestName = null;
	private boolean taskXMLBuilt = false;
	private Object fillObject = null;

	private ArrayList typeNameList = null;
	private HashMap typeNameInfoMap = null;
	private HashMap ibaNameDispMap = null;
	private HashMap ibaNameValueMap = new HashMap();

	private String surl = "http://host.name.com/Windchill";

	private JPanel jContentPane = null;

	private JButton btnSearchPart = null;
	private JButton btnCheckinDocumentNew = null;
	private JButton btnCheckoutDocument = null;
	private JButton btnVerifyAuthorization = null;
	private JButton btnDeleteDocument = null;
	private JLabel jLabel = null;
	private JButton btnOpenPartPage = null;
	private JButton btnSearchDocument = null;
	private JButton btnReviseDocument = null;
	private JButton btnCheckinDocumentOld = null;
	private JSplitPane jSplitPane = null;
	private JScrollPane jScrollPane = null;
	private JTextArea memoInputXML = null;
	private JScrollPane jScrollPane1 = null;
	private JTextArea memoOutputXML = null;
	private JLabel labStatus = null;
	private JButton btnListMarkup = null;
	private JButton btnSaveMarkup = null;
	private JButton btnGetMarkup = null;
	private JButton btnGetDocumentSignatures = null;
	private JLabel jLabel1 = null;
	private JButton btnExecute = null;
	private JButton btnMakeInputXML = null;
	private JPanel jPanel = null;
	private JPanel jPanel1 = null;
	private JLabel jLabel3 = null;
	private JTextField txtUser = null;
	private JLabel jLabel4 = null;
	private JLabel jLabel6 = null;
	private JPasswordField txtPass = null;
	private JButton btnTestFileInput = null;
	private JLabel labTest = null;
	private JPanel inputPanel0 = null;
	private JScrollPane jScrollPane2 = null;
	private JPanel inputPanel = null;
	private JButton btnQuickGetPart = null;
	private JButton btnQuickGetDocument = null;
	private JButton btnQuickDeleteDocument = null;
	private JButton btnQuickCheckoutDocument = null;
	private JButton btnListDir = null;
	private JButton btnListWorkitems = null;
	private JButton btnOpenDocPage = null;
	private JButton btnOpenWorkitemPage = null;
	private JButton btnGetToolingNumber = null;
	private JButton btnQuickReviseDocument = null;
	private JButton btnListPartBaselines = null;
	private JButton btnListPartDocuments = null;
	private JButton btnSetDocumentInCAPPTask = null;

	public TestApp() {
		super();
		initialize();
	}

	public static void doTest(String user, String pass) {
		if (frame == null) {
			frame = new TestApp();
			TestAdapter ta = frame.new TestAdapter();
			frame.addWindowListener(ta);
			frame.btnSearchPart.addActionListener(ta);
			frame.btnSearchDocument.addActionListener(ta);
			frame.btnDeleteDocument.addActionListener(ta);
			frame.btnCheckoutDocument.addActionListener(ta);
			frame.btnReviseDocument.addActionListener(ta);

			frame.btnGetToolingNumber.addActionListener(ta);
			frame.btnListPartBaselines.addActionListener(ta);
			frame.btnListPartDocuments.addActionListener(ta);
			frame.btnSetDocumentInCAPPTask.addActionListener(ta);

			frame.btnCheckinDocumentNew.addActionListener(ta);
			frame.btnCheckinDocumentOld.addActionListener(ta);

			frame.btnQuickGetPart.addActionListener(ta);
			frame.btnQuickGetDocument.addActionListener(ta);
			frame.btnQuickDeleteDocument.addActionListener(ta);
			frame.btnQuickCheckoutDocument.addActionListener(ta);
			frame.btnQuickReviseDocument.addActionListener(ta);

			frame.btnListDir.addActionListener(ta);
			frame.btnListWorkitems.addActionListener(ta);

			frame.btnListMarkup.addActionListener(ta);
			frame.btnSaveMarkup.addActionListener(ta);
			frame.btnGetMarkup.addActionListener(ta);
			frame.btnGetDocumentSignatures.addActionListener(ta);

			frame.btnOpenPartPage.addActionListener(ta);
			frame.btnOpenDocPage.addActionListener(ta);
			frame.btnOpenWorkitemPage.addActionListener(ta);

			frame.btnVerifyAuthorization.addActionListener(ta);

			frame.btnTestFileInput.addActionListener(ta);
			frame.btnExecute.addActionListener(ta);
			frame.btnMakeInputXML.addActionListener(ta);
		}
		frame._doTest(user, pass);
	}

	private void _doTest(String user, String pass) {
		txtUser.setText(user);
		txtPass.setText(pass);
		HashMap<String, Object> vars = (HashMap<String, Object>) Settings.getVariables();
		if (vars.get(SERVER_URL) != null)
			this.surl = (String) vars.get(SERVER_URL);

		initParams();
		memoInputXML.setTabSize(4);
		memoOutputXML.setTabSize(4);
		ScreenUtil.centerWindow(this);
		setVisible(true);
	}

	/**
	 * 初始化全部测试参数
	 */
	private void initParams() {
		initSearchPart();

		initSearchDocument();
		initDeleteDocument();
		initCheckoutDocument();
		initReviseDocument();

		initGetToolingNumber();
		initListPartBaselines();
		initListPartDocuments();
		initSetDocumentInCAPPTask();

		initCheckinDocumentNew();
		initCheckinDocumentOld();

		initQuickGetPart();
		initQuickGetDoc();
		initQuickDeleteDoc();
		initQuickCheckoutDoc();
		initQuickReviseDoc();

		initListDir();
		initListWorkitems();

		initListMarkup();
		initSaveMarkup();
		initGetMarkup();
		initGetDocumentSignatures();

		initOpenPartPage();
		initOpenDocPage();
		initOpenWorkitemPage();

		initVerifyAuthorization();
	}

	/**
	 * 设定测试参数, 将Object[][]格式转换ArrayList
	 * @param vals
	 * @return *
	 */
	private ArrayList<HashMap<String, Object>> setValues(Object[][] vals) {
		ArrayList<HashMap<String, Object>> ll = new ArrayList<HashMap<String, Object>>();

		for (int i = 0; i < vals.length; i++) {
			Object name = vals[i][0];
			Object value = vals[i][1];
			Object klass = vals[i][2];
			Object hint = vals[i][3];
			Object readonly = vals[i][4];

			HashMap<String, Object> mm = new HashMap<String, Object>();
			mm.put(K_NAME, name);
			mm.put(K_VALUE, value);
			mm.put(K_KLASS, klass);
			mm.put(K_HINT, hint);
			mm.put(K_READONLY, readonly);

			ll.add(mm);
		}

		return ll;
	}

	/**
	 * 准备好测试参数输入界面
	 * @param sender 测试按钮对象
	 */
	private void prepareTest(Object sender) {
		String testText = "";
		if (sender instanceof JButton) {
			testText = ((JButton) sender).getText();
			if (!testText.startsWith("测试"))
				testText = "测试: " + testText + "　　";
		}

		// 生成任务XML文件
		if (sender == btnMakeInputXML) {
			makeInputXML();
		}
		// 执行测试任务
		else if (sender == btnExecute) {
			executeTest();
		}
		// 测试外部XML任务文件按钮
		else if (sender == btnTestFileInput) {
			labTest.setText(testText);
			testFileInput();
		}
		// 在参数面板上按选择文件按钮
		else if (sender instanceof JButton
				&& ((JButton) sender).getName() != null
				&& ((JButton) sender).getName().equals("file_chooser_button")) {
			chooseFile(sender);
		} else if (sender instanceof JButton
				&& ((JButton) sender).getName() != null
				&& ((JButton) sender).getName().equals("list_ibas")) {
			listTypeIBAs(sender);
		}
		// 具体测试按钮
		else {
			labTest.setText(testText);
			Component[] c = inputPanel.getComponents();
			for (int i = 0; c != null && i < c.length; i++)
				inputPanel.remove(c[i]);
			fileChooserMap.clear();
			textFieldMap.clear();
			ibaNameList.clear();
			ibaFieldMap.clear();
			taskXMLBuilt = false;
			memoInputXML.setText("任务请求XML");

			ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) params.get(sender);
			if (list == null) {
				inputPanel.updateUI();
				activeTestButton = null;
				return;
			}

			activeTestButton = sender;
			inputPanel.setLayout(new GridBagLayout());
			for (int i = 0; i < list.size(); i++) {
				HashMap<String, Object> h = (HashMap<String, Object>) list
						.get(i);
				String name = (String) h.get(K_NAME);
				String value = (String) h.get(K_VALUE);
				String hint = (String) h.get(K_HINT);
				Class<?> klass = (Class<?>) h.get(K_KLASS);
				Object readonly = h.get(K_READONLY);

				if (name.equals(SERVER_URL))
					value = surl;
				else if (name.equals(TYPE))
					activeTestName = value;

				JLabel lab = new JLabel(name);
				JComponent tail = null;
				JComponent fld = null;

				if (klass == File.class) {
					JTextField f = new JTextField(value);
					f.setEditable(readonly == null);
					f.setPreferredSize(new Dimension(200, 25));
					fld = f;
					JButton btn = new JButton("选择...");
					btn.setToolTipText("选择本地文件");
					btn.setName("file_chooser_button");
					tail = btn;
					fileChooserMap.put(btn, fld);
					btn.addActionListener(new TestAdapter());
				} else if (klass == JComboBox.class) {
					fld = new JComboBox();
					JComboBox combo = (JComboBox) fld;
					combo.setEditable(readonly == null);

					combo.addItem("");
					for (int ii = 0; typeNameList != null
							&& ii < typeNameList.size(); ii++) {
						combo.addItem(typeNameList.get(ii));
					}

					JButton btn = new JButton("列出...");
					btn.setToolTipText("列出类型列表和按类型名称获取属性输入列表.");
					btn.setName("list_ibas");
					tail = btn;
					buttonFieldMap.put(btn, fld);
					btn.addActionListener(new TestAdapter());
				} else {
					fld = new JTextField(value);
					((JTextField) fld).setEditable(readonly == null);
					tail = new JLabel("");
				}

				textFieldMap.put(name, fld);
				lab.setToolTipText(hint);
				fld.setToolTipText(hint);

				Component cc[] = { lab, fld, tail };
				for (int j = 0; j < 3; j++) {
					if (cc[j] instanceof JButton)
						cc[j].setFont(new Font("宋体", Font.PLAIN, 12));
					else
						cc[j].setFont(new Font("DialogInput", Font.PLAIN, 12));
					GridBagConstraints gbc = new GridBagConstraints();
					gbc.gridx = j;
					gbc.gridy = i;
					gbc.insets = new Insets(0, 4, 0, 0);
					if (j == 0) {
						gbc.anchor = GridBagConstraints.EAST;
					} else if (j == 1) {
						gbc.fill = GridBagConstraints.HORIZONTAL;
						gbc.weightx = 10;
					} else {
						gbc.anchor = GridBagConstraints.WEST;
					}
					inputPanel.add(cc[j], gbc);
				}

				// 占满区域底部的占位控件
				JLabel fillLabel = new JLabel("");
				fillLabel.setName("FILL_LABEL");
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = list.size();
				gbc.weighty = 10;
				gbc.fill = GridBagConstraints.VERTICAL;
				inputPanel.add(fillLabel, gbc);
				fillObject = fillLabel;
			}

			inputPanel.updateUI();
		}
	}

	/**
	 * 生成任务描述XML
	 */
	private void makeInputXML() {
		try {
			if (activeTestButton == null) {
				throw new Exception("请先点击相关按钮, 选择一个测试!");
			}

			JTextField typeField = (JTextField) textFieldMap.get(TYPE);
			if (typeField == null)
				throw new Exception("调用类型显示域未定义!");
			String type = typeField.getText();

			ArrayList list = (ArrayList) params.get(activeTestButton);
			if (list == null)
				throw new Exception("选中测试未定义!");

			StringWriter sw = new StringWriter();
			PrintWriter w = new PrintWriter(sw);

			final String i1 = "\t";
			final String i2 = i1 + i1;
			w.println("<?xml version=\"1.0\" encoding=\"gb2312\"?>");
			w.println("<cappdata>");
			w.println(i1 + "<capp type=\"" + type + "\">");

			// 固定参数列表
			String docType = null;
			for (int i = 0; i < list.size(); i++) {
				HashMap m = (HashMap) list.get(i);
				String name = (String) m.get(NAME);
				if (name.equals(TYPE))
					continue;

				JComponent f = (JComponent) textFieldMap.get(name);
				String value = null;
				if (f instanceof JTextField) {
					value = ((JTextField) f).getText();
					if (!((JTextField) f).isEditable() && "(不使用)".equals(value))
						continue;
				} else if (f instanceof JComboBox) {
					value = ((JComboBox) f).getSelectedItem() + "";
				} else
					throw new Exception("意料之外的控件类型: " + f.getClass().getName());

				if (name.equals("DOC_TYPE"))
					docType = value;

				if (name.equals(SERVER_URL))
					surl = value;
				m.put(VALUE, value);

				w.println(i2 + "<metadata name=\"" + name + "\" value=\"" + Tools.xmlEscape(value) + "\"/>");
			}

			// 检入时的IBA属性列表
			for (int i = 0; i < ibaNameList.size(); i++) {
				String ibaName = (String) ibaNameList.get(i);
				Object field = ibaFieldMap.get(ibaName);
				if (field != null && field instanceof JTextField) {
					JTextField ibaField = (JTextField) field;
					ibaNameValueMap.put(ibaName, ibaField.getText());
					w.println(i2 + "<metadata name=\"" + ibaName
							+ "\" value=\""
							+ Tools.xmlEscape(ibaField.getText()) + "\"/>");
				}
			}

			// 如果是检入AO，预留安装关系描述行
			if (activeTestButton == btnCheckinDocumentNew
					|| activeTestButton == btnCheckinDocumentOld) {
				if (docType != null && docType.trim().equalsIgnoreCase("AO")) {
					w.println(i2 + "<!-- 下面给出AO安装关系表 -->");
					for (int i = 0; i < 10; i++) {
						w.println(i2 + "<part_usage parent=\"\" version=\"\" "
								+ "child=\"\" quantity=\"\"/>");
					}
				}

				if (docType != null && docType.indexOf("定额") >= 0) {
					w.println(i2 + "<!-- 下面给出材料定额属性表 -->");
					for (int i = 0; i < 10; i++) {
						w.println(i2 + "<part_iba number=\"\" version=\"\" "
								+ "name=\"\" value=\"\"/>");
					}
				}
			}

			w.println(i1 + "</capp>");
			w.println("</cappdata>");

			w.flush();
			memoInputXML.setText(sw.toString());

			taskXMLBuilt = true;
		} catch (Exception e) {
			Logger.log(e);
			JOptionPane.showMessageDialog(this, "错误: " + Tools.getErrorMessage(e));
		}
	}

	/**
	 * 测试外部输入文件的处理
	 */
	private void testFileInput() {
		if (fileChooser == null)
			fileChooser = new JFileChooser();
		JFileChooser fc = (JFileChooser) fileChooser;
		int returnVal = fc.showOpenDialog(this);
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;

		try {
			File f = fc.getSelectedFile();
			BufferedReader r = new BufferedReader(new FileReader(f));
			String line = null;
			StringBuffer buf = new StringBuffer();
			while ((line = r.readLine()) != null)
				buf.append(line).append("\n");

			memoInputXML.setText(buf.toString());
			taskXMLBuilt = true;
		} catch (Exception e) {
			Logger.log(e);
			JOptionPane.showMessageDialog(this, "错误: " + Tools.getErrorMessage(e));
		}
	}

	/**
	 * 执行测试
	 */
	private void executeTest() {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
			labStatus.setText("　");

			if (!taskXMLBuilt)
				btnMakeInputXML.doClick();
			if (!taskXMLBuilt)
				return;

			File home = new File(Settings.getHomePath());
			File xmlFile = File.createTempFile("TSK", ".XML", home);
			xmlFile.deleteOnExit();
			FileOutputStream fos = new FileOutputStream(xmlFile);
			PrintWriter w = new PrintWriter(new OutputStreamWriter(fos, "gb2312"));
			w.println(memoInputXML.getText());
			w.flush();
			w.close();

			int stat = Launcher.launch(new String[] { txtUser.getText(),
					new String(txtPass.getPassword()),
					xmlFile.getAbsolutePath(), });

			String resName = Worker.getResultFileName(xmlFile.getAbsolutePath());
			File resFile = new File(resName);
			if (!resFile.exists())
				resName = "未生成";
			else {
				resName = resFile.getName();
				resFile.deleteOnExit();
			}

			String msg = activeTestName + ": 输入文件: " + xmlFile.getPath()
					+ ", 返回值: " + stat + ", 输出文件: " + resName;
			labStatus.setText(msg);

			if (resFile.exists()) {
				BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(resFile), "gb2312"));
				StringBuffer buf = new StringBuffer();
				String line = null;
				while ((line = r.readLine()) != null) {
					buf.append(line).append("\n");
				}

				r.close();
				memoOutputXML.setText(buf.toString());
			}
		} catch (Exception e) {
			Logger.log(e);
			JOptionPane.showMessageDialog(this,
					"错误: " + Tools.getErrorMessage(e));
		} finally {
			setCursor(Cursor.getDefaultCursor());
		}
	}

	/**
	 * 选择外部测试文件
	 * @param sender 测试按钮对象
	 */
	private void chooseFile(Object sender) {
		Object target = fileChooserMap.get(sender);
		if (target == null || !(target instanceof JTextField))
			return;

		if (fileChooser == null)
			fileChooser = new JFileChooser();
		JFileChooser fc = (JFileChooser) fileChooser;
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fc.getSelectedFile();
			((JTextField) target).setText(f.getAbsolutePath());
		}
	}

	/**
	 * 列出指定类型名称的属性输入
	 * @param sender
	 */
	private void listTypeIBAs(Object sender) {
		Object target = buttonFieldMap.get(sender);
		if (target == null || !(target instanceof JComboBox))
			return;

		JComboBox combo = (JComboBox) target;
		String typeName = combo.getSelectedItem() + "";
		try {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			// 如果本地还没有类型定义信息，调用服务器，取得类型、属性信息
			if (typeNameInfoMap == null && ibaNameDispMap == null
					&& typeNameList == null) {
				String userName = txtUser.getText();
				String password = new String(txtPass.getPassword());
				String serviceUrl = Worker.getServiceUrl(surl);
				HashMap<String, Object> task = new HashMap<String, Object>();
				HashMap<String, Object> capp = new LinkedHashMap<String, Object>();
				task.put(CAPP, capp);
				task.put(TYPE, GET_TYPE_INFO);
				HashMap<String, Object> result = ServerHelper.callServer(userName, password, serviceUrl, task, null, null);
				if (result != null) {
					typeNameList = (ArrayList<?>) result.get(TYPE_NAME_LIST);
					typeNameInfoMap = (HashMap<?, ?>) result.get(TYPE_NAME_INFO_MAP);
					ibaNameDispMap = (HashMap<?, ?>) result.get(IBA_NAME_DISP_MAP);

					for (int ii = 0; typeNameList != null
							&& ii < typeNameList.size(); ii++) {
						combo.addItem(typeNameList.get(ii));
					}
				}
			}

			// 当本地已有类型定义信息时
			if (typeNameInfoMap != null && ibaNameDispMap != null
					&& typeNameList != null && typeName.length() > 0) {
				// 找到类型和属性定义信息
				HashMap<?, ?> typeInfo = (HashMap<?, ?>) typeNameInfoMap.get(typeName);
				if (typeInfo == null)
					throw new Exception("未定义的类型名称：" + typeName);

				// 清除掉以前可能遗留的属性列表
				ArrayList<?> ibaNameList1 = (ArrayList<?>) typeInfo.get(TYPE_IBA_LIST);
				ibaNameList.clear();
				if (ibaNameList1 != null)
					ibaNameList.addAll(ibaNameList1);

				// 移除已添加的属性相关控件
				if (fillObject != null)
					inputPanel.remove((Component) fillObject);
				if (ibaComponents != null) {
					for (int i = 0; i < ibaComponents.size(); i++)
						inputPanel.remove((Component) ibaComponents.get(i));
					ibaComponents.clear();
				}

				// 添加新的属性标签和输入控件
				Font font = new Font("DialogInput", Font.PLAIN, 12);
				for (int i = 0; i < ibaNameList.size(); i++) {
					String ibaName = (String) ibaNameList.get(i);
					String ibaDisp = (String) ibaNameDispMap.get(ibaName);

					JLabel labIBA = new JLabel(ibaName);
					JTextField txtIBA = new JTextField();
					JLabel labTail = new JLabel("(" + ibaDisp + ")");

					labIBA.setToolTipText(ibaDisp);
					txtIBA.setToolTipText(ibaDisp);

					labIBA.setFont(font);
					txtIBA.setFont(font);
					labTail.setFont(font);
					labIBA.setForeground(Color.BLUE);

					ibaComponents.add(labIBA);
					ibaComponents.add(txtIBA);
					ibaComponents.add(labTail);
					ibaFieldMap.put(ibaName, txtIBA);

					// 设定为最近一次输入的属性值
					String ibaValue = (String) ibaNameValueMap.get(ibaName);
					if (ibaValue != null)
						txtIBA.setText(ibaValue);

					Component cc[] = { labIBA, txtIBA, labTail };
					for (int j = 0; j < 3; j++) {
						GridBagConstraints gbc = new GridBagConstraints();
						gbc.gridx = j;
						gbc.gridy = textFieldMap.size() + i;
						gbc.insets = new Insets(0, 4, 0, 0);
						if (j == 0)
							gbc.anchor = GridBagConstraints.EAST;
						else if (j == 1) {
							gbc.fill = GridBagConstraints.HORIZONTAL;
							gbc.weightx = 10;
						} else
							gbc.anchor = GridBagConstraints.WEST;
						inputPanel.add(cc[j], gbc);
					}
				}

				// 占满区域底部的占位控件
				JLabel fillLabel = new JLabel("");
				fillLabel.setName("FILL_LABEL");
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = textFieldMap.size() + ibaFieldMap.size();
				gbc.weighty = 10;
				gbc.fill = GridBagConstraints.VERTICAL;
				inputPanel.add(fillLabel, gbc);
				fillObject = fillLabel;

				inputPanel.updateUI();
			} else if (typeName.length() > 0) {
				throw new Exception("未能获取文档类型和属性数据！");
			}
		} catch (Exception e) {
			Logger.log(e);
			JOptionPane.showMessageDialog(this, Tools.getErrorMessage(e));
		}
		setCursor(Cursor.getDefaultCursor());
	}

	private void initSearchPart() {
		params.put(btnSearchPart, setValues(new Object[][] {
				{ TYPE, SEARCH_PART, null, "调用名称", "READONLY" },
				{ SERVER_URL, "", null, "服务器URL", null },
				{ WINDOW_CAPTION, "查询并获取零部件信息", null, "窗口显示标题", null },
				{ MULTIPLE_SELECTABLE, "false", null, "是否允许多选", null },
				// { VIEW, "Manufacturing", null, "零件视图", null },
				{ IBA_LIST, "*", null, "需要返回的IBA属性名称清单", null },
				{ GET_BOM_LIST, "false", null, "是否返回BOM清单", null },
		// { BASELINE_NUMBER, "", null, "在返回BOM清单时有效，指定获取BOM的基线，不指定时取最新规则", null
		// },
				}));
	}

	private void initSearchDocument() {
		params.put(btnSearchDocument, setValues(new Object[][] {
				{ TYPE, SEARCH_DOCUMENT, null, "调用名称", "READONLY" },
				{ SERVER_URL, "", null, "服务器URL", null },
				{ WINDOW_CAPTION, "查询并获取文档信息", null, "窗口显示标题", null },
				{ DOC_TYPE, "*", null, "文档小类类型", null },
				{ IBA_LIST, "*", null, "需要返回的IBA属性名称清单", null },
				{ DOWNLOAD, FALSE, null, "是否下载主文件", null }, }));
	}

	private void initDeleteDocument() {
		params.put(btnDeleteDocument, setValues(new Object[][] {
				{ TYPE, DELETE_DOCUMENT, null, "调用名称", "READONLY" },
				{ SERVER_URL, "", null, "服务器URL", null },
				{ WINDOW_CAPTION, "删除工艺文档", null, "窗口显示标题", null },
				{ DOC_TYPE, "*", null, "文档小类类型", null }, }));
	}

	private void initCheckoutDocument() {
		params.put(btnCheckoutDocument, setValues(new Object[][] {
				{ TYPE, CHECKOUT_DOCUMENT, null, "调用名称", "READONLY" },
				{ SERVER_URL, "", null, "服务器URL", null },
				{ WINDOW_CAPTION, "检出工艺文档", null, "窗口显示标题", null },
				{ DOC_TYPE, "*", null, "文档小类类型", null },
				{ IBA_LIST, "*", null, "需要返回的IBA属性名称清单", null }, }));
	}

	private void initReviseDocument() {
		params.put(btnReviseDocument, setValues(new Object[][] {
				{ TYPE, REVISE_DOCUMENT, null, "调用名称", "READONLY" },
				{ SERVER_URL, "", null, "服务器URL", null },
				{ WINDOW_CAPTION, "修订工艺文档", null, "窗口显示标题", null },
				{ DOC_TYPE, "*", null, "文档小类类型", null },
				{ IBA_LIST, "*", null, "需要返回的IBA属性名称清单", null }, }));
	}

	private void initGetToolingNumber() {
		params.put(btnGetToolingNumber, setValues(new Object[][] {
				{ TYPE, GET_TOOLING_NUMBER, null, "调用名称", "READONLY" },
				{ SERVER_URL, "", null, "服务器URL", null }, }));
	}

	private void initListPartBaselines() {
		params.put(btnListPartBaselines, setValues(new Object[][] {
				{ TYPE, LIST_PART_BASELINES, null, "调用名称", "READONLY" },
				{ SERVER_URL, "", null, "服务器URL", null },
				{ NUMBER, "", null, "零部件编号", null },
				{ VERSION, "", null, "零部件大版本号", null }, }));
	}

	private void initListPartDocuments() {
		params.put(btnListPartDocuments, setValues(new Object[][] {
				{ TYPE, LIST_PART_DOCUMENTS, null, "调用名称", "READONLY" },
				{ SERVER_URL, "", null, "服务器URL", null },
				{ NUMBER, "", null, "零部件编号", null }, }));
	}

	private void initSetDocumentInCAPPTask() {
		params.put(btnSetDocumentInCAPPTask, setValues(new Object[][] {
				{ TYPE, SET_DOCUMENT_IN_CAPP_TASK, null, "调用名称", "READONLY" },
				{ SERVER_URL, "", null, "服务器URL", null },
				{ PART_NUMBER, "", null, "零部件编号", null },
				{ PART_VERSION, "", null, "零部件大版本号", null },
				{ CAPP_TASK_NUMBER, "", null, "工艺任务编号", null },
				{ DOC_NUMBER, "", null, "文档编号", null },
				{ DOC_VERSION, "", null, "文档大版本号", null }, }));
	}

	private void initCheckinDocumentNew() {
		params.put(btnCheckinDocumentNew, setValues(new Object[][] {
				{ TYPE, CHECKIN_DOCUMENT, null, "调用名称", "READONLY" },
				{ SERVER_URL, "", null, "服务器URL", null },
				{ WINDOW_CAPTION, "检入工艺文档", null, "窗口显示标题", null },
				{ RELATED_PART_NUMBER, "", null, "关联零部件编号(关联零部件时)", null },
				{ RELATED_PART_VERSION, "", null, "关联零部件版本(关联零部件时)", null },
				// { RELATED_DOC_NUMBER, "", null, "关联文档编号(关联文档时)", null },
				// { RELATED_DOC_VERSION, "", null, "关联文档版本(关联文档时)", null },
				// { PRODUCT_NAME, "GOLF_CART", null, "所属产品名称(无关联对象时)", null },
				{ DOC_NUMBER, "", null, "检入新文件时可以不填", null },
				{ DOC_NAME, "", null, "检入新文件时可以提供一个缺省名称", null },
				{ DOC_VERSION, "", null, "检入新文件时这里不需填写", "READONLY" },
				{ DOC_NEW, "TRUE", null, "是否新建文档", "READONLY" },
				{ DOC_TYPE, "", JComboBox.class, "文档类型名称(注意, 检入新文档时必需正确填写)",
						null },
				{ PRIMARY_FILE, "", File.class, "主文件完整路径", null },
				{ SECONDARY_FILE, "", File.class, "如果有其他附件", null }, }));
	}

	private void initCheckinDocumentOld() {
		params.put(btnCheckinDocumentOld, setValues(new Object[][] {
				{ TYPE, CHECKIN_DOCUMENT, null, "调用名称", "READONLY" },
				{ SERVER_URL, "", null, "服务器URL", null },
				{ WINDOW_CAPTION, "检入工艺文档", null, "窗口显示标题", null },
				{ RELATED_PART_NUMBER, "(不使用)", null, "关联零部件编号(关联零部件时)",
						"READONLY" },
				{ RELATED_PART_VERSION, "(不使用)", null, "关联零部件版本(关联零部件时)",
						"READONLY" },
				// { RELATED_DOC_NUMBER, "(不使用)", null, "关联文档编号(关联文档时)",
				// "READONLY" },
				// { RELATED_DOC_VERSION, "(不使用)", null, "关联文档版本(关联文档时)",
				// "READONLY" },
				// { PRODUCT_NAME, "(不使用)", null, "所属产品名称(无关联对象时)", "READONLY"
				// },
				{ DOC_NUMBER, "", null, "文档编号, 原检出文档必需填写", null },
				{ DOC_VERSION, "", null, "文档版本, 原检查文件必需填写", null },
				{ DOC_NEW, "FALSE", null, "是否新建文档", "READONLY" },
				{ DOC_TYPE, "", JComboBox.class, "文档类型名称(正确填写后可以获取属性输入列表)",
						null },
				{ PRIMARY_FILE, "", File.class, "主文件完整路径", null },
				{ SECONDARY_FILE, "", File.class, "如果有其他附件", null }, }));
	}

	private void initQuickGetPart() {
		params.put(btnQuickGetPart, setValues(new Object[][] {
				{ TYPE, QUICK_GET_PART, null, "调用名称", "READONLY" },
				{ SERVER_URL, "", null, "服务器URL", null },
				{ NUMBER, "", null, "零部件编号", null },
				{ VERSION, "A", null, "零部件大版本号", null },
				{ IBA_LIST, "*", null, "需要返回的IBA属性名称清单", null },
				{ GET_BOM_LIST, "false", null, "是否获取BOM清单", null },
				{ BASELINE_NUMBER, "", null,
						"在返回BOM清单时有效，指定获取BOM的基线，不指定时取最新规则", null }, }));
	}

	private void initQuickGetDoc() {
		params.put(btnQuickGetDocument, setValues(new Object[][] {
				{ TYPE, QUICK_GET_DOCUMENT, null, "调用名称", "READONLY" },
				{ SERVER_URL, "", null, "服务器URL", null },
				{ NUMBER, "", null, "文档编号", null },
				{ VERSION, "A", null, "文档大版本号", null },
				{ IBA_LIST, "*", null, "需要返回的IBA属性名称清单", null },
				{ DOWNLOAD, "false", null, "是否需要下载主文件和附件", null }, }));
	}

	private void initQuickDeleteDoc() {
		params.put(btnQuickDeleteDocument, setValues(new Object[][] {
				{ TYPE, QUICK_DELETE_DOCUMENT, null, "调用名称", "READONLY" },
				{ SERVER_URL, "", null, "服务器URL", null },
				{ NUMBER, "", null, "文档编号", null },
				{ VERSION, "A", null, "文档大版本号", null }, }));
	}

	private void initQuickCheckoutDoc() {
		params.put(btnQuickCheckoutDocument, setValues(new Object[][] {
				{ TYPE, QUICK_CHECKOUT_DOCUMENT, null, "调用名称", "READONLY" },
				{ SERVER_URL, "", null, "服务器URL", null },
				{ NUMBER, "", null, "零部件编号", null },
				{ VERSION, "A", null, "零部件大版本号", null },
				{ IBA_LIST, "*", null, "需要返回的IBA属性名称清单", null }, }));
	}

	private void initQuickReviseDoc() {
		params.put(btnQuickReviseDocument, setValues(new Object[][] {
				{ TYPE, QUICK_REVISE_DOCUMENT, null, "调用名称", "READONLY" },
				{ SERVER_URL, "", null, "服务器URL", null },
				{ NUMBER, "", null, "零部件编号", null },
				{ VERSION, "A", null, "零部件大版本号", null },
				{ IBA_LIST, "*", null, "需要返回的IBA属性名称清单", null }, }));
	}

	private void initListDir() {
		params.put(btnListDir, setValues(new Object[][] {
				{ TYPE, LIST_DIR, null, "调用名称", "READONLY" },
				{ SERVER_URL, "", null, "服务器URL", null },
				{ NODE_CLASS, "", null,
						"结点类别(ROOT, PRODUCT, BASELINE, FOLDER, PART)", null },
				{ NODE_NUMBER, "", null, "结点编号", null },
				{ NODE_VERSION, "", null, "结点大版本号", null },
				{ BASELINE_NUMBER, "", null, "基线编号", null }, }));
	}

	private void initListWorkitems() {
		params.put(btnListWorkitems, setValues(new Object[][] {
				{ TYPE, LIST_WORKITEMS, null, "调用名称", "READONLY" },
				{ SERVER_URL, "", null, "服务器URL", null }, }));
	}

	private void initListMarkup() {
		params.put(btnListMarkup, setValues(new Object[][] {
				{ TYPE, LIST_MARKUP, null, "调用名称", "READONLY" },
				{ SERVER_URL, "", null, "服务器URL", null },
				{ DOC_NUMBER, "", null, "文档编号", null },
				{ DOC_VERSION, "", null, "文档版本", null }, }));
	}

	private void initSaveMarkup() {
		params.put(btnSaveMarkup, setValues(new Object[][] {
				{ TYPE, SAVE_MARKUP, null, "调用名称", "READONLY" },
				{ SERVER_URL, "", null, "服务器URL", null },
				{ DOC_NUMBER, "", null, "文档编号", null },
				{ DOC_VERSION, "", null, "文档版本", null },
				{ NAME, "", null, "批准名称(可选)", null },
				{ DESCRIPTION, "", null, "批注说明(可选)", null },
				{ PROMPT, "true", null, "显示确认窗口", null },
				{ PRIMARY_FILE, "", File.class, "批注文件完整路径", null }, }));
	}

	private void initGetMarkup() {
		params.put(btnGetMarkup, setValues(new Object[][] {
				{ TYPE, GET_MARKUP, null, "调用名称", "READONLY" },
				{ SERVER_URL, "", null, "服务器URL", null },
				{ DOC_NUMBER, "", null, "文档编号", null },
				{ DOC_VERSION, "", null, "文档版本", null },
				{ MARKUP_NAME, "", null, "批注名称", null }, }));
	}

	private void initGetDocumentSignatures() {
		params.put(btnGetDocumentSignatures, setValues(new Object[][] {
				{ TYPE, GET_DOCUMENT_SIGNATURES, null, "调用名称", "READONLY" },
				{ SERVER_URL, "", null, "服务器URL", null },
				{ DOC_NUMBER, "", null, "文档编号", null },
				{ DOC_VERSION, "", null, "文档版本", null }, }));
	}

	private void initOpenPartPage() {
		params.put(btnOpenPartPage, setValues(new Object[][] {
				{ TYPE, OPEN_PART_PAGE, null, "调用名称", "READONLY" },
				{ SERVER_URL, "", null, "服务器URL", null },
				{ OID, "", null, "零部件对象ID(可替代编号版本参数)", null },
				{ PART_NUMBER, "", null, "零部件编号(零部件的属性页面)", null },
				{ DOC_NUMBER, "", null, "文档编号(文档的相关零部件页面)", null },
				{ VERSION, "", null, "零部件或文档的大版本好(零部件编号优先)", null },
				{ GET_URL_ONLY, "false", null, "TRUE/FALSE, 只获取URL而不打开浏览器",
						null }, }));
	}

	private void initOpenDocPage() {
		params.put(btnOpenDocPage, setValues(new Object[][] {
				{ TYPE, OPEN_DOCUMENT_PAGE, null, "调用名称", "READONLY" },
				{ SERVER_URL, "", null, "服务器URL", null },
				{ OID, "", null, "文档对象ID(可替代编号版本参数)", null },
				{ DOC_NUMBER, "", null, "文档编号(文档的属性页面)", null },
				{ PART_NUMBER, "", null, "零部件编号(零部件的相关文档页面)", null },
				{ VERSION, "", null, "零部件或文档的大版本号(文档编号优先)", null },
				{ GET_URL_ONLY, "false", null, "TRUE/FALSE, 只获取URL而不打开浏览器",
						null }, }));
	}

	private void initOpenWorkitemPage() {
		params.put(btnOpenWorkitemPage, setValues(new Object[][] {
				{ TYPE, OPEN_WORKITEM_PAGE, null, "调用名称", "READONLY" },
				{ SERVER_URL, "", null, "服务器URL", null },
				{ OID, "", null, "流程任务对象ID", null },
				{ GET_URL_ONLY, "false", null, "TRUE/FALSE, 只获取URL而不打开浏览器",
						null }, }));
	}

	private void initVerifyAuthorization() {
		params.put(btnVerifyAuthorization, setValues(new Object[][] {
				{ TYPE, VERIFY_AUTHORIZATION, null, "调用名称", "READONLY" },
				{ SERVER_URL, "", null, "服务器URL", null }, }));
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setSize(731, 565);
		this.setContentPane(getJContentPane());
		this.setTitle("Windchill PDMLink CAPP集成接口");
	}

	/**
	 * This method initializes jContentPane
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints33 = new GridBagConstraints();
			gridBagConstraints33.gridx = 3;
			gridBagConstraints33.insets = new Insets(2, 0, 0, 4);
			gridBagConstraints33.gridy = 16;
			GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
			gridBagConstraints22.gridx = 2;
			gridBagConstraints22.insets = new Insets(2, 0, 0, 4);
			gridBagConstraints22.gridy = 16;
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 1;
			gridBagConstraints12.insets = new Insets(2, 4, 0, 4);
			gridBagConstraints12.gridy = 16;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 2;
			gridBagConstraints11.insets = new java.awt.Insets(2, 0, 0, 4);
			gridBagConstraints11.gridy = 9;
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 1;
			gridBagConstraints10.insets = new java.awt.Insets(2, 4, 0, 4);
			gridBagConstraints10.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints10.anchor = GridBagConstraints.SOUTH;
			gridBagConstraints10.gridy = 15;
			GridBagConstraints gridBagConstraints91 = new GridBagConstraints();
			gridBagConstraints91.gridx = 3;
			gridBagConstraints91.insets = new java.awt.Insets(2, 0, 0, 4);
			gridBagConstraints91.gridy = 12;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 3;
			gridBagConstraints8.insets = new java.awt.Insets(2, 0, 0, 4);
			gridBagConstraints8.gridy = 10;
			GridBagConstraints gridBagConstraints71 = new GridBagConstraints();
			gridBagConstraints71.gridx = 2;
			gridBagConstraints71.insets = new java.awt.Insets(2, 0, 0, 4);
			gridBagConstraints71.anchor = java.awt.GridBagConstraints.NORTH;
			gridBagConstraints71.gridy = 13;
			GridBagConstraints gridBagConstraints61 = new GridBagConstraints();
			gridBagConstraints61.gridx = 2;
			gridBagConstraints61.insets = new java.awt.Insets(2, 0, 0, 4);
			gridBagConstraints61.gridy = 12;
			GridBagConstraints gridBagConstraints52 = new GridBagConstraints();
			gridBagConstraints52.gridx = 2;
			gridBagConstraints52.insets = new java.awt.Insets(2, 0, 0, 4);
			gridBagConstraints52.gridy = 8;
			GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
			gridBagConstraints41.gridx = 2;
			gridBagConstraints41.insets = new java.awt.Insets(2, 0, 0, 4);
			gridBagConstraints41.gridy = 7;
			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			gridBagConstraints31.gridx = 2;
			gridBagConstraints31.insets = new java.awt.Insets(2, 0, 0, 4);
			gridBagConstraints31.gridy = 6;
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 2;
			gridBagConstraints21.insets = new java.awt.Insets(2, 0, 0, 4);
			gridBagConstraints21.gridy = 4;
			GridBagConstraints gridBagConstraints32 = new GridBagConstraints();
			gridBagConstraints32.gridx = 4;
			gridBagConstraints32.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints32.gridheight = 12;
			gridBagConstraints32.weighty = 1.0;
			gridBagConstraints32.weightx = 1.0;
			gridBagConstraints32.gridy = 4;
			GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
			gridBagConstraints17.gridx = 2;
			gridBagConstraints17.insets = new java.awt.Insets(2, 0, 0, 4);
			gridBagConstraints17.anchor = GridBagConstraints.SOUTH;
			gridBagConstraints17.gridy = 15;
			GridBagConstraints gridBagConstraints161 = new GridBagConstraints();
			gridBagConstraints161.gridx = 4;
			gridBagConstraints161.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints161.insets = new java.awt.Insets(4, 0, 4, 4);
			gridBagConstraints161.gridy = 3;
			GridBagConstraints gridBagConstraints141 = new GridBagConstraints();
			gridBagConstraints141.gridx = 4;
			gridBagConstraints141.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints141.insets = new java.awt.Insets(2, 0, 0, 0);
			gridBagConstraints141.gridy = 16;
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 1;
			gridBagConstraints9.insets = new java.awt.Insets(8, 0, 0, 0);
			gridBagConstraints9.gridy = 5;
			jLabel1 = new JLabel();
			jLabel1.setText("　");
			jLabel1.setFont(new Font("宋体", Font.PLAIN, 12));
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 3;
			gridBagConstraints7.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints7.insets = new java.awt.Insets(2, 0, 0, 4);
			gridBagConstraints7.gridy = 7;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 3;
			gridBagConstraints6.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints6.insets = new java.awt.Insets(2, 0, 0, 4);
			gridBagConstraints6.gridy = 6;
			GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
			gridBagConstraints51.gridx = 3;
			gridBagConstraints51.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints51.insets = new java.awt.Insets(2, 0, 0, 4);
			gridBagConstraints51.gridy = 5;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 3;
			gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints4.insets = new java.awt.Insets(2, 0, 0, 4);
			gridBagConstraints4.gridy = 4;
			GridBagConstraints gridBagConstraints110 = new GridBagConstraints();
			gridBagConstraints110.gridx = 1;
			gridBagConstraints110.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints110.insets = new java.awt.Insets(0, 4, 4, 4);
			gridBagConstraints110.gridwidth = 4;
			gridBagConstraints110.gridy = 19;
			labStatus = new JLabel();
			labStatus.setText("　");
			labStatus.setFont(new Font("Dialog", Font.PLAIN, 12));
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints.gridy = 18;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.weighty = 10.0;
			gridBagConstraints.gridwidth = 4;
			gridBagConstraints.insets = new java.awt.Insets(8, 4, 4, 4);
			gridBagConstraints.gridx = 1;
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints111 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			jLabel = new JLabel();
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints210 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints29 = new GridBagConstraints();
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new java.awt.GridBagLayout());
			gridBagConstraints2.gridx = 1;
			gridBagConstraints2.gridy = 4;
			gridBagConstraints2.gridwidth = 1;
			gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints2.insets = new java.awt.Insets(2, 4, 0, 4);
			gridBagConstraints3.gridx = 1;
			gridBagConstraints3.anchor = java.awt.GridBagConstraints.SOUTH;
			gridBagConstraints3.gridy = 12;
			gridBagConstraints3.gridwidth = 1;
			gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints3.insets = new java.awt.Insets(2, 4, 0, 4);
			gridBagConstraints29.gridx = 1;
			gridBagConstraints29.gridy = 8;
			gridBagConstraints29.gridwidth = 1;
			gridBagConstraints29.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints29.insets = new java.awt.Insets(2, 4, 0, 4);
			gridBagConstraints29.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints1.gridx = 3;
			gridBagConstraints1.anchor = GridBagConstraints.SOUTH;
			gridBagConstraints1.gridy = 15;
			gridBagConstraints1.insets = new java.awt.Insets(2, 0, 0, 4);
			gridBagConstraints1.gridwidth = 1;
			gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints210.gridx = 1;
			gridBagConstraints210.gridy = 7;
			gridBagConstraints210.insets = new java.awt.Insets(2, 4, 0, 4);
			gridBagConstraints210.gridwidth = 1;
			gridBagConstraints210.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints5.gridx = 1;
			gridBagConstraints5.gridy = 3;
			gridBagConstraints5.gridwidth = 3;
			gridBagConstraints5.insets = new java.awt.Insets(2, 4, 2, 4);
			gridBagConstraints5.fill = java.awt.GridBagConstraints.HORIZONTAL;
			jLabel.setText("测试内容:");
			jLabel.setFont(new Font("宋体", Font.PLAIN, 14));
			gridBagConstraints13.gridx = 3;
			gridBagConstraints13.gridy = 9;
			gridBagConstraints13.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints13.insets = new java.awt.Insets(2, 0, 0, 4);
			gridBagConstraints15.gridx = 1;
			gridBagConstraints15.gridy = 6;
			gridBagConstraints15.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints15.insets = new java.awt.Insets(2, 4, 0, 4);
			gridBagConstraints111.gridx = 1;
			gridBagConstraints111.gridy = 9;
			gridBagConstraints111.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints111.insets = new java.awt.Insets(2, 4, 0, 4);
			gridBagConstraints16.gridx = 1;
			gridBagConstraints16.anchor = GridBagConstraints.NORTH;
			gridBagConstraints16.gridy = 13;
			gridBagConstraints16.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints16.insets = new java.awt.Insets(2, 4, 0, 4);
			jContentPane.setFont(new Font("宋体", Font.PLAIN, 12));
			jContentPane.add(jLabel, gridBagConstraints5);
			jContentPane.add(getBtnSearchPart(), gridBagConstraints2);
			jContentPane.add(getBtnCheckinDocumentNew(), gridBagConstraints3);
			jContentPane.add(getBtnCheckinDocumentOld(), gridBagConstraints16);
			jContentPane.add(getBtnCheckoutDocument(), gridBagConstraints29);
			jContentPane.add(getBtnVerifyAuthorization(), gridBagConstraints1);
			jContentPane.add(getBtnDeleteDocument(), gridBagConstraints210);
			jContentPane.add(getBtnGotoProductStructurePage(),
					gridBagConstraints13);
			jContentPane.add(getBtnSearchDocument(), gridBagConstraints15);
			jContentPane.add(getBtnReviseDocument(), gridBagConstraints111);
			jContentPane.add(getJSplitPane(), gridBagConstraints);
			jContentPane.add(labStatus, gridBagConstraints110);
			jContentPane.add(getBtnListMarkup(), gridBagConstraints4);
			jContentPane.add(getBtnSaveMarkup(), gridBagConstraints51);
			jContentPane.add(getBtnGetMarkup(), gridBagConstraints6);
			jContentPane
					.add(getBtnGetDocumentSignatures(), gridBagConstraints7);
			jContentPane.add(jLabel1, gridBagConstraints9);
			jContentPane.add(getJPanel(), gridBagConstraints141);
			jContentPane.add(getJPanel1(), gridBagConstraints161);
			jContentPane.add(getBtnTestFileInput(), gridBagConstraints17);
			jContentPane.add(getInputPanel0(), gridBagConstraints32);
			jContentPane.add(getBtnQuickGetPart(), gridBagConstraints21);
			jContentPane.add(getBtnQuickGetDocument(), gridBagConstraints31);
			jContentPane.add(getBtnQuickDeleteDocument(), gridBagConstraints41);
			jContentPane.add(getBtnQuickCheckoutDocument(),
					gridBagConstraints52);
			jContentPane.add(getBtnListDir(), gridBagConstraints61);
			jContentPane.add(getBtnListWorkitems(), gridBagConstraints71);
			jContentPane.add(getBtnOpenDocPage(), gridBagConstraints8);
			jContentPane.add(getBtnOpenWorkitemPage(), gridBagConstraints91);
			jContentPane.add(getBtnGetToolingNumber(), gridBagConstraints10);
			jContentPane.add(getBtnQuickReviseDocument(), gridBagConstraints11);
			jContentPane.add(getBtnListPartBaselines(), gridBagConstraints12);
			jContentPane.add(getBtnListPartDocuments(), gridBagConstraints22);
			jContentPane.add(getBtnSetDocumentInCAPPTask(),
					gridBagConstraints33);
		}
		return jContentPane;
	}

	/**
	 * This method initializes btnResolveGYHZ
	 * @return javax.swing.JButton
	 */
	private JButton getBtnReviseDocument() {
		if (btnReviseDocument == null) {
			btnReviseDocument = new JButton();
			btnReviseDocument.setFont(new Font("宋体", Font.PLAIN, 12));
			btnReviseDocument.setText("修订工艺文档");
		}
		return btnReviseDocument;
	}

	/**
	 * This method initializes btnGetPartData
	 * @return javax.swing.JButton
	 */
	private JButton getBtnSearchPart() {
		if (btnSearchPart == null) {
			btnSearchPart = new JButton();
			btnSearchPart.setText("查询零件信息");
			btnSearchPart.setFont(new Font("宋体", Font.PLAIN, 12));
			btnSearchPart.setPreferredSize(new Dimension(107, 25));
			btnSearchPart.setActionCommand("测试: 获取零件信息");
		}
		return btnSearchPart;
	}

	/**
	 * This method initializes btnGetPartBOM
	 * @return javax.swing.JButton
	 */
	private JButton getBtnSearchDocument() {
		if (btnSearchDocument == null) {
			btnSearchDocument = new JButton();
			btnSearchDocument.setText("查询文档信息");
			btnSearchDocument.setFont(new Font("宋体", Font.PLAIN, 12));
		}
		return btnSearchDocument;
	}

	/**
	 * This method initializes btnCheckoutDocument
	 * @return javax.swing.JButton
	 */
	private JButton getBtnCheckinDocumentNew() {
		if (btnCheckinDocumentNew == null) {
			btnCheckinDocumentNew = new JButton();
			btnCheckinDocumentNew.setText("检入新建文档");
			btnCheckinDocumentNew.setFont(new Font("宋体", Font.PLAIN, 12));
		}
		return btnCheckinDocumentNew;
	}

	/**
	 * This method initializes btnDownloadDocument
	 * @return javax.swing.JButton
	 */
	private JButton getBtnCheckinDocumentOld() {
		if (btnCheckinDocumentOld == null) {
			btnCheckinDocumentOld = new JButton();
			btnCheckinDocumentOld.setText("检入原有文档");
			btnCheckinDocumentOld.setFont(new Font("宋体", Font.PLAIN, 12));
		}
		return btnCheckinDocumentOld;
	}

	/**
	 * This method initializes btnCheckinDocument
	 * @return javax.swing.JButton
	 */
	private JButton getBtnCheckoutDocument() {
		if (btnCheckoutDocument == null) {
			btnCheckoutDocument = new JButton();
			btnCheckoutDocument.setText("检出工艺文档");
			btnCheckoutDocument.setFont(new Font("宋体", Font.PLAIN, 12));
			btnCheckoutDocument
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
						}
					});
		}
		return btnCheckoutDocument;
	}

	/**
	 * This method initializes btnGetPartBOM
	 * @return javax.swing.JButton
	 */
	private JButton getBtnVerifyAuthorization() {
		if (btnVerifyAuthorization == null) {
			btnVerifyAuthorization = new JButton();
			btnVerifyAuthorization.setText("验证用户口令");
			btnCheckinDocumentOld.setFont(new Font("宋体", Font.PLAIN, 12));
			btnVerifyAuthorization.setName("btnClearBox");
		}
		return btnVerifyAuthorization;
	}

	/**
	 * This method initializes btnSelectAtt2Checkin
	 * @return javax.swing.JButton
	 */
	private JButton getBtnDeleteDocument() {
		if (btnDeleteDocument == null) {
			btnDeleteDocument = new JButton();
			btnDeleteDocument.setText("删除工艺文档");
			btnDeleteDocument.setFont(new Font("宋体", Font.PLAIN, 12));
			btnDeleteDocument.setName("btnDoXMLTask");
		}
		return btnDeleteDocument;
	}

	/**
	 * This method initializes jButton2
	 * @return javax.swing.JButton
	 */
	private JButton getBtnGotoProductStructurePage() {
		if (btnOpenPartPage == null) {
			btnOpenPartPage = new JButton();
			btnOpenPartPage.setText("打开零件页面");
			btnOpenPartPage.setFont(new Font("宋体", Font.PLAIN, 12));
			btnOpenPartPage.setName("btnLogout");
		}
		return btnOpenPartPage;
	}

	/**
	 * This method initializes jSplitPane
	 * @return javax.swing.JSplitPane
	 */
	private JSplitPane getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new JSplitPane();
			jSplitPane.setResizeWeight(0.5D);
			jSplitPane.setPreferredSize(new java.awt.Dimension(149, 400));
			jSplitPane.setLeftComponent(getJScrollPane());
			jSplitPane.setRightComponent(getJScrollPane1());
		}
		return jSplitPane;
	}

	/**
	 * This method initializes jScrollPane
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getMemoInputXML());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jTextArea
	 * @return javax.swing.JTextArea
	 */
	private JTextArea getMemoInputXML() {
		if (memoInputXML == null) {
			memoInputXML = new JTextArea();
			memoInputXML.setName("textArea");
			memoInputXML.setFont(new Font("DialogInput", Font.PLAIN, 12));
			memoInputXML.setText("任务请求XML");
		}
		return memoInputXML;
	}

	/**
	 * This method initializes jScrollPane1
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane1() {
		if (jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setViewportView(getMemoOutputXML());
		}
		return jScrollPane1;
	}

	/**
	 * This method initializes jTextArea1
	 * @return javax.swing.JTextArea
	 */
	private JTextArea getMemoOutputXML() {
		if (memoOutputXML == null) {
			memoOutputXML = new JTextArea();
			memoOutputXML.setText("执行结果XML");
			memoOutputXML.setFont(new Font("DialogInput", Font.PLAIN, 12));
		}
		return memoOutputXML;
	}

	/**
	 * This method initializes jButton1
	 * @return javax.swing.JButton
	 */
	private JButton getBtnListMarkup() {
		if (btnListMarkup == null) {
			btnListMarkup = new JButton();
			btnListMarkup.setText("文档批注列表");
			btnListMarkup.setPreferredSize(new Dimension(107, 25));
			btnListMarkup.setFont(new Font("宋体", Font.PLAIN, 12));
		}
		return btnListMarkup;
	}

	/**
	 * This method initializes jButton2
	 * @return javax.swing.JButton
	 */
	private JButton getBtnSaveMarkup() {
		if (btnSaveMarkup == null) {
			btnSaveMarkup = new JButton();
			btnSaveMarkup.setText("保存文档批注");
			btnSaveMarkup.setFont(new Font("宋体", Font.PLAIN, 12));
		}
		return btnSaveMarkup;
	}

	/**
	 * This method initializes jButton3
	 * @return javax.swing.JButton
	 */
	private JButton getBtnGetMarkup() {
		if (btnGetMarkup == null) {
			btnGetMarkup = new JButton();
			btnGetMarkup.setText("获取文档批注");
			btnGetMarkup.setFont(new Font("宋体", Font.PLAIN, 12));
		}
		return btnGetMarkup;
	}

	/**
	 * This method initializes 获取文档签名
	 * @return javax.swing.JButton
	 */
	private JButton getBtnGetDocumentSignatures() {
		if (btnGetDocumentSignatures == null) {
			btnGetDocumentSignatures = new JButton();
			btnGetDocumentSignatures.setText("获取文档签名");
			btnGetDocumentSignatures.setFont(new Font("宋体", Font.PLAIN, 12));
		}
		return btnGetDocumentSignatures;
	}

	/**
	 * This method initializes jButton4
	 * @return javax.swing.JButton
	 */
	private JButton getBtnExecute() {
		if (btnExecute == null) {
			btnExecute = new JButton();
			btnExecute.setText("执行测试");
			btnExecute.setFont(new Font("宋体", Font.PLAIN, 12));
			btnExecute
					.setToolTipText("请先选择测试内容，填写测试参数，生成任务XML，视需要修改XML后按执行测试.");
		}
		return btnExecute;
	}

	/**
	 * This method initializes btnMakeInputXML
	 * @return javax.swing.JButton
	 */
	private JButton getBtnMakeInputXML() {
		if (btnMakeInputXML == null) {
			btnMakeInputXML = new JButton();
			btnMakeInputXML.setText("生成任务XML");
			btnMakeInputXML.setFont(new Font("宋体", Font.PLAIN, 12));
			btnMakeInputXML.setToolTipText("请在修改测试参数后按此按钮重新生成任务XML");
		}
		return btnMakeInputXML;
	}

	/**
	 * This method initializes jPanel
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			labTest = new JLabel();
			labTest.setText("　");
			labTest.setFont(new Font("DialogInput", Font.PLAIN, 12));
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setAlignment(java.awt.FlowLayout.RIGHT);
			flowLayout.setVgap(0);
			flowLayout.setHgap(4);
			jPanel = new JPanel();
			jPanel.setLayout(flowLayout);
			jPanel.add(labTest, null);
			jPanel.add(getBtnMakeInputXML(), null);
			jPanel.add(getBtnExecute(), null);
		}
		return jPanel;
	}

	/**
	 * This method initializes jPanel1
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jLabel6 = new JLabel();
			jLabel6.setText("测试参数:　　");
			jLabel6.setFont(new Font("宋体", Font.PLAIN, 14));
			jLabel4 = new JLabel();
			jLabel4.setText("　登录口令:　");
			jLabel4.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabel3 = new JLabel();
			jLabel3.setText("　登录名:　");
			jLabel3.setFont(new Font("Dialog", Font.PLAIN, 12));
			jPanel1 = new JPanel();
			jPanel1.setLayout(new BoxLayout(getJPanel1(), BoxLayout.X_AXIS));
			jPanel1.add(jLabel6, null);
			jPanel1.add(jLabel3, null);
			jPanel1.add(getTxtUser(), null);
			jPanel1.add(jLabel4, null);
			jPanel1.add(getTxtPass(), null);
		}
		return jPanel1;
	}

	/**
	 * This method initializes txtUser
	 * @return javax.swing.JTextField
	 */
	private JTextField getTxtUser() {
		if (txtUser == null) {
			txtUser = new JTextField();
			txtUser.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return txtUser;
	}

	/**
	 * This method initializes txtPass
	 * @return javax.swing.JPasswordField
	 */
	private JPasswordField getTxtPass() {
		if (txtPass == null) {
			txtPass = new JPasswordField();
			txtPass.setFont(new Font("Dialog", Font.PLAIN, 12));
		}
		return txtPass;
	}

	/**
	 * This method initializes jButton
	 * @return javax.swing.JButton
	 */
	private JButton getBtnTestFileInput() {
		if (btnTestFileInput == null) {
			btnTestFileInput = new JButton();
			btnTestFileInput.setFont(new Font("宋体", Font.PLAIN, 12));
			btnTestFileInput.setText("测试外部文件");
			btnTestFileInput.setName("btnClearBox");
		}
		return btnTestFileInput;
	}

	/**
	 * This method initializes jPanel2
	 * @return javax.swing.JPanel
	 */
	private JPanel getInputPanel0() {
		if (inputPanel0 == null) {
			inputPanel0 = new JPanel();
			inputPanel0.setLayout(new BorderLayout());
			inputPanel0.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
			inputPanel0.setPreferredSize(new java.awt.Dimension(25, 500));
			inputPanel0.add(getJScrollPane2(), java.awt.BorderLayout.CENTER);
		}
		return inputPanel0;
	}

	/**
	 * This method initializes jScrollPane2
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane2() {
		if (jScrollPane2 == null) {
			jScrollPane2 = new JScrollPane();
			jScrollPane2
					.setHorizontalScrollBarPolicy(javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			jScrollPane2
					.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			jScrollPane2.setViewportView(getInputPanel());
		}
		return jScrollPane2;
	}

	/**
	 * This method initializes jPanel2
	 * @return javax.swing.JPanel
	 */
	private JPanel getInputPanel() {
		if (inputPanel == null) {
			inputPanel = new JPanel();
			inputPanel.setLayout(new GridBagLayout());
		}
		return inputPanel;
	}

	/**
	 * This method initializes jButton
	 * @return javax.swing.JButton
	 */
	private JButton getBtnQuickGetPart() {
		if (btnQuickGetPart == null) {
			btnQuickGetPart = new JButton();
			btnQuickGetPart.setText("读取指定零件");
			btnQuickGetPart.setPreferredSize(new Dimension(107, 25));
			btnQuickGetPart.setFont(new Font("宋体", Font.PLAIN, 12));
		}
		return btnQuickGetPart;
	}

	/**
	 * This method initializes jButton1
	 * @return javax.swing.JButton
	 */
	private JButton getBtnQuickGetDocument() {
		if (btnQuickGetDocument == null) {
			btnQuickGetDocument = new JButton();
			btnQuickGetDocument.setText("读取指定文档");
			btnQuickGetDocument.setFont(new Font("宋体", Font.PLAIN, 12));
		}
		return btnQuickGetDocument;
	}

	/**
	 * This method initializes jButton2
	 * @return javax.swing.JButton
	 */
	private JButton getBtnQuickDeleteDocument() {
		if (btnQuickDeleteDocument == null) {
			btnQuickDeleteDocument = new JButton();
			btnQuickDeleteDocument.setText("删除指定文档");
			btnQuickDeleteDocument.setFont(new Font("宋体", Font.PLAIN, 12));
		}
		return btnQuickDeleteDocument;
	}

	/**
	 * This method initializes jButton3
	 * @return javax.swing.JButton
	 */
	private JButton getBtnQuickCheckoutDocument() {
		if (btnQuickCheckoutDocument == null) {
			btnQuickCheckoutDocument = new JButton();
			btnQuickCheckoutDocument.setText("检出指定文档");
			btnQuickCheckoutDocument.setFont(new Font("宋体", Font.PLAIN, 12));
		}
		return btnQuickCheckoutDocument;
	}

	/**
	 * This method initializes jButton4
	 * @return javax.swing.JButton
	 */
	private JButton getBtnListDir() {
		if (btnListDir == null) {
			btnListDir = new JButton();
			btnListDir.setText("列出虚拟目录");
			btnListDir.setFont(new Font("宋体", Font.PLAIN, 12));
		}
		return btnListDir;
	}

	/**
	 * This method initializes jButton5
	 * @return javax.swing.JButton
	 */
	private JButton getBtnListWorkitems() {
		if (btnListWorkitems == null) {
			btnListWorkitems = new JButton();
			btnListWorkitems.setText("列出流程任务");
			btnListWorkitems.setFont(new Font("宋体", Font.PLAIN, 12));
		}
		return btnListWorkitems;
	}

	/**
	 * This method initializes jButton6
	 * @return javax.swing.JButton
	 */
	private JButton getBtnOpenDocPage() {
		if (btnOpenDocPage == null) {
			btnOpenDocPage = new JButton();
			btnOpenDocPage.setText("打开文档页面");
			btnOpenDocPage.setFont(new Font("宋体", Font.PLAIN, 12));
		}
		return btnOpenDocPage;
	}

	/**
	 * This method initializes jButton7
	 * @return javax.swing.JButton
	 */
	private JButton getBtnOpenWorkitemPage() {
		if (btnOpenWorkitemPage == null) {
			btnOpenWorkitemPage = new JButton();
			btnOpenWorkitemPage.setText("打开任务页面");
			btnOpenWorkitemPage.setFont(new Font("宋体", Font.PLAIN, 12));
		}
		return btnOpenWorkitemPage;
	}

	/**
	 * This method initializes jButton8
	 * @return javax.swing.JButton
	 */
	private JButton getBtnGetToolingNumber() {
		if (btnGetToolingNumber == null) {
			btnGetToolingNumber = new JButton();
			btnGetToolingNumber.setText("工装申请取号");
			btnGetToolingNumber.setFont(new Font("宋体", Font.PLAIN, 12));
		}
		return btnGetToolingNumber;
	}

	/**
	 * This method initializes jButton
	 * @return javax.swing.JButton
	 */
	private JButton getBtnQuickReviseDocument() {
		if (btnQuickReviseDocument == null) {
			btnQuickReviseDocument = new JButton();
			btnQuickReviseDocument.setFont(new Font("宋体", Font.PLAIN, 12));
			btnQuickReviseDocument.setText("修订指定文档");
		}
		return btnQuickReviseDocument;
	}

	/**
	 * This method initializes btnListPartBaselines
	 * @return javax.swing.JButton
	 */
	private JButton getBtnListPartBaselines() {
		if (btnListPartBaselines == null) {
			btnListPartBaselines = new JButton();
			btnListPartBaselines.setFont(new Font("宋体", Font.PLAIN,
					12));
			btnListPartBaselines.setText("列出零件基线");
		}
		return btnListPartBaselines;
	}

	/**
	 * This method initializes btnListPartDocuments
	 * @return javax.swing.JButton
	 */
	private JButton getBtnListPartDocuments() {
		if (btnListPartDocuments == null) {
			btnListPartDocuments = new JButton();
			btnListPartDocuments.setFont(new Font("宋体", Font.PLAIN, 12));
			btnListPartDocuments.setText("列出所有工艺");
		}
		return btnListPartDocuments;
	}

	/**
	 * This method initializes btnSetDocumentInBaseline
	 * @return javax.swing.JButton
	 */
	private JButton getBtnSetDocumentInCAPPTask() {
		if (btnSetDocumentInCAPPTask == null) {
			btnSetDocumentInCAPPTask = new JButton();
			btnSetDocumentInCAPPTask.setFont(new Font("宋体", Font.PLAIN, 12));
			btnSetDocumentInCAPPTask.setText("指定零件工艺");
			btnSetDocumentInCAPPTask.setToolTipText("指定零件在工艺任务中使用现有的指定工艺");
		}
		return btnSetDocumentInCAPPTask;
	}

	public static void main(String[] args) throws InterruptedException {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			Logger.log(e);
			e.printStackTrace();
		}
		test("wcadmin", "winadmin");
	}

	public static void test(String user, String pass)
			throws InterruptedException {
		doTest(user, pass);
	}

	class TestAdapter extends WindowAdapter implements ActionListener {
		public void windowClosing(WindowEvent e) {
			HashMap<String, Object> vars = Settings.getVariables();
			vars.put(SERVER_URL, surl);
			Settings.saveVariables();
			System.exit(0);
		}

		public void actionPerformed(ActionEvent e) {
			prepareTest(e.getSource());
		}
	}
}