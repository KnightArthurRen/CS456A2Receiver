import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by renyi on 2018-03-12.
 */
public class Receiver {
    private InetAddress emulator_ip;
    private int emulator_port, reciver_port,last_seq;
    private DatagramSocket socket;
    private DatagramPacket buffer;
    private packet received_packet;
    private FileWriter output,arrival;

    private void save_data(String data) {
        try {
            output.write(data);
        } catch(java.io.IOException e) {
            System.err.println("Receiver: failed to save data!");
        }
    }
    private void log(int seqnum) {
        String log = String.valueOf(seqnum);
        log += "\n";
        try {
            arrival.write(log);
        } catch (java.io.IOException e) {
            System.err.println("Receiver: failed to log!");
        }

    }
    private void send_ack() {
        try {
            packet old_ack = packet.createACK(last_seq);
            try {
                DatagramPacket binary = new DatagramPacket(old_ack.getUDPdata(), old_ack.getUDPdata().length,emulator_ip,emulator_port);
                socket.send(binary);
            } catch (java.io.IOException e) {
                System.err.println("Receiver: cannot send old_ack!");
            }
        } catch (java.lang.Exception e ) {
            System.err.println("Receiver: cannot create old_ack package");
        }
    }

//    Constructor
    public Receiver(InetAddress emulator_ip, int emulator_port, int reciver_port, String output_file_name) {
        this.emulator_ip = emulator_ip;
        this.emulator_port = emulator_port;
        this.reciver_port = reciver_port;
        this.last_seq = -1;

//        Build the socket
        try {
            socket = new DatagramSocket(reciver_port);
        } catch (java.net.SocketException e) {
            System.err.println("Receiver: cannot create socket");
        }

        byte [] buff = new byte[1000];
        this.buffer = new DatagramPacket(buff,buff.length);

//        Create log + output file
        try {
            output = new FileWriter(output_file_name);
            arrival = new FileWriter("arrival.log");
        } catch (java.io.IOException e) {
            System.err.println("Receiver: cannot create output files");
        }

//        Start the infinite loop
        while(true) {
//            Receive the package
            try {
                socket.receive(buffer);
            } catch (java.io.IOException e) {
                System.err.println("Receiver: cannot receive package!");
            }
//            Parse the package
            try{
                received_packet = packet.parseUDPdata(buffer.getData());
            } catch (java.lang.Exception e) {
                System.err.println("Receiver: cannot parse package");
            }
//            Check the package
//            Check if it's EOT
            if(received_packet.getType() == 2) {
                DatagramPacket binary = new DatagramPacket(received_packet.getData(), received_packet.getData().length,emulator_ip,emulator_port);
                try {
                    socket.send(binary);
                } catch (java.io.IOException e) {
                    System.err.println("Receiver: failed to send EOT");
                }
                break;
            }
//            If regular package, check seq number
//            If it's the first ever received package
            if(last_seq == -1) {
//                If the first package is wrong, no ack is received
                if(received_packet.getSeqNum() != 0) break;
//                If the first package is correct, update seq and log
                last_seq = 0;
                send_ack();
            } else if(last_seq +1 % 32 != received_packet.getSeqNum()) {
//                If not expected package, drop it and send ack of last package
                send_ack();
            } else {
//                If it's expected, write down the data, update seq and send ack
//                Write down data
                log(received_packet.getSeqNum());
                save_data(received_packet.getData().toString());
                last_seq = received_packet.getSeqNum();
                send_ack();
            }
        }
    }
}
