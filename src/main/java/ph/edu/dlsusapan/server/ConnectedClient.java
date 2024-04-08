package ph.edu.dlsusapan.server;

import ph.edu.dlsusapan.common.message.Message;
import ph.edu.dlsusapan.common.message.MessageType;
import ph.edu.dlsusapan.common.serializer.ObjectSerializer;

import java.io.*;
import java.net.Socket;
import java.util.Base64;
import java.util.UUID;

/**
 * @author XC23 - Chael Sumilang & Arron Baranquil @ 2024
 *
 * A server-side representation of a client that is connected to our server socket
 */
public class ConnectedClient {
    private final Server server;

    public final UUID uuid;
    public final String name;
    public final Socket socket;

    /**
     * used to send a message towards the client
     */
    private final PrintWriter out;
    /**
     * used to receive a message from the client
     */
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

    /**
     * Sends a message to the client
     * @param message the message
     */
    public void sendMessage(Message message) {
        try {
            /**
             * converts the message to byte array, then encode it to string to send it to the socket
             */
            out.println(Base64.getEncoder().encodeToString(ObjectSerializer.serialize(message)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void listen() {
        /**
         * Threading, similarly, this repeatedly reads the output from the client
         * and when there is a message, then it calls the server.receive() method to process the message
         * this also blocks the main thread due to repeatedly reading the output in an indefinite manner, hence
         * the threading
         */
        new Thread(() -> {
            String message;

            try {
                while (true) {
                    /**
                     * Checks if there is a new message
                     */
                    if ((message = in.readLine()) != null) {
                        /**
                         * Decodes the string to byte array, then convert it to message object
                         * Call the server.receive() function from the server, to process the message
                         */
                        server.receive((Message) ObjectSerializer.deserialize(Base64.getDecoder().decode(message)));
                    }
                }
            } catch (IOException e) {
                /**
                 * Error occurs when client terminates the program, hence, we send a logout message to the server
                 * informing that the user has logged out.
                 */
                Message logoutMessage = new Message(uuid, name, MessageType.LOGOUT, "has logged out.", null);
                server.receive(logoutMessage);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            
        }).start();
    }

}