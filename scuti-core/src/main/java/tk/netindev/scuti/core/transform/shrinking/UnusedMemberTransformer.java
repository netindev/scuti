package tk.netindev.scuti.core.transform.shrinking;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tk.netindev.scuti.core.configuration.Configuration;
import tk.netindev.scuti.core.rewrite.Hierarchy;
import tk.netindev.scuti.core.transform.Transformer;
import tk.netindev.scuti.core.util.ASMUtil;
import tk.netindev.scuti.core.util.Util;

/**
 *
 * @author netindev
 *
 */
public class UnusedMemberTransformer extends Transformer {

	private static final Logger LOGGER = LoggerFactory.getLogger(UnusedMemberTransformer.class);

	private final Hierarchy unusedHierarchy = new Hierarchy(this.getClasses(), this.getDependencies());

	public UnusedMemberTransformer(final Configuration configuration, final Map<String, ClassNode> classes,
			final Map<String, ClassNode> dependencies) {
		super(configuration, classes, dependencies);
		LOGGER.info(" Unused Members Transformer ->");
	}

	@Override
	public void transform() {
		LOGGER.info(" - Analysing...");
		final Set<ClassNode> classes = new HashSet<>();
		final AtomicInteger foundField = new AtomicInteger(), foundMethod = new AtomicInteger();
		Util.sortByComparator(this.getClasses()).values().forEach(classNode -> {
			if (!this.getConfiguration().getUnusedMembers().getKeepClasses().contains(classNode.name)) {
				if (this.getConfiguration().getUnusedMembers().isClasses()) {
					if (this.isUnusedClass(classNode)) {
						classes.add(classNode);
					}
				}
				if (this.getConfiguration().getUnusedMembers().isMethods()) {
					final Set<MethodNode> methods = new HashSet<>();
					classNode.methods.forEach(methodNode -> {
						if (this.isUnusedMethod(classNode, methodNode)) {
							foundMethod.incrementAndGet();
							methods.add(methodNode);
						}
					});
					classNode.methods.removeAll(methods);
				}
				if (this.getConfiguration().getUnusedMembers().isFields()) {
					final Set<FieldNode> fields = new HashSet<>();
					classNode.fields.forEach(fieldNode -> {
						if (this.isUnusedField(fieldNode)) {
							foundField.incrementAndGet();
							fields.add(fieldNode);
						}
					});
					classNode.fields.removeAll(fields);
				}
			}
		});
		if (classes.size() > 0) {
			LOGGER.info(" - Removed " + classes.size() + " classes");
		}
		if (foundMethod.get() > 0) {
			LOGGER.info(" - Removed " + foundMethod.get() + " methods");
		}
		if (foundField.get() > 0) {
			LOGGER.info(" - Removed " + foundField.get() + " fields");
		}
		classes.forEach(classNode -> this.getClasses().remove(classNode.name, classNode));
	}

	private boolean isUnusedClass(final ClassNode classNode) {
		if (this.hasMain(classNode)) {
			return false;
		}
		for (final ClassNode classNode_ : this.getClasses().values()) {
			if (classNode_.superName.equals(classNode.name)) {
				return false;
			} else if (classNode_.interfaces.contains(classNode.name)) {
				return false;
			}
			for (final MethodNode methodNode : classNode_.methods) {
				if (this.hasAnnotation(methodNode.visibleAnnotations, classNode)) {
					return false;
				} else if (this.contains(methodNode.exceptions, classNode)) {
					return false;
				} else if (methodNode.desc.contains(classNode.name)) {
					return false;
				}
				for (final TryCatchBlockNode tryCatchBlockNode : methodNode.tryCatchBlocks) {
					if (tryCatchBlockNode.type != null) {
						if (tryCatchBlockNode.type.contains(classNode.name)) {
							return false;
						}
					}
				}
				for (final AbstractInsnNode insnNode : methodNode.instructions.toArray()) {
					if (insnNode instanceof MethodInsnNode) {
						final MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
						if (methodInsnNode.owner.equals(classNode.name)) {
							return false;
						} else if (methodInsnNode.desc.contains(classNode.name)) {
							return false;
						}
					} else if (insnNode instanceof FieldInsnNode) {
						final FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNode;
						if (fieldInsnNode.owner.equals(classNode.name)) {
							return false;
						} else if (fieldInsnNode.desc.contains(classNode.name)) {
							return false;
						}
					} else if (insnNode instanceof TypeInsnNode) {
						final TypeInsnNode typeInsnNode = (TypeInsnNode) insnNode;
						if (typeInsnNode.desc.contains(classNode.name)) {
							return false;
						}
					} else if (insnNode instanceof LdcInsnNode) {
						final LdcInsnNode ldcInsnNode = (LdcInsnNode) insnNode;
						if (ldcInsnNode.cst instanceof Type) {
							if (((Type) ldcInsnNode.cst).getDescriptor().contains(classNode.name)) {
								return false;
							}
						} else if (ldcInsnNode.cst instanceof String) {
							if (((String) ldcInsnNode.cst).contains(classNode.name)) {
								return false;
							}
						}
					} else if (insnNode instanceof InvokeDynamicInsnNode) {
						final InvokeDynamicInsnNode invokeDynamicInsnNode = (InvokeDynamicInsnNode) insnNode;
						if (invokeDynamicInsnNode.desc.contains(classNode.name)) {
							return false;
						}
						final Object[] bsmArgs = invokeDynamicInsnNode.bsmArgs;
						for (final Object bsmArg : bsmArgs) {
							if (bsmArg instanceof Type) {
								if (((Type) bsmArg).getDescriptor().contains(classNode.name)) {
									return false;
								}
							}
							if (bsmArg instanceof Handle) {
								final Handle handle = (Handle) bsmArg;
								if (handle.getOwner().equals(classNode.name)
										|| handle.getDesc().endsWith(classNode.name)) {
									return false;
								}
							}
						}
					} else if (insnNode instanceof FrameNode) {
						final FrameNode frameNode = (FrameNode) insnNode;
						if (frameNode.local != null) {
							for (final Object local : frameNode.local) {
								if (local instanceof String) {
									if (((String) local).contains(classNode.name)) {
										return false;
									}
								}
							}
						}
						if (frameNode.stack != null) {
							for (final Object stack : frameNode.stack) {
								if (stack instanceof String) {
									if (((String) stack).contains(classNode.name)) {
										return false;
									}
								}
							}
						}
					} else if (insnNode instanceof MultiANewArrayInsnNode) {
						final MultiANewArrayInsnNode multiANewArrayInsnNode = (MultiANewArrayInsnNode) insnNode;
						if (multiANewArrayInsnNode.desc.contains(classNode.name)) {
							return false;
						}
					}
				}
			}
			for (final FieldNode fieldNode : classNode_.fields) {
				if (this.hasAnnotation(fieldNode.visibleAnnotations, classNode)) {
					return false;
				} else if (fieldNode.desc.contains(classNode.name)) {
					return false;
				}
			}
			if (this.hasAnnotation(classNode_.visibleAnnotations, classNode)) {
				return false;
			}
		}
		return true;
	}

