/*
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
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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

public class FrameSearchDocument extends JDialog implements CAPPConstants,
		ServerHelper.HTTPCallback {

	private static final long serialVersionUID = 1L;
//	private static final String desc = "说明: 除整件编号、基线编号外，用%可以通配任意子字符串";
	private static final String desc = "说明: 用%可以通配任意子字符串";
	private Object frame = this;
	private Object lock = new Object();

	private HashMap<String, Object> task = null;
	private HashMap<String, String> webInfo = null;
	private ArrayList searchedDocuments = null;
	private ArrayList<HashMap<String, Object>> selectedDocuments = null;
	private boolean secondCallNeeded = true; // 是否在选中文档后还有再次调用服务器功能
	private String docAction = null;
	private String workType = null;
	private boolean effSearched = false;
	private boolean downloadNeeded = false;

	private DataModel dataModel = null;

	// --------------------------------------------------------------------------
	private JPanel jContentPane = null;

	private JLabel jLabel = null;
	private JLabel jLabel1 = null;
	private JTextField txtName = null;
	private JTextField txtNumber = null;
	private JButton btnSearch = null;
	private JLabel labDesc = null;
	private JTable docTable = null;
	private JScrollPane jScrollPane = null;
	private JLabel labStatus = null;
	private JLabel jLabel4 = null;
	private JTextField txtSelected = null;
	private JButton btnSelect = null;
	private JButton btnCancel = null;
	private JLabel labSavePath = null;
	private JTextField txtSavePath = null;
	private JButton btnSetSavePath = null;
	private JLabel jLabel5 = null;
	private JComboBox comboContainer = null;

	private JButton btnSearchProducts = null;
	private JButton btnAllProducts = null;
	private JProgressBar progressBar = null;
	private JPanel panelEff = null;
	private JLabel labEff = null;
	private JTextField txtEff = null;
	private JButton btnEff = null;
	private JLabel jLabel2 = null;
	private JTextField txtPartNumber = null;
	private JCheckBox chkLatestVersionOnly = null;
	private JLabel labelBaseline = null;
	private JTextField txtBaselineNumber = null;
	private JCheckBox chkSearchBaseline = null;
	private JPanel panelBaseline = null;

	private FrameSearchDocument() {
		super();
		initialize();
	}

	public static HashMap<String, Object> searchDocument(
			HashMap<String, Object> task, HashMap<String, String> webInfo)
			throws Exception {
		return new FrameSearchDocument()._searchDocument(task, webInfo);
	}

	private HashMap<String, Object> _searchDocument(
			HashMap<String, Object> task, HashMap<String, String> webInfo)
			throws Exception {
		// 获取功能调用信息
		HashMap<String, Object> capp = (HashMap<String, Object>) task.get(CAPP);
		if (capp == null)
			throw new Exception("内部错误，未找到任务描述参数！");

		this.webInfo = webInfo;
		this.task = task;

		String type = (String) task.get(TYPE);
		String windowCaption = (String) capp.get(WINDOW_CAPTION);
		String docType = (String) capp.get(DOC_TYPE);
		String downloadStr = (String) capp.get(DOWNLOAD);

		downloadNeeded = downloadStr != null
				&& downloadStr.equalsIgnoreCase(TRUE);
		workType = type;

		// 提前相关调用参数
		String note = desc;
		if (docType != null && !docType.equals("") && !docType.equals("*"))
			note += "，搜索文档类型被指定为: " + docType;
		secondCallNeeded = type.equalsIgnoreCase(SEARCH_DOCUMENT)
				&& downloadNeeded || !type.equalsIgnoreCase(SEARCH_DOCUMENT);
		String btnCaption = "确定";
		docAction = null;
		if (type.equalsIgnoreCase(SEARCH_DOCUMENT)) {
			if (windowCaption == null || windowCaption.trim().equals(""))
				windowCaption = "查找工艺文档";
			btnCaption = "确定";
			docAction = "下载";
		} else if (type.equalsIgnoreCase(DELETE_DOCUMENT)) {
			if (windowCaption == null || windowCaption.trim().equals(""))
				windowCaption = "查找并删除工艺文档";
			btnCaption = "删除";
			docAction = "删除";
		} else if (type.equalsIgnoreCase(CHECKOUT_DOCUMENT)) {
			if (windowCaption == null || windowCaption.trim().equals(""))
				windowCaption = "查找并检出工艺文档";
			btnCaption = "检出";
		} else if (type.equalsIgnoreCase(REVISE_DOCUMENT)) {
			if (windowCaption == null || windowCaption.trim().equals(""))
				windowCaption = "查找并修订工艺文档";
			btnCaption = "修订";
		} else
			throw new Exception("不支持的工艺文档查询调用: " + type);

		if (docAction == null)
			docAction = btnCaption;
		btnSelect.setText(btnCaption);

		ScreenUtil.centerWindow(this);
		setTitle(windowCaption);

		// 隐藏不需要的组件(以前版本遗留组件)
		labSavePath.setVisible(false);
		txtSavePath.setVisible(false);
		btnSetSavePath.setVisible(false);

		labelBaseline.setVisible(false);
		jLabel2.setVisible(false);
		chkSearchBaseline.setVisible(false);
		txtBaselineNumber.setVisible(false);
		txtPartNumber.setVisible(false);

		boolean effEnabled = docType != null
				&& (docType.equalsIgnoreCase("AO") || docType
						.equalsIgnoreCase("FO")) && type != null
				&& type.equals(REVISE_DOCUMENT);

		labEff.setVisible(effEnabled);
		txtEff.setVisible(effEnabled);
		txtEff.setEnabled(effEnabled);
		btnEff.setVisible(effEnabled);
		btnEff.setEnabled(effEnabled);

		btnSelect.setEnabled(false);

		// 初始化各组件
		progressBar.setVisible(false);
		labDesc.setText(note);
		txtSelected.setEditable(false);
		labStatus.setText("请指定搜索条件...");
		if (type.equals(SEARCH_DOCUMENT))
			docTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		else
			docTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// 读取上次查询参数
		try {
			HashMap<String, Object> vars = Settings.getVariables();
			txtName.setText((String) vars.get(DOCUMENT + NAME));
			txtNumber.setText((String) vars.get(DOCUMENT + NUMBER));
			txtBaselineNumber.setText((String) vars.get(DOCUMENT + BASELINE));
			txtPartNumber.setText((String) vars.get(DOCUMENT + PART_NUMBER));
			Boolean latestVersionOnly = (Boolean) vars.get(DOCUMENT + LATEST_VERSION_ONLY);
			chkLatestVersionOnly.setSelected(latestVersionOnly != null && latestVersionOnly.booleanValue());
			ArrayList<String> list = (ArrayList<String>) vars.get(DOCUMENT+ PRODUCT + PRODUCT);
			Integer iProd = (Integer) vars.get(DOCUMENT + PRODUCT);

			if (list != null && list.size() > 0) {
				comboContainer.removeAllItems();
				for (int i = 0; i < list.size(); i++)
					comboContainer.addItem(list.get(i));
				comboContainer.addItem("");
				if (iProd != null && iProd.intValue() >= 0
						&& iProd.intValue() <= list.size())
					comboContainer.setSelectedIndex(iProd.intValue());
			}

			Rectangle r = (Rectangle) vars.get(DOCUMENT + "BOUNDS");
			setBounds(r);

			HashMap<String, Integer> widths = (HashMap<String, Integer>) vars.get(DOCUMENT + "COLUMNWIDTHS");
			for (int i = 0; i < docTable.getColumnCount(); i++) {
				Integer w = (Integer) widths.get(docTable.getColumnName(i));
				if (w != null && w.intValue() > 0) {
					TableColumnModel cm = docTable.getColumnModel();
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
		vars.put(DOCUMENT + "BOUNDS", getBounds());
		HashMap<String, Integer> widths = new HashMap<String, Integer>();
		for (int i = 0; i < docTable.getColumnCount(); i++) {
			TableColumnModel cm = docTable.getColumnModel();
			widths.put(docTable.getColumnName(i), new Integer(cm.getColumn(i).getWidth()));
		}
		vars.put(DOCUMENT + "COLUMNWIDTHS", widths);
		Settings.saveVariables();

		// 用户取消标志
		if (selectedDocuments == null || selectedDocuments.size() <= 0)
			return null;

		task.put(DOCUMENT, selectedDocuments);
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
		}
	}

	private void threadStart(String status) throws Exception {
		SwingUtilities.invokeAndWait(new Runnable() {
			String status;

			public void run() {
				((FrameSearchDocument) frame).setCursor(Cursor
						.getPredefinedCursor(Cursor.WAIT_CURSOR));

				labStatus.setText(status);
				txtSelected.setText("");
				docTable.clearSelection();
				progressBar.setVisible(true);

				txtName.setEnabled(false);
				txtNumber.setEnabled(false);
				txtPartNumber.setEnabled(false);
				comboContainer.setEnabled(false);
				btnSearchProducts.setEnabled(false);
				btnAllProducts.setEnabled(false);
				btnSearch.setEnabled(false);

				btnCancel.setEnabled(false);
				btnEff.setEnabled(false);
				btnSelect.setEnabled(false);
				btnSetSavePath.setEnabled(false);
				docTable.setEnabled(false);
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
				txtName.setEnabled(true);
				txtNumber.setEnabled(true);
				txtPartNumber.setEnabled(true);
				comboContainer.setEnabled(true);
				btnSearchProducts.setEnabled(true);
				btnAllProducts.setEnabled(true);
				btnSearch.setEnabled(true);

				btnCancel.setEnabled(true);
				btnSetSavePath.setEnabled(true);
				docTable.setEnabled(true);

				progressBar.setVisible(false);
				((FrameSearchDocument) frame).setCursor(Cursor.getDefaultCursor());
			}
		});
	}

	/**
	 * 产品搜索线程 Created on 2005-7-26
	 * @author liuld
	 */
	class SearchProductThread extends Thread {
		String _prodName;
		ArrayList<String> _products;
		Exception _exception;

		public SearchProductThread(String prodName) {
			_prodName = prodName;
		}

		public void run() {
			try {
				// --------------------------------------------------------------
				threadStart("正在搜索产品......");

				// --------------------------------------------------------------
				HashMap<String, Object> task = new HashMap<String, Object>();
				HashMap<String, Object> capp = new HashMap<String, Object>();

				task.put(CAPP, capp);
				task.put(TYPE, SEARCH_PRODUCT);
				capp.put(PRODUCT_NAME, _prodName);

				String user = (String) webInfo.get(USER);
				String pass = (String) webInfo.get(PASS);
				String surl = (String) webInfo.get(SERVER_URL);
				HashMap<String, Object> result = ServerHelper.callServer(user, pass, surl,task, null, frame);
				_products = (ArrayList<String>) result.get(PRODUCT);

				// --------------------------------------------------------------
				if (_products == null)
					throw new Exception("查询产品过程中发现未知错误！");

				HashMap<String, Object> vars = Settings.getVariables();
				vars.put(DOCUMENT + PRODUCT + PRODUCT, _products);
				vars.put(DOCUMENT + PRODUCT, new Integer(0));
				Settings.saveVariables();

				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						comboContainer.removeAllItems();
						for (int i = 0; i < _products.size(); i++)
							comboContainer.addItem(_products.get(i));
						comboContainer.addItem(""); // 加一个空行以便选空
						comboContainer.setSelectedIndex(0);

						labStatus.setText("共找到 " + _products.size() + " 个产品.");
					}
				});
			} catch (Exception e) {
				_exception = e;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						Logger.log(_exception);
						String msg = "搜索产品过程中出错: "
								+ Tools.getErrorMessage(_exception);
						labStatus.setText(msg);
						JOptionPane.showMessageDialog(
								((FrameSearchDocument) frame), msg);
					}
				});
			} finally {
				threadEnd();
			}
		}
	}

	/**
	 * 文档搜索线程 Created on 2005-7-28
	 * @author liuld
	 */
	class SearchDocumentThread extends Thread {
		String _name;
		String _number;
		String _baselineNumber;
		String _partNumber;
		String _product;
		String _oid;
		Exception _exception;
		boolean _searchEff = false;

		// boolean _latestVersionOnly = false;

		SearchDocumentThread(String name, String number, String baselineNumber,
				String partNumber, String product) {
			_name = name;
			_number = number;
			_baselineNumber = baselineNumber;
			_partNumber = partNumber;
			_product = product;
			// _latestVersionOnly = latestVersionOnly;
			_searchEff = false;
			_oid = "";
		}

		SearchDocumentThread(String number, String oid) {
			_name = "";
			_number = number;
			_baselineNumber = null;
			_partNumber = null;
			_product = "";

			_searchEff = true;
			_oid = oid;
		}

		public void run() {
			try {
				threadStart("正在搜索文档......");

				// --------------------------------------------------------------
				HashMap<String, Object> task1 = (HashMap<String, Object>) DataPacker.clone(task);
				HashMap<String, Object> capp = (HashMap<String, Object>) task1.get(CAPP);

				// IBA_LIST, DOC_TYPE已包含在task内
				capp.put(DOC_NAME, _name);
				capp.put(DOC_NUMBER, _number);
				capp.put(PRODUCT_NAME, _product);
				// capp.put(LATEST_VERSION_ONLY,
				// String.valueOf(_latestVersionOnly));

				if (_partNumber != null && !_partNumber.trim().equals("")) {
					capp.put(BASELINE_NUMBER, _baselineNumber);
					capp.put(PART_NUMBER, _partNumber);
				}

				// 如果是SEARCH_DOCUMENT,而且需要下载,则将获取IBA的任务留给下载时执行
				String type = (String) task1.get(TYPE);
				String dnld = (String) capp.get(DOWNLOAD);
				if (type.equalsIgnoreCase(SEARCH_DOCUMENT)) {
					if (dnld.equalsIgnoreCase(TRUE))
						capp.remove(IBA_LIST);
				} else { // 所有其他功能, 在第一步全部执行查询功能
					task1.put(TYPE, SEARCH_DOCUMENT);
				}

				String user = (String) webInfo.get(USER);
				String pass = (String) webInfo.get(PASS);
				String surl = (String) webInfo.get(SERVER_URL);
				HashMap<String, Object> result = ServerHelper.callServer(user, pass, surl,task1, null, frame);
				// --------------------------------------------------------------

				if (result == null || result.get(DOCUMENT) == null)
					throw new Exception("查询文档过程中发现未知异常!");

				ArrayList documents = (ArrayList) result.get(DOCUMENT);
				SwingUtilities.invokeAndWait(new Runnable() {
					ArrayList documents;

					public void run() {
						synchronized (lock) {
							if (_searchEff && documents != null) {
								for (int i = documents.size() - 1; i >= 0; i--) {
									HashMap docInfo = (HashMap) documents.get(i);
									String oid = (String) docInfo.get(OID);

									// 准备修订的文档，不排除
									if (oid != null && oid.equals(_oid))
										continue;
								}
							}

							searchedDocuments = documents;
							labStatus.setText("共找到 " + documents.size() + " 个符合条件的文档.");
						}

						effSearched = _searchEff;

						docTable.updateUI();
					}

					public Runnable get(ArrayList documents) {
						this.documents = documents;
						return this;
					}
				}.get(documents));
			} catch (Exception e) {
				SwingUtilities.invokeLater(new Runnable() {
					Exception exception;

					public void run() {
						Logger.log(exception);
						String msg = "搜索文档过程中出错: " + Tools.getErrorMessage(exception);
						labStatus.setText(msg);
						JOptionPane.showMessageDialog(((FrameSearchDocument) frame), msg);
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
	 * 文档活动执行线程,执行活动包括: 下载,删除,检出,修订,预先更改
	 * Created on 2005-7-28
	 * @author liuld
	 */
	class MakeDocumentActionThread extends Thread {
		String docOid;

		MakeDocumentActionThread(String docOid) {
			this.docOid = docOid;
		}

		public void run() {
			try {
				HashMap<String, Object> result = null;

				if (secondCallNeeded) {
					threadStart("正在" + docAction + "......");

					HashMap<String, Object> task1 = (HashMap<String, Object>) DataPacker.clone(task);
					HashMap<String, Object> capp = (HashMap<String, Object>) task1.get(CAPP);

					// 如果原操作为SEARCH_DOCUMENT, 则在此执行DOWNLOAD_DOCUMENT
					String type = (String) task1.get(TYPE);
					if (type.equalsIgnoreCase(SEARCH_DOCUMENT)) {
						type = DOWNLOAD_DOCUMENT;
						task1.put(TYPE, type);
					}
					capp.put(OID, docOid);

					String user = (String) webInfo.get(USER);
					String pass = (String) webInfo.get(PASS);
					String surl = (String) webInfo.get(SERVER_URL);
					ArrayList files = new ArrayList();

					result = ServerHelper.callServer(user, pass, surl, task1,files, frame);

					if (result == null || result.get(DOCUMENT) == null)
						throw new Exception(docAction + "文档过程中发现未知异常!");

					ArrayList documents = (ArrayList) result.get(DOCUMENT);
					String errorMsg = null;
					if (documents.size() < 1)
						errorMsg = "未能找到指定的工艺文档!";
					else if (documents.size() > 1)
						errorMsg = "内部错误: 不应出现找到多个文档与选中文档相对应!";

					if (errorMsg != null)
						throw new Exception(errorMsg);

					result.put(FILES, files);
				} else {
					result = new HashMap<String, Object>();
					result.put(CAPP, task.get(CAPP));
					result.put(DOCUMENT, selectedDocuments);
				}

				finishAction(result);
			} catch (Exception e) {
				SwingUtilities.invokeLater(new Runnable() {
					Exception exception;

					public void run() {
						Logger.log(exception);
						String msg = docAction + "文档过程中出错: " + Tools.getErrorMessage(exception);
						labStatus.setText(msg);
						JOptionPane.showMessageDialog(((FrameSearchDocument) frame), msg);
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

		public void finishAction(HashMap<String, Object> result) throws Exception {
			HashMap<String, Object> di = (HashMap<String, Object>) ((ArrayList) result.get(DOCUMENT)).get(0);
			HashMap<String, Object> dd = new LinkedHashMap<String, Object>();
			ArrayList files = (ArrayList) result.get(FILES);

			// 基本属性
			dd.put(NAME, di.get(NAME));
			dd.put(NUMBER, di.get(NUMBER));
			dd.put(VERSION, di.get(VERSION));
			dd.put(PRODUCT_NAME, di.get(PRODUCT_NAME));

			// 主文件、附件
			for (int i = 0; files != null && i < files.size(); i++) {
				String FILE = i == 0 ? PRIMARY_FILE : SECONDARY_FILE;
				HashMap fi = (HashMap) files.get(i);
				if (fi == null) {
					dd.put(FILE, "");
				} else {
					File file = (File) fi.get(DataPacker.FILE);
					String name = (String) fi.get(DataPacker.NAME);

					// 文件更改为原名
					File newFile = new File(file.getParent() + "/" + name);
					if (!file.equals(newFile)) {
						Tools.copyFile(file, newFile);
						file.delete();
					}
					Object existing = dd.get(FILE);
					String path = newFile.getAbsolutePath();
					if (existing == null)
						dd.put(FILE, path);
					else if (existing instanceof List)
						((List) dd.get(FILE)).add(path);
					else {
						if (existing.equals(""))
							dd.put(FILE, path);
						else {
							List list = new ArrayList();
							files.add(existing);
							files.add(path);
							dd.put(FILE, list);
						}
					}
				}
			}

			// IBA属性
			HashMap capp = (HashMap) task.get(CAPP);
			String ibaCSV = (String) capp.get(IBA_LIST);
			Object[] ibas = null;
			if (ibaCSV != null && !ibaCSV.equals("")) {
				if (!ibaCSV.equals("*"))
					ibas = ibaCSV.split(",");
				else {
					ArrayList ibaNameList = (ArrayList) di.get(TYPE_IBA_LIST);
					if (ibaNameList != null)
						ibas = ibaNameList.toArray();
				}
			}
			for (int i = 0; ibas != null && i < ibas.length; i++) {
				if (ibas[i] == null)
					continue;
				String ibaName = String.valueOf(ibas[i]).trim();
				if (!ibaName.equals(""))
					dd.put(ibaName, di.get(ibaName));
			}

			// 测试XML转义
			// di.put("TEST_IBA", "<\"'&>需要转义的XML字符");

			selectedDocuments = new ArrayList<HashMap<String, Object>>();
			selectedDocuments.add(dd);
			((FrameSearchDocument) frame).setVisible(false);
		}
	}

	/**
	 * 文档表模型
	 * Created on 2005-7-26
	 * @author liuld
	 */
	class DataModel extends AbstractTableModel {
		final String[] tableColNames = { "产品", "名称", "编号", "版本", "类型", "检出状态",
				"生命周期状态", "最后修改" };
		final String[] tableColKeys = { PRODUCT_NAME, NAME, NUMBER, ITERATION,
				DOC_TYPE, CHECKOUT_STATUS, LIFECYCLE_STATUS, LAST_MODIFIED, };

		public int getColumnCount() {
			return tableColNames.length;
		}

		public String getColumnName(int col) {
			return col < tableColNames.length ? tableColNames[col] : "";
		}

		public int getRowCount() {
			synchronized (lock) {
				return searchedDocuments == null ? 0 : searchedDocuments.size();
			}
		}

		public Object getValueAt(int row, int col) {
			synchronized (lock) {
				if (searchedDocuments != null && row < searchedDocuments.size()
						&& col < tableColNames.length) {
					HashMap part = (HashMap) searchedDocuments.get(row);
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
		this.setSize(720, 480);
		this.setContentPane(getJContentPane());
		this.setTitle("文档检出");
	}

	/**
	 * This method initializes jContentPane
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if (jContentPane == null) {
			GridBagConstraints gridBagConstraints42 = new GridBagConstraints();
			gridBagConstraints42.gridx = 1;
			gridBagConstraints42.fill = GridBagConstraints.BOTH;
			gridBagConstraints42.gridwidth = 3;
			gridBagConstraints42.gridy = 3;
			GridBagConstraints gridBagConstraints33 = new GridBagConstraints();
			gridBagConstraints33.gridx = 1;
			gridBagConstraints33.gridwidth = 3;
			gridBagConstraints33.anchor = GridBagConstraints.WEST;
			gridBagConstraints33.gridy = 2;
			GridBagConstraints gridBagConstraints111 = new GridBagConstraints();
			gridBagConstraints111.gridx = 0;
			gridBagConstraints111.anchor = GridBagConstraints.EAST;
			gridBagConstraints111.insets = new Insets(4, 4, 0, 4);
			gridBagConstraints111.gridy = 3;
			labelBaseline = new JLabel();
			labelBaseline.setFont(new Font("DialogInput", Font.PLAIN, 12));
			labelBaseline.setText("基线编号:");
			GridBagConstraints gridBagConstraints110 = new GridBagConstraints();
			gridBagConstraints110.gridx = 2;
			gridBagConstraints110.gridwidth = 3;
			gridBagConstraints110.anchor = GridBagConstraints.WEST;
			gridBagConstraints110.gridy = 6;
			jLabel2 = new JLabel();
			jLabel2.setText("整件编号:");
			jLabel2.setFont(new Font("DialogInput", Font.PLAIN, 12));
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.gridx = 1;
			gridBagConstraints15.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints15.gridwidth = 3;
			gridBagConstraints15.gridy = 9;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 2;
			gridBagConstraints.gridwidth = 4;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.gridy = 12;
			GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints32 = new GridBagConstraints();
			jLabel5 = new JLabel();
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
			labSavePath = new JLabel();
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			jLabel4 = new JLabel();
			labStatus = new JLabel();
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
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new GridBagLayout());
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.insets = new Insets(15, 4, 0, 4);
			gridBagConstraints1.anchor = GridBagConstraints.EAST;
			jLabel.setText("文档名称:");
			jLabel.setFont(new Font("DialogInput", Font.PLAIN, 12));
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.anchor = GridBagConstraints.EAST;
			gridBagConstraints2.insets = new Insets(4, 4, 0, 4);
			jLabel1.setText("文档编号:");
			jLabel1.setFont(new Font("DialogInput", Font.PLAIN, 12));
			gridBagConstraints3.gridx = 1;
			gridBagConstraints3.gridy = 0;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints3.insets = new Insets(15, 0, 0, 0);
			gridBagConstraints3.gridwidth = 3;
			gridBagConstraints4.gridx = 1;
			gridBagConstraints4.gridy = 1;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints4.insets = new Insets(4, 0, 0, 0);
			gridBagConstraints4.gridwidth = 3;
			gridBagConstraints5.gridx = 4;
			gridBagConstraints5.gridy = 0;
			gridBagConstraints5.gridheight = 5;
			gridBagConstraints5.gridwidth = 2;
			gridBagConstraints5.insets = new Insets(15, 0, 0, 0);
			gridBagConstraints6.gridx = 1;
			gridBagConstraints6.gridy = 6;
			gridBagConstraints6.anchor = GridBagConstraints.WEST;
			gridBagConstraints6.insets = new Insets(4, 0, 4, 0);
//			labDesc.setText("说明: 除整件编号外，用%可以通配任意子字符串");
			labDesc.setText("用%可以通配任意子字符串");
			labDesc.setFont(new Font("DialogInput", Font.PLAIN, 12));
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.gridy = 7;
			gridBagConstraints7.weightx = 1.0;
			gridBagConstraints7.weighty = 1.0;
			gridBagConstraints7.fill = GridBagConstraints.BOTH;
			gridBagConstraints7.gridwidth = 6;
			gridBagConstraints7.insets = new Insets(0, 4, 0, 4);
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.insets = new Insets(0, 0, 0, 0);
			gridBagConstraints8.gridy = 12;
			gridBagConstraints8.anchor = GridBagConstraints.WEST;
			gridBagConstraints8.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints8.gridwidth = 2;
			labStatus.setText("　");
			labStatus.setFont(new Font("DialogInput", Font.PLAIN, 12));
			gridBagConstraints9.gridx = 0;
			gridBagConstraints9.insets = new Insets(4, 4, 0, 4);
			gridBagConstraints9.gridy = 9;
			gridBagConstraints9.anchor = GridBagConstraints.EAST;
			jLabel4.setText("选中文档:");
			jLabel4.setFont(new Font("DialogInput", Font.PLAIN, 12));
			gridBagConstraints11.gridx = 4;
			gridBagConstraints11.gridy = 9;
			gridBagConstraints11.insets = new Insets(4, 4, 0, 2);
			gridBagConstraints12.gridx = 5;
			gridBagConstraints12.gridy = 9;
			gridBagConstraints12.insets = new Insets(4, 2, 0, 4);
			gridBagConstraints13.gridx = 0;
			gridBagConstraints13.insets = new Insets(4, 4, 4, 4);
			gridBagConstraints13.gridy = 10;
			gridBagConstraints13.anchor = GridBagConstraints.EAST;
			labSavePath.setText("下载位置:");
			labSavePath.setFont(new Font("DialogInput", Font.PLAIN, 12));
			gridBagConstraints21.gridx = 1;
			gridBagConstraints21.gridy = 10;
			gridBagConstraints21.weightx = 1.0;
			gridBagConstraints21.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints21.insets = new Insets(4, 0, 4, 0);
			gridBagConstraints21.gridwidth = 3;
			gridBagConstraints31.gridx = 4;
			gridBagConstraints31.gridy = 10;
			gridBagConstraints31.gridwidth = 2;
			gridBagConstraints31.insets = new Insets(4, 4, 4, 4);
			gridBagConstraints31.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints14.gridx = 0;
			gridBagConstraints14.gridy = 5;
			gridBagConstraints14.insets = new Insets(4, 4, 0, 4);
			gridBagConstraints14.anchor = GridBagConstraints.EAST;
			jLabel5.setText("产品名称:");
			jLabel5.setFont(new Font("DialogInput", Font.PLAIN, 12));
			gridBagConstraints22.gridx = 1;
			gridBagConstraints22.gridy = 5;
			gridBagConstraints22.weightx = 1.0;
			gridBagConstraints22.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints22.insets = new Insets(4, 0, 0, 0);
			gridBagConstraints32.gridx = 2;
			gridBagConstraints32.gridy = 5;
			gridBagConstraints32.insets = new Insets(4, 4, 0, 2);
			gridBagConstraints41.gridx = 3;
			gridBagConstraints41.anchor = GridBagConstraints.WEST;
			gridBagConstraints41.gridy = 5;
			gridBagConstraints41.insets = new Insets(4, 2, 0, 0);
			jContentPane.add(jLabel4, gridBagConstraints9);
			jContentPane.add(labStatus, gridBagConstraints8);
			jContentPane.add(labSavePath, gridBagConstraints13);
			jContentPane.add(jLabel, gridBagConstraints1);
			jContentPane.add(jLabel1, gridBagConstraints2);
			jContentPane.add(jLabel5, gridBagConstraints14);
			jContentPane.add(labDesc, gridBagConstraints6);
			jContentPane.add(getTxtName(), gridBagConstraints3);
			jContentPane.add(getTxtNumber(), gridBagConstraints4);
			jContentPane.add(getComboContainer(), gridBagConstraints22);
			jContentPane.add(getTxtSavePath(), gridBagConstraints21);
			jContentPane.add(getBtnSelect(), gridBagConstraints11);
			jContentPane.add(getBtnCancel(), gridBagConstraints12);
			jContentPane.add(getJScrollPane(), gridBagConstraints7);
			jContentPane.add(getBtnSearch(), gridBagConstraints5);
			jContentPane.add(getBtnSetSavePath(), gridBagConstraints31);
			jContentPane.add(getBtnSearchProducts(), gridBagConstraints32);
			jContentPane.add(getBtnAllProducts(), gridBagConstraints41);
			jContentPane.add(getProgressBar(), gridBagConstraints);
			jContentPane.add(getPanelEff(), gridBagConstraints15);
			jContentPane.add(getChkLatestVersionOnly(), gridBagConstraints110);
			jContentPane.add(labelBaseline, gridBagConstraints111);
			jContentPane.add(getChkSearchBaseline(), gridBagConstraints33);
			jContentPane.add(getPanelBaseline(), gridBagConstraints42);
		}
		return jContentPane;
	}

	/**
	 * This method initializes txtName
	 * @return javax.swing.JTextField
	 */
	private JTextField getTxtName() {
		if (txtName == null) {
			txtName = new JTextField();
			txtName.setFont(new Font("DialogInput", Font.PLAIN, 12));
		}
		return txtName;
	}

	/**
	 * This method initializes txtNumber
	 * @return javax.swing.JTextField
	 */
	private JTextField getTxtNumber() {
		if (txtNumber == null) {
			txtNumber = new JTextField();
			txtNumber.setFont(new Font("DialogInput", Font.PLAIN, 12));
		}
		return txtNumber;
	}

	/**
	 * This method initializes docTable
	 * @return javax.swing.JTable
	 */
	private JTable getDocTable() {
		if (docTable == null) {
			dataModel = new DataModel();
			docTable = new JTable(dataModel);
			docTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			docTable.setFont(new Font("DialogInput", Font.PLAIN, 12));
			docTable.setDefaultRenderer(Object.class,
					new MyDefaultCellRenderer());
			JTableHeader tableHeader = docTable.getTableHeader();
			tableHeader.setFont(new Font("DialogInput", Font.PLAIN, 12));
			docTable.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					String docType = null;
					String type = workType;
					int row = docTable.getSelectedRow();
					btnSelect.setEnabled(false);
					if (row < 0)
						return;

					HashMap doc = (HashMap) searchedDocuments.get(row);
					txtSelected.setText(doc.get(DOC_TYPE) + " "
							+ doc.get(NUMBER) + " - " + doc.get(NAME) + " "
							+ doc.get(ITERATION) + " "
							+ doc.get(CHECKOUT_STATUS) + " "
							+ doc.get(LIFECYCLE_STATUS));
					docType = (String) doc.get(DOC_TYPE);

					boolean effEnabled = docType != null
							&& (docType.equalsIgnoreCase("AO") || docType
									.equalsIgnoreCase("FO")) && type != null
							&& type.equals(REVISE_DOCUMENT);

					btnEff.setEnabled(effEnabled);
					if (labEff.isVisible() != effEnabled) {
						labEff.setVisible(effEnabled);
						txtEff.setVisible(effEnabled);
						txtEff.setEnabled(effEnabled);
						btnEff.setVisible(effEnabled);
						panelEff.updateUI();
					}

					btnSelect.setEnabled(!labEff.isVisible() || effSearched);
				}
			});
		}
		return docTable;
	}

	/**
	 * This method initializes jScrollPane
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getDocTable());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes txtSelected
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
	 * This method initializes btnSearch
	 * @return javax.swing.JButton
	 */
	private JButton getBtnSearch() {
		if (btnSearch == null) {
			btnSearch = new JButton();
			btnSearch.setFont(new Font("宋体", Font.PLAIN, 12));
			btnSearch.setPreferredSize(new Dimension(83, 21));
			btnSearch.setText("搜索文档");
			getRootPane().setDefaultButton(btnSearch);
			btnSearch.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					btnSearch.setEnabled(false);

					HashMap<String, Object> vars = Settings.getVariables();
					vars.put(DOCUMENT + NAME, txtName.getText());
					vars.put(DOCUMENT + NUMBER, txtNumber.getText());
					vars.put(DOCUMENT + BASELINE, txtBaselineNumber.getText());
					vars.put(DOCUMENT + PART_NUMBER, txtPartNumber.getText());
					vars.put(DOCUMENT + PRODUCT,
							new Integer(comboContainer.getSelectedIndex()));
					vars.put(DOCUMENT + LATEST_VERSION_ONLY, new Boolean(
							chkLatestVersionOnly.isSelected()));
					Settings.saveVariables();

					String baselineNumber = "";
					String zhengjianNumber = "";
					if (chkSearchBaseline.isSelected()) {
						baselineNumber = txtBaselineNumber.getText().trim();
						zhengjianNumber = txtPartNumber.getText().trim();
						if (zhengjianNumber.equals("")) {
							String msg = "请输入基线和整件编号!";
							JOptionPane.showMessageDialog(
									((FrameSearchDocument) frame), msg);
							return;
						}
					}

					new SearchDocumentThread(txtName.getText(), txtNumber
							.getText(), baselineNumber, zhengjianNumber,
							(String) comboContainer.getSelectedItem()).start();
				}
			});
		}
		return btnSearch;
	}

	/**
	 * This method initializes btnSelect
	 * @return javax.swing.JButton
	 */
	private JButton getBtnSelect() {
		if (btnSelect == null) {
			btnSelect = new JButton();
			btnSelect.setFont(new Font("宋体", Font.PLAIN, 12));
			btnSelect.setPreferredSize(new Dimension(59, 21));
			btnSelect.setText("检出");
			btnSelect.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int selectedRow = docTable.getSelectedRow();
					if (selectedRow < 0) {
						JOptionPane.showMessageDialog(
								((FrameSearchDocument) frame), "您未选择任何文档!",
								"提示", JOptionPane.INFORMATION_MESSAGE);
						return;
					}

					// 视情况检查是否下载目录必需为空
					int count = docTable.getSelectedRowCount();
					int[] rows = docTable.getSelectedRows();
					ArrayList docList = new ArrayList();
					if (count == 1) {
						HashMap doc = (HashMap) searchedDocuments
								.get(selectedRow);
						selectedDocuments = new ArrayList();
						selectedDocuments.add(doc);

						String oid = (String) doc.get(OID);
						new MakeDocumentActionThread(oid).start();
					} else {
						boolean error = false;
						for (int i = 0; i < count; i++) {
							try {
								HashMap doc = (HashMap) searchedDocuments
										.get(rows[i]);

								if (downloadNeeded) {
									HashMap task1 = (HashMap) DataPacker
											.clone(task);
									HashMap capp = (HashMap) task1.get(CAPP);
									String docOid = (String) doc.get(OID);
									task1.put(TYPE, DOWNLOAD_DOCUMENT);
									capp.put(OID, docOid);

									ArrayList files = new ArrayList();
									ServerHelper.callServer("正在下载...", webInfo,
											task1, files, null);

									Worker.fillDocFileParams(doc, files);
								}

								docList.add(doc);
							} catch (Exception ex) {
								ex.printStackTrace();
								error = true;
								String msg = Tools.getErrorMessage(ex);
								JOptionPane.showMessageDialog(
										((FrameSearchDocument) frame), msg);
								break;
							}
						}

						if (!error) {
							selectedDocuments = docList;
							((FrameSearchDocument) frame).setVisible(false);
						}
					}
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
			btnCancel.setFont(new Font("宋体", Font.PLAIN, 12));
			btnCancel.setPreferredSize(new Dimension(59, 21));
			btnCancel.setText("取消");
			btnCancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					selectedDocuments = null;
					((FrameSearchDocument) frame).setVisible(false);
				}
			});
		}
		return btnCancel;
	}

	/**
	 * This method initializes txtContainer
	 * @return javax.swing.JTextField
	 */
	private JTextField getTxtSavePath() {
		if (txtSavePath == null) {
			txtSavePath = new JTextField();
			txtSavePath.setEditable(false);
			txtSavePath.setName("txtPath");
		}
		return txtSavePath;
	}

	/**
	 * This method initializes btnSearchProducts
	 * @return javax.swing.JButton
	 */
	private JButton getBtnSetSavePath() {
		if (btnSetSavePath == null) {
			btnSetSavePath = new JButton();
			btnSetSavePath.setText("浏览...");
			btnSetSavePath.setFont(new Font("宋体", Font.PLAIN, 12));
			btnSetSavePath.setPreferredSize(new Dimension(79, 21));
			btnSetSavePath.setName("btnSetPath");
			btnSetSavePath
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							JFileChooser fc = new JFileChooser();
							fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
							int fcVal = fc.showOpenDialog(((FrameSearchDocument) frame));
							if (fcVal == JFileChooser.APPROVE_OPTION) {
								String savePath = fc.getSelectedFile().getPath();
								if (!savePath.endsWith(File.separator))
									savePath += File.separator;
								txtSavePath.setText(savePath);
								Settings.setSavePath(savePath);
							}
						}
					});
		}
		return btnSetSavePath;
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
			btnSearchProducts.setPreferredSize(new Dimension(83, 21));
			btnSearchProducts.setFont(new Font("宋体", Font.PLAIN, 12));
			btnSearchProducts
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
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
			btnAllProducts.setPreferredSize(new Dimension(83, 21));
			btnAllProducts.setFont(new Font("宋体", Font.PLAIN, 12));
			btnAllProducts
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							new SearchProductThread(null).start();
						}
					});
		}
		return btnAllProducts;
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

	/**
	 * This method initializes jPanel
	 * @return javax.swing.JPanel
	 */
	private JPanel getPanelEff() {
		if (panelEff == null) {
			GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
			gridBagConstraints18.insets = new Insets(4, 0, 0, 0);
			GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
			gridBagConstraints17.anchor = GridBagConstraints.EAST;
			gridBagConstraints17.insets = new Insets(4, 8, 0, 0);
			gridBagConstraints17.fill = GridBagConstraints.NONE;
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			gridBagConstraints16.fill = GridBagConstraints.NONE;
			gridBagConstraints16.anchor = GridBagConstraints.EAST;
			gridBagConstraints16.insets = new Insets(4, 0, 0, 0);
			gridBagConstraints16.weightx = 0.0;
			labEff = new JLabel();
			labEff.setText("有效性：");
			labEff.setFont(new Font("Dialog", Font.PLAIN, 12));
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints10.gridx = 0;
			gridBagConstraints10.gridy = 0;
			gridBagConstraints10.weightx = 1.0;
			gridBagConstraints10.insets = new Insets(4, 0, 0, 0);
			panelEff = new JPanel();
			panelEff.setLayout(new GridBagLayout());
			panelEff.add(getTxtSelected(), gridBagConstraints10);
			panelEff.add(labEff, gridBagConstraints17);
			panelEff.add(getTxtEff(), gridBagConstraints16);
			panelEff.add(getBtnEff(), gridBagConstraints18);
		}
		return panelEff;
	}

	/**
	 * This method initializes jTextField
	 * @return javax.swing.JTextField
	 */
	private JTextField getTxtEff() {
		if (txtEff == null) {
			txtEff = new JTextField();
			txtEff.setFont(new Font("Dialog", Font.PLAIN, 12));
			txtEff.setPreferredSize(new java.awt.Dimension(108, 23));
		}
		return txtEff;
	}

	/**
	 * This method initializes jButton
	 * @return javax.swing.JButton
	 */
	private JButton getBtnEff() {
		if (btnEff == null) {
			btnEff = new JButton();
			btnEff.setText("准备修订");
			btnEff.setPreferredSize(new Dimension(83, 21));
			btnEff.setFont(new Font("宋体", Font.PLAIN, 12));
			btnEff.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int selectedRow = docTable.getSelectedRow();
					if (selectedRow < 0)
						return;

					// 对修订AO、FO，需要输入有效性
					HashMap doc = (HashMap) searchedDocuments.get(selectedRow);
					if (txtEff.getText().trim().equals("")) {
						String docType = (String) doc.get("DOC_TYPE");
						JOptionPane.showMessageDialog(
								((FrameSearchDocument) frame), "请输入要修订的"
										+ docType + "的新有效性值!", "提示",
								JOptionPane.INFORMATION_MESSAGE);
						return;
					}

					// 查询同一编号的有效性相关文档
					String number = (String) doc.get(NUMBER);
					String oid = (String) doc.get(OID);
					new SearchDocumentThread(number, oid).start();
				}
			});
		}
		return btnEff;
	}

	/**
	 * This method initializes jTextField
	 */
	private JTextField getTxtPartNumber() {
		if (txtPartNumber == null) {
			txtPartNumber = new JTextField();
			txtPartNumber.setFont(new Font("DialogInput", Font.PLAIN, 12));
			txtPartNumber.setPreferredSize(new Dimension(200, 23));
			txtPartNumber.setToolTipText("整件编号1;整件编号2;...，如：AL2.123.001;AL2.123.003");
		}
		return txtPartNumber;
	}

	private class MyDefaultCellRenderer extends DefaultTableCellRenderer {
		private final HashSet<String> endStateSet = new HashSet<String>(
				Arrays.asList(new String[] { "已批准", "已归档", "已发放", }));

		private final HashSet<String> workingStateSet = new HashSet<String>(
				Arrays.asList(new String[] { "正在工作", "修改", }));

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			DefaultTableCellRenderer c = (DefaultTableCellRenderer) super
					.getTableCellRendererComponent(table, value, isSelected,
							hasFocus, row, column);

			Color fg = isSelected ? table.getSelectionForeground() : table
					.getForeground();
			Color bg = isSelected ? table.getSelectionBackground() : table
					.getBackground();
			c.setBackground(bg);
			c.setForeground(fg);

			if (searchedDocuments == null || row >= searchedDocuments.size())
				return c;

			HashMap di = (HashMap) searchedDocuments.get(row);
			String state = (String) di.get(LIFECYCLE_STATUS);

			if (workingStateSet.contains(state)) {
				c.setBackground(isSelected ? Color.BLUE : Color.GREEN);
				c.setForeground(isSelected ? Color.WHITE : Color.BLACK);
			} else if (!endStateSet.contains(state)) {
				c.setBackground(isSelected ? Color.RED : Color.YELLOW);
				c.setForeground(Color.BLACK);
			}

			return c;
		}
	}

	/**
	 * This method initializes jCheckBox
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getChkLatestVersionOnly() {
		if (chkLatestVersionOnly == null) {
			chkLatestVersionOnly = new JCheckBox();
			chkLatestVersionOnly.setText("仅查询最新大版本");
			chkLatestVersionOnly.setSelected(true);
			chkLatestVersionOnly.setVisible(false);
			chkLatestVersionOnly.setFont(new Font("DialogInput", Font.PLAIN, 12));
		}
		return chkLatestVersionOnly;
	}

	/**
	 * This method initializes txtBaselineNumber
	 * @return javax.swing.JTextField
	 */
	private JTextField getTxtBaselineNumber() {
		if (txtBaselineNumber == null) {
			txtBaselineNumber = new JTextField();
			txtBaselineNumber.setFont(new Font("DialogInput", Font.PLAIN, 12));
			txtBaselineNumber.setPreferredSize(new Dimension(100, 23));
		}
		return txtBaselineNumber;
	}

	/**
	 * This method initializes chkSearchBaseline
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getChkSearchBaseline() {
		if (chkSearchBaseline == null) {
			chkSearchBaseline = new JCheckBox();
			chkSearchBaseline.setText("按基线查询整件工艺文档（填写基线编号和以分号分隔的整件编号列表）");
			chkSearchBaseline.setFont(new Font("DialogInput", Font.PLAIN, 12));
		}
		return chkSearchBaseline;
	}

	/**
	 * This method initializes panelBaseline
	 * @return javax.swing.JPanel
	 */
	private JPanel getPanelBaseline() {
		if (panelBaseline == null) {
			GridBagConstraints gridBagConstraints23 = new GridBagConstraints();
			gridBagConstraints23.fill = GridBagConstraints.BOTH;
			gridBagConstraints23.gridx = 2;
			gridBagConstraints23.gridy = 0;
			gridBagConstraints23.weightx = 1.0;
			gridBagConstraints23.insets = new Insets(4, 4, 0, 1);
			GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
			gridBagConstraints20.insets = new Insets(4, 4, 0, 0);
			gridBagConstraints20.gridy = 0;
			gridBagConstraints20.gridx = 1;
			GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
			gridBagConstraints19.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints19.gridx = 0;
			gridBagConstraints19.gridy = 0;
			gridBagConstraints19.weightx = 0.0;
			gridBagConstraints19.insets = new Insets(4, 0, 0, 0);
			panelBaseline = new JPanel();
			panelBaseline.setLayout(new GridBagLayout());
			panelBaseline
					.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			panelBaseline.add(getTxtBaselineNumber(), gridBagConstraints19);
			panelBaseline.add(jLabel2, gridBagConstraints20);
			panelBaseline.add(getTxtPartNumber(), gridBagConstraints23);
		}
		return panelBaseline;
	}

	/**
	 * class main method for test
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		test(args);
	}

	public static void test(String[] args) throws Exception {
		HashMap<String, Object> task = new HashMap<String, Object>();
		HashMap<String, Object> capp = new LinkedHashMap<String, Object>();
		task.put(CAPP, capp);
		task.put(TYPE, SEARCH_DOCUMENT);
//		task.put(TYPE, CHECKOUT_DOCUMENT);
//		task.put(TYPE, DELETE_DOCUMENT);
//		task.put(TYPE, REVISE_DOCUMENT);
//		task.put(TYPE, PRE_REVISE_DOCUMENT);
		capp.put(SERVER_URL, "http://pdm.fastgroup.cn/Windchill/");
		capp.put(WINDOW_CAPTION, "查找工艺文档");
//		capp.put(DOC_TYPE, "*");
		capp.put(IBA_LIST, "*");
//		capp.put(IBA_LIST, "htDocumentSubClass");
		capp.put(DOWNLOAD, FALSE);

		Worker worker = new Worker("wcadmin", "winadmin");
		HashMap<String, Object> result = worker.performTask(task);
		worker.saveResult(System.out, result);
		System.exit(0);
	}
}