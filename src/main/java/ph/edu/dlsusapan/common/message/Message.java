package ph.edu.dlsusapan.common.message;

import java.io.Serializable;
import java.util.UUID;

public class Message implements Serializable {

    public final UUID fromId;
    public String fromName;
    public final int type;
    public final String content;

    public Message(UUID fromId, String fromName, int type, String content) {
        this.fromId = fromId;
        this.fromName = fromName;
        this.type = type;
        this.content = content;
    }

}
