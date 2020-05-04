package tk.netindev.scuti.core.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.CRC32;
import java.util.zip.ZipOutputStream;

import org.objectweb.asm.tree.ClassNode;

/**
 *
 * @author netindev
 *
 */
public class Util {

	public static byte[] toByteArray(final InputStream inputStream) throws IOException {
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		final byte[] buffer = new byte[0xFFFF];
		int length;
		while ((length = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, length);
		}
		outputStream.flush();
		return outputStream.toByteArray();
	}

	public static String xor(final String string, final int key) {
		final StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < string.length(); ++i) {
			stringBuilder.append((char) (string.charAt(i) ^ key));
		}
		return stringBuilder.toString();
	}

	public static byte[] xor(final byte[] array, final int key) {
		final byte[] bytes = new byte[array.length];
		for (int i = 0; i < array.length; ++i) {
			bytes[i] = (byte) (array[i] ^ key);
		}
		return bytes;
	}

	public static Map<String, ClassNode> sortByComparator(final Map<String, ClassNode> unsortMap) {
		final List<Entry<String, ClassNode>> list = new LinkedList<>(unsortMap.entrySet());
		Collections.sort(list, (first, second) -> first.getValue().name.compareTo(second.getValue().name));
		final Map<String, ClassNode> sortedMap = new LinkedHashMap<>();
		for (final Entry<String, ClassNode> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	public static void corruptCRC32(final ZipOutputStream outputStream) throws Exception {
		final Field field = ZipOutputStream.class.getDeclaredField("crc");
		field.setAccessible(true);
		field.set(outputStream, new CRC32() {

			@Override
			public void update(final byte[] bytes, final int i, final int length) {
				return;
			}

			@Override
			public long getValue() {
				return RandomUtil.getRandom(0, Integer.MAX_VALUE);
			}
		});
	}

}
