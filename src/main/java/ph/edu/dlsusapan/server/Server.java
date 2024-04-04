package ph.edu.dlsusapan.server;

import ph.edu.dlsusapan.common.message.Message;
import ph.edu.dlsusapan.common.message.MessageType;
import ph.edu.dlsusapan.common.message.Transceiver;
import ph.edu.dlsusapan.common.serializer.ObjectSerializer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server implements Transceiver {
    private final ServerSocket serverSocket;

    private final boolean saveLogFile;

    private boolean isGatewayOpen = false;

    private final Map<UUID, ConnectedClient> connectedClients = new HashMap<>();

    private final List<Log> logs = new ArrayList<>();

    private final List<Message> messages = new ArrayList<>();

    public Server(int port, boolean saveLogFile) throws IOException {

        System.out.println("\nThe DLSUsapan Chat Room Server has been successfully initialized.\n" +
                "The server is now active on port " + port + ".\n" +
                "Please ensure that your clients connect to this port for communication.\n" +
                "Logs will be printed for server activities.\n");

        System.out.println("\t\tCommands (/):\n\t\t\ttype '/terminate' to send a file.\n");


        serverSocket = new ServerSocket(port);

        this.saveLogFile = saveLogFile;

        Scanner scanner = new Scanner(System.in);

        openGateway();

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
        new Thread(() -> {
            if (isGatewayOpen) return;

            while ((isGatewayOpen = true)) {
                try {
                    Socket socket = serverSocket.accept();

                    Message message = (Message) ObjectSerializer.deserialize(Base64.getDecoder().decode(new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine()));

                    ConnectedClient connectedClient = new ConnectedClient(this, message.fromId, message.fromName, socket);

                    addClient(connectedClient);

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

    @Override
    public void transmit(Message message) {
        connectedClients.values().stream().filter(connectedClient -> !connectedClient.uuid.equals(message.fromId))
                .forEach(connectedClient -> connectedClient.sendMessage(message));
    }

    @Override
    public void receive(Message message) {
        switch (message.type) {
            case 0, 1, 2, 4: {
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

    public void log(Message message) {

        Date timestamp = new Date();

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

    public void terminate() {

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
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("\nServer shutdown initiated. Thank you for using DLSUsapan Chat Room. Have a great day!");
    }
}