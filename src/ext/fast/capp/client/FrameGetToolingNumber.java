package ext.fast.capp.client;

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ext.fast.util.Tools;

public class FrameGetToolingNumber extends JDialog implements CAPPConstants {
    private HashMap task = null;
    private HashMap webInfo = null;
    private HashMap result = null;
    
    private JPanel jContentPane = null;
    private JLabel jLabel = null;
    private JComboBox comboCode = null;
    private JLabel jLabel1 = null;
    private JLabel jLabel2 = null;
    private JLabel jLabel3 = null;
    private JTextField txtClass = null;
    private JPanel jPanel = null;
    private JPanel jPanel1 = null;
    private JButton btnOk = null;
    private JButton btnCancel = null;
    private JLabel jLabel4 = null;
    private JLabel jLabel5 = null;
    private JTextField txtTarget = null;
    
    public static HashMap getToolingNumber(HashMap task, HashMap webInfo) {
        return new FrameGetToolingNumber()._getToolingNumber(task, webInfo);
    }
    
    private Window getWindow() {
        return this;
    }
    
    private HashMap _getToolingNumber(HashMap task, HashMap webInfo) {
        this.task = task;
        this.webInfo = webInfo;
        
        setTitle("获取工装申请单编号");
        
        comboCode.removeAllItems();
        comboCode.addItem("AL");
        comboCode.addItem("T");
        comboCode.addItem("K");
        
        txtClass.setText("");

        ScreenUtil.centerWindow(this);
        setModal(true);
        show();
        dispose();

        HashMap vars = Settings.getVariables();
        vars.put(CHECKIN_DOCUMENT + "BOUNDS", getBounds());
        Settings.saveVariables();
        
        // 判断用户是否选择取消
        if (result == null)
            return null;

        // 组装返回数据并返回
        task.put(TOOLING, result.get(TOOLING));
        task.put(MESSAGE, result.get(MESSAGE));
        return task;
    }
    
    private class CallAdapter extends ServerHelper.HTTPCallAdapter {
        public void onInit() {
            getWindow().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            comboCode.setEnabled(false);
            txtClass.setEnabled(false);
            btnOk.setEnabled(false);
            btnCancel.setEnabled(false);
            result = null;
        }
        
        public void onComplete(HashMap _result) {
            result = _result;
            getWindow().setVisible(false);
        }
        
        public void onError(Exception e) {
            String err = Tools.getErrorMessage(e);
            JOptionPane.showMessageDialog(getWindow(), err);
        }
        
        public void onEnd(HashMap _result) {
            comboCode.setEnabled(true);
            txtClass.setEnabled(true);
            btnOk.setEnabled(true);
            btnCancel.setEnabled(true);
            getWindow().setCursor(Cursor.getDefaultCursor());
        }
    }
    
    /**
     * This method initializes 
     */
    public FrameGetToolingNumber() {
    	super();
    	initialize();
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        this.setSize(new java.awt.Dimension(390,254));
        this.setContentPane(getJContentPane());
    }

