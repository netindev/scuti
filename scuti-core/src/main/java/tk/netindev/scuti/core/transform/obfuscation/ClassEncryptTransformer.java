package tk.netindev.scuti.core.transform.obfuscation;

import java.util.Map;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tk.netindev.scuti.core.configuration.Configuration;
import tk.netindev.scuti.core.transform.Transformer;
import tk.netindev.scuti.core.util.ASMUtil;
import tk.netindev.scuti.core.util.RandomUtil;

/**
 *
 * @author netindev
 *
 */
public class ClassEncryptTransformer extends Transformer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClassEncryptTransformer.class.getName());

	private String loaderName;
	private int stringDecryptorKey, classDecryptorKey;

	public ClassEncryptTransformer(final Configuration configuration, final Map<String, ClassNode> classes,
			final Map<String, ClassNode> dependencies) {
		super(configuration, classes, dependencies);
		LOGGER.info(" Class Encrypt Transformer ->");
	}

	@Override
	public void transform() {
		LOGGER.info(
				" - Creating decrypter class \"" + this.getConfiguration().getClassEncrypt().getLoaderName() + "\"");
		this.loaderName = this.getConfiguration().getClassEncrypt().getLoaderName();
		this.stringDecryptorKey = this.getConfiguration().getClassEncrypt().getStringKey();
		this.classDecryptorKey = this.getConfiguration().getClassEncrypt().getClassKey();
		if (this.stringDecryptorKey >= Short.MAX_VALUE) {
			this.stringDecryptorKey = RandomUtil.getRandom(Short.MAX_VALUE - 1);
		}
		if (this.classDecryptorKey >= Short.MAX_VALUE) {
			this.classDecryptorKey = RandomUtil.getRandom(Short.MAX_VALUE - 1);
		}
		this.getConfiguration().getRenameMembers().getExcludeClasses().add(this.loaderName);
		final String mainClass = this.getConfiguration().getClassEncrypt().getMainClass().replaceAll("/", ".");
		final ClassNode classNode = new ClassNode();
		classNode.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, this.loaderName, null,
				"java/lang/ClassLoader", null);
		classNode.methods.add(this.createInit());
		classNode.methods.add(this.createMain(mainClass));
		classNode.methods.add(this.createFileDecryptor());
		classNode.methods.add(this.createLoadClass());
		classNode.methods.add(this.createStringDecryptor());
		classNode.methods.add(this.createToByteArray());
		this.getClasses().put(classNode.name, classNode);
	}

	private MethodNode createInit() {
		final MethodNode methodNode = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
		methodNode.visitCode();
		methodNode.visitVarInsn(Opcodes.ALOAD, 0);
		methodNode.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/ClassLoader", "<init>", "()V", false);
		methodNode.visitInsn(Opcodes.RETURN);
		methodNode.visitMaxs(1, 1);
		methodNode.visitEnd();
		return methodNode;
	}

	private MethodNode createMain(final String mainClass) {
		final MethodNode methodNode = new MethodNode(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, new String[] { "java/lang/Throwable" });
		methodNode.visitCode();
		methodNode.visitTypeInsn(Opcodes.NEW, this.loaderName);
		methodNode.visitInsn(Opcodes.DUP);
		methodNode.visitMethodInsn(Opcodes.INVOKESPECIAL, this.loaderName, "<init>", "()V", false);
		methodNode.visitLdcInsn(mainClass);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, this.loaderName, "loadClass",
				"(Ljava/lang/String;)Ljava/lang/Class;", false);
		methodNode.visitLdcInsn("main");
		methodNode.visitInsn(Opcodes.ICONST_1);
		methodNode.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Class");
		methodNode.visitInsn(Opcodes.DUP);
		methodNode.visitInsn(Opcodes.ICONST_0);
		methodNode.visitLdcInsn(Type.getType("[Ljava/lang/String;"));
		methodNode.visitInsn(Opcodes.AASTORE);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getMethod",
				"(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false);
		methodNode.visitInsn(Opcodes.ACONST_NULL);
		methodNode.visitInsn(Opcodes.ICONST_1);
		methodNode.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
		methodNode.visitInsn(Opcodes.DUP);
		methodNode.visitInsn(Opcodes.ICONST_0);
		methodNode.visitVarInsn(Opcodes.ALOAD, 0);
		methodNode.visitInsn(Opcodes.AASTORE);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke",
				"(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", false);
		methodNode.visitInsn(Opcodes.POP);
		methodNode.visitInsn(Opcodes.RETURN);
		methodNode.visitMaxs(6, 1);
		methodNode.visitEnd();
		return methodNode;
	}

	private MethodNode createLoadClass() {
		final MethodNode methodNode = new MethodNode(Opcodes.ACC_PUBLIC, "loadClass",
				"(Ljava/lang/String;)Ljava/lang/Class;", "(Ljava/lang/String;)Ljava/lang/Class<*>;",
				new String[] { "java/lang/ClassNotFoundException" });
		methodNode.visitCode();
		final Label firstLabel = new Label();
		final Label secondLabel = new Label();
		final Label thirdLabel = new Label();
		methodNode.visitTryCatchBlock(firstLabel, secondLabel, thirdLabel, "java/lang/Exception");
		final Label forthLabel = new Label();
		final Label fifthLabel = new Label();
		methodNode.visitTryCatchBlock(forthLabel, secondLabel, fifthLabel, "java/lang/Exception");
		final Label sixthLabel = new Label();
		methodNode.visitTryCatchBlock(thirdLabel, sixthLabel, fifthLabel, "java/lang/Exception");
		final Label seventhLabel = new Label();
		methodNode.visitTryCatchBlock(seventhLabel, fifthLabel, fifthLabel, "java/lang/Exception");
		methodNode.visitLabel(forthLabel);
		methodNode.visitInsn(Opcodes.ACONST_NULL);
		methodNode.visitVarInsn(Opcodes.ASTORE, 2);
		methodNode.visitLabel(firstLabel);
		methodNode.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/ClassLoader", "getSystemClassLoader",
				"()Ljava/lang/ClassLoader;", false);
		methodNode.visitVarInsn(Opcodes.ALOAD, 1);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/ClassLoader", "loadClass",
				"(Ljava/lang/String;)Ljava/lang/Class;", false);
		methodNode.visitVarInsn(Opcodes.ASTORE, 2);
		methodNode.visitVarInsn(Opcodes.ALOAD, 2);
		final Label eigthLabel = new Label();
		methodNode.visitJumpInsn(Opcodes.IFNULL, eigthLabel);
		methodNode.visitVarInsn(Opcodes.ALOAD, 2);
		methodNode.visitLabel(secondLabel);
		methodNode.visitInsn(Opcodes.ARETURN);
		methodNode.visitLabel(thirdLabel);
		methodNode.visitFrame(Opcodes.F_FULL, 3,
				new Object[] { this.loaderName, "java/lang/String", "java/lang/Class" }, 1,
				new Object[] { "java/lang/Exception" });
		methodNode.visitVarInsn(Opcodes.ASTORE, 3);
		methodNode.visitLabel(eigthLabel);
		methodNode.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		methodNode.visitVarInsn(Opcodes.ALOAD, 2);
		methodNode.visitJumpInsn(Opcodes.IFNONNULL, seventhLabel);
		methodNode.visitTypeInsn(Opcodes.NEW, "java/lang/String");
		methodNode.visitInsn(Opcodes.DUP);
		methodNode.visitVarInsn(Opcodes.ALOAD, 0);
		methodNode.visitVarInsn(Opcodes.ALOAD, 1);
		methodNode.visitLdcInsn(".");
		methodNode.visitLdcInsn("/");
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "replace",
				"(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;", false);
		methodNode.visitMethodInsn(Opcodes.INVOKESPECIAL, this.loaderName, "stringDecryptor",
				"(Ljava/lang/String;)Ljava/lang/String;", false);
		methodNode.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/String", "<init>", "(Ljava/lang/String;)V", false);
		methodNode.visitVarInsn(Opcodes.ASTORE, 3);
		methodNode.visitVarInsn(Opcodes.ALOAD, 0);
		methodNode.visitVarInsn(Opcodes.ALOAD, 3);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, this.loaderName, "getResource",
				"(Ljava/lang/String;)Ljava/net/URL;", false);
		methodNode.visitVarInsn(Opcodes.ASTORE, 4);
		methodNode.visitVarInsn(Opcodes.ALOAD, 4);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/net/URL", "openStream", "()Ljava/io/InputStream;",
				false);
		methodNode.visitVarInsn(Opcodes.ASTORE, 5);
		methodNode.visitVarInsn(Opcodes.ALOAD, 0);
		methodNode.visitVarInsn(Opcodes.ALOAD, 5);
		methodNode.visitMethodInsn(Opcodes.INVOKESTATIC, this.loaderName, "toByteArray", "(Ljava/io/InputStream;)[B",
				false);
		methodNode.visitMethodInsn(Opcodes.INVOKESPECIAL, this.loaderName, "fileDecryptor", "([B)[B", false);
		methodNode.visitVarInsn(Opcodes.ASTORE, 6);
		methodNode.visitVarInsn(Opcodes.ALOAD, 0);
		methodNode.visitVarInsn(Opcodes.ALOAD, 1);
		methodNode.visitVarInsn(Opcodes.ALOAD, 6);
		methodNode.visitInsn(Opcodes.ICONST_0);
		methodNode.visitVarInsn(Opcodes.ALOAD, 6);
		methodNode.visitInsn(Opcodes.ARRAYLENGTH);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, this.loaderName, "defineClass",
				"(Ljava/lang/String;[BII)Ljava/lang/Class;", false);
		methodNode.visitLabel(sixthLabel);
		methodNode.visitInsn(Opcodes.ARETURN);
		methodNode.visitLabel(seventhLabel);
		methodNode.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		methodNode.visitTypeInsn(Opcodes.NEW, "java/lang/ClassNotFoundException");
		methodNode.visitInsn(Opcodes.DUP);
		methodNode.visitVarInsn(Opcodes.ALOAD, 1);
		methodNode.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/ClassNotFoundException", "<init>",
				"(Ljava/lang/String;)V", false);
		methodNode.visitInsn(Opcodes.ATHROW);
		methodNode.visitLabel(fifthLabel);
		methodNode.visitFrame(Opcodes.F_FULL, 2, new Object[] { this.loaderName, "java/lang/String" }, 1,
				new Object[] { "java/lang/Exception" });
		methodNode.visitVarInsn(Opcodes.ASTORE, 2);
		methodNode.visitTypeInsn(Opcodes.NEW, "java/lang/ClassNotFoundException");
		methodNode.visitInsn(Opcodes.DUP);
		methodNode.visitVarInsn(Opcodes.ALOAD, 1);
		methodNode.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/ClassNotFoundException", "<init>",
				"(Ljava/lang/String;)V", false);
		methodNode.visitInsn(Opcodes.ATHROW);
		methodNode.visitMaxs(6, 7);
		methodNode.visitEnd();
		return methodNode;
	}

	private MethodNode createToByteArray() {
		final MethodNode methodNode = new MethodNode(Opcodes.ACC_PRIVATE + Opcodes.ACC_STATIC, "toByteArray",
				"(Ljava/io/InputStream;)[B", null, new String[] { "java/io/IOException" });
		methodNode.visitCode();
		methodNode.visitTypeInsn(Opcodes.NEW, "java/io/ByteArrayOutputStream");
		methodNode.visitInsn(Opcodes.DUP);
		methodNode.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/io/ByteArrayOutputStream", "<init>", "()V", false);
		methodNode.visitVarInsn(Opcodes.ASTORE, 1);
		methodNode.visitLdcInsn(new Integer(65535));
		methodNode.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BYTE);
		methodNode.visitVarInsn(Opcodes.ASTORE, 2);
		final Label firstLabel = new Label();
		methodNode.visitJumpInsn(Opcodes.GOTO, firstLabel);
		final Label secondLabel = new Label();
		methodNode.visitLabel(secondLabel);
		methodNode.visitFrame(Opcodes.F_APPEND, 3,
				new Object[] { "java/io/ByteArrayOutputStream", "[B", Opcodes.INTEGER }, 0, null);
		methodNode.visitVarInsn(Opcodes.ALOAD, 1);
		methodNode.visitVarInsn(Opcodes.ALOAD, 2);
		methodNode.visitInsn(Opcodes.ICONST_0);
		methodNode.visitVarInsn(Opcodes.ILOAD, 3);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/ByteArrayOutputStream", "write", "([BII)V", false);
		methodNode.visitLabel(firstLabel);
		methodNode.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
		methodNode.visitVarInsn(Opcodes.ALOAD, 0);
		methodNode.visitVarInsn(Opcodes.ALOAD, 2);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/InputStream", "read", "([B)I", false);
		methodNode.visitInsn(Opcodes.DUP);
		methodNode.visitVarInsn(Opcodes.ISTORE, 3);
		methodNode.visitInsn(Opcodes.ICONST_M1);
		methodNode.visitJumpInsn(Opcodes.IF_ICMPNE, secondLabel);
		methodNode.visitVarInsn(Opcodes.ALOAD, 1);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/ByteArrayOutputStream", "flush", "()V", false);
		methodNode.visitVarInsn(Opcodes.ALOAD, 1);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/ByteArrayOutputStream", "toByteArray", "()[B",
				false);
		methodNode.visitInsn(Opcodes.ARETURN);
		methodNode.visitMaxs(4, 4);
		methodNode.visitEnd();
		return methodNode;
	}

	private MethodNode createStringDecryptor() {
		final MethodNode methodNode = new MethodNode(Opcodes.ACC_PRIVATE, "stringDecryptor",
				"(Ljava/lang/String;)Ljava/lang/String;", null, null);
		methodNode.visitCode();
		methodNode.visitTypeInsn(Opcodes.NEW, "java/lang/String");
		methodNode.visitInsn(Opcodes.DUP);
		methodNode.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/String", "<init>", "()V", false);
		methodNode.visitVarInsn(Opcodes.ASTORE, 2);
		methodNode.visitInsn(Opcodes.ICONST_0);
		methodNode.visitVarInsn(Opcodes.ISTORE, 3);
		final Label firstLabel = new Label();
		methodNode.visitJumpInsn(Opcodes.GOTO, firstLabel);
		final Label secondLabel = new Label();
		methodNode.visitLabel(secondLabel);
		methodNode.visitFrame(Opcodes.F_APPEND, 2, new Object[] { "java/lang/String", Opcodes.INTEGER }, 0, null);
		methodNode.visitTypeInsn(Opcodes.NEW, "java/lang/StringBuilder");
		methodNode.visitInsn(Opcodes.DUP);
		methodNode.visitVarInsn(Opcodes.ALOAD, 2);
		methodNode.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/String", "valueOf",
				"(Ljava/lang/Object;)Ljava/lang/String;", false);
		methodNode.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V",
				false);
		methodNode.visitVarInsn(Opcodes.ALOAD, 1);
		methodNode.visitVarInsn(Opcodes.ILOAD, 3);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C", false);
		methodNode.visitIntInsn(ASMUtil.getOpcodeInsn(this.stringDecryptorKey), this.stringDecryptorKey);
		methodNode.visitInsn(Opcodes.IXOR);
		methodNode.visitInsn(Opcodes.I2C);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
				"(C)Ljava/lang/StringBuilder;", false);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;",
				false);
		methodNode.visitVarInsn(Opcodes.ASTORE, 2);
		methodNode.visitIincInsn(3, 1);
		methodNode.visitLabel(firstLabel);
		methodNode.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		methodNode.visitVarInsn(Opcodes.ILOAD, 3);
		methodNode.visitVarInsn(Opcodes.ALOAD, 1);
		methodNode.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);
		methodNode.visitJumpInsn(Opcodes.IF_ICMPLT, secondLabel);
		methodNode.visitVarInsn(Opcodes.ALOAD, 2);
		methodNode.visitInsn(Opcodes.ARETURN);
		methodNode.visitMaxs(3, 4);
		methodNode.visitEnd();
		return methodNode;
	}

	private MethodNode createFileDecryptor() {
		final MethodNode methodNode = new MethodNode(Opcodes.ACC_PRIVATE, "fileDecryptor", "([B)[B", null, null);
		methodNode.visitCode();
		methodNode.visitVarInsn(Opcodes.ALOAD, 1);
		methodNode.visitInsn(Opcodes.ARRAYLENGTH);
		methodNode.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BYTE);
		methodNode.visitVarInsn(Opcodes.ASTORE, 2);
		methodNode.visitInsn(Opcodes.ICONST_0);
		methodNode.visitVarInsn(Opcodes.ISTORE, 3);
		final Label firstLabel = new Label();
		methodNode.visitJumpInsn(Opcodes.GOTO, firstLabel);
		final Label secondLabel = new Label();
		methodNode.visitLabel(secondLabel);
		methodNode.visitFrame(Opcodes.F_APPEND, 2, new Object[] { "[B", Opcodes.INTEGER }, 0, null);
		methodNode.visitVarInsn(Opcodes.ALOAD, 2);
		methodNode.visitVarInsn(Opcodes.ILOAD, 3);
		methodNode.visitVarInsn(Opcodes.ALOAD, 1);
		methodNode.visitVarInsn(Opcodes.ILOAD, 3);
		methodNode.visitInsn(Opcodes.BALOAD);
		methodNode.visitIntInsn(ASMUtil.getOpcodeInsn(this.classDecryptorKey), this.classDecryptorKey);
		methodNode.visitInsn(Opcodes.IXOR);
		methodNode.visitInsn(Opcodes.I2B);
		methodNode.visitInsn(Opcodes.BASTORE);
		methodNode.visitIincInsn(3, 1);
		methodNode.visitLabel(firstLabel);
		methodNode.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		methodNode.visitVarInsn(Opcodes.ILOAD, 3);
		methodNode.visitVarInsn(Opcodes.ALOAD, 1);
		methodNode.visitInsn(Opcodes.ARRAYLENGTH);
		methodNode.visitJumpInsn(Opcodes.IF_ICMPLT, secondLabel);
		methodNode.visitVarInsn(Opcodes.ALOAD, 2);
		methodNode.visitInsn(Opcodes.ARETURN);
		methodNode.visitMaxs(4, 4);
		methodNode.visitEnd();
		return methodNode;
	}

}
