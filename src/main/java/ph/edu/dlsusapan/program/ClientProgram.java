package ph.edu.dlsusapan.program;

import ph.edu.dlsusapan.client.Client;

import java.io.IOException;
import java.util.UUID;

public class ClientProgram {

    public static void main(String[] args) {
        try {
            new Client("localhost", 9201, UUID.randomUUID(), "John", false);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
