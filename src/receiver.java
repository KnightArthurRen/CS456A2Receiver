import java.net.InetAddress;

/**
 * Created by renyi on 2018-03-12.
 */
public class receiver {
    public static void main(String[] args) {
        if(args.length < 4) {
            System.err.print("Receiver: not enough arguments!");
        }
        try{
            InetAddress emulator_ip = InetAddress.getByName(args[0]);
            Receiver_Class sender = new Receiver_Class(emulator_ip,Integer.parseInt(args[1]),Integer.parseInt(args[2]),args[3]);
        } catch (java.net.UnknownHostException e) {
            System.err.println("Receiver: unknow host!");
        }

    }
}
