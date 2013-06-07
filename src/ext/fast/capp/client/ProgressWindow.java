/**
 * Created on 2006-1-1
 * @author liuld
 */
package ext.fast.capp.client;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.Point;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/**
 * Created on 2006-1-1
 * @author liuld
 * 进度显示窗口
 */
public class ProgressWindow implements Runnable {
    private JDialog window;
    private JProgressBar progressBar;
    private Thread worker;
    private Object lock = new Object();
    private long total = 100;
    private long progress = 0;
    private boolean working = true;

    /**
     * constructor
     * @param title 进度窗口标题
     */
    public ProgressWindow(String title) {
        JDialog frame = new JDialog();
        frame.setTitle(title == null ? "正在执行......" : title);
        frame.setSize(500, 80);
        Point p = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();

        int xPos = p.x - frame.getWidth() / 2;
        int yPos = p.y - frame.getHeight() / 2;

        frame.setLocation(xPos, yPos);
        frame.setResizable(false);

        JPanel cont = new JPanel();
        frame.setContentPane(cont);

        cont.setBorder(javax.swing.BorderFactory.createEmptyBorder(15,15,15,15));
        cont.setLayout(new BorderLayout());
        progressBar = new JProgressBar();
        progressBar.setMaximumSize(new Dimension(0, 20));
        progressBar.setPreferredSize(new Dimension(0, 20));
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setValue(0);
        cont.add(progressBar, BorderLayout.CENTER);

        window = frame;
        window.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        worker = new Thread(this);
    }

    /**
     * 显示进度窗口，modal显示
     */
    public void show() {
        if (EventQueue.isDispatchThread()) {
            window.setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
            worker.start();
            window.setVisible(true);
            window.setAlwaysOnTop(true);
            window.dispose();
            return;
        }

        window.setModal(false);
        worker.start();
        window.setVisible(true);
        while (isWorking()) {
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
        window.setVisible(false);
        window.dispose();
    }
    
    /**
     * 进度更新线程工作函数
     */
    public void run() {
        Runnable updater = new Runnable() {
            public void run() {
                if (window == null) 
                    return;
                
                long total1;
                long progress1;
                synchronized(lock) {
                    total1 = total;
                    progress1 = progress;
                }
                
                if (progressBar != null) {
                    while (total1 > Integer.MAX_VALUE || progress1 > Integer.MAX_VALUE) {
                        total1 /= 10;
                        progress1 /= 10;
                    }
                    if (total1 <= 0)
                        total1 = 100;
                    
                    progressBar.setMinimum(0);
                    progressBar.setMaximum((int) total1);
                    progressBar.setValue((int) progress1);
                }
            }
        };
        
        Thread th = Thread.currentThread();
        while (!window.isVisible() && !th.isInterrupted()) {
            synchronized(lock) {
                if (!working)
                    break;
            }
            
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
                // sleep interrupted
                break;
            }
        }
        Thread.interrupted();
        
        while (true) {
            if (!window.isVisible() || th.isInterrupted())
                break;
            
            synchronized(lock) {
                if (!working)
                    break;
            }
            
            try {
                SwingUtilities.invokeLater(updater);
                Thread.sleep(100);
            }
            //catch (InvocationTargetException e) {
            //    // invokeAndWait exception
            //    e.printStackTrace();
            //}
            catch (InterruptedException e) {
                // sleep interrupted
                break;
            }
        }
        Thread.interrupted();

        synchronized(lock) {
            working = false;
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                window.setVisible(false);
            }
        });
    }
    
    /**
     * 进度窗口是否正在工作
     * @return
     */
    public boolean isWorking() {
        synchronized(lock) {
            return working;
        }
    }
    
    /**
     * 设定进度值
     * @param total     总值
     * @param progress  进度
     */
    public void setProgress(long total, long progress) {
        //if (window != null) {
        //    if (window.isVisible())
        //        window.toFront();
        //}

        synchronized(lock) {
            this.total = total;
            this.progress = progress;
        }
    }
    
    /**
     * 关闭进度窗口
     */
    public void close() {
        synchronized(lock) {
            working = false;
        }
    }
}
