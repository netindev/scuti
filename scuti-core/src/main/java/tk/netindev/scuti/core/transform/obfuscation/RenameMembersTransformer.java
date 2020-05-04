package tk.netindev.scuti.core.transform.obfuscation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tk.netindev.scuti.core.configuration.Configuration;
import tk.netindev.scuti.core.dictionary.Dictionary;
import tk.netindev.scuti.core.dictionary.Types;
import tk.netindev.scuti.core.dictionary.Types.CustomDictionary;
import tk.netindev.scuti.core.rewrite.Hierarchy;
import tk.netindev.scuti.core.transform.Transformer;
import tk.netindev.scuti.core.util.ASMUtil;
import tk.netindev.scuti.core.util.Util;

/**
 *
 * @author netindev
 *
 */
public class RenameMembersTransformer extends Transformer {

	private final Hierarchy remmapingHierarchy = new Hierarchy(this.getClasses(), this.getDependencies());
	private final Map<String, String> classMapping = new HashMap<>(), fieldMapping = new HashMap<>(),
			methodMapping = new HashMap<>();

	private static final Logger LOGGER = LoggerFactory.getLogger(RenameMembersTransformer.class);

	public RenameMembersTransformer(final Configuration configuration, final Map<String, ClassNode> classes,
			final Map<String, ClassNode> dependencies) {
		super(configuration, classes, dependencies);
		LOGGER.info(" Rename Transfomer ->");
	}

	@Override
	public void transform() {
		if (this.getConfiguration().getRenameMembers().isRenameFields()) {
			this.remapFields();
			this.applyFields();
		}
		if (this.getConfiguration().getRenameMembers().isRenameMethods()) {
			this.remapMethods();
			this.applyMethods();
		}
		if (this.getConfiguration().getRenameMembers().isRenameClasses()) {
			this.remapClasses();
			this.applyClasses();
		}
		if (this.classMapping.values().size() > 0) {
			LOGGER.info(" - Remapped " + this.classMapping.values().size() + " classes");
		}
		if (this.methodMapping.values().size() > 0) {
			LOGGER.info(" - Remapped " + this.methodMapping.values().size() + " methods");
		}
		if (this.fieldMapping.values().size() > 0) {
			LOGGER.info(" - Remapped " + this.fieldMapping.values().size() + " fields");
		}
	}

	private void remapMethods() {
		final Dictionary methodsDictionary = Types.getDictionary(this.getConfiguration(), CustomDictionary.METHODS);
		this.getClasses().values().stream().filter(classNode -> ((classNode.access & Opcodes.ACC_ENUM) == 0))
				.forEach(classNode -> {
					classNode.methods.stream().filter(methodNode -> !this.isExcludedMethod(classNode, methodNode)
							&& !ASMUtil.isMainMethod(methodNode) && !ASMUtil.isLambdaMethod(methodNode)
							&& !ASMUtil.isInitializer(methodNode)
							&& !this.iterateParents(this.remmapingHierarchy.getTree(classNode.name).getParentClasses(),
									new HashSet<>()).contains(methodNode.name)
							&& !this.iterateInterfaces(classNode.interfaces, new HashSet<>()).contains(methodNode.name))
							.forEach(methodNode -> {
								String next = methodsDictionary.next();
								this.methodMapping.put(classNode.name + "." + methodNode.name + "." + methodNode.desc,
										next);
								methodNode.name = next;
							});
				});
		this.getClasses().values().forEach(classNode -> {
			this.remapParentMethods(this.remmapingHierarchy.getTree(classNode.name).getParentClasses(), classNode);
		});
	}

	private void applyMethods() {
		final Map<String, ClassNode> remappedMethods = new HashMap<>();
		this.getClasses().values().forEach(classNode -> {
			final ClassNode remappingNode = new ClassNode();
			classNode.accept(new ClassRemapper(remappingNode, new SimpleRemapper(this.methodMapping) {
				@Override
				public String mapMethodName(final String owner, final String name, final String desc) {
					final String remappedName = this.map(owner + "." + name + "." + desc);
					return remappedName != null ? remappedName : name;
				}
			}));
			remappedMethods.put(remappingNode.name, remappingNode);
		});
		this.getClasses().clear();
		this.getClasses().putAll(remappedMethods);
	}

