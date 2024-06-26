package ph.edu.dlsusapan.program;

import ph.edu.dlsusapan.client.Client;

import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;

/**
 * @author XC23 - Chael Sumilang & Arron Baranquil @ 2024
 */
public class ClientProgram {

    public static void main(String[] args) {

        System.out.println("""
                \t\t _____ _           _    ______                     \s
                \t\t/  __ \\ |         | |   | ___ \\                    \s
                \t\t| /  \\/ |__   __ _| |_  | |_/ /___   ___  _ __ ___ \s
                \t\t| |   | '_ \\ / _` | __| |    // _ \\ / _ \\| '_ ` _ \\\s
                \t\t| \\__/\\ | | | (_| | |_  | |\\ \\ (_) | (_) | | | | | |
                \t\t \\____/_| |_|\\__,_|\\__| \\_| \\_\\___/ \\___/|_| |_| |_|
                """.stripIndent());

        System.out.println("Welcome to the DLSUsapan Chat Room! Before we proceed, we kindly request\nyour cooperation in providing the following essential details:\n");

        Scanner scanner = new Scanner(System.in);

        String host = Input.getString("\t\tEnter the host address", scanner);
        int port = Input.getInt("\t\tEnter the port number", scanner);
        String name = Input.getString("\t\tEnter your username", scanner);

        try {
            new Client(host, port, UUID.randomUUID(), name);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