    /**
     * This method initializes jContentPane	
     * @return javax.swing.JPanel	
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            gridBagConstraints11.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints11.gridy = 3;
            gridBagConstraints11.weightx = 1.0;
            gridBagConstraints11.insets = new java.awt.Insets(8,0,0,0);
            gridBagConstraints11.gridx = 2;
            GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
            gridBagConstraints12.gridx = 3;
            gridBagConstraints12.anchor = java.awt.GridBagConstraints.SOUTHWEST;
            gridBagConstraints12.gridy = 2;
            jLabel5 = new JLabel();
            jLabel5.setText("(三位数字)");
            jLabel5.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
            GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
            gridBagConstraints10.gridx = 1;
            gridBagConstraints10.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints10.insets = new java.awt.Insets(8,0,0,0);
            gridBagConstraints10.gridy = 3;
            jLabel4 = new JLabel();
            jLabel4.setText("目标编号：");
            jLabel4.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
            GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
            gridBagConstraints7.gridx = 0;
            gridBagConstraints7.gridwidth = 4;
            gridBagConstraints7.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints7.weighty = 1.0;
            gridBagConstraints7.gridy = 0;
            GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
            gridBagConstraints6.gridx = 0;
            gridBagConstraints6.gridwidth = 4;
            gridBagConstraints6.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints6.weighty = 2.0;
            gridBagConstraints6.anchor = java.awt.GridBagConstraints.SOUTH;
            gridBagConstraints6.gridy = 4;
            GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
            gridBagConstraints5.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints5.gridy = 2;
            gridBagConstraints5.weightx = 1.0;
            gridBagConstraints5.insets = new java.awt.Insets(8,0,0,0);
            gridBagConstraints5.gridx = 2;
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.gridx = 1;
            gridBagConstraints4.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints4.insets = new java.awt.Insets(8,0,0,0);
            gridBagConstraints4.gridy = 2;
            jLabel3 = new JLabel();
            jLabel3.setText("分类编号：");
            jLabel3.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.gridx = 3;
            gridBagConstraints3.gridwidth = 1;
            gridBagConstraints3.weightx = 0.5;
            gridBagConstraints3.gridy = 1;
            jLabel2 = new JLabel();
            jLabel2.setText("　");
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridx = 0;
            gridBagConstraints2.weightx = 1.0;
            gridBagConstraints2.gridy = 1;
            jLabel1 = new JLabel();
            jLabel1.setText("　");
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.gridy = 1;
            gridBagConstraints1.weightx = 2.0;
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.CENTER;
            gridBagConstraints1.gridx = 2;
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.weightx = 0.0;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints.gridy = 1;
            jLabel = new JLabel();
            jLabel.setText("工装代号：");
            jLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
            jContentPane = new JPanel();
            jContentPane.setLayout(new GridBagLayout());
            jContentPane.add(jLabel, gridBagConstraints);
            jContentPane.add(getJComboBox(), gridBagConstraints1);
            jContentPane.add(jLabel1, gridBagConstraints2);
            jContentPane.add(jLabel2, gridBagConstraints3);
            jContentPane.add(jLabel3, gridBagConstraints4);
            jContentPane.add(getJTextField(), gridBagConstraints5);
            jContentPane.add(getJPanel(), gridBagConstraints6);
            jContentPane.add(getJPanel1(), gridBagConstraints7);
            jContentPane.add(jLabel4, gridBagConstraints10);
            jContentPane.add(jLabel5, gridBagConstraints12);
            jContentPane.add(getJTextField2(), gridBagConstraints11);
        }
        return jContentPane;
    }

    /**
     * This method initializes jComboBox	
     * 	
     * @return javax.swing.JComboBox	
     */
    private JComboBox getJComboBox() {
        if (comboCode == null) {
            comboCode = new JComboBox();
            comboCode.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
            comboCode.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    String code = String.valueOf(comboCode.getSelectedItem());
                    String number = txtClass.getText();
                    if (number.length() == 3)
                        txtTarget.setText(code + number + "." + "????");
                }
            });
        }
        return comboCode;
    }

    /**
     * This method initializes jTextField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getJTextField() {
        if (txtClass == null) {
            txtClass = new JTextField();
            txtClass.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
            txtClass.setPreferredSize(new java.awt.Dimension(6,22));
            txtClass.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyReleased(java.awt.event.KeyEvent e) {
                    String code = String.valueOf(comboCode.getSelectedItem());
                    String number = txtClass.getText();
                    if (number.length() == 3)
                        txtTarget.setText(code + number + "." + "????");
                    else
                        txtTarget.setText(" ");
                }
            });
        }
        return txtClass;
    }

    /**
     * This method initializes jPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
            gridBagConstraints9.gridx = 1;
            gridBagConstraints9.insets = new java.awt.Insets(0,8,0,0);
            gridBagConstraints9.gridy = 0;
            GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
            gridBagConstraints8.gridx = 0;
            gridBagConstraints8.insets = new java.awt.Insets(0,0,0,8);
            gridBagConstraints8.gridy = 0;
            jPanel = new JPanel();
            jPanel.setLayout(new GridBagLayout());
            jPanel.add(getBtnOk(), gridBagConstraints8);
            jPanel.add(getBtnCancel(), gridBagConstraints9);
        }
        return jPanel;
    }

    /**
     * This method initializes jPanel1	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getJPanel1() {
        if (jPanel1 == null) {
            jPanel1 = new JPanel();
        }
        return jPanel1;
    }

    /**
     * This method initializes jButton	
     * 	
     * @return javax.swing.JButton	
     */
    private JButton getBtnOk() {
        if (btnOk == null) {
            btnOk = new JButton();
            btnOk.setText("确定(O)");
            btnOk.setMnemonic(java.awt.event.KeyEvent.VK_O);
            btnOk.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
            btnOk.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    int index = comboCode.getSelectedIndex();
                    String err = null;
                    if (index < 0) 
                        err = "请选择一个工装代号！";
                    else if (txtClass.getText().length() != 3)
                        err = "请输入三位工装分类代号！";
                    else {
                        char[] num = txtClass.getText().toCharArray();
                        if (num[0] < '0' || num[1] < '0' || num[2] < '0' ||
                                num[0] > '9' || num[1] > '9' || num[2] > '9')
                            err = "工装分类代号必须是三位数字！";
                    }
                    
                    if (err != null) {
                        JOptionPane.showMessageDialog(null, err);
                        return;
                    }
                        
                    HashMap task0 = null;
                    try {
                        task0 = (HashMap) DataPacker.clone(task);
                    }
                    catch (Exception ex) {
                        JOptionPane.showMessageDialog(getWindow(), Tools.getErrorMessage(ex));
                        return;
                    }
                    String classNumber = String.valueOf(comboCode.getSelectedItem())
                            + txtClass.getText() + ".";
                    HashMap capp = (HashMap) task0.get(CAPP);
                    capp.put(CLASS_NUMBER, classNumber);

                    ServerHelper.callServer("正在执行工装申请单取号...",
                            webInfo, task0, null, new CallAdapter());
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
    private JButton getBtnCancel() {
        if (btnCancel == null) {
            btnCancel = new JButton();
            btnCancel.setText("取消(C)");
            btnCancel.setMnemonic(java.awt.event.KeyEvent.VK_C);
            btnCancel.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 12));
            btnCancel.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    result = null;
                    getWindow().setVisible(false);
                }
            });
        }
        return btnCancel;
    }

    /**
     * This method initializes jTextField	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getJTextField2() {
        if (txtTarget == null) {
            txtTarget = new JTextField();
            txtTarget.setEditable(false);
        }
        return txtTarget;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
