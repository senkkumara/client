/**
 * Client error log support
 */
package ext.fast.capp.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created on 2005-7-18
 * @author liuld
 */
public class Logger extends PrintStream {
    private static OutputStream os = null;
    private static Logger logger = null;
    
    static {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMM");
        String fileName = Settings.getHomePath() + df.format(new Date()) + ".log";
        File file = new File(fileName);
        
        try {
            os = new FileOutputStream(file, true);
            //System.setOut(new PrintStream(os));
            //System.setErr(new PrintStream(os));
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public Logger() {
        super(os, true);
    }
    
    public static synchronized Logger getLogger() {
        if (logger == null)
            logger = new Logger();
        return logger;
    }
    
    public static String getTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        return sdf.format(new Date());
    }

    public static void log(Object o) {
        StackTraceElement ste = new Throwable().fillInStackTrace().getStackTrace()[1];
        String s = getTime() + " " + ste.getFileName() + "." + ste.getLineNumber() + ": " + o;
        getLogger().println(s);
        getLogger().flush();
    }
    
    public static void log(Throwable t) {
        StackTraceElement ste = new Throwable().fillInStackTrace().getStackTrace()[1];
        String s = getTime() + " " + ste.getFileName() + "." + ste.getLineNumber() + ": ";

        Logger logger = getLogger();
        if (t != null) {
            logger.println(s + t.getClass().getName());
            t.printStackTrace(logger);
        }
        else {
            logger.println(s + "Throwable = null");
        }
        
        logger.flush();
    }
}
