/**
 * Created on 2004-10-22
 * @author lld
 */
package ext.fast.capp.client;

import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Window;
import java.io.PrintStream;

public class ScreenUtil {
    private static PrintStream cout = System.out;
    private static boolean __DEBUGGING = false;

    public static void centerWindow(Window win) {
        Point p = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();

        int xPos = p.x - win.getWidth() / 2;
        int yPos = p.y - win.getHeight() / 2;

        if (__DEBUGGING) {
            cout.println("center.x: " + p.x);
            cout.println("center.y: " + p.y);
            cout.println("win.x: " + win.getX());
            cout.println("win.y: " + win.getY());
            cout.println("win.width: " + win.getWidth());
            cout.println("win.height: " + win.getHeight());
            cout.println("---------------------------");
            cout.println("win.x: " + xPos);
            cout.println("win.y: " + yPos);
        }

        win.setLocation(xPos, yPos);
    }

}
