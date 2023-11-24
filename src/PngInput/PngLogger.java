package PngInput;

public final class PngLogger {
    static final private boolean showDebug = false;
    static final private boolean showInfo = false;

    public static void info(String msg){
        if(showInfo)
            System.out.println("INFO: " + msg);
    }

    public static void debug(String msg){
        if(showDebug)
            System.out.println("DEBUG: " + msg);
    }
}
