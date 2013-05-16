/**
 * 创建日期 2004-10-18
 * @author lld
 *
 * 客户端软件配置参数
 */
package ext.fast.capp.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class Settings {
    public static final String SAVE_PATH = "SavePath";
    public static final String DEFAULT_WINDCHILL_URL = "http://localhost/Windchill/";

    private static HashMap<String, Object> variables = null;

    static {
        // force the application home directory to be existing
        new File(getHomePath()).mkdir();
    }

    public static String getSavePath() {
        HashMap<String, Object> vars = getVariables();
        String result = (String) vars.get(SAVE_PATH);
        
        if (result == null) {
            result = System.getProperty("java.io.tmpdir");
            if (result == null)
                result = getHomePath();
        }
        if (!result.endsWith(File.separator))
            result += "/";

        return result;
    }

    public static void setSavePath(String savePath) {
        if (!savePath.endsWith(File.separator))
            savePath += "/";
        HashMap<String, Object> vars = getVariables();
        vars.put(SAVE_PATH, savePath);
        saveVariables();
    }

    public static String getHomePath() {
        String userHome = System.getProperty("user.home");
        return userHome + "/.wc_capp/";
    }

    @SuppressWarnings("unchecked")
	public synchronized static HashMap<String, Object> getVariables() {
        if (variables == null) {
            File f = new File(getHomePath() + "capp14i.ser");
            if (f.exists()) {
                ObjectInputStream ois = null;
                try {
                    ois = new ObjectInputStream(new FileInputStream(f));
                    variables = (HashMap<String, Object>) ois.readObject();
                }
                catch (Exception e) {
                    Logger.log(e);
                }
                finally {
                    try {
                        if (ois != null)
                            ois.close();
                    }
                    catch (Exception e) {}
                }
            }
            if (variables == null)
                variables = new HashMap<String, Object>(); 
        }
        return variables;
    }

    public synchronized static void saveVariables() {
        File f = new File(getHomePath() + "capp14i.ser");
        ObjectOutputStream oos = null;

        try {
            oos = new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject(getVariables());
        } catch (Exception e) {
            Logger.log(e);
        } finally {
            try {
                if (oos != null)
                    oos.close();
            }
            catch (Exception e) {}
        }
    }
}