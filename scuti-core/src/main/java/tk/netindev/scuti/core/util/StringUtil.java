package tk.netindev.scuti.core.util;

import java.text.DecimalFormat;

/**
 * 
 * @author netindev
 *
 */
public class StringUtil {

	private static final StringBuilder MASSIVE_STRING;

	public static String getMassiveString() {
		if (MASSIVE_STRING.length() > 0) {
			return MASSIVE_STRING.toString();
		}
		for (int i = 0; i < Short.MAX_VALUE; i++) {
			MASSIVE_STRING.append(" ");
		}
		return MASSIVE_STRING.toString();
	}

	public static String getRandomString() {
		return Long.toHexString(Double.doubleToLongBits(Math.random()));
	}

	public static String getFileSize(final long size) {
		final DecimalFormat decimalFormat = new DecimalFormat("0.00");
		final float kilobytes = 1024.0F, megabytes = kilobytes * kilobytes;
		return size < megabytes ? decimalFormat.format(size / kilobytes) + " KB"
				: decimalFormat.format(size / megabytes) + " MB";
	}

	static {
		MASSIVE_STRING = new StringBuilder(Short.MAX_VALUE - 1);
	}

}
