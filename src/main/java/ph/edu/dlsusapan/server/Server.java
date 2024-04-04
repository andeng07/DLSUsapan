package ph.edu.dlsusapan.server;

import ph.edu.dlsusapan.common.message.Message;
import ph.edu.dlsusapan.common.message.MessageType;
import ph.edu.dlsusapan.common.message.Transceiver;
import ph.edu.dlsusapan.common.serializer.ObjectSerializer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Server representation that can transmit and receive data
 */
public class Server implements Transceiver {
    /**
     * The server socket that you'll host.
     */
    private final ServerSocket serverSocket;

    /**
     * The flag whether you want to save the file on terminate.
     */
    private final boolean saveLogFile;

    /**
     * Can enter or not
     */
    private boolean isGatewayOpen = false;

    /**
     * List of the connected clients in the socket
     */
    private final Map<UUID, ConnectedClient> connectedClients = new HashMap<>();

    /**
     * List of logs, used when saving a file, loop to each and then write to file.
     */
    private final List<Log> logs = new ArrayList<>();

    /**
     * List of sent messages. Gagamitin sana sa login feature, however 'di kaya ng oras :<
     */
    private final List<Message> messages = new ArrayList<>();

    /**
     * @param port port of the socket
     * @param saveLogFile flag to save file on terminate
     * @throws IOException exception
     */
    public Server(int port, boolean saveLogFile) throws IOException {


        System.out.println("\nThe DLSUsapan Chat Room Server has been successfully initialized.\n" +
                "The server is now active on port " + port + ".\n" +
                "Please ensure that your clients connect to this port for communication.\n" +
                "Logs will be printed for server activities.\n");

        System.out.println("\t\tCommands (/):\n\t\t\ttype '/terminate' to send a file.\n");

        /**
         * create socket
         */
        serverSocket = new ServerSocket(port);

        this.saveLogFile = saveLogFile;

        /**
         * Used for inputs from console
         */
        Scanner scanner = new Scanner(System.in);

        /**
         * Accept sockets
         */
        openGateway();

        /**
         * Process inputs from console
         */
        do {
            String messageContent = scanner.nextLine();

            if (messageContent.isBlank() || messageContent.isEmpty()) continue;

            if (messageContent.startsWith("/")) { // meaning this is a command
                String[] command = messageContent.split(" ");

                switch (command[0]) {

                    case "/terminate" -> {
                        terminate();
                    }

                }
            }

        } while(true);
    }

    private void openGateway() {
        /**
         * Threading, since, serverSocket.accept() method on line 117 of this file blocks the main thread
         * and waits for a connection, it stops the process of other functions, therefore we create a new thread
         * so that, our main thread doesn't get blocked while waiting for connections
         */

        new Thread(() -> {
            if (isGatewayOpen) return;

            while ((isGatewayOpen = true)) {
                try {
                    /**
                     * waits for a connection
                     */
                    Socket socket = serverSocket.accept();

                    /**
                     * gets the initial message, containing the user's information to create the user
                     */
                    Message message = (Message) ObjectSerializer.deserialize(Base64.getDecoder().decode(new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine()));

                    /**
                     * creates the server-side representation of a client
                     */
                    ConnectedClient connectedClient = new ConnectedClient(this, message.fromId, message.fromName, socket);

                    /**
                     * add the client
                     */
                    addClient(connectedClient);

                    /**
                     * listens for the message from the connected client
                     */
                    connectedClient.listen();

                    receive(message);
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private void closeGateway() {
        isGatewayOpen = false;
    }

    public void addClient(ConnectedClient client) {
        connectedClients.put(client.uuid, client);

        if (connectedClients.size() >= 2) closeGateway();
    }

    /**
     * Sends the message to other connected clients excluding from the sender.
     *
     * @param message the message
     */
    @Override
    public void transmit(Message message) {
        connectedClients.values().stream().filter(connectedClient -> !connectedClient.uuid.equals(message.fromId))
                .forEach(connectedClient -> connectedClient.sendMessage(message));
    }

    /**
     * Process the message being sent and logs the message
     *
     * @param message the message
     */
    @Override
    public void receive(Message message) {
        switch (message.type) {
            case MessageType.LOGIN, MessageType.LOGOUT, MessageType.SEND_MESSAGE, MessageType.SEND_ATTACHMENT: {
                if (message.type == MessageType.LOGOUT) {
                    connectedClients.remove(message.fromId);
                }

                if (message.type == MessageType.SEND_MESSAGE || message.type == MessageType.SEND_ATTACHMENT) {
                    if (connectedClients.size() != 2) {
                        transmit(new Message(null, null, MessageType.NOT_RECEIVED, "you're the only one in this chat room.", null));
                        return;
                    }
                }

                messages.add(message);
                transmit(message);

                log(message);
            }
        }
    }

    /**
     * logs the message
     * @param message the message
     */
    public void log(Message message) {

        Date timestamp = new Date();

        /**
         * where the message is being sent
         */
        ConnectedClient destination = connectedClients.values().stream()
                .filter(connectedClient -> !connectedClient.uuid.equals(message.fromId)).findFirst().orElse(null);

        String action = switch (message.type) {
            case MessageType.LOGIN -> "User Login";
            case MessageType.LOGOUT -> "User Logout";
            case MessageType.SEND_MESSAGE -> "User Send Message";
            case MessageType.RECEIVED_MESSAGE -> "User Received Message";
            case MessageType.SEND_ATTACHMENT -> "User Send Attachment";
            case MessageType.RECEIVED_ATTACHMENT -> "User Received Attachment";
            default -> "None";
        };

        Log log = new Log(timestamp, message.fromName + " (UUID:" + message.fromId + ")", (destination == null ? "None" : destination.name + " (UUID:" + destination.uuid + ")"), action);

        logs.add(log);

        System.out.println(log);
    }

    /**
     * termination process
     */
    public void terminate() {

        /**
         * if save logs to file, then save it
         */
        if (saveLogFile) {

            File file = new File(".", "logs_" + System.currentTimeMillis() + ".txt");

            try {
                FileOutputStream out = new FileOutputStream(file);

                logs.forEach(log -> {
                    try {
                        out.write(log.toString().getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

                out.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        try {
            /**
             * close the server socket
             */
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("\nServer shutdown initiated. Thank you for using DLSUsapan Chat Room. Have a great day!");
    }
}