package tk.netindev.scuti.core.transform.obfuscation;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tk.netindev.scuti.core.configuration.Configuration;
import tk.netindev.scuti.core.transform.Transformer;
import tk.netindev.scuti.core.util.ASMUtil;
import tk.netindev.scuti.core.util.RandomUtil;
import tk.netindev.scuti.core.util.Util;

/**
 *
 * @author netindev
 *
 */
public class NumberObfuscationTransformer extends Transformer {

	private static final Logger LOGGER = LoggerFactory.getLogger(NumberObfuscationTransformer.class.getName());

	public NumberObfuscationTransformer(final Configuration configuration, final Map<String, ClassNode> classes,
			final Map<String, ClassNode> dependencies) {
		super(configuration, classes, dependencies);
		LOGGER.info(" Number Obfuscation Transformer ->");
	}

	@Override
	public void transform() {
		AtomicInteger atomicInteger = new AtomicInteger();
		this.getClasses().values().forEach(classNode -> {
			classNode.methods.stream().filter(
					methodNode -> !Modifier.isAbstract(methodNode.access) && !Modifier.isNative(methodNode.access))
					.forEach(methodNode -> {
						for (int i = 0; i < (this.getConfiguration().getNumberObfuscation().isExecuteTwice() ? 2
								: 1); i++) {
							Arrays.stream(methodNode.instructions.toArray())
									.filter(insnNode -> insnNode.getOpcode() != Opcodes.NEWARRAY).forEach(insnNode -> {
										if (insnNode.getOpcode() >= 0x2 && insnNode.getOpcode() <= 0x8) {
											methodNode.instructions.insertBefore(insnNode,
													this.getInsnList(insnNode.getOpcode() - 0x3));
											methodNode.instructions.remove(insnNode);
											atomicInteger.incrementAndGet();
										} else if (insnNode instanceof IntInsnNode) {
											final int operand = ((IntInsnNode) insnNode).operand;
											methodNode.instructions.insertBefore(insnNode, this.getInsnList(operand));
											methodNode.instructions.remove(insnNode);
											atomicInteger.incrementAndGet();
										} else if (insnNode instanceof LdcInsnNode
												&& ((LdcInsnNode) insnNode).cst instanceof Integer) {
											final int value = (int) ((LdcInsnNode) insnNode).cst;
											if (value < -(Short.MAX_VALUE * 8) + Integer.MAX_VALUE) {
												methodNode.instructions.insertBefore(insnNode, this.getInsnList(value));
												methodNode.instructions.remove(insnNode);
												atomicInteger.incrementAndGet();
											}
										}

									});
						}
					});
		});
		LOGGER.info(" - Obfuscated " + atomicInteger.get() + " numbers");
	}

	private InsnList getInsnList(final int value) {
		return /* Util.getRandom() ? */this.xor(value);// : sub(value);
	}

	private InsnList xor(final int value) {
		final InsnList insnList = new InsnList();
		final int first = RandomUtil.getRandom(Short.MAX_VALUE) + value,
				second = -RandomUtil.getRandom(Short.MAX_VALUE) + value;
		final int random = RandomUtil.getRandom(Short.MAX_VALUE);
		insnList.add(ASMUtil.toInsnNode(first ^ value));
		insnList.add(ASMUtil.toInsnNode(second ^ value + random));
		insnList.add(new InsnNode(Opcodes.IXOR));
		insnList.add(ASMUtil.toInsnNode(first ^ value + random));
		insnList.add(new InsnNode(Opcodes.IXOR));
		insnList.add(ASMUtil.toInsnNode(second));
		insnList.add(new InsnNode(Opcodes.IXOR));
		return insnList;
	}

	/*
	 * private InsnList sub(int value) { final InsnList insnList = new InsnList();
	 * final int first = Util.getRandom(Short.MAX_VALUE) + value, second =
	 * -Util.getRandom(Short.MAX_VALUE) + value; final int random =
	 * Util.getRandom(Short.MAX_VALUE); insnList.add(ASM.toInsnNode(first + value));
	 * insnList.add(ASM.toInsnNode(second + (value + random))); insnList.add(new
	 * InsnNode(Opcodes.ISUB)); insnList.add(ASM.toInsnNode(first - (value +
	 * random))); insnList.add(new InsnNode(Opcodes.ISUB));
	 * insnList.add(ASM.toInsnNode(second - value + value)); insnList.add(new
	 * InsnNode(Opcodes.IADD)); return insnList; }
	 */

}
