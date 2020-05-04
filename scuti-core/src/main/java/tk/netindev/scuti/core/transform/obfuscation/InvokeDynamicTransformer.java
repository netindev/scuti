package tk.netindev.scuti.core.transform.obfuscation;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tk.netindev.scuti.core.configuration.Configuration;
import tk.netindev.scuti.core.transform.Transformer;
import tk.netindev.scuti.core.util.ASMUtil;
import tk.netindev.scuti.core.util.RandomUtil;
import tk.netindev.scuti.core.util.StringUtil;
import tk.netindev.scuti.core.util.Util;

/**
 *
 * @author netindev
 *
 */
public class InvokeDynamicTransformer extends Transformer {

	private static final Logger LOGGER = LoggerFactory.getLogger(InvokeDynamicTransformer.class);
	private final AtomicInteger atomicInteger = new AtomicInteger();

	public InvokeDynamicTransformer(final Configuration configuration, final Map<String, ClassNode> classes,
			final Map<String, ClassNode> dependencies) {
		super(configuration, classes, dependencies);
		LOGGER.info(" Invoke Dynamic Transformer ->");
	}

	@Override
	public void transform() {
		this.getClasses().values().stream()
				.filter(classNode -> !Modifier.isInterface(classNode.access)
						&& (classNode.access & Opcodes.ACC_ENUM) == 0 && classNode.version >= Opcodes.V1_7)
				.forEach(classNode -> {
					final String bootstrapName = StringUtil.getMassiveString(),
							decryptName = StringUtil.getMassiveString();
					final int decryptValue = RandomUtil.getRandom(100, 150);
					if (this.insertDynamic(classNode, bootstrapName, decryptName, decryptValue)) {
						classNode.methods.add(this.createDecrypt(decryptName, decryptValue));
						classNode.methods.add(this.createBootstrap(classNode.name, bootstrapName, decryptName));
					}
				});
		LOGGER.info(" - Replaced " + this.atomicInteger.get() + " calls");
	}

	private boolean insertDynamic(final ClassNode classNode, final String bootstrapName, final String decryptName,
			final int decryptValue) {
		final AtomicBoolean atomicBoolean = new AtomicBoolean();
		classNode.methods.stream().filter(methodNode -> !Modifier.isAbstract(methodNode.access)).forEach(methodNode -> {
			Arrays.asList(methodNode.instructions.toArray()).stream()
					.filter(insnNode -> (insnNode instanceof MethodInsnNode || insnNode instanceof FieldInsnNode)
							&& insnNode.getOpcode() != Opcodes.INVOKESPECIAL)
					.forEach(insnNode -> {
						if (insnNode instanceof MethodInsnNode) {
							final MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
							final Handle handle = new Handle(Opcodes.H_INVOKESTATIC, classNode.name, bootstrapName,
									MethodType.methodType(CallSite.class, MethodHandles.Lookup.class, String.class,
											MethodType.class, String.class, String.class, String.class, Integer.class)
											.toMethodDescriptorString(),
									false);
							final InvokeDynamicInsnNode invokeDynamicInsnNode = new InvokeDynamicInsnNode(
									StringUtil.getMassiveString(),
									methodInsnNode.getOpcode() == Opcodes.INVOKESTATIC ? methodInsnNode.desc
											: methodInsnNode.desc.replace("(", "(Ljava/lang/Object;"),
									handle, this.encrypt(methodInsnNode.owner.replace("/", "."), decryptValue),
									this.encrypt(methodInsnNode.name, decryptValue),
									this.encrypt(methodInsnNode.desc, decryptValue),
									methodInsnNode.getOpcode() == Opcodes.INVOKESTATIC ? 0 : 1);
							methodNode.instructions.insert(insnNode, invokeDynamicInsnNode);
							methodNode.instructions.remove(insnNode);
							atomicBoolean.set(true);
							this.atomicInteger.incrementAndGet();
						}
					});
		});
		return atomicBoolean.get();
	}

