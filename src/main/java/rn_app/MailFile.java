package rn_app;

import java.io.IOException;

/**
 * Created by Neak on 30.03.2017.
 * <p>
 * Implementieren Sie eine JAVA-Klasse rn_app.MailFile, die als „User Agent“ einen festgelegten Nachrichtenbody
 * sowie einen beliebigen Dateianhang mittels SMTP-Protokoll zur Weiterleitung an einen SMTP-Server überträgt.
 */
public class MailFile {

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("Arguments Missing: 'rn_app.MailFile <recipient mail address> <file path>'"); // TODO help output
            System.exit(1);
        }

        String host = "127.0.0.1";
        Integer port = 60000;
        String email = args[0];
        String attachment = args[1];
        EMailClient tcp = new EMailClient(host, port, email, attachment);
        tcp.startJob();
    }
}
