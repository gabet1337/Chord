package chord;

public class Helper {

    public static boolean between(int k, int a, int b) {
        k = k % (1 << 31);
        if (b <= a) {
            b += (1 << 31);
            if (k <= a)
                k += (1 << 31);
        }
        return (a < k && k <= b);
    }
}