	private void remapFields() {
		final Dictionary fieldsDictionary = Types.getDictionary(this.getConfiguration(), CustomDictionary.FIELDS);
		this.getClasses().values().forEach(classNode -> {
			classNode.fields.stream().filter(fieldNode -> !this.isExcludedField(classNode, fieldNode))
					.forEach(fieldNode -> {
						this.fieldMapping.put(classNode.name + "." + fieldNode.name + "." + fieldNode.desc,
								fieldsDictionary.next());
					});
		});
		this.getClasses().values().forEach(classNode -> {
			this.remapParentFields(this.remmapingHierarchy.getTree(classNode.name).getParentClasses(), classNode);
		});
	}

	private void applyFields() {
		final Map<String, ClassNode> remappedFields = new HashMap<>();
		this.getClasses().values().forEach(classNode -> {
			final ClassNode remappingNode = new ClassNode();
			classNode.accept(new ClassRemapper(remappingNode, new SimpleRemapper(this.fieldMapping) {
				@Override
				public String mapFieldName(final String owner, final String name, final String desc) {
					final String remappedName = this.map(owner + "." + name + "." + desc);
					return remappedName != null ? remappedName : name;
				}
			}));
			remappedFields.put(remappingNode.name, remappingNode);
		});
		this.getClasses().clear();
		this.getClasses().putAll(remappedFields);
	}

	private void remapClasses() {
		final Dictionary packagesDictionary = Types.getDictionary(this.getConfiguration(), CustomDictionary.PACKAGES),
				classesDictionary = Types.getDictionary(this.getConfiguration(), CustomDictionary.CLASSES);
		final Map<String, String> packages = new HashMap<>();
		final Map<String, ClassNode> classes = new HashMap<>();
		Util.sortByComparator(this.getClasses()).values().forEach(classNode -> {
			if (!this.isExcludedClass(classNode)) {
				if (this.getConfiguration().getRenameMembers().isRemovePackages() || !classNode.name.contains("/")) {
					final String classNext = classesDictionary.next();
					this.classMapping.put(classNode.name, classNext);
					classNode.name = classNext;
				} else {
					final String packageName = classNode.name.substring(0, classNode.name.lastIndexOf('/'));
					if (!packages.containsKey(packageName)) {
						classesDictionary.reset();
						packages.put(packageName, packagesDictionary.next());
					}
					String classNext = classesDictionary.next();
					final String packageNext = packages.get(packageName) + "/";
					while (this.classMapping.containsValue(packageNext + classNext)) {
						classNext = classesDictionary.next();
					}
					this.classMapping.put(classNode.name, packageNext + classNext);
					classNode.name = packageNext + classNext;
				}
			}
			classes.put(classNode.name, classNode);
		});
		this.getClasses().clear();
		this.getClasses().putAll(classes);
	}

	private void applyClasses() {
		final Map<String, ClassNode> remappedClasses = new HashMap<>();
		this.getClasses().values().forEach(classNode -> {
			final ClassNode remappingNode = new ClassNode();
			classNode.accept(new ClassRemapper(remappingNode, new Remapper() {
				@Override
				public String map(final String type) {
					if (!RenameMembersTransformer.this.classMapping.containsKey(type)) {
						return type;
					}
					return RenameMembersTransformer.this.classMapping.get(type);
				}
			}));
			remappedClasses.put(remappingNode.name, remappingNode);
		});
		this.getClasses().values().clear();
		this.getClasses().keySet().clear();
		this.getClasses().putAll(remappedClasses);
	}

