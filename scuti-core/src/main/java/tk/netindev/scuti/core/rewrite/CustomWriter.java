package tk.netindev.scuti.core.rewrite;

import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import tk.netindev.scuti.core.exception.ClassNotFoundException;
import tk.netindev.scuti.core.rewrite.Hierarchy.Tree;

/**
 *
 * @author netindev
 *
 */
public class CustomWriter extends ClassWriter {

	private final Hierarchy hierarchy;

	public CustomWriter(final Hierarchy hierarchy, final int flags) {
		super(flags);
		this.hierarchy = hierarchy;
	}

	@Override
	protected String getCommonSuperClass(final String firstType, final String secondType) {
		if ("java/lang/Object".equals(firstType) || "java/lang/Object".equals(secondType)) {
			return "java/lang/Object";
		}
		final String first = this.deriveCommonSuperName(firstType, secondType);
		final String second = this.deriveCommonSuperName(secondType, firstType);
		if (!"java/lang/Object".equals(first)) {
			return first;
		}
		if (!"java/lang/Object".equals(second)) {
			return second;
		}
		return this.getCommonSuperClass(this.hierarchy.getClassNode(firstType).superName,
				this.hierarchy.getClassNode(secondType).superName);
	}

	private String deriveCommonSuperName(final String firstType, final String secondType) {
		ClassNode first = this.hierarchy.getClassNode(firstType);
		final ClassNode second = this.hierarchy.getClassNode(secondType);
		if (this.isAssignableFrom(firstType, secondType)) {
			return firstType;
		} else if (this.isAssignableFrom(secondType, firstType)) {
			return secondType;
		} else if (Modifier.isInterface(first.access) || Modifier.isInterface(second.access)) {
			return "java/lang/Object";
		} else {
			String string;
			do {
				string = first.superName;
				first = this.hierarchy.getClassNode(string);
			} while (!this.isAssignableFrom(string, secondType));
			return string;
		}
	}

	public boolean isAssignableFrom(final String firstType, final String secondType) {
		if ("java/lang/Object".equals(firstType)) {
			return true;
		}
		if (firstType.equals(secondType)) {
			return true;
		}
		final Tree firstTree = this.hierarchy.getTree(firstType);
		if (firstTree == null) {
			throw new ClassNotFoundException("Could not find " + firstType + " in the built class hierarchy");
		}
		final Set<String> set = new HashSet<>();
		final Deque<String> deque = new ArrayDeque<>(firstTree.getSubClasses());
		while (!deque.isEmpty()) {
			final String string = deque.poll();
			if (set.add(string)) {
				final Tree tree = this.hierarchy.getTree(string);
				deque.addAll(tree.getSubClasses());
			}
		}
		return set.contains(secondType);
	}

}
