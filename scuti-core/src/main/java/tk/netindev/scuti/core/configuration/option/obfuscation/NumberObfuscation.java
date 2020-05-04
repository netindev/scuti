package tk.netindev.scuti.core.configuration.option.obfuscation;

import tk.netindev.scuti.core.configuration.Option;

/**
 *
 * @author netindev
 *
 */
public class NumberObfuscation extends Option {

	private boolean executeTwice;

	public boolean isExecuteTwice() {
		return this.executeTwice;
	}

	public void setExecuteTwice(final boolean executeTwice) {
		this.executeTwice = executeTwice;
	}

}
