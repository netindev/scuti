package tk.netindev.scuti.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tk.netindev.scuti.core.configuration.Configuration;
import tk.netindev.scuti.core.configuration.io.Parser;
import tk.netindev.scuti.core.exception.ClassNotFoundException;
import tk.netindev.scuti.core.rewrite.CustomWriter;
import tk.netindev.scuti.core.rewrite.Hierarchy;
import tk.netindev.scuti.core.transform.Transformer;
import tk.netindev.scuti.core.transform.Transformers.Obfuscation;
import tk.netindev.scuti.core.transform.Transformers.Optimization;
import tk.netindev.scuti.core.transform.Transformers.Shrinking;
import tk.netindev.scuti.core.util.StringUtil;
import tk.netindev.scuti.core.util.Util;

/**
 *
 * @author netindev
 *
 */
public class Scuti {

	private static final Logger LOGGER = LoggerFactory.getLogger(Scuti.class.getClass());
	private static final double PACKAGE_VERSION = 1.4D;

	private ZipOutputStream outputStream;

	private final long START_TIME;

	private final ExecutorService executorService = Executors
			.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	private final Configuration configuration;

	final Map<String, ClassNode> classes, dependencies;
	final List<Class<? extends Transformer>> ordered;

	Hierarchy hierarchy;

	public static void main(final String[] args) {
		System.out.println("Scuti java obfuscator, written by netindev, V: " + Scuti.PACKAGE_VERSION);
		if (args == null || args.length == 0) {
			System.err.println(
					"You need to inform your configuration file in options section.\nCorrect usage: \"java -jar scuti.jar configuration.json\".");
		} else {
			final File file = new File(args[0]);
			if (file == null || !file.isFile() || !file.canRead()) {
				System.err.println("Your configuration file \"" + file.getPath() + "\" isn't accessible.");
			}
			LOGGER.info("Initialize..");
			try {
				LOGGER.info(" Parsing configuration \"" + file.getPath() + "\"");
				new Scuti(new Parser(file));
			} catch (final Exception e) {
				LOGGER.error("Initialization exception occurred, message: " + e.getMessage());
			}
		}
	}

	public Scuti(final Configuration configuration) throws Exception {
		this.START_TIME = System.nanoTime();
		this.configuration = Objects.requireNonNull(configuration);
		this.init();
	}

	private void init() throws Exception {
		final File input = this.configuration.getInput();
		if (!input.canRead() || !input.isFile()) {
			throw new Exception("Your input file \"" + input.getName() + "\" isn't accessible.");
		}
		for (final File file : this.configuration.getDependencies()) {
			if (!file.exists() || !file.canRead()) {
				throw new Exception("The classpath: \"" + file.getAbsolutePath() + "\" isn't accessible");
			}
		}
		if (this.configuration.getTransformers().isEmpty()) {
			throw new Exception("No transformer available");
		} else {
			this.order();
		}
		final JarFile jarFile = new JarFile(input);
		if (jarFile.getManifest() == null) {
			this.outputStream = new JarOutputStream(new FileOutputStream(this.configuration.getOutput()), null);
		} else {
			final Manifest manifest = jarFile.getManifest();
			final Attributes attributes = manifest.getMainAttributes();
			if (this.configuration.getTransformers().contains(Obfuscation.CLASS_ENCRYPT_TRANSFORMER)) {
				attributes.put(Name.MAIN_CLASS, this.configuration.getClassEncrypt().getLoaderName());
			}
			this.outputStream = new JarOutputStream(new FileOutputStream(this.configuration.getOutput()), manifest);
		}
		jarFile.close();
		this.executorService.execute(new Runner());
	}

