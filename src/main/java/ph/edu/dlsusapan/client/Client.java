package ph.edu.dlsusapan.client;

import ph.edu.dlsusapan.common.message.Message;
import ph.edu.dlsusapan.common.message.MessageType;
import ph.edu.dlsusapan.common.message.Transceiver;
import ph.edu.dlsusapan.common.object.ObjectSerializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;
import java.util.Scanner;
import java.util.UUID;

public class Client implements Transceiver {

    public final UUID uuid;
    public final String name;

    public final boolean logToFile;

    private final Socket socket;

    private final PrintWriter out;


    public Client(String host, int port, UUID uuid, String name, boolean logToFile) throws IOException, InterruptedException {
        this.uuid = uuid;
        this.name = name;
        this.logToFile = logToFile;

        socket = new Socket(host, port);

        out = new PrintWriter(socket.getOutputStream(), true);

        transmit(new Message(uuid, name, MessageType.LOGIN, "Joined the chat!"));

        listen();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print(name + " => ");
            transmit(new Message(uuid, name, MessageType.SEND_MESSAGE, scanner.nextLine()));
        }
    }

    @Override
    public void transmit(Message message) {
        try {
            out.println(Base64.getEncoder().encodeToString(ObjectSerializer.serialize(message)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void receive(Message message) {
        System.out.println(message.fromName + " => " + message.content);

        int action = -1;

        if (message.type == MessageType.SEND_MESSAGE) action = MessageType.RECEIVED_MESSAGE;
        else if (message.type == MessageType.SEND_FILE) action = MessageType.RECEIVED_MESSAGE;

        switch (message.type) {
            case MessageType.SEND_MESSAGE, MessageType.SEND_FILE -> transmit(new Message(uuid, name, action, null));
        }
    }

    public void listen() {
        new Thread(() -> {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String message;
                while ((message = bufferedReader.readLine()) != null) {

                    receive((Message) ObjectSerializer.deserialize(Base64.getDecoder().decode(message)));
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }).start();
    }

}
