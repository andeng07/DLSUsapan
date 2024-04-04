package ph.edu.dlsusapan.server;

import java.util.Date;

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