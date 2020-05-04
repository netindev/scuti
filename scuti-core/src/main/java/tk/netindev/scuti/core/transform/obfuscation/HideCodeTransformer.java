package tk.netindev.scuti.core.transform.obfuscation;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tk.netindev.scuti.core.configuration.Configuration;
import tk.netindev.scuti.core.transform.Transformer;
import tk.netindev.scuti.core.util.ASMUtil;

/**
 *
 * @author netindev
 *
 */
public class HideCodeTransformer extends Transformer {

	private static final Logger LOGGER = LoggerFactory.getLogger(HideCodeTransformer.class.getName());
	private final AtomicInteger atomicInteger = new AtomicInteger();

	public HideCodeTransformer(final Configuration configuration, final Map<String, ClassNode> classes,
			final Map<String, ClassNode> dependencies) {
		super(configuration, classes, dependencies);
		LOGGER.info(" Hide Code Transformer ->");
	}

	@Override
	public void transform() {
		this.getClasses().values().forEach(classNode -> {
			this.pushSynthetic(classNode);
			this.pushBridge(classNode);
		});
		LOGGER.info(" - Inserted " + this.atomicInteger.get() + " synthetic/bridge");
	}

	private void pushSynthetic(final ClassNode classNode) {
		classNode.access |= Opcodes.ACC_SYNTHETIC;
		if (classNode.innerClasses.isEmpty()) {
			classNode.innerClasses.forEach(innerClass -> {
				innerClass.access |= Opcodes.ACC_SYNTHETIC;
				this.atomicInteger.incrementAndGet();
			});
		}
		classNode.methods.forEach(methodNode -> {
			methodNode.access |= Opcodes.ACC_SYNTHETIC;
			this.atomicInteger.incrementAndGet();
		});
		classNode.fields.forEach(fieldNode -> {
			fieldNode.access |= Opcodes.ACC_SYNTHETIC;
			this.atomicInteger.incrementAndGet();
		});
	}

	private void pushBridge(final ClassNode classNode) {
		classNode.methods.stream()
				.filter(methodNode -> !ASMUtil.isInitializer(methodNode) && !Modifier.isAbstract(methodNode.access))
				.forEach(methodNode -> {
					methodNode.access |= Opcodes.ACC_BRIDGE;
					this.atomicInteger.incrementAndGet();
				});
	}

}
