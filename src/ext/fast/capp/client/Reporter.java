/**
 * Created on 2006-1-7
 * @author liuld
 */
package ext.fast.capp.client;

import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import ext.fast.util.Tools;

/**
 * Created on 2006-1-7
 * @author liuld 工艺统计支持
 */
public class Reporter extends JFrame implements CAPPConstants {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JButton btnClose = null;
	private JButton btnOk = null;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JComboBox comboProduct = null;
	private JTextField txtDateFrom = null;
	private JTextField txtDateTo = null;
	private JButton jButton = null;
	private JLabel jLabel3 = null;
	private JLabel jLabel4 = null;
	private JLabel jLabel5 = null;
	private JTextField txtServer = null;
	private JTextField txtUser = null;
	private JPasswordField txtPass = null;

	/**
	 * This method initializes
	 * 
	 */
	public Reporter() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		this.setSize(new java.awt.Dimension(407, 348));
		this.setResizable(false);
		this.setTitle("工艺任务统计");
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabel5 = new JLabel();
			jLabel5.setText("用户口令：");
			jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
			jLabel5.setLocation(new java.awt.Point(15, 90));
			jLabel5.setSize(new java.awt.Dimension(76, 22));
			jLabel5.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabel4 = new JLabel();
			jLabel4.setText("用户名：");
			jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
			jLabel4.setLocation(new java.awt.Point(15, 60));
			jLabel4.setSize(new java.awt.Dimension(76, 22));
			jLabel4.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabel3 = new JLabel();
			jLabel3.setText("服务器URL：");
			jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
			jLabel3.setLocation(new java.awt.Point(15, 30));
			jLabel3.setSize(new java.awt.Dimension(76, 22));
			jLabel3.setFont(new Font("Dialog", Font.PLAIN, 12));
			jLabel2 = new JLabel();
			jLabel2.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			jLabel2.setLocation(new java.awt.Point(15, 195));
			jLabel2.setSize(new java.awt.Dimension(76, 22));
			jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
			jLabel2.setText("截至日期：");
			jLabel1 = new JLabel();
			jLabel1.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			jLabel1.setLocation(new java.awt.Point(15, 165));
			jLabel1.setSize(new java.awt.Dimension(76, 22));
			jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
			jLabel1.setText("起始日期：");
			jLabel = new JLabel();
			jLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			jLabel.setLocation(new java.awt.Point(15, 135));
			jLabel.setSize(new java.awt.Dimension(76, 22));
			jLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
			jLabel.setText("统计产品：");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getBtnClose(), null);
			jContentPane.add(jLabel, null);
			jContentPane.add(jLabel1, null);
			jContentPane.add(jLabel2, null);
			jContentPane.add(getComboProduct(), null);
			jContentPane.add(getTxtDateFrom(), null);
			jContentPane.add(getTxtDateTo(), null);
			jContentPane.add(getJButton(), null);
			jContentPane.add(getBtnOk(), null);
			jContentPane.add(jLabel3, null);
			jContentPane.add(jLabel4, null);
			jContentPane.add(jLabel5, null);
			jContentPane.add(getTxtServer(), null);
			jContentPane.add(getTxtUser(), null);
			jContentPane.add(getTxtPass(), null);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getBtnClose() {
		if (btnClose == null) {
			btnClose = new JButton();
			btnClose.setLocation(new java.awt.Point(240, 255));
			btnClose.setText("关闭");
			btnClose.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN,
					12));
			btnClose.setSize(new java.awt.Dimension(76, 24));
			btnClose.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					setVisible(false);
					getWindow().dispose();
					System.exit(0);
				}
			});
		}
		return btnClose;
	}

	/**
	 * This method initializes jButton1
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getBtnOk() {
		if (btnOk == null) {
			btnOk = new JButton();
			btnOk.setLocation(new java.awt.Point(90, 255));
			btnOk.setText("统计");
			btnOk.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
			btnOk.setSize(new java.awt.Dimension(76, 24));
			btnOk.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String product = comboProduct.getSelectedIndex() < 0 ? ""
							: String.valueOf(comboProduct.getSelectedItem());
					String dateFromStr = txtDateFrom.getText();
					String dateToStr = txtDateTo.getText();
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/d");
					Date dateFrom, dateTo;
					try {
						dateFrom = sdf.parse(dateFromStr);
						dateTo = sdf.parse(dateToStr);
					} catch (ParseException pe) {
						pe.printStackTrace();
						JOptionPane.showMessageDialog(getWindow(),
								"请按yyyy/mm/dd格式输入日期！");
						return;
					}

					HashMap webInfo = getWebInfo();
					HashMap task = new HashMap();
					HashMap capp = new HashMap();
					task.put(CAPP, capp);
					task.put(TYPE, REPORT);
					capp.put(TYPE, REPORT);
					capp.put(PRODUCT, product);
					capp.put(DATE_FROM, dateFrom);
					capp.put(DATE_TO, dateTo);
					ArrayList files = new ArrayList();
					HashMap result = ServerHelper.callServer("正在统计......",
							webInfo, task, files, null);
					if (result != null) {
						if (files.size() <= 0) {
							JOptionPane.showMessageDialog(getWindow(),
									"服务器未返回统计结果！", "错误",
									JOptionPane.ERROR_MESSAGE);
							return;
						}

						HashMap fileInfo = (HashMap) files.get(0);
						File file = (File) fileInfo.get(DataPacker.FILE);
						if (!file.exists()) {
							JOptionPane.showMessageDialog(getWindow(),
									"未找到服务器返回的统计结果文件！", "错误",
									JOptionPane.ERROR_MESSAGE);
							return;
						}

						try {
							// 将文件复制为指定名称
							if (fileInfo.get(DataPacker.NAME) != null) {
								File file1 = new File(file.getParent()
										+ File.separator
										+ fileInfo.get(DataPacker.NAME));
								Tools.copyFile(file, file1, false);
								file = file1;
							}

							Runtime.getRuntime().exec(
									"explorer.exe \"" + file.getAbsolutePath()
											+ "\"");
						} catch (Throwable t) {
							Logger.log(t);
							JOptionPane.showMessageDialog(getWindow(),
									Tools.getErrorMessage(t));
						}
					}

					HashMap vars = Settings.getVariables();
					vars.put(SERVER_URL, txtServer.getText());
					vars.put(USER, txtUser.getText());
					Settings.saveVariables();
				}
			});
		}
		return btnOk;
	}

	/**
	 * This method initializes jComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getComboProduct() {
		if (comboProduct == null) {
			comboProduct = new JComboBox();
			comboProduct.setBounds(new java.awt.Rectangle(90, 135, 219, 22));
		}
		return comboProduct;
	}

	/**
	 * This method initializes jTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getTxtDateFrom() {
		if (txtDateFrom == null) {
			txtDateFrom = new JTextField();
			txtDateFrom.setLocation(new java.awt.Point(90, 165));
			txtDateFrom.setSize(new java.awt.Dimension(121, 22));
		}
		return txtDateFrom;
	}

	/**
	 * This method initializes jTextField1
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getTxtDateTo() {
		if (txtDateTo == null) {
			txtDateTo = new JTextField();
			txtDateTo.setLocation(new java.awt.Point(90, 195));
			txtDateTo.setSize(new java.awt.Dimension(121, 22));
		}
		return txtDateTo;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setLocation(new java.awt.Point(315, 135));
			jButton.setText("刷新");
			jButton.setFont(new java.awt.Font("宋体", java.awt.Font.PLAIN, 12));
			jButton.setSize(new java.awt.Dimension(61, 22));
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					HashMap<String, Object> task = new HashMap<String, Object>();
					HashMap<String, String> capp = new HashMap<String, String>();
					HashMap<String, String> webInfo = getWebInfo();
					task.put(CAPP, capp);
					task.put(TYPE, SEARCH_PRODUCT);
					capp.put(TYPE, SEARCH_PRODUCT);
					HashMap<String, Object> result = ServerHelper.callServer("刷新产品列表......", webInfo, task, null, null);
					if (result != null) {
						ArrayList<String> prodList = (ArrayList<String>) result.get(PRODUCT);
						Collections.sort(prodList, new Comparator<String>() {
							public int compare(String o1, String o2) {
								o1 = o1 == null ? "" : o1;
								o2 = o2 == null ? "" : o2;
								try {
									return new String(o1.getBytes("GB18030"))
											.compareTo(new String(o2.getBytes("GB18030")));
								} catch (UnsupportedEncodingException uee) {
									return o1.compareTo(o2);
								}
							}
						});

						comboProduct.removeAllItems();
						comboProduct.addItem("");
						for (Iterator<String> it = prodList.iterator(); it.hasNext();)
							comboProduct.addItem(it.next());
						if (comboProduct.getItemCount() > 1)
							comboProduct.setSelectedIndex(1);

						HashMap<String, Object> vars = Settings.getVariables();
						vars.put("REPORT_PRODUCT_LIST", prodList);
						vars.put(SERVER_URL, txtServer.getText());
						vars.put(USER, txtUser.getText());
						Settings.saveVariables();
					}
				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes jTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getTxtServer() {
		if (txtServer == null) {
			txtServer = new JTextField();
			txtServer.setBounds(new java.awt.Rectangle(90, 30, 286, 22));
		}
		return txtServer;
	}

	/**
	 * This method initializes jTextField1
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getTxtUser() {
		if (txtUser == null) {
			txtUser = new JTextField();
			txtUser.setBounds(new java.awt.Rectangle(90, 60, 121, 22));
		}
		return txtUser;
	}

	/**
	 * This method initializes jPasswordField
	 * 
	 * @return javax.swing.JPasswordField
	 */
	private JPasswordField getTxtPass() {
		if (txtPass == null) {
			txtPass = new JPasswordField();
			txtPass.setLocation(new java.awt.Point(90, 90));
			txtPass.setSize(new java.awt.Dimension(121, 22));
		}
		return txtPass;
	}

	public static void main(String[] args) {
		doWork();
	}

	public static void doWork() {
		new Reporter()._doWork();
	}

	private void _doWork() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		Calendar ca = Calendar.getInstance();
		Date dateTo = ca.getTime();
		ca.set(Calendar.DAY_OF_MONTH, 1);
		Date dateFrom = ca.getTime();

		txtDateFrom.setText(sdf.format(dateFrom));
		txtDateTo.setText(sdf.format(dateTo));

		HashMap<String, Object> vars = Settings.getVariables();
		ArrayList<String> prodList = (ArrayList<String>) vars.get("REPORT_PRODUCT_LIST");
		comboProduct.addItem("");
		if (prodList != null) {
			for (Iterator<String> it = prodList.iterator(); it.hasNext();)
				comboProduct.addItem(it.next());
		}

		txtServer.setText((String) vars.get(SERVER_URL));
		txtUser.setText((String) vars.get(USER));

		ScreenUtil.centerWindow(this);

		KeyStroke escKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0,
				false);
		Action escapeAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				btnClose.doClick();
			}
		};
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
				escKeyStroke, "ESCAPE");
		getRootPane().getActionMap().put("ESCAPE", escapeAction);
		getRootPane().setDefaultButton(btnOk);

		setVisible(true);
	}

	private Window getWindow() {
		return this;
	}

	private HashMap<String, String> getWebInfo() {
		HashMap<String, String> webInfo = new HashMap<String, String>();
		webInfo.put(SERVER_URL, Worker.getServiceUrl(txtServer.getText()));
		webInfo.put(USER, txtUser.getText());
		webInfo.put(PASS, new String(txtPass.getPassword()));
		return webInfo;
	}
} // @jve:decl-index=0:visual-constraint="10,10"
