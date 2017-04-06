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
import java.util.Base64;

class EMailClient {
    private static final Logger LOG = Logger.getLogger(EMailClient.class);
    public static final int TIMEOUT = 20;
    public static final String ENCODING = "base64";
    public static final String MIME_VERSION = "1.0";

    private SSLSocketFactory socketFactory;
    private SSLSocket clientSocket;
    private Base64.Encoder base64;

    private DataOutputStream outToServer;
    private BufferedReader inFromServer;

    private String email;
    private String attachment;
    private PropertyConfig properties;

    private boolean handshakeSuccessfull = false;

    /**
     * @param host Address from the server.
     * @param port Port to the server socket.
     */
    EMailClient(@NotNull String host, @NotNull Integer port) {
        base64 = Base64.getEncoder();
        socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try {
            clientSocket = (SSLSocket) socketFactory.createSocket(host, port);
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            properties = new PropertyConfig();
        } catch (final IOException e) {
            LOG.error("EMailClient(String, Integer)", e);
        }
    }


    /**
     * Transfers attachmentPath to email.
     *
     * @param email          target mail.
     * @param attachmentPath not null.
     */
    void transfer(@NotNull String email, @NotNull String attachmentPath) {
        this.email = email;
        this.attachment = attachmentPath;

        try {
            handshake();
            authentication();
            sendMIME1(
                    properties.getSenderMailAddress(),
                    email,
                    properties.getSubject(),
                    attachmentPath);
        } catch (IOException e) {
            LOG.error("send()", e);
        }
        ;
    }

    private void handshake() throws IOException {
        send("EHLO client.example.de");
        // usable commands?
        receive();
        receive();
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
     */
    private void sendMIME1(@NotNull String senderEmail,
                           @NotNull String receiverEmail,
                           @NotNull String subject,
                           @NotNull String attachmentPath) throws IOException {
        File attachment = new File(attachmentPath);
        Path filePath = Paths.get(attachmentPath);
        byte[] attachmentContent = Files.readAllBytes(filePath);

        sendAndReceive("MAIL FROM: <" + senderEmail + ">");
//        sendAndReceive("RCPT TO: <" + receiverEmail + ">");
        sendAndReceive("RCPT TO: <max.malinowski@haw-hamburg.de>");
        sendAndReceive("DATA");

        send("From : <" + senderEmail + ">");
        send("To: <" + receiverEmail + ">");
        send("Reply-To: <" + receiverEmail + ">");
        send("cc: <max.malinowski@haw-hamburg.de>");
        send("bcc: <max.malinowski@haw-hamburg.de>");
        send("SUBJECT: " + subject);
        send("MIME-Version: " + MIME_VERSION);

        send("Content-Type: multipart/mixed; boundary=frontier");
        send("This is a message with multiple parts in MIME format.");
        send("--frontier");

        send("Content-Type: text/plain");
        send(properties.getContent());
        send("--frontier");

        send("Content-Type: application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        send("Content-Transfer-Encoding: " + ENCODING);
        send("Content-Disposition: attachment; filename=" + attachment.getName());
        send(encode(attachmentContent));
        send("."); // END
    }

    /**
     * Encodes Strings with Base64
     */
    private String encode(String str) {
        return encode(str.getBytes());
    }

    /**
     * Encodes bytes with Base64
     */
    private String encode(byte[] bytes) {
        return base64.encodeToString(bytes);
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
            sleep();
        } catch (final Exception e) {
            LOG.error("send()", e);
        }
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
            if (reply.matches("[0-9][0-9][0-9]-.*")) {
                receive();
            }
        } catch (final Exception e) {
            LOG.error("receive()", e);
        }
        return reply;
    }


    private void sleep() {
        try {
            Thread.sleep(TIMEOUT);
        } catch (InterruptedException e) {
            LOG.debug("sleep()", e);
        }
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