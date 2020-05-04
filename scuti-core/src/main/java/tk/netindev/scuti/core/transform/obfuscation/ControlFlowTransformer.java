package tk.netindev.scuti.core.transform.obfuscation;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
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
public class ControlFlowTransformer extends Transformer implements Opcodes {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControlFlowTransformer.class.getName());

	private final FieldNode negativeField = new FieldNode(ACC_PRIVATE + ACC_STATIC + ACC_SYNTHETIC,
			StringUtil.getMassiveString().substring((int) (Short.MAX_VALUE / 2.51)), "I", null, null);
	private final FieldNode positiveField = new FieldNode(ACC_PRIVATE + ACC_STATIC + ACC_SYNTHETIC,
			StringUtil.getMassiveString().substring((int) (Short.MAX_VALUE / 2.49)), "I", null, null);

	int[] conditions = { Opcodes.IF_ICMPLT, Opcodes.IF_ICMPLE, Opcodes.IF_ICMPNE };

	public ControlFlowTransformer(final Configuration configuration, final Map<String, ClassNode> classes,
			final Map<String, ClassNode> dependencies) {
		super(configuration, classes, dependencies);
	}

	@Override
	public void transform() {
		LOGGER.info(" Executing control flow (experimental)");
		final ClassNode throwableClass = this.createThrowableClass(StringUtil.getMassiveString());
		this.getClasses().values().stream()
				.filter(classNode -> !Modifier.isInterface(classNode.access)
						&& !this.getConfiguration().getClassEncrypt().getLoaderName().equals(classNode.name))
				.forEach(classNode -> {
					classNode.methods.stream().filter(methodNode -> !Modifier.isAbstract(methodNode.access)
							&& !Modifier.isNative(methodNode.access)).forEach(methodNode -> {
								Arrays.stream(methodNode.instructions.toArray()).forEach(insnNode -> {
									if (insnNode instanceof LabelNode && (insnNode.getOpcode() != Opcodes.RETURN
											|| insnNode.getOpcode() != Opcodes.ARETURN)) {
										methodNode.instructions.insert(insnNode,
												this.getRandomConditionList(classNode));
										final LabelNode labelAfter = new LabelNode();
										final LabelNode labelBefore = new LabelNode();
										final LabelNode labelFinal = new LabelNode();
										methodNode.instructions.insertBefore(insnNode, labelBefore);
										methodNode.instructions.insert(insnNode, labelAfter);
										methodNode.instructions.insert(labelAfter, labelFinal);
										methodNode.instructions.insert(labelBefore,
												new JumpInsnNode(Opcodes.GOTO, labelAfter));
										methodNode.instructions.insert(labelAfter,
												new JumpInsnNode(Opcodes.GOTO, labelFinal));
									}
								});
								this.heavyDoubleAthrow(classNode, methodNode, throwableClass);
							});
					final MethodNode staticInitializer = ASMUtil.getOrCreateClinit(classNode);

					final InsnList insnList = new InsnList();
					final int splitable = -RandomUtil.getRandom(Short.MAX_VALUE);
					insnList.add(ASMUtil.toInsnNode(-splitable ^ 50 + RandomUtil.getRandom(Short.MAX_VALUE)));
					insnList.add(ASMUtil.toInsnNode(splitable));
					insnList.add(new InsnNode(Opcodes.IXOR));
					insnList.add(new FieldInsnNode(Opcodes.PUTSTATIC, classNode.name, this.negativeField.name, "I"));
					insnList.add(ASMUtil.toInsnNode(splitable ^ 50 + RandomUtil.getRandom(Short.MAX_VALUE)));
					insnList.add(ASMUtil.toInsnNode(splitable));
					insnList.add(new InsnNode(Opcodes.IXOR));
					insnList.add(new FieldInsnNode(Opcodes.PUTSTATIC, classNode.name, this.positiveField.name, "I"));
					staticInitializer.instructions.insert(insnList);
					classNode.fields.add(this.negativeField);
					classNode.fields.add(this.positiveField);
				});
		this.getClasses().put(throwableClass.name, throwableClass);
	}

	private ClassNode createThrowableClass(final String className) {
		final ClassNode classNode = new ClassNode();
		classNode.visit(V1_8, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Throwable", null);

		final FieldNode serialVersionUID = new FieldNode(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "serialVersionUID", "J",
				null, new Long(1L));
		classNode.fields.add(serialVersionUID);

		final MethodNode methodNode = new MethodNode(ACC_PUBLIC, "<init>", "(Ljava/lang/String;)V", null, null);
		final Label firstLabel = new Label(), secondLabel = new Label(), thirdLabel = new Label();
		methodNode.visitCode();
		methodNode.visitLabel(firstLabel);
		methodNode.visitLineNumber(10, firstLabel);
		methodNode.visitVarInsn(ALOAD, 0);
		methodNode.visitVarInsn(ALOAD, 1);
		methodNode.visitMethodInsn(INVOKESPECIAL, "java/lang/Throwable", "<init>", "(Ljava/lang/String;)V", false);
		methodNode.visitLabel(secondLabel);
		methodNode.visitLineNumber(11, secondLabel);
		methodNode.visitInsn(RETURN);
		methodNode.visitLabel(thirdLabel);
		methodNode.visitLocalVariable("this", "LMain;", null, firstLabel, thirdLabel, 0);
		methodNode.visitLocalVariable("string", "Ljava/lang/String;", null, firstLabel, thirdLabel, 1);
		methodNode.visitMaxs(2, 2);
		methodNode.visitEnd();

		classNode.methods.add(methodNode);
		return classNode;
	}

	private InsnList getRandomConditionList(final ClassNode classNode) {
		final InsnList insnList = new InsnList();
		/*
		 * switch (RandomUtil.getRandom(6)) { default: final LabelNode startLabel = new
		 * LabelNode(); insnList.add(new FieldInsnNode(GETSTATIC, classNode.name,
		 * this.negativeField.name, "I")); insnList.add(new FieldInsnNode(GETSTATIC,
		 * classNode.name, this.positiveField.name, "I")); insnList.add(new
		 * JumpInsnNode(Opcodes.IF_ICMPLT, startLabel)); insnList.add(new
		 * InsnNode(Opcodes.ACONST_NULL)); insnList.add(new InsnNode(Opcodes.ATHROW));
		 * insnList.add(startLabel); break; }
		 */
		final LabelNode startLabel = new LabelNode();
		insnList.add(new FieldInsnNode(GETSTATIC, classNode.name, this.negativeField.name, "I"));
		insnList.add(new FieldInsnNode(GETSTATIC, classNode.name, this.positiveField.name, "I"));
		insnList.add(new JumpInsnNode(Opcodes.IF_ICMPLT, startLabel));
		insnList.add(new InsnNode(Opcodes.ACONST_NULL));
		insnList.add(new InsnNode(Opcodes.ATHROW));
		insnList.add(startLabel);
		return insnList;
	}

	private void heavyDoubleAthrow(final ClassNode classNode, final MethodNode methodNode,
			final ClassNode throwableClass) {
		final InsnList insnlist = new InsnList();
		final LabelNode firstLabel = new LabelNode();
		final LabelNode secondLabel = new LabelNode();
		final TryCatchBlockNode firstTryCatch = new TryCatchBlockNode(firstLabel, secondLabel, secondLabel,
				throwableClass.name);
		final LabelNode thirdLabel = new LabelNode();
		final TryCatchBlockNode secondTryCatch = new TryCatchBlockNode(secondLabel, thirdLabel, firstLabel,
				throwableClass.name);
		insnlist.add(new FieldInsnNode(Opcodes.GETSTATIC, classNode.name, this.negativeField.name, "I"));
		insnlist.add(new FieldInsnNode(Opcodes.GETSTATIC, classNode.name, this.positiveField.name, "I"));
		insnlist.add(new JumpInsnNode(this.conditions[RandomUtil.getRandom(this.conditions.length)], thirdLabel));
		insnlist.add(new InsnNode(Opcodes.ACONST_NULL));
		insnlist.add(firstLabel);
		insnlist.add(new InsnNode(Opcodes.ATHROW));
		insnlist.add(secondLabel);
		insnlist.add(new InsnNode(Opcodes.ATHROW));
		insnlist.add(thirdLabel);
		methodNode.instructions.insert(insnlist);
		methodNode.tryCatchBlocks.add(firstTryCatch);
		methodNode.tryCatchBlocks.add(secondTryCatch);
	}

}
