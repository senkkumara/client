/**
 * Created on 2006-1-7
 * @author liuld
 */
package ext.fast.capp.client;

import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import ext.fast.util.Tools;

/**
 * Created on 2006-1-7
 * 
 * @author liuld 历史数据导入支持
 */
public class Loader extends JFrame implements CAPPConstants, Runnable {

	private Object fc = null;
	private HashMap webInfo = new HashMap();
	private Exception error;
	private HashMap loadResult;
	private long total = 100;
	private long progress = 0;
	private String text = "";

	private JPanel jContentPane = null;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JTextField txtUser = null;
	private JPasswordField txtPass = null;
	private JButton btnOk = null;
	private JButton btnCancel = null;
	private JLabel jLabel2 = null;
	private JTextField txtFile = null;
	private JButton btnSelect = null;
	private JProgressBar progressBar = null;

	private JLabel jLabel3 = null;

	private JTextField txtServer = null;

	private Object lock = new Object();

	/**
	 * This method initializes jProgressBar
	 * 
	 * @return javax.swing.JProgressBar
	 */
	private JProgressBar getJProgressBar() {
		if (progressBar == null) {
			progressBar = new JProgressBar();
			progressBar.setVisible(false);
			progressBar.setBounds(new java.awt.Rectangle(6, 223, 344, 16));
		}
		return progressBar;
	}

