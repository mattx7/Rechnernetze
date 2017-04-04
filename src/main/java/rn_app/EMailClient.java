package rn_app;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Base64;

class EMailClient {

    private static final int TIMEOUT = 100;

    private Socket clientSocket;
    private SocketAddress address;
    private Base64.Encoder encoder;

    private BufferedReader inFromUser;
    private DataOutputStream outToServer;
    private BufferedReader inFromServer;

    private String email;
    private String attachment;

    EMailClient(String host,
                Integer port,
                String email,
                String attachment) throws IOException {

        this.clientSocket = new Socket();
        this.address = new InetSocketAddress(host, port);

//        inFromUser = new BufferedReader(new InputStreamReader(email)); // TODO System.in ersetzen
        outToServer = new DataOutputStream(clientSocket.getOutputStream());
        inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        this.email = email;
        this.attachment = attachment;

    }

    void startJob() throws IOException {
        String out;
        String modifiedOut;

        clientSocket.connect(address, TIMEOUT);
        out = inFromUser.readLine(); // TODO anpassen

        writeToServer(email);
        readFromServer();

        clientSocket.close();
    }

    private void writeToServer(String request) throws IOException {
        // Send one line (with CRLF) to server
        outToServer.writeBytes(request + '\r' + '\n');
        System.out.println("TCP Client has sent the message: " + request);
    }

    private String readFromServer() throws IOException {
        // Read reply from server
        String reply = inFromServer.readLine();
        System.out.println("TCP Client got from Server: " + reply);
        return reply;
    }

}