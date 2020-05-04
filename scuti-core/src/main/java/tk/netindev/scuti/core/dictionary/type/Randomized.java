package tk.netindev.scuti.core.dictionary.type;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import tk.netindev.scuti.core.dictionary.Dictionary;

/**
 *
 * @author netindev
 *
 */
public class Randomized implements Dictionary {

	private final char[] character = { '\u2000', '\u2001', '\u2002', '\u2003', '\u2004', '\u2005', '\u2006', '\u2007',
			'\u2008', '\u2009', '\u200A', '\u200B', '\u200C', '\u200D', '\u200E', '\u200F' };

	private final Set<String> set;
	private final Alphabet alphabet;

	@Override
	public String next() {
		final Random random = new Random();
		final StringBuilder builder = new StringBuilder(5);
		for (int i = 0; i < 6; i++) {
			builder.append(this.character[random.nextInt(this.character.length)]);
		}
		if (this.set.contains(builder.toString())) {
			return this.alphabet.next();
		}
		this.set.add(builder.toString());
		return builder.toString();
	}

	@Override
	public void reset() {
		/* no sense */
	}

	/* init */ {
		this.set = new HashSet<>();
		this.alphabet = new Alphabet();
	}

}
