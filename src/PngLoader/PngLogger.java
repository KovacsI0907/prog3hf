package PngLoader;

import java.util.logging.Logger;

public final class PngLogger {
    static final private boolean showDebug = true;
    static final private boolean showInfo = false;

    private static final Logger builtInLogger = Logger.getLogger("Global Logger");

    public static void info(String msg){
        if(showInfo)
            System.out.println("INFO: " + msg);
    }

    public static void debug(String msg){
        if(showDebug)
            System.out.println("DEBUG: " + msg);
    }
}