	/**
	 * This method initializes
	 * 
	 */
	public Loader() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		this.setSize(new java.awt.Dimension(362, 270));
		this.setResizable(false);
		this.setTitle("导入历史数据");
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel3 = new JLabel();
			jLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabel3.setText("服务器：");
			jLabel3.setVerticalAlignment(SwingConstants.CENTER);
			jLabel3.setPreferredSize(new java.awt.Dimension(48, 22));
			jLabel3.setLocation(new java.awt.Point(15, 15));
			jLabel3.setSize(new java.awt.Dimension(61, 22));
			jLabel3.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabel2 = new JLabel();
			jLabel2.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
			jLabel2.setLocation(new java.awt.Point(15, 106));
			jLabel2.setSize(new java.awt.Dimension(61, 22));
			jLabel2.setText("文件清单：");
			jLabel1 = new JLabel();
			jLabel1.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
			jLabel1.setLocation(new java.awt.Point(15, 76));
			jLabel1.setSize(new java.awt.Dimension(61, 22));
			jLabel1.setText("口令：");
			jLabel = new JLabel();
			jLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			jLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
			jLabel.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
			jLabel.setLocation(new java.awt.Point(15, 46));
			jLabel.setSize(new java.awt.Dimension(61, 22));
			jLabel.setText("用户名：");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(jLabel, null);
			jContentPane.add(jLabel1, null);
			jContentPane.add(getJTextField(), null);
			jContentPane.add(getJPasswordField(), null);
			jContentPane.add(getJButton(), null);
			jContentPane.add(getJButton1(), null);
			jContentPane.add(jLabel2, null);
			jContentPane.add(getJTextField1(), null);
			jContentPane.add(getJButton2(), null);
			jContentPane.add(getJProgressBar(), null);
			jContentPane.add(jLabel3, null);
			jContentPane.add(getJTextField2(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextField() {
		if (txtUser == null) {
			txtUser = new JTextField();
			txtUser.setBounds(new java.awt.Rectangle(90, 46, 211, 22));
			txtUser.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
		}
		return txtUser;
	}

	/**
	 * This method initializes jPasswordField
	 * 
	 * @return javax.swing.JPasswordField
	 */
	private JPasswordField getJPasswordField() {
		if (txtPass == null) {
			txtPass = new JPasswordField();
			txtPass.setLocation(new java.awt.Point(90, 76));
			txtPass.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			txtPass.setSize(new java.awt.Dimension(211, 22));
		}
		return txtPass;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton() {
		if (btnOk == null) {
			btnOk = new JButton();
			btnOk.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			btnOk.setPreferredSize(new java.awt.Dimension(59, 24));
			btnOk.setLocation(new java.awt.Point(90, 166));
			btnOk.setSize(new java.awt.Dimension(69, 24));
			btnOk.setText("确定");
			btnOk.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					webInfo.put(USER, txtUser.getText());
					webInfo.put(PASS, new String(txtPass.getPassword()));
					webInfo.put(SERVER_URL,
							Worker.getServiceUrl(txtServer.getText()));

					HashMap vars = Settings.getVariables();
					vars.put(SERVER_URL, txtServer.getText());
					vars.put(USER, txtUser.getText());
					Settings.saveVariables();

					File file = new File(txtFile.getText());
					if (!file.exists()) {
						JOptionPane.showMessageDialog(getWindow(), "指定文件不存在："
								+ txtFile.getText());
						return;
					}

					setEnables(false);
					new Thread() {
						public void run() {
							try {
								// 返回false表示用户取消了操作
								if (doUpload(txtFile.getText())) {
									String file = new File(txtFile.getText())
											.getName();
									JOptionPane.showMessageDialog(getWindow(),
											"成功导入" + file + "中所列工艺文档！");

									// 完成后保存服务器地址
									getWindow().setVisible(false);
									getWindow().dispose();
									System.exit(0);
								}
							} catch (Throwable e) {
								e.printStackTrace();
								JOptionPane.showMessageDialog(getWindow(),
										Tools.getErrorMessage(e));
							} finally {
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										setEnables(true);
									}
								});
							}
						}
					}.start();
				}
			});
		}
		return btnOk;
	}

	/**
	 * This method initializes jButton1
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton1() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN,
					12));
			btnCancel.setLocation(new java.awt.Point(210, 166));
			btnCancel.setSize(new java.awt.Dimension(71, 24));
			btnCancel.setText("取消");
			btnCancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getWindow().setVisible(false);
					getWindow().dispose();
					System.exit(-1);
				}
			});
		}
		return btnCancel;
	}

	/**
	 * This method initializes jTextField1
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextField1() {
		if (txtFile == null) {
			txtFile = new JTextField();
			txtFile.setBounds(new java.awt.Rectangle(90, 106, 211, 22));
			txtFile.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			txtFile.setEditable(false);
		}
		return txtFile;
	}

	/**
	 * This method initializes jButton2
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton2() {
		if (btnSelect == null) {
			btnSelect = new JButton();
			btnSelect.setBounds(new java.awt.Rectangle(315, 105, 30, 22));
			btnSelect.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN,
					12));
			btnSelect.setText("...");
			btnSelect.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (fc == null)
						fc = new JFileChooser();
					JFileChooser jfc = (JFileChooser) fc;

					FileFilter filter = new FileFilter() {
						public boolean accept(File f) {
							if (f.isDirectory())
								return true;
							String fn = f.getName().toLowerCase();
							return fn.endsWith(".txt") || fn.endsWith(".csv");
						}

						public String getDescription() {
							return "逗号或制表符分隔历史文档清单文件(*.txt,*.csv)";
						}
					};

					jfc.setFileFilter(filter);
					int returnVal = jfc.showOpenDialog(getWindow());
					if (returnVal != JFileChooser.APPROVE_OPTION)
						return;

					txtFile.setText(jfc.getSelectedFile().getAbsolutePath());
				}
			});
		}
		return btnSelect;
	}

	/**
	 * This method initializes jTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextField2() {
		if (txtServer == null) {
			txtServer = new JTextField();
			txtServer.setBounds(new java.awt.Rectangle(90, 15, 211, 22));
		}
		return txtServer;
	}

	/**
	 * 测试入口
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		doWork();
	}

	/**
	 * 历史数据导入功能入口
	 * 
	 * @param args
	 */
	public static void doWork() {
		new Loader()._doWork();
	}

	private void _doWork() {
		HashMap vars = Settings.getVariables();
		if (vars.get(SERVER_URL) != null)
			txtServer.setText((String) vars.get(SERVER_URL));
		else
			txtServer.setText("http://pds.nriet.com/Windchill");
		txtUser.setText((String) vars.get(USER));

		ScreenUtil.centerWindow(this);

		KeyStroke escKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0,
				false);
		Action escapeAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				btnCancel.doClick();
			}
		};
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				escKeyStroke, "ESCAPE");
		getRootPane().getActionMap().put("ESCAPE", escapeAction);
		getRootPane().setDefaultButton(btnOk);

		setVisible(true);
		new Thread(this).start();
	}

	private Window getWindow() {
		return this;
	}

	private void setProgress(long total, long progress, String text) {
		synchronized (lock) {
			this.total = total;
			this.progress = progress;
			this.text = text;
		}
	}

	public void run() {
		int progress0 = -1;
		int total0 = -1;
		String text0 = null;
		progressBar.setStringPainted(true);
		while (getWindow().isVisible()) {
			try {
				if (progressBar.isVisible()) {
					long _total;
					long _progress;
					String _text;
					synchronized (lock) {
						_total = total;
						_progress = progress;
						_text = text;
					}

					while (_total >= Integer.MAX_VALUE) {
						_total /= 10;
						_progress /= 10;
					}

					if (total0 != _total || progress0 != _progress
							|| text0 != _text /* 是否已重新赋值 */) {
						total0 = (int) _total;
						progress0 = (int) _progress;
						text0 = _text;
						SwingUtilities.invokeLater(new Runnable() {
							private int p, t;
							private String x;

							public void run() {
								if (progressBar.getMaximum() != t)
									progressBar.setMaximum(t);
								progressBar.setValue(p);
								progressBar.setString(x);
							}

							public Runnable get(int p, int t, String x) {
								this.p = p;
								this.t = t;
								this.x = x;
								return this;
							}
						}.get(progress0, total0, text0));
					}
				}

				Thread.sleep(50);
			} catch (InterruptedException e) {
				break;
			}
		}
		System.exit(0);
	}

	private boolean doUpload(String fileName) throws Exception {
		final int INDEX_SERIAL = 0;
		final int INDEX_PART_NUMBER = 1;
		final int INDEX_DOC_NUMBER = 2;
		final int INDEX_DOC_NAME = 3;
		final int INDEX_TYPE = 4;
		final int INDEX_DESCRIPTION = 5;
		final int INDEX_PRIMARY = 6;
		final int INDEX_SECONDARY = 7;
		final int INDEX_MININUM = 7;
		final int ERROR_MAXIMUM = 20;

		File upfile = new File(fileName);
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(upfile), "GB18030"));

		int iLine = 0;
		String line;
		String delimEle = null;
		String delimFile = ";";
		String dir = upfile.getAbsoluteFile().getParent();
		ArrayList errList = new ArrayList();
		ArrayList docList = new ArrayList();
		ArrayList srcList = new ArrayList();

		long totalSize = 0;

		while ((line = br.readLine()) != null && errList.size() < ERROR_MAXIMUM) {
			String[] ele;
			iLine++;
			String kLine = "第" + iLine + "行";

			// 判断分隔符
			if (delimEle == null) {
				String[] line1 = line.split(",");
				String[] line2 = line.split("\t");
				delimEle = line1.length > line2.length ? "," : "\t";
				ele = line1.length > line2.length ? line1 : line2;
			} else
				ele = line.split(delimEle);

			// 去除可能的外围双引号，将连续双引号转换为单个双引号
			for (int i = 0; i < ele.length; i++) {
				if (ele[i] != null && ele[i].startsWith("\"")
						&& ele[i].endsWith("\""))
					ele[i] = ele[i].substring(1, ele[i].length() - 2);
				if (ele[i] != null)
					ele[i] = ele[i].replaceAll("\"\"", "\"");
			}

			ele[0] = ele[0].trim();
			if (ele[0].startsWith("#") || ele[0].startsWith("序"))
				continue;

			if (ele.length < INDEX_MININUM) {
				errList.add(kLine + "列数不足：" + ele.length);
				continue;
			}

			DocInfo di = new DocInfo();
			docList.add(di);
			srcList.add(line);
			di.partNumber = ele[INDEX_PART_NUMBER].trim();
			di.docNumber = ele[INDEX_DOC_NUMBER].trim();
			di.docName = ele[INDEX_DOC_NAME].trim();
			di.type = ele[INDEX_TYPE].trim();
			di.description = ele[INDEX_DESCRIPTION].trim();
			di.docSize = 0;

			String primary = ele[INDEX_PRIMARY].trim();
			String secondary = ele.length > INDEX_SECONDARY ? ele[INDEX_SECONDARY]
					.trim() : null;

			if (primary != null && primary.length() > 0) {
				if (primary.indexOf(File.separator) < 0)
					primary = dir + File.separator + primary;
				di.primary = new File(primary);
				if (!di.primary.exists()) {
					errList.add(kLine + "主文件不存在：" + primary);
					continue;
				}
				di.docSize += di.primary.length();
			}

			if (secondary != null && secondary.length() > 0) {
				String[] files = secondary.split(delimFile);
				ArrayList fileList = new ArrayList();
				for (int i = 0; i < files.length; i++) {
					if (files[i].trim().length() <= 0)
						continue;
					if (files[i].indexOf(File.separator) < 0)
						files[i] = dir + File.separator + files[i];
					File file = new File(files[i]);
					if (!file.exists()) {
						errList.add(kLine + "附件不存在：" + files[i]);
						files = null;
						break;
					}
					fileList.add(file);
					di.docSize += file.length();
				}

				// 有附件不存在时
				if (files == null)
					continue;

				if (fileList.size() > 0)
					di.secondary = (File[]) fileList.toArray(new File[0]);
			}

			totalSize += di.docSize;
		}

		// 报错
		if (errList.size() > 0) {
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < errList.size(); i++)
				buf.append(errList.get(i)).append("\n");
			if (errList.size() == ERROR_MAXIMUM)
				buf.append("......");
			throw new Exception(buf.toString());
		}

		// 逐个导入
		File errFile = null;
		PrintWriter errWriter = null;
		int errCount = 0;
		long progress = 0;
		setProgress(totalSize, progress, "");
		progressBar.setVisible(true);
		for (int i = 0; i < docList.size(); i++) {
			DocInfo di = (DocInfo) docList.get(i);
			setProgress(totalSize, progress, di.docNumber);
			try {
				if (!loadDoc(di))
					return false;
			} catch (Throwable e) {
				if (errWriter == null) {
					errFile = new File(fileName + ".log");
					errWriter = new PrintWriter(new OutputStreamWriter(
							new FileOutputStream(errFile), "GB18030"));
				}

				errCount++;
				String srcLine = (String) srcList.get(i);
				String errMsg = Tools.getErrorMessage(e);
				srcLine += delimEle + escapeNewLine(errMsg);
				errWriter.println(srcLine);
				errWriter.flush();
			}
			progress += di.docSize;
			setProgress(totalSize, progress, di.docNumber);

			// 每100个文档回收内存一次
			// if ((i+1) % 100 == 0)
			// System.gc();
		}

		if (errWriter != null) {
			errWriter.close();
			throw new Exception(docList.size() + "个中的" + errCount
					+ "个文档在导入时发生错误，请查看导入错误记录：" + errFile.getAbsolutePath());
		}

		return true;
	}

	private static String escapeNewLine(String original) {
		int beforeIndex = original.indexOf("\n");
		if (beforeIndex >= 0)
			return replaceAll(original, "\\n", "\n");
		else
			return original;
	}

	private static String replaceAll(String original, String insert,
			String remove) {
		for (; original.indexOf(remove) >= 0; original = replace(original,
				insert, remove))
			;
		return original;
	}

	public static String replace(String original, String insert, String remove) {
		int beforeIndex = original.indexOf(remove);
		if (beforeIndex >= 0)
			return original.substring(0, beforeIndex) + insert
					+ original.substring(beforeIndex + remove.length());
		else
			return original;
	}

	private boolean loadDoc(DocInfo di) throws Exception {
		HashMap task = new HashMap();
		HashMap capp = new HashMap();
		ArrayList fileList = new ArrayList();

		task.put(TYPE, LOAD_DOCUMENT);
		task.put(CAPP, capp);
		capp.put(TYPE, LOAD_DOCUMENT);
		capp.put(PART_NUMBER, di.partNumber);
		capp.put(DOC_NUMBER, di.docNumber);
		capp.put(DOC_NAME, di.docName);
		capp.put(DOC_TYPE, di.type);
		capp.put(DESCRIPTION, di.description);

		if (di.primary != null) {
			HashMap fileInfo = new HashMap();
			fileInfo.put(DataPacker.PATH, di.primary);
			fileList.add(fileInfo);
		}

		if (di.secondary != null && di.secondary.length > 0) {
			if (di.primary == null)
				fileList.add(null);
			for (int i = 0; i < di.secondary.length; i++) {
				File file = di.secondary[i];
				if (file != null) {
					HashMap fileInfo = new HashMap();
					fileInfo.put(DataPacker.PATH, file);
					fileList.add(fileInfo);
				}
			}
		}

		error = null;
		loadResult = null;
		ServerHelper.callServer("正在导入：" + di.docNumber, webInfo, task,
				fileList, new ServerHelper.HTTPCallAdapter() {
					public void onComplete(HashMap result) {
						loadResult = result;
					}

					public void onError(Exception e) {
						error = e;
					}
				});

		if (error != null)
			throw error;

		if (loadResult == null) // 用户取消操作
			return false;

		return true;
	}

	private void setEnables(boolean enabled) {
		txtServer.setEnabled(enabled);
		txtUser.setEnabled(enabled);
		txtPass.setEnabled(enabled);
		btnOk.setEnabled(enabled);
		// btnCancel.setEnabled(enabled);
	}

	private static class DocInfo {
		String partNumber;
		String docNumber;
		String docName;
		String type;
		String description;
		File primary;
		File[] secondary;
		long docSize;
	}
} // @jve:decl-index=0:visual-constraint="10,10"
