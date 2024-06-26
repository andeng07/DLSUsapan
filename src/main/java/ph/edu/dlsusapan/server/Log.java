package ph.edu.dlsusapan.server;

import java.util.Date;

/**
 * @author XC23 - Chael Sumilang & Arron Baranquil @ 2024
 *
 * A representation of a log object
 */
public class Log {
    public final Date timestamp;
    public final String source;
    public final String destination;
    public final String action;

    public Log(Date timestamp, String source, String destination, String action) {
        this.timestamp = timestamp;
        this.source = source;
        this.destination = destination;
        this.action = action;
    }

    /**
     * converts the object to string in a format
     * @return log string
     */
    @Override
    public String toString() {
        return "\n" +
                "Timestamp => " + timestamp + "\n" +
                "Message Source => " + source + "\n" +
                "Message Destination => " + destination + "\n" +
                "Action => " + action +
                "\n".stripIndent();
    }
}