	private void walk(final File file, final boolean dependency) throws Exception {
		final JarFile jarFile = new JarFile(file);
		if (dependency) {
			jarFile.stream().filter(entries -> entries.getName().endsWith(".class") && !entries.isDirectory())
					.forEach(entry -> {
						try (InputStream stream = jarFile.getInputStream(entry)) {
							final ClassReader classReader = new ClassReader(stream);
							final ClassNode classNode = new ClassNode();
							classReader.accept(classNode, 0x7 /* FRAMES | DEBUG | CODE */);
							this.dependencies.put(classNode.name, classNode);
						} catch (final Exception e) {
							LOGGER.error("Error on entry: " + entry.getName() + " with exception: " + e.getMessage());
						}
					});
			jarFile.close();
		} else {
			jarFile.stream().filter(entries -> !entries.isDirectory()).forEach(entry -> {
				try (InputStream stream = jarFile.getInputStream(entry)) {
					if (entry.getName().endsWith(".class") && entry.getSize() != 0x0) {
						final ClassReader classReader = new ClassReader(stream);
						final ClassNode classNode = new ClassNode();
						classReader.accept(classNode, 0x6 /* FRAMES | DEBUG */);
						this.classes.put(classNode.name, classNode);
					} else {
						if (!entry.getName().contains("META-INF/MANIFEST.MF")) {
							this.outputStream.putNextEntry(new ZipEntry(entry.getName()));
							this.outputStream.write(Util.toByteArray(stream));
						}
					}
				} catch (final Exception e) {
					LOGGER.error("Error on entry: " + entry.getName() + " with exception: " + e.getMessage());
				}
			});
			jarFile.close();
		}
	}

	private void dump() throws Exception {
		LOGGER.info(" Dumping into output \"" + Scuti.this.configuration.getOutput().getName() + "\"");
		if (this.configuration.corruptCRC32()) {
			LOGGER.info(" Corrupting \"" + this.configuration.getOutput().getName() + "\"");
			Util.corruptCRC32(this.outputStream);
		}
		this.classes.values().forEach(classNode -> {
			ClassWriter classWriter = new CustomWriter(this.hierarchy, ClassWriter.COMPUTE_FRAMES);
			try {
				classNode.accept(classWriter);
			} catch (final Exception e) {
				if (e.getMessage() != null) {
					if (e instanceof ClassNotFoundException) {
						LOGGER.warn(e.getMessage() + "\" could not be found while writing \"" + classNode.name
								+ "\". Forcing COMPUTE_MAXS");
					} else if (e.getMessage().contains("JSR/RET")) {
						LOGGER.warn("JSR/RET found on: \"" + classNode.name + "\". Forcing COMPUTE_MAXS");
					}
					classWriter = new CustomWriter(this.hierarchy, ClassWriter.COMPUTE_MAXS);
					classNode.accept(classWriter);
				} else {
					LOGGER.error("Unknown error while accept: " + classNode.name + ", ignoring...");
				}
			}
			this.write(classNode.name, classWriter.toByteArray());
		});
		LOGGER.info("Closing output stream..");
		this.outputStream.close();
		LOGGER.info("Done in " + String.format("%.2f", (System.nanoTime() - this.START_TIME) / 1e9)
				+ " seconds, output file size: " + StringUtil.getFileSize(this.configuration.getOutput().length()));
		this.executorService.shutdown();
	}

	/**
	 *
	 * @author netindev
	 *
	 */
	final class Runner extends Thread {

		private final Logger logger = LoggerFactory.getLogger(Runner.class.getClass());

