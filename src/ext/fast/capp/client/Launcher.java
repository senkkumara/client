/**
 * Windchill CAPP客户端启动类
 */
package ext.fast.capp.client;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * Created on 2004-10-4
 * @author lld
 */
public class Launcher {
    static boolean testing = false;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            Logger.log(e);
            e.printStackTrace();
        }

        int errStat = launch(args);
        if (!testing)
            System.exit(errStat);
    }

	public static int launch(String[] args) {
		int argc = args.length;
		String user = "";
		String pass = "";

		String javaVer = System.getProperty("java.version", "");
		if (javaVer.compareTo("1.4.2") < 0) {
			JOptionPane.showMessageDialog(null, "系统Java版本太低: " + javaVer
					+ ", 系统至少需要1.4.2版");
			System.exit(1);
		}

		// 测试
		if (argc > 0 && args[0].startsWith("--test")) {
			try {
				user = argc >= 2 ? args[1] : "wcadmin";
				pass = argc >= 3 ? args[2] : "winadmin";
				TestApp.test(user, pass);
				testing = true;
				return 0;
			} catch (Exception e) {
				Logger.log(e);
				e.printStackTrace();
				return 2;
			}
		}

		// 历史数据导入
		if (argc > 0 && args[0].startsWith("--load")) {
			testing = true;
			Loader.doWork();
			return 0;
		}

		if (argc > 0 && args[0].startsWith("--report")) {
			testing = true;
			Reporter.doWork();
			return 0;
		}

		if (argc != 3) {
			String message = "\nUsage:\n\n"
					+ "java ext.fast.capp.client.Launcher <user> <pass> <path_to.xml>\n"
					+ "\njava ext.fast.capp.client.Launcher --test [<user> [<pass>]]\n";
			System.out.println(message);
			JOptionPane.showMessageDialog(null, message, "使用方法", JOptionPane.INFORMATION_MESSAGE);
			return 1;
		}

		// 正常运行
		else {
			user = args[0];
			pass = args[1];
			String path = args[2];

			Worker worker = new Worker(user, pass);
			return worker.performTask(path);
		}
	}
}
