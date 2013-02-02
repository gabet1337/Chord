package chord;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class Tester {
    public static void main(String[] args) throws IOException {
        String lol = "0123 4567";
        System.out.println(lol.substring(0, 4));
        System.out.println(lol.substring(5, lol.length()-1));
        
        InetSocketAddress rofl = new InetSocketAddress(InetAddress.getLocalHost(), 1337);
        System.out.println(rofl.toString());
        //InetSocketAddress inet = new InetSocketAddress("peterg-tp/127.0.1.1".substring(0, "peterg-tp/127.0.1.1".indexOf(':')), );
        
    }
}
