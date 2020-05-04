package tk.netindev.scuti.core.rewrite;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;

import tk.netindev.scuti.core.exception.ClassNotFoundException;

/**
 *
 * @author netindev
 *
 */
public class Hierarchy {

	private final Map<String, ClassNode> classes, dependencies;
	private final Map<String, Tree> hierarchy;

	public Hierarchy(final Map<String, ClassNode> classes, final Map<String, ClassNode> dependencies) {
		this.classes = Collections.synchronizedMap(classes);
		this.dependencies = Collections.synchronizedMap(dependencies);
	}

	public ClassNode getClassNode(final String string) {
		if (!this.classes.containsKey(string)) {
			if (!this.dependencies.containsKey(string)) {
				throw new ClassNotFoundException("Missing class \"" + string + "\", please update your dependencies");
			} else {
				return this.dependencies.get(string);
			}
		} else {
			return this.classes.get(string);
		}
	}

	public Tree getTree(final String string) {
		if (!this.hierarchy.containsKey(string)) {
			final ClassNode classNode = this.getClassNode(string);
			this.buildHierarchy(classNode, null);
		}
		return this.hierarchy.get(string);
	}

	public void buildHierarchy(final ClassNode classNode, final ClassNode subNode) {
		if (this.hierarchy.get(classNode.name) == null) {
			final Tree hierarchy = new Tree(classNode);
			if (classNode.superName != null) {
				hierarchy.getParentClasses().add(classNode.superName);
				this.buildHierarchy(this.getClassNode(classNode.superName), classNode);
			}
			if (classNode.interfaces != null) {
				classNode.interfaces.forEach(interfaces -> {
					hierarchy.getParentClasses().add(interfaces);
					this.buildHierarchy(this.getClassNode(interfaces), classNode);
				});
			}
			this.hierarchy.put(classNode.name, hierarchy);
		}
		if (subNode != null) {
			this.hierarchy.get(classNode.name).getSubClasses().add(subNode.name);
		}
	}

	/**
	 *
	 * @author netindev
	 *
	 */
	public class Tree {

		private final ClassNode classNode;

		private final Set<String> parentClasses = new HashSet<>();
		private final Set<String> subClasses = new HashSet<>();

		public Tree(final ClassNode classNode) {
			this.classNode = classNode;
		}

		public ClassNode getClassNode() {
			return this.classNode;
		}

		public Set<String> getParentClasses() {
			return this.parentClasses;
		}

		public Set<String> getSubClasses() {
			return this.subClasses;
		}

	}

	/* init */ {
		this.hierarchy = new HashMap<>();
	}

}