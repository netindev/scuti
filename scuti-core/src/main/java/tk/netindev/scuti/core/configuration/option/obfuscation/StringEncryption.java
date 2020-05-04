package tk.netindev.scuti.core.configuration.option.obfuscation;

import tk.netindev.scuti.core.configuration.Option;

/**
 *
 * @author netindev
 *
 */
public class StringEncryption extends Option {

	private EncryptionType encryptionType;

	public EncryptionType getEncryptionType() {
		return this.encryptionType;
	}

	public void setEncryptionType(final EncryptionType encryptionType) {
		this.encryptionType = encryptionType;
	}

	public enum EncryptionType {
		FAST, STRONG
	}

	/* default config */ {
		this.setEncryptionType(EncryptionType.FAST);
	}

}