	private Set<String> iterateParents(final Set<String> parents, final Set<String> set) {
		parents.stream().map(parent -> this.remmapingHierarchy.getClassNode(parent)).forEach(parent -> {
			set.addAll(this.iterateInterfaces(parent.interfaces, new HashSet<>()));
			parent.methods.forEach(methodNode -> set.add(methodNode.name));
		});
		parents.forEach(parent -> {
			this.iterateParents(this.remmapingHierarchy.getTree(parent).getParentClasses(), set);
		});
		return set;
	}

	private Set<String> iterateInterfaces(final List<String> interfaces, final Set<String> set) {
		interfaces.stream().map(interfaceNode -> this.remmapingHierarchy.getClassNode(interfaceNode))
				.forEach(parent -> {
					parent.methods.forEach(methodNode -> set.add(methodNode.name));
				});
		interfaces.forEach(parent -> {
			this.iterateInterfaces(this.remmapingHierarchy.getTree(parent).getClassNode().interfaces, set);
		});
		return set;
	}

	private void remapParentFields(final Set<String> tree, final ClassNode classNode) {
		tree.stream().map(parent -> this.remmapingHierarchy.getClassNode(parent))
				.filter(parent -> !this.getDependencies().containsKey(parent.name)).forEach(parent -> {
					parent.fields.forEach(fieldNode -> {
						this.fieldMapping.put(classNode.name + "." + fieldNode.name + "." + fieldNode.desc,
								this.fieldMapping.get(parent.name + "." + fieldNode.name + "." + fieldNode.desc));
					});
				});
		tree.forEach(parent -> {
			if (!this.getDependencies().containsKey(parent)) {
				this.remapParentFields(this.remmapingHierarchy.getTree(parent).getParentClasses(), classNode);
			}
		});
	}

	private void remapParentMethods(final Set<String> tree, final ClassNode classNode) {
		tree.stream().map(parent -> this.remmapingHierarchy.getClassNode(parent))
				.filter(parent -> !this.getDependencies().containsKey(parent.name)).forEach(parent -> {
					parent.methods.forEach(methodNode -> {
						this.methodMapping.put(classNode.name + "." + methodNode.name + "." + methodNode.desc,
								this.methodMapping.get(parent.name + "." + methodNode.name + "." + methodNode.desc));
					});
				});
		tree.forEach(parent -> {
			if (!this.getDependencies().containsKey(parent)) {
				this.remapParentMethods(this.remmapingHierarchy.getTree(parent).getParentClasses(), classNode);
			}
		});
	}

	private boolean isExcludedClass(final ClassNode classNode) {
		final AtomicBoolean atomicBoolean = new AtomicBoolean(false);
		this.getConfiguration().getRenameMembers().getExcludeClasses().forEach(classNode_ -> {
			if (classNode.name.startsWith(classNode_) || classNode.name.equals(classNode_)) {
				atomicBoolean.set(true);
			}
		});
		return atomicBoolean.get();
	}

	private boolean isExcludedField(final ClassNode classNode, final FieldNode fieldNode) {
		final AtomicBoolean atomicBoolean = new AtomicBoolean(false);
		this.getConfiguration().getRenameMembers().getExcludeFields().forEach(classNode_ -> {
			if (classNode.name.startsWith(classNode_) || classNode.name.equals(classNode_)
					|| classNode_.equals(classNode.name + "/" + fieldNode.name)) {
				atomicBoolean.set(true);
			}
		});
		return atomicBoolean.get();
	}

	private boolean isExcludedMethod(final ClassNode classNode, final MethodNode methodNode) {
		final AtomicBoolean atomicBoolean = new AtomicBoolean(false);
		this.getConfiguration().getRenameMembers().getExcludeMethods().forEach(classNode_ -> {
			if (classNode.name.startsWith(classNode_) || classNode.name.equals(classNode_)
					|| classNode_.equals(classNode.name + "/" + methodNode.name)) {
				atomicBoolean.set(true);
			}
		});
		return atomicBoolean.get();
	}

}
