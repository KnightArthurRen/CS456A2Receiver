import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by renyi on 2018-03-12.
 */
public class Receiver_Class {
    private InetAddress emulator_ip;
    private int emulator_port,last_seq;
    private DatagramSocket socket;
    private packet received_packet;
    private FileWriter output,arrival;

    private void save_data(String data) {
        try {
            output.write(data);
        } catch(java.io.IOException e) {
            System.err.println("Receiver_Class: failed to save data!");
        }
    }
    private void log(int seqnum) {
        String log = String.valueOf(seqnum);
        log += "\n";
        try {
            arrival.write(log);
        } catch (java.io.IOException e) {
            System.err.println("Receiver_Class: failed to log!");
        }

    }
    private void send_ack() {
        try {
            packet old_ack = packet.createACK(last_seq);
            try {
                DatagramPacket binary = new DatagramPacket(old_ack.getUDPdata(), old_ack.getUDPdata().length,emulator_ip,emulator_port);
                socket.send(binary);
            } catch (java.io.IOException e) {
                System.err.println("Receiver_Class: cannot send old_ack!");
            }
        } catch (java.lang.Exception e ) {
            System.err.println("Receiver_Class: cannot create old_ack package");
        }
    }

//    Constructor
    public Receiver_Class(InetAddress emulator_ip, int emulator_port, int reciver_port, String output_file_name) {
        this.emulator_ip = emulator_ip;
        this.emulator_port = emulator_port;
        this.last_seq = -1;
//        Build the socket
        try {
            socket = new DatagramSocket(reciver_port);
        } catch (java.net.SocketException e) {
            System.err.println("Receiver_Class: cannot create socket");
        }

        byte [] buff = new byte[1000];
        DatagramPacket buffer = new DatagramPacket(buff,buff.length);

//        Create log + output file
        try {
            output = new FileWriter(output_file_name);
            arrival = new FileWriter("arrival.log");
        } catch (java.io.IOException e) {
            System.err.println("Receiver_Class: cannot create output files");
        }

//        Start the infinite loop
        while(true) {
//            Receive the package
            try {
                socket.receive(buffer);
            } catch (java.io.IOException e) {
                System.err.println("Receiver_Class: cannot receive package!");
            }
//            Parse the package
            try{
                received_packet = packet.parseUDPdata(buffer.getData());
            } catch (java.lang.Exception e) {
                System.err.println("Receiver_Class: cannot parse package");
            }
//            Check the package
//            Check if it's EOT
            if(received_packet.getType() == 2) {
                try{
                    packet eot_ack = packet.createEOT(received_packet.getSeqNum());
                    DatagramPacket binary = new DatagramPacket(eot_ack.getUDPdata(), eot_ack.getUDPdata().length,emulator_ip,emulator_port);
                    try {
                        socket.send(binary);
                    } catch (java.io.IOException e) {
                        System.err.println("Receiver_Class: failed to send EOT");
                    }
                } catch (java.lang.Exception e) {
                    System.err.println("Receiver_Class: cannot make EOT package");
                }

                break;
            }
//            If regular package, check seq number
//            If it's the first ever received package
//            If the first package is wrong, no ack is received
//            If the first package is correct, update seq and log
            if(last_seq == -1 && received_packet.getSeqNum() != 0) {
                continue;
            }
            if(last_seq +1 % 32 != received_packet.getSeqNum()) {
//                If not expected package, drop it and send ack of last package
                log(received_packet.getSeqNum());
                send_ack();
            } else {
//                If it's expected, write down the data, update seq and send ack
//                Write down data
                log(received_packet.getSeqNum());
                save_data( new String(received_packet.getData()));
                last_seq = received_packet.getSeqNum();
                send_ack();
            }
        }
//        After finished, close the log file writers
        try{
            arrival.close();
            output.close();
        } catch (java.io.IOException e) {
            System.err.println("Receiver_Class: cannot close file writers");
        }

    }
}
