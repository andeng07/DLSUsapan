package ph.edu.dlsusapan.program;

import java.util.Scanner;

/**
 * @author XC23 - Chael Sumilang & Arron Baranquil @ 2024
 */
public class Input {

    public static String getString(String prompt, Scanner scanner) {
        System.out.print(prompt + " => ");
        return scanner.next();
    }

    public static int getInt(String prompt, Scanner scanner) {
        System.out.print(prompt + " => ");
        while (!scanner.hasNextInt()) {
            scanner.next(); // consume non-integer token
            System.out.print(prompt + " => ");
        }
        return scanner.nextInt();
    }

    public static boolean getConsent(String prompt, Scanner scanner) {
        System.out.print(prompt + " => ");

        String consent = scanner.nextLine();

        while (!consent.equalsIgnoreCase("Yes") && !consent.equalsIgnoreCase("No")
                && !consent.equalsIgnoreCase("Y") && !consent.equalsIgnoreCase("N")) {
            consent = scanner.nextLine();
        }

        return consent.equalsIgnoreCase("Yes") || consent.equalsIgnoreCase("Y");
    }

}
