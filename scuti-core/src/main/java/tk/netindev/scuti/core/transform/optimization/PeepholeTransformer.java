package tk.netindev.scuti.core.transform.optimization;

import java.util.Map;

import org.objectweb.asm.tree.ClassNode;

import tk.netindev.scuti.core.configuration.Configuration;
import tk.netindev.scuti.core.transform.Transformer;

public class PeepholeTransformer extends Transformer {

	public PeepholeTransformer(final Configuration configuration, final Map<String, ClassNode> classes,
			final Map<String, ClassNode> dependencies) {
		super(configuration, classes, dependencies);
	}

	@Override
	public void transform() {

	}

}
