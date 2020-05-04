package tk.netindev.scuti.core.transform.obfuscation;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tk.netindev.scuti.core.configuration.Configuration;
import tk.netindev.scuti.core.configuration.option.obfuscation.StringEncryption.EncryptionType;
import tk.netindev.scuti.core.transform.Transformer;
import tk.netindev.scuti.core.util.RandomUtil;
import tk.netindev.scuti.core.util.StringUtil;
import tk.netindev.scuti.core.util.Util;

/**
 *
 * @author netindev
 *
 */
public class StringEncryptionTransformer extends Transformer {

	private static final Logger LOGGER = LoggerFactory.getLogger(StringEncryptionTransformer.class.getName());
	private final AtomicInteger atomicInteger = new AtomicInteger();

	public StringEncryptionTransformer(final Configuration configuration, final Map<String, ClassNode> classes,
			final Map<String, ClassNode> dependencies) {
		super(configuration, classes, dependencies);
		LOGGER.info(" String Encryption Transformer ->");
	}

	@Override
	public void transform() {
		this.getClasses().values().stream().filter(classNode -> !Modifier.isInterface(classNode.access))
				.forEach(classNode -> {
					final AtomicBoolean atomicBoolean = new AtomicBoolean();
					final String randomString = StringUtil.getRandomString();
					if (this.getConfiguration().getStringEncryption().getEncryptionType() == EncryptionType.FAST) {
						final int random = RandomUtil.getRandom(0x8, 0x800);
						classNode.methods.stream().filter(methodNode -> !Modifier.isAbstract(methodNode.access))
								.forEach(methodNode -> {
									Arrays.asList(methodNode.instructions.toArray()).stream()
											.filter(insnNode -> insnNode instanceof LdcInsnNode
													&& ((LdcInsnNode) insnNode).cst instanceof String)
											.forEach(insnNode -> {
												methodNode.instructions.insert(insnNode,
														new MethodInsnNode(Opcodes.INVOKESTATIC, classNode.name,
																randomString, "(Ljava/lang/String;)Ljava/lang/String;",
																false));
												((LdcInsnNode) insnNode).cst = this
														.xor((String) ((LdcInsnNode) insnNode).cst, random);
												this.atomicInteger.incrementAndGet();
												atomicBoolean.set(true);
											});
								});
						if (atomicBoolean.get()) {
							classNode.methods.add(this.createDecryptFast(randomString, random));
						}
					} else if (this.getConfiguration().getStringEncryption()
							.getEncryptionType() == EncryptionType.STRONG) {
						classNode.methods.stream().filter(methodNode -> !Modifier.isAbstract(methodNode.access))
								.forEach(methodNode -> {
									Arrays.stream(methodNode.instructions.toArray()).forEach(insnNode -> {
										if (insnNode instanceof LdcInsnNode
												&& ((LdcInsnNode) insnNode).cst instanceof String) {
											final LdcInsnNode ldcInsnNode = (LdcInsnNode) insnNode;
											final int random = RandomUtil.getRandom(0x8, 0x800);
											methodNode.instructions.insert(insnNode,
													this.getInsnListStrong(this.encryptStrong((String) ldcInsnNode.cst,
															randomString, random), random, classNode.name,
															randomString));
											methodNode.instructions.remove(ldcInsnNode);
											this.atomicInteger.incrementAndGet();
											atomicBoolean.set(true);
										}
									});
								});
						if (atomicBoolean.get()) {
							classNode.methods.add(this.createDecryptStrong(randomString));
						}
					} else {
						throw new RuntimeException("Unknown type value on EncryptionType");
					}
				});
		LOGGER.info(" - Encrypted " + this.atomicInteger.get() + " strings");
	}

	/* FAST */

