package tk.netindev.scuti.core.transform.optimization;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.Opcodes;
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
public class DeadCodeTransformer extends Transformer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeadCodeTransformer.class.getName());

	public DeadCodeTransformer(final Configuration configuration, final Map<String, ClassNode> classes,
			final Map<String, ClassNode> dependencies) {
		super(configuration, classes, dependencies);
		LOGGER.info(" Dead Code Transformer -> ");
	}

	@Override
	public void transform() {
		final AtomicInteger insnCount = new AtomicInteger();
		this.getClasses().values()
				.forEach(classNode -> classNode.methods.stream().filter(
						methodNode -> (!Modifier.isAbstract(methodNode.access) || !Modifier.isNative(methodNode.access))
								&& methodNode.instructions.getFirst() != null)
						.forEach(methodNode -> {
							Arrays.stream(methodNode.instructions.toArray()).forEach(insnNode -> {
								if (insnNode.getOpcode() == Opcodes.POP) {
									if (insnNode.getPrevious().getOpcode() == Opcodes.ILOAD
											|| insnNode.getPrevious().getOpcode() == Opcodes.FLOAD
											|| insnNode.getPrevious().getOpcode() == Opcodes.ALOAD
											|| insnNode.getPrevious().getOpcode() == Opcodes.LLOAD) {
										methodNode.instructions.remove(insnNode.getPrevious());
										methodNode.instructions.remove(insnNode);
										insnCount.addAndGet(2);
									}
								} else if (insnNode.getOpcode() == Opcodes.POP2) {
									if (insnNode.getPrevious().getOpcode() == Opcodes.DLOAD
											|| insnNode.getPrevious().getOpcode() == Opcodes.LLOAD) {
										methodNode.instructions.remove(insnNode.getPrevious());
										methodNode.instructions.remove(insnNode);
										insnCount.addAndGet(2);
									} else if (insnNode.getPrevious().getOpcode() == Opcodes.ILOAD
											|| insnNode.getPrevious().getOpcode() == Opcodes.FLOAD
											|| insnNode.getPrevious().getOpcode() == Opcodes.ALOAD) {
										if (insnNode.getPrevious().getPrevious().getOpcode() == Opcodes.ILOAD
												|| insnNode.getPrevious().getPrevious().getOpcode() == Opcodes.FLOAD
												|| insnNode.getPrevious().getPrevious().getOpcode() == Opcodes.ALOAD) {
											methodNode.instructions.remove(insnNode.getPrevious().getPrevious());
											methodNode.instructions.remove(insnNode.getPrevious());
											methodNode.instructions.remove(insnNode);
											insnCount.addAndGet(3);
										}
									}
								}
							});
						}));
		if (insnCount.get() > 0) {
			LOGGER.info(" - Removed " + insnCount.get() + " instructions");
		}
	}

}