	private MethodNode createBootstrap(final String className, final String methodName, final String decryptName) {
		final MethodNode methodNode = new MethodNode(
				Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_SYNTHETIC + Opcodes.ACC_BRIDGE, methodName,
				MethodType.methodType(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class,
						String.class, String.class, String.class, Integer.class).toMethodDescriptorString(),
				null, null);
		methodNode.visitCode();
		final Label firstLabel = new Label();
		final Label secondLabel = new Label();
		final Label thirthLabel = new Label();
		methodNode.visitTryCatchBlock(firstLabel, secondLabel, thirthLabel, "java/lang/Exception");
		final Label fourthLabel = new Label();
		final Label fifthLabel = new Label();
		methodNode.visitTryCatchBlock(fourthLabel, fifthLabel, thirthLabel, "java/lang/Exception");
		methodNode.visitVarInsn(Opcodes.ALOAD, 3);
		methodNode.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/String");
		methodNode.visitVarInsn(Opcodes.ASTORE, 7);
		methodNode.visitVarInsn(Opcodes.ALOAD, 4);
		methodNode.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/String");
		methodNode.visitVarInsn(Opcodes.ASTORE, 8);
		methodNode.visitVarInsn(Opcodes.ALOAD, 5);
		methodNode.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/String");
		methodNode.visitVarInsn(Opcodes.ASTORE, 9);
		methodNode.visitVarInsn(Opcodes.ALOAD, 6);
		methodNode.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer");
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
		methodNode.visitVarInsn(Opcodes.ISTORE, 10);
		methodNode.visitVarInsn(Opcodes.ALOAD, 9);
		methodNode.visitMethodInsn(Opcodes.INVOKESTATIC, className, decryptName,
				"(Ljava/lang/String;)Ljava/lang/String;", false);
		methodNode.visitLdcInsn(Type.getType("L" + className + ";"));
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getClassLoader",
				"()Ljava/lang/ClassLoader;", false);
		methodNode.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/invoke/MethodType", "fromMethodDescriptorString",
				"(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;", false);
		methodNode.visitVarInsn(Opcodes.ASTORE, 11);
		methodNode.visitLabel(firstLabel);
		methodNode.visitVarInsn(Opcodes.ILOAD, 10);
		methodNode.visitInsn(Opcodes.ICONST_1);
		methodNode.visitJumpInsn(Opcodes.IF_ICMPNE, fourthLabel);
		methodNode.visitTypeInsn(Opcodes.NEW, "java/lang/invoke/ConstantCallSite");
		methodNode.visitInsn(Opcodes.DUP);
		methodNode.visitVarInsn(Opcodes.ALOAD, 0);
		methodNode.visitVarInsn(Opcodes.ALOAD, 7);
		methodNode.visitMethodInsn(Opcodes.INVOKESTATIC, className, decryptName,
				"(Ljava/lang/String;)Ljava/lang/String;", false);
		methodNode.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Class", "forName",
				"(Ljava/lang/String;)Ljava/lang/Class;", false);
		methodNode.visitVarInsn(Opcodes.ALOAD, 8);
		methodNode.visitMethodInsn(Opcodes.INVOKESTATIC, className, decryptName,
				"(Ljava/lang/String;)Ljava/lang/String;", false);
		methodNode.visitVarInsn(Opcodes.ALOAD, 11);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandles$Lookup", "findVirtual",
				"(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;",
				false);
		methodNode.visitVarInsn(Opcodes.ALOAD, 2);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "asType",
				"(Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;", false);
		methodNode.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/invoke/ConstantCallSite", "<init>",
				"(Ljava/lang/invoke/MethodHandle;)V", false);
		methodNode.visitLabel(secondLabel);
		methodNode.visitInsn(Opcodes.ARETURN);
		methodNode.visitLabel(fourthLabel);
		methodNode.visitFrame(Opcodes.F_FULL, 12,
				new Object[] { "java/lang/invoke/MethodHandles$Lookup", "java/lang/String",
						"java/lang/invoke/MethodType", "java/lang/Object", "java/lang/Object", "java/lang/Object",
						"java/lang/Object", "java/lang/String", "java/lang/String", "java/lang/String", Opcodes.INTEGER,
						"java/lang/invoke/MethodType" },
				0, new Object[] {});
		methodNode.visitTypeInsn(Opcodes.NEW, "java/lang/invoke/ConstantCallSite");
		methodNode.visitInsn(Opcodes.DUP);
		methodNode.visitVarInsn(Opcodes.ALOAD, 0);
		methodNode.visitVarInsn(Opcodes.ALOAD, 7);
		methodNode.visitMethodInsn(Opcodes.INVOKESTATIC, className, decryptName,
				"(Ljava/lang/String;)Ljava/lang/String;", false);
		methodNode.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Class", "forName",
				"(Ljava/lang/String;)Ljava/lang/Class;", false);
		methodNode.visitVarInsn(Opcodes.ALOAD, 8);
		methodNode.visitMethodInsn(Opcodes.INVOKESTATIC, className, decryptName,
				"(Ljava/lang/String;)Ljava/lang/String;", false);
		methodNode.visitVarInsn(Opcodes.ALOAD, 11);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandles$Lookup", "findStatic",
				"(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;",
				false);
		methodNode.visitVarInsn(Opcodes.ALOAD, 2);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "asType",
				"(Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;", false);
		methodNode.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/invoke/ConstantCallSite", "<init>",
				"(Ljava/lang/invoke/MethodHandle;)V", false);
		methodNode.visitLabel(fifthLabel);
		methodNode.visitInsn(Opcodes.ARETURN);
		methodNode.visitLabel(thirthLabel);
		methodNode.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { "java/lang/Exception" });
		methodNode.visitVarInsn(Opcodes.ASTORE, 12);
		methodNode.visitInsn(Opcodes.ACONST_NULL);
		methodNode.visitInsn(Opcodes.ARETURN);
		methodNode.visitMaxs(6, 13);
		methodNode.visitEnd();
		return methodNode;
	}

	private MethodNode createDecrypt(final String methodName, final int decryptValue) {
		final MethodNode methodNode = new MethodNode(
				Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC + Opcodes.ACC_BRIDGE + Opcodes.ACC_SYNTHETIC, methodName,
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
		methodNode.visitIntInsn(ASMUtil.getOpcodeInsn(decryptValue), decryptValue);
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

	private String encrypt(final String string, final int decryptValue) {
		final StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < string.length(); i++) {
			stringBuilder.append((char) (string.charAt(i) ^ decryptValue));
		}
		return stringBuilder.toString();
	}

}
