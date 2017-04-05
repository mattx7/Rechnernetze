package rn_app;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;

class EMailClient {
    private static final Logger LOG = Logger.getLogger(EMailClient.class);
    public static final int TIMEOUT = 100;
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
        clientSocket = new Socket();
        this.address = new InetSocketAddress(host, port);
        properties = new PropertyConfig();
        base64 = Base64.getEncoder();
    }

    /**
     * Starts connection and create
     *
     * @throws IOException Error during connection.
     */
    void connect() {
        try {
            clientSocket.connect(address, TIMEOUT);
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            LOG.error("Error during connection!", e);
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

        handshake();
        authentication();

        sendToServer(
                createMIME1(
                        properties.getSenderMailAddress(),
                        email,
                        properties.getSubject(),
                        attachmentPath));
        receiveFromServer();
    }

    private void authentication() throws IOException {
        sendToServer("AUTH LOGIN");
        receiveFromServer();
        // Authentification of user and password with Base64
        sendToServer(encode(properties.getUser()));
        receiveFromServer();
        sendToServer(encode(properties.getPassword()));
        receiveFromServer();
    }

    private void handshake() throws IOException {
        receiveFromServer();
        sendToServer("EHLO client.example.de");
        // Loop terminates when buffer is empty
        for (int i = 0; i < 8; i++) {
            receiveFromServer();
        }
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

        return "From: <" + senderEmail + "> " + "\n" +
                "To: <" + receiverEmail + ">" + "\n" +
                "Subject:" + subject + "\n" +
                "MIME-Version: " + MIME_VERSION + "\n" +
                "Content-Type: multipart/mixed; boundary=frontier" + "\n" +// TODO KA HEADER?
                "" +
                "This is a message with multiple parts in MIME format." + "\n" +
                "" +
                "--frontier" + "\n" +
                "Content-Type: text/plain" + "\n" +// BODY
                "" +
                properties.getContent() + "\n" +
                "" +
                "--frontier" + "\n" +
                "Content-Type: text/plain" + "\n" +// ANHANG
                "Content-Transfer-Encoding: " + ENCODING + "\n" +
                "Content-Disposition: attachment; filename=" + attachment.getName() + "\n" +
                "" +
                encode(attachmentContent) + "\n" + // Anhang verschlüsseln
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
     * Encodes bytes with Base64
     */
    private String encode(byte[] bytes) {
        return Arrays.toString((base64.encode((bytes))));
    }

    /**
     * Transfers request to server.
     *
     * @param request as String.
     */
    private void sendToServer(@NotNull String request) throws IOException {
        // Send one line (with CRLF) to server
        try {
            outToServer.writeBytes(request + '\r' + '\n');
            LOG.debug("Sent: " + request);
        } catch (final SocketException e) {
            LOG.error("sendToServer()", e);
        }
    }

    /**
     * Reads from Server.
     *
     * @return reply as String.
     */
    @Nullable
    private String receiveFromServer() {
        // Read reply from server
        String reply = null;
        try {
            reply = inFromServer.readLine();
            LOG.debug("Received: " + reply);
        } catch (final Exception e) {
            LOG.error("receiveFromServer()", e);
        }
        return reply;
    }

    /**
     * Close connection.
     */
    void close() {
        try {
            clientSocket.close();
            inFromServer.close();
            outToServer.close();
            inFromUser.close();
        } catch (Exception e) {
            LOG.error("IOException", e);
        }
    }
}