	private boolean isUnusedMethod(final ClassNode classNode, final MethodNode methodNode) {
		if (ASMUtil.isInitializer(methodNode)) {
			return false;
		} else if (methodNode.name.equals("main") && methodNode.desc.equals("([Ljava/lang/String;)V")) {
			return false;
		} else if (methodNode.name.startsWith("lambda$")) {
			return false;
		} else if (methodNode.visibleAnnotations != null && !methodNode.visibleAnnotations.isEmpty()) {
			return false;
		}
		for (final ClassNode classNode_ : this.getClasses().values()) {
			for (final MethodNode methodNode_ : classNode_.methods) {
				for (final AbstractInsnNode insnNode : methodNode_.instructions.toArray()) {
					if (insnNode instanceof MethodInsnNode) {
						if (((MethodInsnNode) insnNode).name.equals(methodNode.name)) {
							return false;
						}
					} else if (insnNode instanceof InvokeDynamicInsnNode) {
						for (final Object args : ((InvokeDynamicInsnNode) insnNode).bsmArgs) {
							if (args.toString().contains(methodNode.name)) {
								return false;
							}
						}
					}
				}
			}
		}
		if (this.iterateParents(this.unusedHierarchy.getTree(classNode.name).getParentClasses(), new HashSet<>())
				.contains(methodNode.name)
				|| this.iterateInterfaces(classNode.interfaces, new HashSet<>()).contains(methodNode.name)) {
			return false;
		}
		return true;
	}

	private Set<String> iterateParents(final Set<String> parents, final Set<String> set) {
		parents.stream().map(parent -> this.unusedHierarchy.getClassNode(parent)).forEach(parent -> {
			set.addAll(this.iterateInterfaces(parent.interfaces, new HashSet<>()));
			parent.methods.forEach(methodNode -> set.add(methodNode.name));
		});
		parents.forEach(parent -> {
			this.iterateParents(this.unusedHierarchy.getTree(parent).getParentClasses(), set);
		});
		return set;
	}

	private Set<String> iterateInterfaces(final List<String> interfaces, final Set<String> set) {
		interfaces.stream().map(interfaceNode -> this.unusedHierarchy.getClassNode(interfaceNode))
				.forEach(interfaceNode -> {
					interfaceNode.methods.forEach(methodNode -> set.add(methodNode.name));
				});
		interfaces.forEach(parent -> {
			this.iterateInterfaces(this.unusedHierarchy.getTree(parent).getClassNode().interfaces, set);
		});
		return set;
	}

	private boolean isUnusedField(final FieldNode fieldNode) {
		if (fieldNode.visibleAnnotations != null && !fieldNode.visibleAnnotations.isEmpty()) {
			return false;
		}
		for (final ClassNode classNode : this.getClasses().values()) {
			for (final MethodNode methodNode : classNode.methods) {
				for (final AbstractInsnNode insnNode : methodNode.instructions.toArray()) {
					if (insnNode instanceof FieldInsnNode) {
						if (((FieldInsnNode) insnNode).name.equals(fieldNode.name)) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	private boolean hasMain(final ClassNode classNode) {
		for (final MethodNode methodNode : classNode.methods) {
			if (methodNode.name.equals("main") && methodNode.desc.equals("([Ljava/lang/String;)V")) {
				return true;
			}
		}
		return false;
	}

	private boolean hasAnnotation(final List<AnnotationNode> list, final ClassNode classNode) {
		if (list != null) {
			for (final AnnotationNode annotationNode : list) {
				if (this.getName(annotationNode.desc).equals(this.getName(classNode.name))) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean contains(final List<String> list, final ClassNode classNode) {
		if (list != null) {
			for (final String string : list) {
				if (string.equals(classNode.name)) {
					return true;
				}
			}
		}
		return false;
	}

	private String getName(final String string) {
		return string.contains("/") ? string.substring(string.lastIndexOf("/") + 1).replace(";", "")
				: string.replace(";", "");
	}

}
