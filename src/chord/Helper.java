package chord;

import java.net.InetSocketAddress;

public class Helper {


    public static InetSocketAddress getInetSocketAddress(int port) {
        InetSocketAddress result = new InetSocketAddress("localhost", port);
        if (result.isUnresolved()) {
            System.err.println("Couldn't resolve the address");
            return null;
        }
        return result;
    }

    public static boolean between(int k, int a, int b) {
        System.out.println("===== KEY: " + k + " A: " + a + " B: " + b);
        k = k % (1 << 31);
        if (b <= a) {
            b += (1 << 31);
            if (k <= a)
                k += (1 << 31);
        }
        return (a < k && k <= b);
    }


}
