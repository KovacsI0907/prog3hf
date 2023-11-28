package gui;

import java.io.*;

/**
 * Szerializálható preferencia osztály, ami menti a falhasználó által kiválasztott konfigurációt
 */
public class UserPreferences implements Serializable {
    public final int threadCount;
    public final int memorySize;

    private static UserPreferences instance = null;


    public UserPreferences(int threadCount, int memorySize) {
        this.threadCount = threadCount;
        this.memorySize = memorySize;
    }

    /**
     * Betölti az alap konfigot, ha megtalálja a fájlt
     * @return
     * @throws IOException Ha nem létezik a fájl
     * @throws ClassNotFoundException Ha sérült a fájl
     */
    public static UserPreferences loadDefaults() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream("defaultPrefs.ser");
        ObjectInputStream ois = new ObjectInputStream(fis);
        UserPreferences loaded = (UserPreferences)ois.readObject();
        ois.close();
        return loaded;
    }

    /**
     * Betölti a felhasználói konfigot, ha megtalálja a fájlt
     * @return
     * @throws IOException Ha nem létezik a fájl
     * @throws ClassNotFoundException Ha sérült a fájl
     */
    public static UserPreferences loadUserPrefs() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream("userPrefs.ser");
        ObjectInputStream ois = new ObjectInputStream(fis);
        UserPreferences loaded = (UserPreferences)ois.readObject();
        ois.close();
        return loaded;
    }

    /**
     * Menti az adott konfigot felhasználói konfigként
     * @param prefs A konfiguráció
     * @throws IOException Ha nem sikerül szerializálni
     */
    public static void savePreferences(UserPreferences prefs) throws IOException {
        FileOutputStream fos = new FileOutputStream("userPrefs.ser");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(prefs);
        oos.close();
    }

    /**
     * Visszaadja a jelelegi konfigot, ha ez null, akkor először
     * megpróbálja betölteni a felhasználói konfigot,
     * ha ez nem létezik, akkor próbálkozik az alap konfiggal,
     * ha ez sem létezik akkor csinál egy új objektumot
     * @return Az aktuális konfig
     */
    synchronized public static UserPreferences getInstance() {
        if(instance == null){
            try{
                instance = loadUserPrefs();
            } catch (Exception e){
                try{
                    instance = loadDefaults();
                } catch (IOException ex) {
                    //return default if all fails
                    return new UserPreferences(4, 1024);
                } catch (ClassNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        return instance;
    }

    /**
     * Beállítja az aktuális konfigot
     * @param instance Az új konfig
     */
    synchronized public static void setInstance(UserPreferences instance) {
        UserPreferences.instance = instance;
    }

}
