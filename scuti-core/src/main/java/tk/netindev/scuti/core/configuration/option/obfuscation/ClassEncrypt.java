package tk.netindev.scuti.core.configuration.option.obfuscation;

import tk.netindev.scuti.core.configuration.Option;
import tk.netindev.scuti.core.util.RandomUtil;

/**
 *
 * @author netindev
 *
 */
public class ClassEncrypt extends Option {

	private String loaderName, mainClass;
	private int stringKey, classKey;

	public String getLoaderName() {
		return this.loaderName;
	}

	public void setLoaderName(final String loaderName) {
		this.loaderName = loaderName;
	}

	public String getMainClass() {
		return this.mainClass;
	}

	public void setMainClass(final String mainClass) {
		this.mainClass = mainClass;
	}

	public int getStringKey() {
		return this.stringKey;
	}

	public void setStringKey(final int stringKey) {
		this.stringKey = stringKey;
	}

	public int getClassKey() {
		return this.classKey;
	}

	public void setClassKey(final int classKey) {
		this.classKey = classKey;
	}

	/* default config */ {
		this.setLoaderName("ScutiTemplate");
		this.setMainClass("MainClass");
		this.setStringKey(RandomUtil.getRandom(20, 40));
		this.setClassKey(RandomUtil.getRandom(20, 40));
	}

}
