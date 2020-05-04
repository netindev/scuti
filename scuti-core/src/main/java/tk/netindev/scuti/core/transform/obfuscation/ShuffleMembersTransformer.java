package tk.netindev.scuti.core.transform.obfuscation;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tk.netindev.scuti.core.configuration.Configuration;
import tk.netindev.scuti.core.transform.Transformer;

/**
 *
 * @author netindev
 *
 */
public class ShuffleMembersTransformer extends Transformer {

	private static final Logger LOGGER = LoggerFactory.getLogger(StringEncryptionTransformer.class.getName());

	public ShuffleMembersTransformer(final Configuration configuration, final Map<String, ClassNode> classes,
			final Map<String, ClassNode> dependencies) {
		super(configuration, classes, dependencies);
		LOGGER.info(" Shuffle Member Transformer ->");
	}

	@Override
	public void transform() {
		final AtomicInteger atomicInteger = new AtomicInteger();
		this.getClasses().values().forEach(classNode -> {
			Collections.shuffle(classNode.methods);
			atomicInteger.addAndGet(classNode.methods.size());
			Collections.shuffle(classNode.fields);
			atomicInteger.addAndGet(classNode.fields.size());
		});
		LOGGER.info(" - Shuffled " + atomicInteger.get() + " members");
	}

}
