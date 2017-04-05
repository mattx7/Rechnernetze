package rn_app;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Base64;

class EMailClient {
    private static final Logger LOG = Logger.getLogger(EMailClient.class);
    public static final int TIMEOUT = 100;

    private Socket clientSocket;
    private SocketAddress address;
    private Base64.Encoder encoder;

    private BufferedReader inFromUser;
    private DataOutputStream outToServer;
    private BufferedReader inFromServer;

    private String email;
    private String attachment;

    /**
     * @param host Address from the server.
     * @param port Port to the server socket.
     */
    EMailClient(String host, Integer port) {

        this.clientSocket = new Socket();
        this.address = new InetSocketAddress(host, port);
        this.email = email;
        this.attachment = attachment;

    }

    /**
     * Starts connection and create
     *
     * @throws IOException Error during connection.
     */
    void connect() throws IOException {
        try {
            clientSocket.connect(address, TIMEOUT);
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            LOG.error("Error during connection!");
            throw e;
        }
    }

    void transfer(String email,
                  String attachment) throws IOException {

        this.email = email;
        this.attachment = attachment;

        writeToServer(email);
        readFromServer();
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

    public void close() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            LOG.error("IOException");
        }
    }
}