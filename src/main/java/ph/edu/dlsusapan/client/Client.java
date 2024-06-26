package ph.edu.dlsusapan.client;

import ph.edu.dlsusapan.common.message.Message;
import ph.edu.dlsusapan.common.message.MessageAttachment;
import ph.edu.dlsusapan.common.message.MessageType;
import ph.edu.dlsusapan.common.message.Transceiver;
import ph.edu.dlsusapan.common.serializer.FileSerializer;
import ph.edu.dlsusapan.common.serializer.ObjectSerializer;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;
import java.util.UUID;

/**
 * @author XC23 - Chael Sumilang & Arron Baranquil @ 2024
 *
 * A client representation
 */
public class Client implements Transceiver {

    public final UUID uuid;
    public final String name;

    private final Socket socket;

    /**
     * used to send a message to the server
     */
    private final PrintWriter out;


    public Client(String host, int port, UUID uuid, String name) throws IOException, InterruptedException {
        System.out.println("\nYour connection to the DLSUsapan Chat Room on port " + port + " has been successfully" +
                "\nestablished. Your unique ID is: " + uuid + ". Please" +
                "\nensure to retain this ID for all future logins.");

        System.out.println("\n\t\tCommands (/):" +
                "\n\t\t\ttype '/attach <file>' to send a file." +
                "\n\t\t\ttype '/logout' to log out and close the connection." +
                "\n\t\t\ttype any message to start messaging.\n");

        this.uuid = uuid;
        this.name = name;

        /**
         * create socket connection
         */
        socket = new Socket(host, port);

        out = new PrintWriter(socket.getOutputStream(), true);

        /**
         * Create an initial message / login message for the server to save this client representation
         */

        transmit(new Message(uuid, name, MessageType.LOGIN, "Joined the chat!", null));

        /**
         * Listens messages from the server
         */
        listen();

        /**
         * Used to read inputs from the console
         */
        Scanner scanner = new Scanner(System.in);

        /**
         * Runs indefinitely until the program's terminated. This processes the input from the scanner above
         */
        do {
            String messageContent = scanner.nextLine();

            if (messageContent.isBlank() || messageContent.isEmpty()) continue;

            String content = null;
            MessageAttachment messageAttachment = null;

            /**
             * indicates a command
             */
            if (messageContent.startsWith("/")) { // meaning this is a command
                String[] command = messageContent.split(" ");

                switch (command[0]) {

                    case "/attach" -> {
                        File file = new File(String.join(" ", Arrays.copyOfRange(command, 1, command.length)));

                        if (!file.exists()) {
                            System.out.println("File not found!");
                            continue;
                        }

                        messageAttachment = new MessageAttachment(file.getName(), FileSerializer.fileToBytes(file.getPath()));
                    }

                    case "/logout" -> {
                        Message logoutMessage = new Message(uuid, name, MessageType.LOGOUT, "has logged out.", null);
                        transmit(logoutMessage);

                        socket.close();
                    }
                }
            } else {
                content = messageContent;
            }

            if (content != null) {
                transmit(new Message(uuid, name, MessageType.SEND_MESSAGE, content, null));
                continue;
            }
            if (messageAttachment != null) {
                transmit(new Message(uuid, name, MessageType.SEND_ATTACHMENT, null, messageAttachment));
                continue;
            }

        } while(true);
    }

    /**
     * Sends the message to the server
     * @param message the message
     */
    @Override
    public void transmit(Message message) {
        try {
            /**
             * convert the object to byte array, then encode it to string
             */
            out.println(Base64.getEncoder().encodeToString(ObjectSerializer.serialize(message)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Process the message received from the server
     * @param message the message
     */
    @Override
    public void receive(Message message) {
        /**
         * if the message is not an attachment
         */
        if (message.content != null) {
            System.out.println(((message.fromName != null) ? message.fromName : "Server") + " => You: " + message.content);
        }

        /**
         * if the message is an attachment
         */
        if (message.messageAttachment != null) {
            try {
                System.out.println(((message.fromName != null) ? message.fromName : "Server") +
                        " => You: Sent an attachment at " +
                        FileSerializer.bytesToFile(message.messageAttachment.content, message.messageAttachment.name)
                                .getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void listen() {

        /**
         * Threading, similarly, this repeatedly reads the output from the client
         * and when there is a message, then it calls receive() method to process the message
         * this also blocks the main thread due to repeatedly reading the output in an indefinite manner, hence
         * the threading
         */
        new Thread(() -> {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String message;
                while ((message = bufferedReader.readLine()) != null) {

                    receive((Message) ObjectSerializer.deserialize(Base64.getDecoder().decode(message)));
                }
            } catch (IOException e) {
                System.out.println(name + " => has logged out.");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }).start();
    }

}