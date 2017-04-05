package rn_app;

import java.io.IOException;

/**
 * Created by Neak on 30.03.2017.
 * <p>
 * Implementieren Sie eine JAVA-Klasse rn_app.MailFile, die als „User Agent“ einen festgelegten Nachrichtenbody
 * sowie einen beliebigen Dateianhang mittels SMTP-Protokoll zur Weiterleitung an einen SMTP-Server überträgt.
 */
public class MailFile {

    public static void main(String[] args) throws IOException { // TODO Exc handling
        if (args.length == 0) {
            System.err.println("Arguments Missing: 'rn_app.MailFile <recipient mail address> <file path>'");
            System.exit(1);
        }

        // get arguments
        String email = args[0];
        String attachment = args[1];

        // New Socket
        PropertyConfig prop = new PropertyConfig();
        EMailClient eMailClient = new EMailClient(
                prop.getHostName(),
                Integer.valueOf(prop.getPort()));

        // connect and transfer data
        eMailClient.connect();
        eMailClient.transfer(email, attachment);
        eMailClient.close();
    }

}
