/**
 * Created on 2004-10-22
 * @author lld
 */
package ext.fast.capp.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import ext.fast.util.Tools;

public class FrameSearchPart extends JDialog implements ActionListener,
		CAPPConstants, ServerHelper.HTTPCallback {

	private static final long serialVersionUID = 1L;
	private static final String desc = "说明: 用%可以通配任意子字符串";
	private FrameSearchPart frame = this;
	private Object lock = new Object();

	private HashMap<String, Object> task = null;
	private HashMap<String, String> webInfo = null;
	private ArrayList<HashMap<String, Object>> selectedParts = null;
	private ArrayList<HashMap<String,Object>> searchedParts = null;
	private DataModel dataModel = null;
	private String keyOid = null;
	private HashMap bomResult = null;

	// --------------------------------------------------------------------------
	private JPanel jContentPane = null;
	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JTextField txtName = null;
	private JTextField txtNumber = null;
	private JButton btnSearch = null;
	private JScrollPane jScrollPane = null;
	private JLabel labSelected = null;
	private JTextField txtSelected = null;
	private JButton btnSelect = null;
	private JLabel labDesc = null;
	private JButton btnCancel = null;
	private JTable partTable = null;
	private JLabel labDownloadPath = null;
	private JTextField txtPath = null;
	private JButton btnSetPath = null;
	private JLabel jLabel5 = null;
	private JComboBox comboContainer = null;

	private JButton btnSearchProducts = null;
	private JButton btnAllProducts = null;
	private JProgressBar progressBar = null;
	private JLabel labStatus = null;
	private JButton btnGetChildren = null;

	private FrameSearchPart() {
		super();
		initialize();
	}

	/**
	 * 搜索零部件
	 * @param task Windchill用户名
	 * @param webInfo webInfo参数HashMap, 含USER, PASS, SERVER_URL
	 * @return *
	 * @throws Exception
	 */
	public static HashMap<String, Object> searchPart(
			HashMap<String, Object> task, HashMap<String, String> webInfo)
			throws Exception {
		return new FrameSearchPart()._searchPart(task, webInfo);
	}

	private HashMap<String, Object> _searchPart(HashMap<String, Object> task,
			HashMap<String, String> webInfo) throws Exception {
		// 获取功能调用信息
		HashMap<String, Object> capp = (HashMap<String, Object>) task.get(CAPP);
		if (capp == null)
			throw new Exception("内部错误，未找到任务描述参数！");

		this.webInfo = webInfo;
		this.task = task;

//		String partView = (String) capp.get(VIEW);
		String windowCaption = (String) capp.get(WINDOW_CAPTION);
		String multipleSelectableStr = (String) capp.get(MULTIPLE_SELECTABLE);
		boolean multipleSelectable = false;
		String note = desc;

		// 提取相关调用参数
//		if (partView != null && !partView.trim().equals(""))
//			note += "，指定零部件视图" + partView;
		if (multipleSelectableStr != null
				&& multipleSelectableStr.equalsIgnoreCase(TRUE))
			multipleSelectable = true;
		if (windowCaption == null || windowCaption.trim().equals(""))
			windowCaption = "提取零部件信息";

		ScreenUtil.centerWindow(this);
		setTitle(windowCaption);

		// 隐藏不需要的组件(以前版本遗留组件)
		labDownloadPath.setVisible(false);
		txtPath.setVisible(false);
		btnSetPath.setVisible(false);
		progressBar.setVisible(false);

		// 初始化各组件
		txtSelected.setEditable(false);
		labDesc.setText(note);
		labStatus.setText("请指定搜索条件...");
		partTable.setSelectionMode(
			multipleSelectable ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION	: ListSelectionModel.SINGLE_SELECTION);

		// 读取上次查询参数
		try {
			HashMap vars = Settings.getVariables();
			txtName.setText((String) vars.get(PART + NAME));
			txtNumber.setText((String) vars.get(PART + NUMBER));
			ArrayList<String> list = (ArrayList<String>) vars.get(PART + PRODUCT + PRODUCT);
			Integer iProd = (Integer) vars.get(PART + PRODUCT);

			if (list != null && list.size() > 0) {
				comboContainer.removeAllItems();
				for (int i = 0; i < list.size(); i++)
					comboContainer.addItem(list.get(i));
				comboContainer.addItem("");
				if (iProd != null && iProd.intValue() >= 0
						&& iProd.intValue() <= list.size())
					comboContainer.setSelectedIndex(iProd.intValue());
			}

			Rectangle r = (Rectangle) vars.get(PART + "BOUNDS");
			setBounds(r);

			HashMap<String, Object> widths = (HashMap) vars.get(PART + "COLUMNWIDTHS");
			for (int i = 0; i < partTable.getColumnCount(); i++) {
				Integer w = (Integer) widths.get(partTable.getColumnName(i));
				if (w != null && w.intValue() > 0) {
					TableColumnModel cm = partTable.getColumnModel();
					cm.getColumn(i).setPreferredWidth(w.intValue());
				}
			}
		} catch (Exception e) {
			Logger.log(e);
		}

		// 显示窗口
		setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
		setVisible(true);
		setAlwaysOnTop(true);
		dispose();

		HashMap<String, Object> vars = Settings.getVariables();
		vars.put(PART + "BOUNDS", getBounds());
		HashMap<String, Integer> widths = new HashMap<String, Integer>();
		for (int i = 0; i < partTable.getColumnCount(); i++) {
			TableColumnModel cm = partTable.getColumnModel();
			widths.put(partTable.getColumnName(i), new Integer(cm.getColumn(i).getWidth()));
		}
		vars.put(PART + "COLUMNWIDTHS", widths);
		Settings.saveVariables();

		// 作为用户选择取消表记
		if (selectedParts == null || selectedParts.size() <= 0)
			return null;

		task.put(PART, selectedParts);
		if (bomResult != null)
			task.put(BOMITEM, bomResult.get(BOMITEM));

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

	private void threadStart(String status) throws Exception {
		SwingUtilities.invokeAndWait(new Runnable() {
			String status;

			public void run() {
				frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

				labStatus.setText(status);
				txtSelected.setText("");
				partTable.clearSelection();
				progressBar.setVisible(true);

				txtName.setEnabled(false);
				txtNumber.setEnabled(false);
				comboContainer.setEnabled(false);
				btnSearchProducts.setEnabled(false);
				btnAllProducts.setEnabled(false);
				btnSelect.setEnabled(false);
				btnSearch.setEnabled(false);
				btnGetChildren.setEnabled(false);
				btnSetPath.setEnabled(false);
				partTable.setEnabled(false);
			}

			public Runnable get(String status) {
				this.status = status;
				return this;
			}
		}.get(status));
	}

	private void threadEnd() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setVisible(false);

				txtName.setEnabled(true);
				txtNumber.setEnabled(true);
				comboContainer.setEnabled(true);
				btnSearchProducts.setEnabled(true);
				btnAllProducts.setEnabled(true);
				btnSelect.setEnabled(true);
				btnSearch.setEnabled(true);
				btnGetChildren.setEnabled(true);
				btnSetPath.setEnabled(true);
				partTable.setEnabled(true);

				frame.setCursor(Cursor.getDefaultCursor());
			}
		});
	}

	/**
	 * 产品搜索线程 Created on 2005-7-26
	 */
	class SearchProductThread extends Thread {
		String _prodName;
		ArrayList<?> _products;
		Exception _exception;

		public SearchProductThread(String prodName) {
			_prodName = prodName;
		}

		public void run() {
			try {
				threadStart("正在搜索产品名称......");

				// --------------------------------------------------------------
				HashMap<String, Object> task = new HashMap<String, Object>();
				HashMap<String, Object> capp = new HashMap<String, Object>();

				task.put(CAPP, capp);
				task.put(TYPE, SEARCH_PRODUCT);
				capp.put(PRODUCT_NAME, _prodName);

				String user = (String) webInfo.get(USER);
				String pass = (String) webInfo.get(PASS);
				String surl = (String) webInfo.get(SERVER_URL);
				HashMap<String, Object> result = ServerHelper.callServer(user, pass, surl, task, null, frame);
				_products = (ArrayList<?>) result.get(PRODUCT);

				// --------------------------------------------------------------
				if (_products == null)
					throw new Exception("查询产品过程中发现未知错误！");

				HashMap<String, Object> vars = Settings.getVariables();
				vars.put(PART + PRODUCT + PRODUCT, _products);
				vars.put(PART + PRODUCT, new Integer(0));
				Settings.saveVariables();

				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						comboContainer.removeAllItems();
						for (int i = 0; i < _products.size(); i++)
							comboContainer.addItem(_products.get(i));
						comboContainer.addItem(""); // 加一个空行以便选空
						comboContainer.setSelectedIndex(0);

						labStatus.setText("共找到" + _products.size() + "个产品.");
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
				_exception = e;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						Logger.log(_exception);
						String msg = "搜索产品过程中出错: " + Tools.getErrorMessage(_exception);
						labStatus.setText(msg);
						JOptionPane.showMessageDialog(frame, msg);
					}
				});
			} finally {
				threadEnd();
			}
		}
	}

	/**
	 * 零部件搜索线程 Created on 2005-7-27
	 */
	class SearchPartThread extends Thread {
		String _name;
		String _number;
		String _product;
		String _parentOid;

		public SearchPartThread(String name, String number, String product,
				String parentOid) {
			_name = name;
			_number = number;
			_product = product;
			_parentOid = parentOid;
			keyOid = null;
		}

		public void run() {
			try {
				threadStart("正在搜索零部件...");

				// --------------------------------------------------------------
				HashMap<String, Object> task1 = (HashMap<String, Object>) DataPacker.clone(task);
				HashMap<String, Object> capp = (HashMap<String, Object>) task1.get(CAPP);

				// VIEW, IBA_LIST已包含在task内
				capp.put(PART_NAME, _name);
				capp.put(PART_NUMBER, _number);
				capp.put(PRODUCT_NAME, _product);
				capp.put(PARENT_OID, _parentOid);

				String user = (String) webInfo.get(USER);
				String pass = (String) webInfo.get(PASS);
				String surl = (String) webInfo.get(SERVER_URL);
				HashMap<String, Object> result = ServerHelper.callServer(user, pass, surl, task1, null, frame);

				// --------------------------------------------------------------
				if (result == null || result.get(PART) == null)
					throw new Exception("查询零部件过程中发现未知异常！");

				keyOid = _parentOid;
				ArrayList parts = (ArrayList) result.get(PART);
				SwingUtilities.invokeAndWait(new Runnable() {
					ArrayList parts;

					public void run() {
						synchronized (lock) {
							searchedParts = parts;
							labStatus.setText("共找到 " + parts.size() + " 个符合条件的零部件.");
						}

						partTable.updateUI();
					}

					public Runnable get(ArrayList parts) {
						this.parts = parts;
						return this;
					}
				}.get(parts));
			} catch (Exception e) {
				SwingUtilities.invokeLater(new Runnable() {
					Exception exception;

					public void run() {
						Logger.log(exception);
						String msg = "搜索零部件过程中出错: "
								+ Tools.getErrorMessage(exception);
						labStatus.setText(msg);
						JOptionPane.showMessageDialog(frame, msg);
					}

					public Runnable get(Exception exception) {
						this.exception = exception;
						return this;
					}
				}.get(e));
			} finally {
				threadEnd();
			}
		}
	}

	/**
	 * 零部件表模型 Created on 2005-7-26
	 * @author liuld
	 */
	class DataModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		final String[] tableColNames = { "产品", "名称", "编号", "版本", "视图", "类型",
				"检出状态", "生命周期状态", "最后修改" };
		final String[] tableColKeys = { PRODUCT_NAME, NAME, NUMBER, ITERATION,
				VIEW, TYPE, CHECKOUT_STATUS, LIFECYCLE_STATUS, LAST_MODIFIED, };

		public int getColumnCount() {
			return tableColNames.length;
		}

		public String getColumnName(int col) {
			return tableColNames[col];
		}

		public int getRowCount() {
			synchronized (lock) {
				return searchedParts == null ? 0 : searchedParts.size();
			}
		}

		public Object getValueAt(int row, int col) {
			synchronized (lock) {
				if (searchedParts != null && row < searchedParts.size()
						&& col < tableColNames.length) {
					HashMap part = (HashMap) searchedParts.get(row);
					return part.get(tableColKeys[col]);
				} else
					return null;
			}
		}
	}

	/**
	 * This method initializes this
	 * @return void
	 */
	private void initialize() {
		this.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		this.setResizable(true);
		this.setSize(720, 480);
		this.setContentPane(getJContentPane());
		this.setTitle("获取零件信息");
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				synchronized (lock) {
					searchedParts = null;
				}
			}
		});
	}

	/**
	 * This method initializes jContentPane
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints24 = new GridBagConstraints();
			gridBagConstraints24.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints24.gridy = 2;
			gridBagConstraints24.weightx = 1.0;
			gridBagConstraints24.gridwidth = 3;
			gridBagConstraints24.insets = new Insets(4, 0, 0, 0);
			gridBagConstraints24.gridx = 2;
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			gridBagConstraints16.gridx = 4;
			gridBagConstraints16.insets = new Insets(4, 4, 0, 2);
			gridBagConstraints16.gridy = 6;
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 1;
			gridBagConstraints12.gridwidth = 2;
			gridBagConstraints12.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints12.anchor = GridBagConstraints.WEST;
			gridBagConstraints12.insets = new Insets(0, 0, 0, 0);
			gridBagConstraints12.gridy = 9;
			labStatus = new JLabel();
			labStatus.setText("　");
			labStatus.setFont(new Font("DialogInput", Font.PLAIN, 12));
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 3;
			gridBagConstraints.gridwidth = 5;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.insets = new Insets(0, 0, 0, 0);
			gridBagConstraints.gridy = 9;
			GridBagConstraints gridBagConstraints23 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			jLabel5 = new JLabel();
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
			labDownloadPath = new JLabel();
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			labDesc = new JLabel();
			jLabel1 = new JLabel();
			jLabel = new JLabel();
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new java.awt.GridBagLayout());
			jContentPane.setFont(new Font("DialogInput", Font.PLAIN, 12));
			gridBagConstraints1.gridx = 1;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.insets = new Insets(15, 4, 0, 4);
			jLabel.setText("零件名称:");
			jLabel.setFont(new Font("DialogInput", Font.PLAIN, 12));
			gridBagConstraints2.gridx = 1;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.insets = new Insets(4, 4, 0, 4);
			jLabel1.setText("零件编号:");
			jLabel1.setFont(new Font("DialogInput", Font.PLAIN, 12));
			gridBagConstraints3.gridx = 2;
			gridBagConstraints3.gridy = 0;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints3.insets = new Insets(15, 0, 0, 0);
			gridBagConstraints3.gridwidth = 3;
			gridBagConstraints4.gridx = 2;
			gridBagConstraints4.gridy = 1;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints4.insets = new Insets(4, 0, 0, 0);
			gridBagConstraints4.gridwidth = 3;
			gridBagConstraints5.gridx = 5;
			gridBagConstraints5.fill = GridBagConstraints.NONE;
			gridBagConstraints5.gridy = 0;
			gridBagConstraints5.gridheight = 4;
			gridBagConstraints5.gridwidth = 2;
			gridBagConstraints5.insets = new Insets(15, 0, 0, 0);
			gridBagConstraints6.gridx = 1;
			gridBagConstraints6.gridy = 5;
			gridBagConstraints6.weightx = 1.0;
			gridBagConstraints6.weighty = 1.0;
			gridBagConstraints6.fill = GridBagConstraints.BOTH;
			gridBagConstraints6.gridwidth = 7;
			gridBagConstraints6.insets = new Insets(0, 4, 0, 4);
			gridBagConstraints7.gridx = 1;
			gridBagConstraints7.gridy = 6;
			gridBagConstraints7.insets = new Insets(4, 4, 0, 4);
			gridBagConstraints8.gridx = 2;
			gridBagConstraints8.insets = new Insets(4, 0, 0, 0);
			gridBagConstraints8.gridy = 6;
			gridBagConstraints8.weightx = 1.0;
			gridBagConstraints8.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints8.gridwidth = 2;
			gridBagConstraints9.gridx = 5;
			gridBagConstraints9.anchor = GridBagConstraints.CENTER;
			gridBagConstraints9.gridy = 6;
			gridBagConstraints9.insets = new Insets(4, 4, 0, 2);
			gridBagConstraints10.gridx = 2;
			gridBagConstraints10.gridwidth = 5;
			gridBagConstraints10.gridy = 4;
			gridBagConstraints10.anchor = GridBagConstraints.WEST;
			gridBagConstraints10.insets = new Insets(4, 0, 4, 0);
			labDesc.setText("说明: 用*可以通配任意子字符串");
			labDesc.setFont(new Font("DialogInput", Font.PLAIN, 12));
			gridBagConstraints11.gridx = 6;
			gridBagConstraints11.gridy = 6;
			gridBagConstraints11.insets = new Insets(4, 2, 0, 4);
			gridBagConstraints13.gridx = 1;
			gridBagConstraints13.gridy = 7;
			gridBagConstraints13.insets = new Insets(4, 4, 4, 4);
			labDownloadPath.setText("下载路径:");
			labDownloadPath.setFont(new Font("DialogInput", Font.PLAIN, 12));
			gridBagConstraints21.gridx = 2;
			gridBagConstraints21.insets = new Insets(4, 0, 4, 0);
			gridBagConstraints21.gridy = 7;
			gridBagConstraints21.weightx = 1.0;
			gridBagConstraints21.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints21.gridwidth = 2;
			gridBagConstraints31.gridx = 4;
			gridBagConstraints31.anchor = GridBagConstraints.NORTH;
			gridBagConstraints31.gridy = 7;
			gridBagConstraints31.gridwidth = 3;
			gridBagConstraints31.insets = new Insets(4, 4, 4, 4);
			gridBagConstraints31.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints14.gridx = 1;
			gridBagConstraints14.gridy = 3;
			gridBagConstraints14.insets = new Insets(4, 4, 0, 4);
			jLabel5.setText("产品名称:");
			jLabel5.setFont(new Font("DialogInput", Font.PLAIN, 12));
			gridBagConstraints22.gridx = 2;
			gridBagConstraints22.gridy = 3;
			gridBagConstraints22.weightx = 1.0;
			gridBagConstraints22.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints22.insets = new Insets(4, 0, 0, 0);
			gridBagConstraints15.gridx = 3;
			gridBagConstraints15.gridy = 3;
			gridBagConstraints15.insets = new Insets(4, 4, 0, 2);
			gridBagConstraints23.gridx = 4;
			gridBagConstraints23.gridy = 3;
			gridBagConstraints23.insets = new Insets(4, 2, 0, 0);
			jContentPane.add(labDesc, gridBagConstraints10);
			jContentPane.add(jLabel, gridBagConstraints1);
			jContentPane.add(jLabel1, gridBagConstraints2);
			jContentPane.add(jLabel5, gridBagConstraints14);
			jContentPane.add(getLabSelected(), gridBagConstraints7);
			jContentPane.add(getTxtName(), gridBagConstraints3);
			jContentPane.add(getTxtNumber(), gridBagConstraints4);
			jContentPane.add(getComboContainer(), gridBagConstraints22);
			jContentPane.add(getTxtSelected(), gridBagConstraints8);
			jContentPane.add(getBtnSearch(), gridBagConstraints5);
			jContentPane.add(getJScrollPane(), gridBagConstraints6);
			jContentPane.add(getBtnSelect(), gridBagConstraints9);
			jContentPane.add(getBtnCancel(), gridBagConstraints11);
			jContentPane.add(labDownloadPath, gridBagConstraints13);
			jContentPane.add(getTxtPath(), gridBagConstraints21);
			jContentPane.add(getBtnSetPath(), gridBagConstraints31);
			jContentPane.add(getBtnSearchProducts(), gridBagConstraints15);
			jContentPane.add(getBtnAllProducts(), gridBagConstraints23);
			jContentPane.add(getProgressBar(), gridBagConstraints);
			jContentPane.add(labStatus, gridBagConstraints12);
			jContentPane.add(getBtnGetChildren(), gridBagConstraints16);
		}
		return jContentPane;
	}

	/**
	 * This method initializes txtContainer
	 * @return javax.swing.JTextField
	 */
	private JTextField getTxtName() {
		if (txtName == null) {
			txtName = new JTextField();
			txtName.setFont(new Font("DialogInput", Font.PLAIN, 12));
			txtName.setPreferredSize(new Dimension(6, 25));
		}
		return txtName;
	}

	/**
	 * This method initializes jTextField1
	 * @return javax.swing.JTextField
	 */
	private JTextField getTxtNumber() {
		if (txtNumber == null) {
			txtNumber = new JTextField();
			txtNumber.setFont(new Font("DialogInput", Font.PLAIN, 12));
			txtNumber.setPreferredSize(new Dimension(6, 25));
		}
		return txtNumber;
	}

	/**
	 * This method initializes btnSearchProducts
	 * @return javax.swing.JButton
	 */
	private JButton getBtnSearch() {
		if (btnSearch == null) {
			btnSearch = new JButton();
			btnSearch.setFont(new Font("DialogInput", Font.PLAIN, 12));
			btnSearch.setText("搜索零部件");
			btnSearch.setActionCommand("搜索(&Q)");
			getRootPane().setDefaultButton(btnSearch);
			btnSearch.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					btnSearch.setEnabled(false);

					HashMap<String, Object> vars = Settings.getVariables();
					vars.put(PART + NAME, txtName.getText());
					vars.put(PART + NUMBER, txtNumber.getText());
					vars.put(PART + PRODUCT, new Integer(comboContainer.getSelectedIndex()));
					Settings.saveVariables();

					new SearchPartThread(txtName.getText(),
							txtNumber.getText(), (String) comboContainer.getSelectedItem(), null).start();
				}
			});
		}
		return btnSearch;
	}

	/**
	 * This method initializes jButton
	 * @return javax.swing.JButton
	 */
	private JButton getBtnGetChildren() {
		if (btnGetChildren == null) {
			btnGetChildren = new JButton();
			btnGetChildren.setFont(new Font("Dialog", Font.PLAIN, 12));
			btnGetChildren.setToolTipText("列出子零部件");
			btnGetChildren.setActionCommand("子部件");
			btnGetChildren.setText("子零部件");
			btnGetChildren.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int[] rows = partTable.getSelectedRows();
					if (rows.length != 1) {
						JOptionPane.showMessageDialog(frame,
								"请（只）选择一个零部件!", "提示",
								JOptionPane.INFORMATION_MESSAGE);
						return;
					}
	
					HashMap partInfo = (HashMap) searchedParts.get(rows[0]);
					String oid = (String) partInfo.get(OID);
					btnGetChildren.setEnabled(false);
					new SearchPartThread(txtName.getText(), txtNumber
							.getText(), (String) comboContainer
							.getSelectedItem(), oid).start();
				}
			});
		}
		return btnGetChildren;
	}

	/**
	 * This method initializes jScrollPane
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getPartTable());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jLabel2
	 * @return javax.swing.JLabel
	 */
	private JLabel getLabSelected() {
		if (labSelected == null) {
			labSelected = new JLabel();
			labSelected.setText("选中零件:");
			labSelected.setFont(new Font("DialogInput", Font.PLAIN, 12));
		}
		return labSelected;
	}

	/**
	 * This method initializes jTextField2
	 * @return javax.swing.JTextField
	 */
	private JTextField getTxtSelected() {
		if (txtSelected == null) {
			txtSelected = new JTextField();
			txtSelected.setFont(new Font("DialogInput", Font.PLAIN, 12));
		}
		return txtSelected;
	}

	/**
	 * This method initializes btnAllProducts
	 * @return javax.swing.JButton
	 */
	private JButton getBtnSelect() {
		if (btnSelect == null) {
			btnSelect = new JButton();
			btnSelect.setFont(new Font("Dialog", Font.PLAIN, 12));
			btnSelect.setText("确定");
			btnSelect.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					synchronized (lock) {
						selectedParts = new ArrayList<HashMap<String, Object>>();
						int[] rows = partTable.getSelectedRows();

						if (rows.length <= 0) {
							JOptionPane.showMessageDialog(frame, "您未选择任何零部件!",
									"提示", JOptionPane.INFORMATION_MESSAGE);
							return;
						}

						frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						HashMap capp = (HashMap) task.get(CAPP);

						// 如果需要获取BOM清单，调用服务获取
						String getBom = (String) capp.get(GET_BOM_LIST);
						boolean getBomList = getBom != null
								&& getBom.equalsIgnoreCase("true");

						String ibaCSV = (String) capp.get(IBA_LIST);
						Object[] ibas = null;
						if (ibaCSV != null && !ibaCSV.equals("")) {
							if (!ibaCSV.equals("*"))
								ibas = ibaCSV.split(",");
						}
						for (int i = 0; i < rows.length; i++) {
							HashMap pi = (HashMap) searchedParts.get(rows[i]);
							HashMap<String, Object> pp = new LinkedHashMap<String, Object>();
							selectedParts.add(pp);

							pp.put(NAME, pi.get(NAME));
							pp.put(NUMBER, pi.get(NUMBER));
							pp.put(VERSION, pi.get(VERSION));
							pp.put(PRODUCT_NAME, pi.get(PRODUCT_NAME));
							pp.put(PRODUCT_NUMBER, pi.get(PRODUCT_NUMBER));
							pp.put(PARENT_NUMBER, pi.get(PARENT_NUMBER));
							pp.put(VIEW, pi.get(VIEW));

							Object[] _ibas = ibas;
							if (ibas == null) {
								ArrayList ibaNameList = (ArrayList) pi
										.get(TYPE_IBA_LIST);
								if (ibaNameList != null)
									_ibas = ibaNameList.toArray();
							}
							for (int j = 0; _ibas != null && j < _ibas.length; j++) {
								if (_ibas[j] == null)
									continue;
								String ibaName = String.valueOf(_ibas[j])
										.trim();
								if (!ibaName.equals(""))
									pp.put(ibaName, pi.get(ibaName));
							}

							if (getBomList) {
								HashMap<String, Object> bomTask = new HashMap<String, Object>();
								HashMap<String, Object> bomCAPP = new HashMap<String, Object>();
								bomTask.put(CAPP, bomCAPP);
								bomTask.put(TYPE, QUICK_GET_PART);
								bomCAPP.put(TYPE, QUICK_GET_PART);
								bomCAPP.put(NUMBER, pi.get(NUMBER));
								bomCAPP.put(VERSION, pi.get(VERSION));
								bomCAPP.put(GET_BOM_LIST, "true");
								bomCAPP.put(IBA_LIST, ibaCSV);
								bomResult = ServerHelper.callServer(
										"正在获取BOM清单...", webInfo, bomTask, null,
										null);
								break;
							}
						}
					}
					setVisible(false);
				}
			});
		}
		return btnSelect;
	}

	/**
	 * This method initializes jButton2
	 * @return javax.swing.JButton
	 */
	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton();
			btnCancel.setText("取消");
			btnCancel.setFont(new Font("DialogInput", Font.PLAIN, 12));
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					synchronized (lock) {
						selectedParts = null;
					}
					frame.setVisible(false);
				}
			});
		}
		return btnCancel;
	}

	/**
	 * This method initializes partsTable
	 * @return javax.swing.JTable
	 */
	private JTable getPartTable() {
		if (partTable == null) {
			dataModel = new DataModel();
			partTable = new JTable(dataModel);
			partTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			partTable.setFont(new Font("DialogInput", Font.PLAIN, 12));
			partTable.setDefaultRenderer(Object.class, new MyDefaultCellRenderer());
			JTableHeader tableHeader = partTable.getTableHeader();
			tableHeader.setFont(new Font("DialogInput", Font.PLAIN, 12));

			partTable.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					synchronized (lock) {
						int count = partTable.getSelectedRowCount();
						if (count != 1) {
							txtSelected.setText(count + "");
						} else {
							int row = partTable.getSelectedRow();
							if (row >= 0) {
								HashMap part = (HashMap) searchedParts.get(row);
								txtSelected.setText(part.get(PRODUCT_NAME)
										+ ": " + part.get(NUMBER) + " - "
										+ part.get(NAME) + " "
										+ part.get(ITERATION) + " "
										+ part.get(CHECKOUT_STATUS) + " "
										+ part.get(LIFECYCLE_STATUS));
							}
						}
					}
				}
			});
		}

		return partTable;
	}

	/**
	 * This method initializes txtContainer
	 * @return javax.swing.JTextField
	 */
	private JTextField getTxtPath() {
		if (txtPath == null) {
			txtPath = new JTextField();
			txtPath.setFont(new Font("DialogInput", Font.PLAIN, 12));
			txtPath.setEditable(false);
			txtPath.setName("txtPath");
		}
		return txtPath;
	}

	/**
	 * This method initializes btnSearchProducts
	 * @return javax.swing.JButton
	 */
	private JButton getBtnSetPath() {
		if (btnSetPath == null) {
			btnSetPath = new JButton();
			btnSetPath.setText("设定路径...");
			btnSetPath.setFont(new Font("DialogInput", Font.PLAIN, 12));
			btnSetPath.setName("btnSetPath");
			btnSetPath.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser fc = new JFileChooser();
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int fcVal = fc.showOpenDialog(frame);
					if (fcVal == JFileChooser.APPROVE_OPTION) {
						String resultPath = fc.getSelectedFile().getPath();
						if (!resultPath.endsWith(File.separator))
							resultPath += File.separator;
						txtPath.setText(resultPath);
						Settings.setSavePath(resultPath);
					}
				}
			});
		}
		return btnSetPath;
	}

	/**
	 * This method initializes txtContainer
	 * @return javax.swing.JTextField
	 */
	private JComboBox getComboContainer() {
		if (comboContainer == null) {
			comboContainer = new JComboBox();
			comboContainer.setFont(new Font("DialogInput", Font.PLAIN, 12));
			comboContainer.setEditable(true);
			comboContainer.setPreferredSize(new Dimension(125, 25));
			comboContainer.setMaximumRowCount(38);
		}
		return comboContainer;
	}

	/**
	 * This method initializes btnSearchProducts
	 * @return javax.swing.JButton
	 */
	private JButton getBtnSearchProducts() {
		if (btnSearchProducts == null) {
			btnSearchProducts = new JButton();
			btnSearchProducts.setText("查找产品");
			btnSearchProducts.setFont(new Font("DialogInput", Font.PLAIN, 12));
			btnSearchProducts
					.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							btnSearchProducts.setEnabled(false);
							new SearchProductThread((String) comboContainer
									.getSelectedItem()).start();
						}
					});
		}
		return btnSearchProducts;
	}

	/**
	 * This method initializes btnAllProducts
	 * @return javax.swing.JButton
	 */
	private JButton getBtnAllProducts() {
		if (btnAllProducts == null) {
			btnAllProducts = new JButton();
			btnAllProducts.setText("全部产品");
			btnAllProducts.setFont(new Font("DialogInput", Font.PLAIN, 12));
			btnAllProducts.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					btnAllProducts.setEnabled(false);
					new SearchProductThread(null).start();
				}
			});
		}
		return btnAllProducts;
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == btnSearch) {

		}
	}

	/**
	 * This method initializes progressBar
	 * @return javax.swing.JProgressBar
	 */
	private JProgressBar getProgressBar() {
		if (progressBar == null) {
			progressBar = new JProgressBar();
		}
		return progressBar;
	}

	private class MyDefaultCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			DefaultTableCellRenderer c = (DefaultTableCellRenderer) super
					.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			Color fg = isSelected ? table.getSelectionForeground() : table.getForeground();
			Color bg = isSelected ? table.getSelectionBackground() : table.getBackground();
			c.setBackground(bg);
			c.setForeground(fg);

			if (keyOid == null || searchedParts == null || row >= searchedParts.size())
				return c;

			HashMap<String, Object> pi = searchedParts.get(row);
			String thisOid = pi == null ? null : (String) pi.get(OID);
			// Debug.P("keyOid=", keyOid, ", thisOid=", thisOid);
			if (!keyOid.equals(thisOid))
				return c;

			c.setBackground(isSelected ? Color.GREEN : Color.YELLOW);
			c.setForeground(Color.BLACK);

			return c;
		}
	}

	public static void main(String[] args) throws Exception {
		HashMap<String, Object> task = new HashMap<String, Object>();
		HashMap<String, Object> capp = new LinkedHashMap<String, Object>();
		task.put(CAPP, capp);
		task.put(TYPE, SEARCH_PART);
		capp.put(SERVER_URL, "http://pdm.fastgroup.cn/Windchill/");
		capp.put(WINDOW_CAPTION, "查询并获取零部件信息");
//		capp.put(VIEW, "Manufacturing");
		capp.put(VIEW, "Design");
		capp.put(IBA_LIST, "htPartMaterial,htPartReplaceable,htPartMaterialGroup");
		capp.put(IBA_LIST, "");
		capp.put(IBA_LIST, "*");

		Worker worker = new Worker("wcadmin", "winadmin");
		HashMap<String, Object> result = worker.performTask(task);
		worker.saveResult(System.out, result);
		System.exit(0);
	}
}
