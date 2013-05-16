/**
 * 
 */
package ext.fast.capp.client;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Created on 2005-7-24
 * @author liuld
 */
public class FrameSaveMarkup extends JDialog implements CAPPConstants,
        ActionListener, ServerHelper.HTTPCallback {
    private FrameSaveMarkup frame = this;
    private HashMap task = null;
    private HashMap webInfo = null;
    private HashMap message = null;
    private ArrayList fileList = null;
    
    private JPanel jContentPane = null;
    private JLabel jLabel = null;
    private JTextField txtName = null;
    private JLabel jLabel1 = null;
    private JTextArea txtDescription = null;
    private JPanel jPanel = null;
    private JScrollPane jScrollPane = null;
    private JButton btnOk = null;
    private JButton btnCancel = null;
    private JPanel jPanel1 = null;
    private JProgressBar progressBar = null;
    /**
     * This is the default constructor
     */
    public FrameSaveMarkup() {
        super();
        initialize();
    }
    
    public static HashMap saveMarkup(HashMap task, HashMap webInfo) 
    throws Exception {
        return new FrameSaveMarkup()._saveMarkup(task, webInfo);
    }
    
    private HashMap _saveMarkup(HashMap task, HashMap webInfo) throws Exception {
        this.task = task;
        this.webInfo = webInfo;
        
        // 检查调用参数
        HashMap capp = (HashMap) task.get(CAPP);
        String primary = (String) capp.get(PRIMARY_FILE);
        if (primary == null || primary.equals(""))
            throw new Exception("请指定批注文件!");
        File primaryFile = new File(primary);
        if (!primaryFile.exists())
            throw new Exception("指定批注文件不存在: " + primary);
        
        String docNumber = (String) capp.get(DOC_NUMBER);
        if (docNumber == null || docNumber.equals(""))
            throw new Exception("请指定文档编号!");
        
        // 获取批注文件列表
        fileList = Worker.getFileList(task);
        if (fileList == null || fileList.size() < 1 || fileList.get(0) == null)
            throw new Exception("未指定批注文件!");

        // 准备窗口
        setTitle("保存工艺文档批注");
        progressBar.setVisible(false);
        btnOk.addActionListener(this);
        btnCancel.addActionListener(this);
        ScreenUtil.centerWindow(this);
        
        if (capp.get(NAME) != null)
            txtName.setText((String) capp.get(NAME));
        if (capp.get(DESCRIPTION) != null)
            txtDescription.setText((String) capp.get(DESCRIPTION));

        // 显示UI
        setModal(true);
        show();
        dispose();
        
        // 如果未生成message, 表示用户取消操作
        if (message == null)
            return null;
        task.put(MESSAGE, message);
        
        return task;
    }
    
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == btnOk) {
            // 允许采用用户全名为默认名称
            //String name = txtName.getText();
            //if (name.trim().equals("")) {
            //    JOptionPane.showMessageDialog(this, "请输入批注名称!");
            //    return;
            //}
            
            new SaveMarkupThread().start();
        }
        else if (ae.getSource() == btnCancel) {
            message = null;
            setVisible(false);
        }
    }
    
    public void setProgress(long total, long progress) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                long _total;
                long _progress;
                public void run() {
                    while (_total > Integer.MAX_VALUE) {
                        _total /= 10;
                        _progress /=10;
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
        }
        catch (Exception e) {
            Logger.log(e);
            e.printStackTrace();
        }
    }
    
    private void threadStart() throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                progressBar.setVisible(true);

                txtName.setEnabled(false);
                txtDescription.setEnabled(false);
                btnOk.setEnabled(false);
            }
        });
    }
    
    private void threadEnd() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                txtName.setEnabled(true);
                txtDescription.setEnabled(true);
                btnOk.setEnabled(true);
             
                progressBar.setVisible(false);
                setCursor(Cursor.getDefaultCursor());
            }
        });
    }
    
    private void threadError(final String title, final Exception e) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, e.getMessage(), title,
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    /**
     * 保存批注线程
     * 
     * Created on 2005-8-5
     * @author liuld
     */
    class SaveMarkupThread extends Thread {
        public void run() {
            try {
                threadStart();
                
                HashMap task1 = (HashMap) DataPacker.clone(task);
                HashMap capp = (HashMap) task1.get(CAPP);
                capp.put(NAME, txtName.getText());
                capp.put(DESCRIPTION, txtDescription.getText());
                ArrayList files = (ArrayList) DataPacker.clone(fileList);
                
                HashMap result = ServerHelper.callServer(
                        (String) webInfo.get(USER), (String) webInfo.get(PASS),
                        (String) webInfo.get(SERVER_URL), task1, files, frame
                        ); 
                
                if (result == null)
                    throw new Exception("保存批注时发生未知异常!");
                
                message = (HashMap) result.get(MESSAGE);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        setVisible(false);
                    }
                });
            }
            catch (Exception e) {
                Logger.log(e);
                threadError("保存批注时发生错误", e);
            }
            finally {
                threadEnd();
            }
        }
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(387, 262);
        this.setContentPane(getJContentPane());
        this.setTitle("JFrame");
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
            gridBagConstraints21.gridx = 0;
            gridBagConstraints21.gridwidth = 2;
            gridBagConstraints21.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints21.gridy = 3;
            GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
            gridBagConstraints5.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints5.weighty = 1.0;
            gridBagConstraints5.gridx = 1;
            gridBagConstraints5.gridy = 1;
            gridBagConstraints5.insets = new java.awt.Insets(4,0,0,50);
            gridBagConstraints5.weightx = 1.0;
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.gridx = 0;
            gridBagConstraints4.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints4.gridwidth = 2;
            gridBagConstraints4.gridy = 2;
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridx = 0;
            gridBagConstraints2.insets = new java.awt.Insets(4,50,0,0);
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHEAST;
            gridBagConstraints2.gridy = 1;
            jLabel1 = new JLabel();
            jLabel1.setText("说明: ");
            jLabel1.setFont(new java.awt.Font("DialogInput", java.awt.Font.PLAIN, 12));
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.gridy = 0;
            gridBagConstraints1.weightx = 1.0;
            gridBagConstraints1.insets = new java.awt.Insets(50,0,0,50);
            gridBagConstraints1.gridx = 1;
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.insets = new java.awt.Insets(50,50,0,0);
            gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints.gridy = 0;
            jLabel = new JLabel();
            jLabel.setText("名称: ");
            jLabel.setFont(new java.awt.Font("DialogInput", java.awt.Font.PLAIN, 12));
            jContentPane = new JPanel();
            jContentPane.setLayout(new GridBagLayout());
            jContentPane.setFont(new java.awt.Font("DialogInput", java.awt.Font.PLAIN, 12));
            jContentPane.add(jLabel, gridBagConstraints);
            jContentPane.add(jLabel1, gridBagConstraints2);
            jContentPane.add(getTxtName(), gridBagConstraints1);
            jContentPane.add(getJScrollPane(), gridBagConstraints5);
            jContentPane.add(getJPanel(), gridBagConstraints4);
            jContentPane.add(getJPanel1(), gridBagConstraints21);
        }
        return jContentPane;
    }

    /**
     * This method initializes jTextField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getTxtName() {
        if (txtName == null) {
            txtName = new JTextField();
            txtName.setFont(new java.awt.Font("DialogInput", java.awt.Font.PLAIN, 12));
        }
        return txtName;
    }

    /**
     * This method initializes jTextArea	
     * 	
     * @return javax.swing.JTextArea	
     */
    private JTextArea getTxtDescription() {
        if (txtDescription == null) {
            txtDescription = new JTextArea();
            txtDescription.setFont(new java.awt.Font("DialogInput", java.awt.Font.PLAIN, 12));
        }
        return txtDescription;
    }

    /**
     * This method initializes jPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setVgap(20);
            flowLayout.setHgap(20);
            jPanel = new JPanel();
            jPanel.setLayout(flowLayout);
            jPanel.setPreferredSize(new java.awt.Dimension(10,60));
            jPanel.add(getBtnOk(), null);
            jPanel.add(getBtnCancel(), null);
        }
        return jPanel;
    }

    /**
     * This method initializes jScrollPane	
     * 	
     * @return javax.swing.JScrollPane	
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getTxtDescription());
        }
        return jScrollPane;
    }

    /**
     * This method initializes jButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBtnOk() {
        if (btnOk == null) {
            btnOk = new JButton();
            btnOk.setText("确定");
            btnOk.setFont(new java.awt.Font("DialogInput", java.awt.Font.PLAIN, 12));
        }
        return btnOk;
    }

    /**
     * This method initializes jButton1	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBtnCancel() {
        if (btnCancel == null) {
            btnCancel = new JButton();
            btnCancel.setText("取消");
            btnCancel.setFont(new java.awt.Font("DialogInput", java.awt.Font.PLAIN, 12));
        }
        return btnCancel;
    }

    /**
     * This method initializes jPanel1	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getJPanel1() {
        if (jPanel1 == null) {
            jPanel1 = new JPanel();
            jPanel1.setLayout(new BoxLayout(getJPanel1(), BoxLayout.X_AXIS));
            jPanel1.setPreferredSize(new java.awt.Dimension(10,20));
            jPanel1.add(getProgressBar(), null);
        }
        return jPanel1;
    }

    /**
     * This method initializes progressBar	
     * 	
     * @return javax.swing.JProgressBar	
     */
    private JProgressBar getProgressBar() {
        if (progressBar == null) {
            progressBar = new JProgressBar();
        }
        return progressBar;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
