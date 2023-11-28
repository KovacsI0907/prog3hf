package PngInput;

/**
 * Segédosztály debug és info logoláshoz
 */
public final class PngLogger {
    /**
     * Ez állytja, hogy a debug üzenetek megjelenjenek-e
     */
    static final private boolean showDebug = false;
    /**
     * Ez állytja, hogy az info üzenetek megjelenjenek-e
     */
    static final private boolean showInfo = false;


    /**
     * Logolás info üzenetként
     * @param msg Az üzenet
     */
    public static void info(String msg){
        if(showInfo)
            System.out.println("INFO: " + msg);
    }

    /**
     * Loglás debughoz
     * @param msg Az üzenet
     */
    public static void debug(String msg){
        if(showDebug)
            System.out.println("DEBUG: " + msg);
    }
}