	private MethodNode createDecryptFast(final String methodName, final int decryptValue) {
		final MethodNode methodNode = new MethodNode(
				Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_SYNTHETIC + Opcodes.ACC_BRIDGE, methodName,
				"(Ljava/lang/String;)Ljava/lang/String;", null, null);
		methodNode.visitCode();
		methodNode.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
		methodNode.visitInsn(Opcodes.DUP);
		methodNode.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
		methodNode.visitVarInsn(Opcodes.ASTORE, 1);
		methodNode.visitInsn(Opcodes.ICONST_0);
		methodNode.visitVarInsn(Opcodes.ISTORE, 2);
		final Label firstLabel = new Label();
		methodNode.visitJumpInsn(Opcodes.GOTO, firstLabel);
		final Label secondLabel = new Label();
		methodNode.visitLabel(secondLabel);
		methodNode.visitFrame(Opcodes.F_APPEND, 2, new Object[] { "java/lang/StringBuilder", Opcodes.INTEGER }, 0,
				null);
		methodNode.visitVarInsn(Opcodes.ALOAD, 1);
		methodNode.visitVarInsn(Opcodes.ALOAD, 0);
		methodNode.visitVarInsn(Opcodes.ILOAD, 2);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C", false);
		if (decryptValue > Byte.MAX_VALUE) {
			methodNode.visitIntInsn(Opcodes.SIPUSH, decryptValue);
		} else {
			methodNode.visitIntInsn(Opcodes.BIPUSH, decryptValue);
		}
		methodNode.visitInsn(Opcodes.IXOR);
		methodNode.visitInsn(Opcodes.I2C);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
				"(C)Ljava/lang/StringBuilder;", false);
		methodNode.visitInsn(Opcodes.POP);
		methodNode.visitIincInsn(2, 1);
		methodNode.visitLabel(firstLabel);
		methodNode.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		methodNode.visitVarInsn(Opcodes.ILOAD, 2);
		methodNode.visitVarInsn(Opcodes.ALOAD, 0);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);
		methodNode.visitJumpInsn(Opcodes.IF_ICMPLT, secondLabel);
		methodNode.visitVarInsn(Opcodes.ALOAD, 1);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;",
				false);
		methodNode.visitInsn(Opcodes.ARETURN);
		methodNode.visitMaxs(3, 3);
		methodNode.visitEnd();
		return methodNode;
	}

	private String xor(final String string, final int xor) {
		final StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < string.length(); i++) {
			stringBuilder.append((char) (string.charAt(i) ^ xor));
		}
		return stringBuilder.toString();
	}

	/* STRONG */

	private InsnList getInsnListStrong(final String encrypt, final int random, final String className,
			final String methodName) {
		int even = RandomUtil.getRandom(1, 9);
		if (even % 2 != 0) {
			even = even + 1;
		}
		final InsnList insnList = new InsnList();
		insnList.add(new LdcInsnNode(encrypt));
		insnList.add(new LdcInsnNode(new Integer(random)));
		if (RandomUtil.getRandom()) {
			insnList.add(new InsnNode(Opcodes.SWAP));
			insnList.add(new InsnNode(Opcodes.SWAP));
		}
		for (int i = 0; i < even; i++) {
			insnList.add(new InsnNode(Opcodes.INEG));
		}
		insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, className, methodName,
				"(Ljava/lang/String;I)Ljava/lang/String;", false));
		return insnList;
	}

	private String encryptStrong(final String string, final String methodName, final int random) {
		final int xor = random ^ methodName.hashCode();
		final char[] array = new char[string.length()];
		for (int i = 0; i < string.length(); i++) {
			array[i] = (char) (string.charAt(i) ^ xor);
		}
		return new StringBuilder(string.length()).append(array).toString();
	}

	private String decryptStrong(final String string, final int random) {
		final StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[2];
		String methodName;
		if (stackTrace.getMethodName() == null) {
			methodName = "";
		} else {
			methodName = stackTrace.getMethodName();
		}
		final int xor = random ^ methodName.hashCode();
		final char[] array = new char[string.length()];
		for (int i = 0; i < string.length(); i++) {
			array[i] = (char) (string.charAt(i) ^ xor);
		}
		return new String(array);
	}

	private MethodNode createDecryptStrong(final String methodName) {
		final MethodNode methodNode = new MethodNode(
				Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_SYNTHETIC + Opcodes.ACC_BRIDGE, methodName,
				"(Ljava/lang/String;I)Ljava/lang/String;", null, null);
		final Label firstLabel = new Label(), secondLabel = new Label(), thirdLabel = new Label(),
				fourthLabel = new Label();
		methodNode.visitCode();
		methodNode.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;",
				false);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Thread", "getStackTrace",
				"()[Ljava/lang/StackTraceElement;", false);
		methodNode.visitInsn(Opcodes.ICONST_1);
		methodNode.visitInsn(Opcodes.AALOAD);
		methodNode.visitVarInsn(Opcodes.ASTORE, 2);
		methodNode.visitVarInsn(Opcodes.ALOAD, 2);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StackTraceElement", "getMethodName",
				"()Ljava/lang/String;", false);
		methodNode.visitJumpInsn(Opcodes.IFNONNULL, firstLabel);
		methodNode.visitLdcInsn("");
		methodNode.visitVarInsn(Opcodes.ASTORE, 3);
		methodNode.visitJumpInsn(Opcodes.GOTO, secondLabel);
		methodNode.visitLabel(firstLabel);
		methodNode.visitFrame(Opcodes.F_APPEND, 1, new Object[] { "java/lang/StackTraceElement" }, 0, null);
		methodNode.visitVarInsn(Opcodes.ALOAD, 2);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StackTraceElement", "getMethodName",
				"()Ljava/lang/String;", false);
		methodNode.visitVarInsn(Opcodes.ASTORE, 3);
		methodNode.visitLabel(secondLabel);
		methodNode.visitFrame(Opcodes.F_APPEND, 1, new Object[] { "java/lang/String" }, 0, null);
		methodNode.visitVarInsn(Opcodes.ALOAD, 3);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
		methodNode.visitVarInsn(Opcodes.ISTORE, 4);
		methodNode.visitVarInsn(Opcodes.ILOAD, 1);
		methodNode.visitVarInsn(Opcodes.ILOAD, 4);
		methodNode.visitInsn(Opcodes.IXOR);
		methodNode.visitVarInsn(Opcodes.ISTORE, 5);
		methodNode.visitVarInsn(Opcodes.ALOAD, 0);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);
		methodNode.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_CHAR);
		methodNode.visitVarInsn(Opcodes.ASTORE, 6);
		methodNode.visitInsn(Opcodes.ICONST_0);
		methodNode.visitVarInsn(Opcodes.ISTORE, 7);
		methodNode.visitJumpInsn(Opcodes.GOTO, thirdLabel);
		methodNode.visitLabel(fourthLabel);
		methodNode.visitFrame(
				Opcodes.F_FULL, 8, new Object[] { "java/lang/String", Opcodes.INTEGER, "java/lang/StackTraceElement",
						"java/lang/String", Opcodes.INTEGER, Opcodes.INTEGER, "[C", Opcodes.INTEGER },
				0, new Object[] {});
		methodNode.visitVarInsn(Opcodes.ALOAD, 6);
		methodNode.visitVarInsn(Opcodes.ILOAD, 7);
		methodNode.visitVarInsn(Opcodes.ALOAD, 0);
		methodNode.visitVarInsn(Opcodes.ILOAD, 7);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C", false);
		methodNode.visitVarInsn(Opcodes.ILOAD, 5);
		methodNode.visitInsn(Opcodes.IXOR);
		methodNode.visitInsn(Opcodes.I2C);
		methodNode.visitInsn(Opcodes.CASTORE);
		methodNode.visitIincInsn(7, 1);
		methodNode.visitLabel(thirdLabel);
		methodNode.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		methodNode.visitVarInsn(Opcodes.ILOAD, 7);
		methodNode.visitVarInsn(Opcodes.ALOAD, 0);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);
		methodNode.visitJumpInsn(Opcodes.IF_ICMPLT, fourthLabel);
		methodNode.visitTypeInsn(Opcodes.NEW, "java/lang/String");
		methodNode.visitInsn(Opcodes.DUP);
		methodNode.visitVarInsn(Opcodes.ALOAD, 6);
		methodNode.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/String", "<init>", "([C)V", false);
		methodNode.visitInsn(Opcodes.ARETURN);
		methodNode.visitMaxs(4, 8);
		methodNode.visitEnd();
		return methodNode;
	}

}
