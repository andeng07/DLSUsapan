package ph.edu.dlsusapan.common.message;

import java.io.Serializable;

public class MessageAttachment implements Serializable {
    public final String name;
    public final byte[] content;

    public MessageAttachment(String name, byte[] content) {
        this.name = name;
        this.content = content;
    }
}
