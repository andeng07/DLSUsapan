package ph.edu.dlsusapan.server;

import ph.edu.dlsusapan.common.message.Message;
import ph.edu.dlsusapan.common.message.MessageType;
import ph.edu.dlsusapan.common.serializer.ObjectSerializer;

import java.io.*;
import java.net.Socket;
import java.util.Base64;
import java.util.UUID;

public class ConnectedClient {
    private final Server server;

    public final UUID uuid;
    public final String name;
    public final Socket socket;

    private final PrintWriter out;
    private final BufferedReader in;
    public ConnectedClient(Server server, UUID uuid, String name, Socket socket) {
        this.server = server;

        this.uuid = uuid;
        this.name = name;
        this.socket = socket;

        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(Message message) {
        try {
            out.println(Base64.getEncoder().encodeToString(ObjectSerializer.serialize(message)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void listen() {
        new Thread(() -> {
            String message;

            try {
                while (true) {
                    if ((message = in.readLine()) != null) {
                        server.receive((Message) ObjectSerializer.deserialize(Base64.getDecoder().decode(message)));
                    }
                }
            } catch (IOException e) {
                Message logoutMessage = new Message(uuid, name, MessageType.LOGOUT, "has logged out.", null);
                server.receive(logoutMessage);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            
        }).start();
    }

}