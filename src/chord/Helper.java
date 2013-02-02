package chord;

import java.util.*;

/**
 * @author mw
 * Helper class for making operations on numbers with a certain bit-length.
 */
public class Helper {
	/// Bit-length to use. Default to 4; all operations use this.
	private static final int bits = 4;
	private static final Random random = new Random();

	/**
	 * @return the largest value of the hard-coded bit-length, n = 2 ** bits - 1
	 */
	public static int getMax() {
		return 1 << bits;
	}

	/**
	 * @return a random integer n, such that 0 <= n < 2 ** bits
	 */
	public static int random() {
		return random.nextInt(getMax());
	}

	/**
	 * @return whether a < k <= b modulo 2 ** bits.
	 */
	public static boolean between(int k, int a, int b) {
		k = k % getMax();
		if (b <= a) { // We go past 0
			b += getMax();
			if (k <= a)
				k += getMax();
		}
		return (a < k && k <= b);
	}
}
