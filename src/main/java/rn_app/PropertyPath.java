package rn_app;

/**
 * Created by Neak on 05.04.2017.
 */
public class PropertyPath {

    public static String getPath() {
        if (PropertyPath.isUnix()) {
            return "./resources/config/config.properties";
        } else {
            return ".\\resources\\config\\config.properties";
        }
    }

    private static boolean isUnix() {
        String os = System.getProperty("os.name");
        return (os.startsWith("Linux") || os.startsWith("Mac"));
    }
}

