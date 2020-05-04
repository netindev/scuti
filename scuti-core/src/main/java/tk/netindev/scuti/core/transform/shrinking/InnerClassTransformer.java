package tk.netindev.scuti.core.transform.shrinking;

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
public class InnerClassTransformer extends Transformer {

	private static final Logger LOGGER = LoggerFactory.getLogger(InnerClassTransformer.class.getName());

	public InnerClassTransformer(final Configuration configuration, final Map<String, ClassNode> classes,
			final Map<String, ClassNode> dependencies) {
		super(configuration, classes, dependencies);
		LOGGER.info(" Inner Class Tranformer ->");
	}

	@Override
	public void transform() {
		final AtomicInteger innerCount = new AtomicInteger();
		this.getClasses().values().forEach(classNode -> {
			classNode.outerClass = null;
			classNode.outerMethod = null;
			classNode.outerMethodDesc = null;

			innerCount.addAndGet(classNode.innerClasses.size());
			classNode.innerClasses.clear();
		});
		if (innerCount.get() > 0) {
			LOGGER.info(" - Removed " + innerCount.get() + " inner classes");
		}
	}

}
