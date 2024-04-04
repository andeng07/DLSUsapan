package ph.edu.dlsusapan.program;

import ph.edu.dlsusapan.server.Server;

import java.io.IOException;

public class ServerProgram {

    public static void main(String[] args) {

        try {
            new Server(9201, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
