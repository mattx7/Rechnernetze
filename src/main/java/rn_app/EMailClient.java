package rn_app;

import com.sun.istack.internal.NotNull;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;

class EMailClient {
    private static final Logger LOG = Logger.getLogger(EMailClient.class);
    public static final int TIMEOUT = 1000;
    public static final String ENCODING = "base64";
    public static final String MIME_VERSION = "1.0";


    private Socket clientSocket;
    private SocketAddress address;
    private Base64.Encoder base64;

    private BufferedReader inFromUser;
    private DataOutputStream outToServer;
    private BufferedReader inFromServer;

    private String email;
    private String attachment;
    private PropertyConfig properties;

    /**
     * @param host Address from the server.
     * @param port Port to the server socket.
     */
    EMailClient(@NotNull String host, @NotNull Integer port) throws IOException {
//        final SocketFactory factory = SSLSocketFactory.getDefault(); TODO SSL
        this.address = new InetSocketAddress(host, port);
        properties = new PropertyConfig();
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

    /**
     * Transfers attachmentPath to email.
     *
     * @param email          target mail.
     * @param attachmentPath not null.
     */
    void transfer(@NotNull String email, @NotNull String attachmentPath) throws IOException {
        this.email = email;
        this.attachment = attachmentPath;

        sendToServer("AUTH LOGIN");
        receiveFromServer();
        sendToServer(encode(properties.getUser()));
        receiveFromServer();
        sendToServer(encode(properties.getPassword()));
        receiveFromServer();

        sendToServer(
                createMIME1(
                        properties.getSenderMailAddress(),
                        email,
                        properties.getSubject(),
                        attachmentPath));
        receiveFromServer();
    }

    /**
     * Creates MIME 1.0 datatype for emails.
     *
     * @param senderEmail    not null.
     * @param receiverEmail  not null.
     * @param subject        not null.
     * @param attachmentPath not null.
     * @return EMail in MIME 1.0
     */
    @NotNull
    private String createMIME1(@NotNull String senderEmail,
                               @NotNull String receiverEmail,
                               @NotNull String subject,
                               @NotNull String attachmentPath) throws IOException {
        File attachment = new File(attachmentPath);
        Path filePath = Paths.get(attachmentPath);
        byte[] attachmentContent = Files.readAllBytes(filePath);

        return "From: <" + senderEmail + ">" +
                "To: <" + receiverEmail + ">" +
                "Subject:" + subject +
                "MIME-Version: " + MIME_VERSION +
                "Content-Type: multipart/mixed; boundary=frontier" + // TODO KA HEADER?
                "" +
                "This is a message with multiple parts in MIME format." +
                "" +
                "--frontier" +
                "Content-Type: text/plain" + // BODY
                "" +
                properties.getContent() +
                "" +
                "--frontier" +
                "Content-Type: text/plain" + // ANHANG
                "Content-Transfer-Encoding: " + ENCODING +
                "Content-Disposition: attachment; filename=" + attachment.getName() +
                "" +
                Arrays.toString(base64.encode(attachmentContent)) + // Anhang verschlüsseln
                "" +
                "";
    }

    /**
     * Encodes str with Base64
     */
    private String encode(String str) {
        return Arrays.toString((base64.encode((str.getBytes()))));
    }

    /**
     * Transfers request to server.
     *
     * @param request as String.
     */
    private void sendToServer(@NotNull String request) throws IOException {
        // Send one line (with CRLF) to server
        outToServer.writeBytes(request + '\r' + '\n');
        System.out.println("Sent: " + request);
    }

    /**
     * Reads from Server.
     *
     * @return reply as String.
     */
    @Nullable
    private String receiveFromServer() throws IOException {
        // Read reply from server
        String reply = inFromServer.readLine();
        System.out.println("Received: " + reply);
        return reply;
    }

    /**
     * Close connection.
     */
    public void close() {
        try {
            clientSocket.close();
            inFromServer.close();
            outToServer.close();
            inFromUser.close();
        } catch (IOException e) {
            LOG.error("IOException");
        }
    }
}