		@Override
		public void run() {
			try {
				this.logger.info("Walk..");
				Scuti.this.configuration.getDependencies().forEach(file -> {
					this.logger.info(" Walking into dependency \"" + file.getName() + "\", size: "
							+ StringUtil.getFileSize(file.length()));
					try {
						Scuti.this.walk(file, true);
					} catch (final Exception e) {
						e.printStackTrace();
					}
				});
				this.logger.info(" Walking into input \"" + Scuti.this.configuration.getInput().getName() + "\", size: "
						+ StringUtil.getFileSize(Scuti.this.configuration.getInput().length()));
				try {
					Scuti.this.walk(Scuti.this.configuration.getInput(), false);
				} catch (final Exception e) {
					e.printStackTrace();
				}
				this.logger.info("Transform..");
				for (final Class<? extends Transformer> clazz : Scuti.this.ordered) {
					clazz.getConstructor(Configuration.class, Map.class, Map.class)
							.newInstance(Scuti.this.configuration, Scuti.this.classes, Scuti.this.dependencies)
							.transform();
				}
				LOGGER.info("Hierarchy..");
				Scuti.this.classes.values().forEach(classNode -> Scuti.this.hierarchy.buildHierarchy(classNode, null));
				this.logger.info("Write..");
				Scuti.this.dump();
			} catch (final Exception e) {
				this.logger.error("Unknown error while executing main thread");
				e.printStackTrace();
				if (e.getMessage() != null) {
					this.logger.error("Exception message: " + e.getMessage());
				} else {
					this.logger.error("Stack trace: ", e);
				}
				this.logger.error("Exiting...");
				Scuti.this.executorService.shutdown();
			}
		}

	}

	void order() {
		if (this.configuration.getTransformers().contains(Obfuscation.INVOKE_DYNAMIC_TRANSFORMER)
				&& this.configuration.getTransformers().contains(Obfuscation.CLASS_ENCRYPT_TRANSFORMER)) {
			throw new RuntimeException(
					"Encrypt and Invoke Dynamic doesn't work together, please select just ONE of them.");
		}

		/* order transformers */
		final Class<?>[] transformers = { Obfuscation.CLASS_ENCRYPT_TRANSFORMER,

				Shrinking.INNER_CLASS_TRANSFORMER, Shrinking.UNUSED_MEMBER_TRANSFORMER,

				Optimization.NO_OPERATION_TRANSFORMER, Optimization.DEAD_CODE_TRANSFORMER,

				Obfuscation.RENAME_MEMBERS_TRANSFORMER, Obfuscation.STRING_ENCRYPTION_TRANSFORMER,
				Obfuscation.INVOKE_DYNAMIC_TRANSFORMER, Obfuscation.HIDE_CODE_TRANSFORMER,
				Obfuscation.MISCELLANEOUS_OBFUSCATION_TRANSFORMER, Obfuscation.NUMBER_OBFUSCATION_TRANSFORMER,
				Obfuscation.CONTROL_FLOW_TRANSFORMER, Obfuscation.SHUFFLE_MEMBERS_TRANSFORMER };

		Stream.of(transformers).filter(transformer -> this.configuration.getTransformers().contains(transformer))
				.forEach(transformer -> this.ordered.add((Class<? extends Transformer>) transformer));
	}

	void write(final String className, final byte[] bytecode) {
		try {
			if (this.configuration.getTransformers().contains(Obfuscation.CLASS_ENCRYPT_TRANSFORMER)
					&& !className.equals(this.configuration.getClassEncrypt().getLoaderName())) {
				this.outputStream.putNextEntry(
						new JarEntry(Util.xor(className, this.configuration.getClassEncrypt().getStringKey())));
				this.outputStream.write(Util.xor(bytecode, this.configuration.getClassEncrypt().getClassKey()));
			} else {
				this.outputStream.putNextEntry(new JarEntry(className.concat(".class" + (this.configuration.corruptNames() ? "/" : ""))));
				this.outputStream.write(bytecode);
			}
		} catch (final Exception e) {
			LOGGER.error("Error while writing " + className + " with exception: " + e.getMessage());
		}
	}

	/* init */ {
		this.classes = Collections.synchronizedMap(new HashMap<>());
		this.dependencies = Collections.synchronizedMap(new HashMap<>());
		this.ordered = Collections.synchronizedList(new ArrayList<>());

		this.hierarchy = new Hierarchy(this.classes, this.dependencies);
	}

}
