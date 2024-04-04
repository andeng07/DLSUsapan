package ph.edu.dlsusapan.client;

import ph.edu.dlsusapan.common.message.Message;
import ph.edu.dlsusapan.common.message.MessageAttachment;
import ph.edu.dlsusapan.common.message.MessageType;
import ph.edu.dlsusapan.common.message.Transceiver;
import ph.edu.dlsusapan.common.serializer.FileSerializer;
import ph.edu.dlsusapan.common.serializer.ObjectSerializer;

import java.io.*;
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

        transmit(new Message(uuid, name, MessageType.LOGIN, "Joined the chat!", null));

        listen();

        Scanner scanner = new Scanner(System.in);

        do {
            String messageContent = scanner.nextLine();

            String content = null;
            MessageAttachment messageAttachment = null;

            if (messageContent.startsWith("/")) { // meaning this is a command
                String[] command = messageContent.split(" ");

                switch (command[0]) {

                    case "/attach" -> {
                        if (command.length != 2) {
                            System.out.println("Usage: /attach <file>");
                            continue;
                        }

                        File file = new File(command[1]);

                        if (!file.exists()) {
                            System.out.println("File not found!");
                            continue;
                        }

                        messageAttachment = new MessageAttachment(file.getName(), FileSerializer.fileToBytes(file.getPath()));
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
        if (message.content != null) {
            System.out.println(message.fromName + " => " + message.content);
        }

        if (message.messageAttachment != null) {
            try {
                System.out.println(message.fromName + " => Sent an attachment: " + FileSerializer.bytesToFile(message.messageAttachment.content, message.messageAttachment.name).getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void listen() {
        new Thread(() -> {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String message;
                while ((message = bufferedReader.readLine()) != null) {

                    receive((Message) ObjectSerializer.deserialize(Base64.getDecoder().decode(message)));
                }
            } catch (IOException e) {

                // TODO

                System.out.println("SERVER ENDED");

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }).start();
    }

}