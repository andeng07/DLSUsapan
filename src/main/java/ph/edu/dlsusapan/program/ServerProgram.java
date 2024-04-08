package ph.edu.dlsusapan.program;

import ph.edu.dlsusapan.server.Server;

import java.io.IOException;
import java.util.Scanner;

/**
 * @author XC23 - Chael Sumilang & Arron Baranquil @ 2024
 */
public class ServerProgram {

    public static void main(String[] args) {

        System.out.println("""
                \t\t _____                         \s
                \t\t/  ___|                        \s
                \t\t\\ `--.  ___ _ ____   _____ _ __\s
                \t\t `--. \\/ _ \\ '__\\ \\ / / _ \\ '__|
                \t\t/\\__/ /  __/ |   \\ V /  __/ |  \s
                \t\t\\____/ \\___|_|    \\_/ \\___|_|  \s
                """.stripIndent());

        System.out.println("Welcome to the DLSUsapan Server! Before we proceed, we kindly request\nyour cooperation in providing the following essential details:\n");

        Scanner scanner = new Scanner(System.in);

        int port = Input.getInt("\t\tEnter the port number", scanner);
        boolean saveLogFile = Input.getConsent("\t\tSave log to file", scanner);

        try {
            new Server(port, saveLogFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
