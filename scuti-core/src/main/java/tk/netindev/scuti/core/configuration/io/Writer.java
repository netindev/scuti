package tk.netindev.scuti.core.configuration.io;

import java.io.File;
import java.io.FileWriter;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import tk.netindev.scuti.core.configuration.Configuration;
import tk.netindev.scuti.core.transform.Transformers.Obfuscation;

/**
 *
 * @author netindev
 *
 */
public class Writer {

	public Writer(final File file, final Configuration configution) throws Exception {
		this.write(file, configution);
	}

	void write(final File file, final Configuration configuration) throws Exception {
		final JsonObject object = new JsonObject();
		object.add("input", configuration.getInput().getAbsolutePath());
		object.add("output", configuration.getOutput().getAbsolutePath());

		final JsonArray libraries = new JsonArray();
		configuration.getDependencies().forEach(library -> libraries.add(library.getAbsolutePath()));
		object.add("libraries", libraries);

		final JsonObject obfuscation = new JsonObject();
		object.add("obfusacation", obfuscation);

		final JsonObject encrypt = new JsonObject();
		encrypt.add("enable", configuration.getTransformers().contains(Obfuscation.CLASS_ENCRYPT_TRANSFORMER));
		encrypt.add("loader_name", configuration.getClassEncrypt().getLoaderName());
		encrypt.add("main_class", configuration.getClassEncrypt().getMainClass());
		encrypt.add("string_key", configuration.getClassEncrypt().getStringKey());
		encrypt.add("class_key", configuration.getClassEncrypt().getClassKey());

		obfuscation.add("encrypt", encrypt);

		final JsonObject flow = new JsonObject();
		flow.add("enable", configuration.getTransformers().contains(Obfuscation.CONTROL_FLOW_TRANSFORMER));

		obfuscation.add("flow", flow);

		final JsonObject hide = new JsonObject();
		hide.add("enable", configuration.getTransformers().contains(Obfuscation.HIDE_CODE_TRANSFORMER));

		object.add("obfuscation", obfuscation);

		final FileWriter writer = new FileWriter(file);
		object.writeTo(writer);
		writer.close();
	}

}
