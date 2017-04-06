package rn_app;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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

    private SSLSocketFactory socketFactory;
    private SSLSocket clientSocket;
    private SocketAddress address;
    private Base64.Encoder base64;

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
        socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        clientSocket = (SSLSocket) socketFactory.createSocket(host, port);
//        clientSocket = new Socket();
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
//            clientSocket.connect(address, TIMEOUT);
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

        createMIME1(
                properties.getSenderMailAddress(),
                email,
                properties.getSubject(),
                attachmentPath);
        receive();
    }

    private void handshake() throws IOException {
        receive();
        send("EHLO client.example.de");
        // usable commands?
        for (int i = 0; i < 9; i++) {
            receive();
        }
    }

    private void authentication() throws IOException {
//        sendAndReceive("STARTTLS");
        sendAndReceive("AUTH LOGIN ");
        sendAndReceive(encode(properties.getUser()));
        sendAndReceive(encode(properties.getPassword()));
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
    private void createMIME1(@NotNull String senderEmail,
                             @NotNull String receiverEmail,
                             @NotNull String subject,
                             @NotNull String attachmentPath) throws IOException {
        File attachment = new File(attachmentPath);
        Path filePath = Paths.get(attachmentPath);
        byte[] attachmentContent = Files.readAllBytes(filePath);
        send("From: " + senderEmail);
        send("To: " + receiverEmail);
        send("Subject:" + subject);
        send("MIME-Version: " + MIME_VERSION);
        send("Content-Type: multipart/mixed; boundary=frontier");
        send("This is a message with multiple parts in MIME format.");
        send("--frontier");
        send("Content-Type: text/plain");
        send(properties.getContent());
//                "--frontier" + "\n" +
//                "Content-Type: text/plain" + "\n" +// ANHANG
//                "Content-Transfer-Encoding: " + ENCODING + "\n" +
//                "Content-Disposition: attachment; filename=" + attachment.getName() + "\n" +
//                "" +
//                encode(attachmentContent) + "\n" + // Anhang verschlüsseln
//                "" +
//        "";
    }

    /**
     * Encodes str with Base64
     */
    private String encode(String str) {
        return (base64.encodeToString(str.getBytes()));
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
    private void send(@NotNull String request) {
        // Send one line (with CRLF) to server
        try {
            outToServer.writeBytes(request + '\r' + '\n');
            LOG.debug("Sent: " + request);
        } catch (final Exception e) {
            LOG.error("send()", e);
        }
    }

    /**
     * Reads from server.
     *
     * @return reply as String.
     */
    @Nullable
    private String receive() {
        // Read reply from server
        String reply = null;
        try {
            reply = inFromServer.readLine();
            LOG.debug("Received: " + reply);
        } catch (final Exception e) {
            LOG.error("receive()", e);
        }
        return reply;
    }

    /**
     * Transfers request to server and reads from server
     *
     * @param request as String.
     * @return reply as String.
     */
    private String sendAndReceive(@NotNull String request) {
        send(request);
        return receive();
    }

    /**
     * Close connection.
     */
    void close() {
        try {
            send("QUIT");
            clientSocket.close();
            inFromServer.close();
            outToServer.close();
        } catch (Exception e) {
            LOG.error("IOException", e);
        }
    }
}