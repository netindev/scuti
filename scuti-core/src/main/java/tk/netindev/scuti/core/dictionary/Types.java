package tk.netindev.scuti.core.dictionary;

import tk.netindev.scuti.core.configuration.Configuration;
import tk.netindev.scuti.core.dictionary.type.Alphabet;
import tk.netindev.scuti.core.dictionary.type.Custom;
import tk.netindev.scuti.core.dictionary.type.Number;
import tk.netindev.scuti.core.dictionary.type.Randomized;

/**
 *
 * @author netindev
 *
 */
public enum Types {

	ALPHABET, NUMBER, RANDOMIZED, CUSTOM;

	public static enum CustomDictionary {
		CLASSES, FIELDS, METHODS, PACKAGES
	}

	public static Dictionary getDictionary(final Configuration configuration, final CustomDictionary customDictionary) {
		switch (configuration.getRenameMembers().getRandomize()) {
		case ALPHABET:
			return new Alphabet();
		case NUMBER:
			return new Number();
		case RANDOMIZED:
			return new Randomized();
		case CUSTOM:
			switch (customDictionary) {
			case CLASSES:
				return new Custom(configuration.getRenameMembers().getClassesDictionary());
			case FIELDS:
				return new Custom(configuration.getRenameMembers().getFieldsDictionary());
			case METHODS:
				return new Custom(configuration.getRenameMembers().getMethodsDictionary());
			case PACKAGES:
				return new Custom(configuration.getRenameMembers().getPackagesDictionary());
			}
		default:
			return new Alphabet();
		}
	}

}
