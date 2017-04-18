package email_app;

/**
 * Created by Neak on 05.04.2017.
 */
public class PropertyPath {

    public static String getPath() {
        if (PropertyPath.isUnix()) {
            return "build/resources/main/config.properties";
        } else {
            return "build\\resources\\main\\config.properties";
        }
    }

    private static boolean isUnix() {
        String os = System.getProperty("os.name");
        return (os.startsWith("Linux") || os.startsWith("Mac"));
    }
}

