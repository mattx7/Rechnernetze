package email_app;


import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Properties;

/**
 * @see <a href="http://crunchify.com/java-properties-file-how-to-read-config-properties-values-in-java/">crunchify.com</a>
 */
public class PropertyConfig {
    private static final Logger LOG = Logger.getLogger(PropertyConfig.class);

    String result = "";
    InputStreamReader inputStream;
    String hostName;
    String port;
    String user;
    String password;
    String mailAddress;
    String subject;
    String content;

    public PropertyConfig() throws IOException {
        getValues();
    }

    // Read properties from input stream
    // Exception when path incorrect/ properties not existent
    public String getValues() throws IOException {
        try {
            Properties prop = new Properties();
            String propFileName = "config.properties";

            inputStream = new InputStreamReader(new FileInputStream(PropertyPath.getPath()), "UTF-8");

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }

            Date time = new Date(System.currentTimeMillis());

            // Get property value and print it out
            mailAddress = prop.getProperty("mailAddress");
            user = prop.getProperty("user");
            password = prop.getProperty("password");
            hostName = prop.getProperty("hostName");
            port = prop.getProperty("port");
            subject = prop.getProperty("subject");
            String temp_content = prop.getProperty("mailBody");

//            LOG.debug("Temp_content: " + temp_content);
//            LOG.debug("Content: " + content);

            if (temp_content.contains("\n.")) {
                content = temp_content.replaceAll("\n.", "\n..");
            } else if (temp_content.startsWith(".")) {
                content = temp_content.replaceAll(".", "..");
            } else {
                content = temp_content;
            }
//            LOG.debug("Content after replacement: " + content);

            result = "Props = " + mailAddress + ", " + user + ", " + password + ", " + hostName + ", " + port;
//            LOG.debug(result + "\nProgram Ran on " + time + " by user=" + user);

        } catch (Exception e) {
            LOG.error("Exception: " + e);
        } finally {
            inputStream.close();
        }
        return result;
    }

    // GETTER
    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }

    public String getSenderMailAddress() {
        return mailAddress;
    }

    public String getHostName() {
        return hostName;
    }

    public String getPort() {
        return port;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}