package gui;

import java.io.*;

public class UserPreferences implements Serializable {
    public final int threadCount;
    public final int memorySize;

    private static UserPreferences instance = null;


    public UserPreferences(int threadCount, int memorySize) {
        this.threadCount = threadCount;
        this.memorySize = memorySize;
    }

    public static UserPreferences loadDefaults() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream("defaultPrefs.ser");
        ObjectInputStream ois = new ObjectInputStream(fis);
        UserPreferences loaded = (UserPreferences)ois.readObject();
        ois.close();
        return loaded;
    }

    public static UserPreferences loadUserPrefs() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream("userPrefs.ser");
        ObjectInputStream ois = new ObjectInputStream(fis);
        UserPreferences loaded = (UserPreferences)ois.readObject();
        ois.close();
        return loaded;
    }

    public static void savePreferences(UserPreferences prefs) throws IOException {
        FileOutputStream fos = new FileOutputStream("userPrefs.ser");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(prefs);
        oos.close();
    }

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

    synchronized public static void setInstance(UserPreferences instance) {
        UserPreferences.instance = instance;
    }

}
