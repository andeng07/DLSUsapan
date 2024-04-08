package ph.edu.dlsusapan.common.message;

/**
 * @author XC23 - Chael Sumilang & Arron Baranquil @ 2024
 */
public interface Transceiver {

    void transmit(Message message);
    void receive(Message message);

}
