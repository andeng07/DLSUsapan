package ph.edu.dlsusapan.common.message;

public interface Transceiver {

    void transmit(Message message);
    void receive(Message message);

}
