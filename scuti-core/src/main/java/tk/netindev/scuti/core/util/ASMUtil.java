package tk.netindev.scuti.core.util;

import java.util.Arrays;
import java.util.Optional;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 *
 * @author netindev
 *
 */
public class ASMUtil {

	public static boolean isInitializer(final MethodNode methodNode) {
		return methodNode.name.contains("<") || methodNode.name.contains(">");
	}

	public static int getOpcodeInsn(final int value) {
		if (value >= -1 && value <= 5) {
			return value + 0x3; /* ICONST_M1 0x2 (-0x1 + 0x3 = 0x2)... */
		} else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
			return Opcodes.BIPUSH;
		} else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
			return Opcodes.SIPUSH;
		}
		throw new RuntimeException("Expected value over -1 and under Short.MAX_VALUE");
	}

	public static AbstractInsnNode toInsnNode(final int value) {
		if (value >= -1 && value <= 5) {
			return new InsnNode(value + 0x3); /* ICONST_M1 0x2 (-0x1 + 0x3 = 0x2)... */
		} else if (value > Byte.MIN_VALUE && value < Byte.MAX_VALUE) {
			return new IntInsnNode(Opcodes.BIPUSH, value);
		} else if (value > Short.MIN_VALUE && value < Short.MAX_VALUE) {
			return new IntInsnNode(Opcodes.SIPUSH, value);
		}
		return new LdcInsnNode(value);
	}

	public static boolean isMainMethod(MethodNode methodNode) {
		return methodNode.name.equals("main") && methodNode.desc.equals("([Ljava/lang/String;)V");
	}

	public static boolean isLambdaMethod(MethodNode methodNode) {
		return methodNode.name.startsWith("lambda$");
	}

	public static MethodNode getOrCreateClinit(final ClassNode classNode) {
		final Optional<MethodNode> optional = classNode.methods.stream()
				.filter(methodNode -> methodNode.name.equals("<clinit>")).findFirst();
		if (optional.isPresent()) {
			return optional.get();
		}
		final MethodNode methodNode = new MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
		methodNode.visitCode();
		methodNode.visitInsn(Opcodes.RETURN);
		methodNode.visitEnd();
		classNode.methods.add(methodNode);
		return methodNode;
	}

	public static LabelNode getFirstLabel(final MethodNode methodNode) {
		return (LabelNode) Arrays.stream(methodNode.instructions.toArray())
				.filter(insnNode -> insnNode instanceof LabelNode).findFirst()
				.orElseThrow(() -> new RuntimeException("That shouldn't happen"));
	}

	public static LabelNode getLastLabel(final MethodNode methodNode) {
		return (LabelNode) Arrays.stream(methodNode.instructions.toArray())
				.filter(insnNode -> insnNode instanceof LabelNode).reduce((first, second) -> second)
				.orElseThrow(() -> new RuntimeException("That shouldn't happen"));
	}

	public static boolean hasLabels(final MethodNode methodNode) {
		return Arrays.stream(methodNode.instructions.toArray()).filter(insnNode -> insnNode instanceof LabelNode)
				.findFirst().isPresent();
	}

}
