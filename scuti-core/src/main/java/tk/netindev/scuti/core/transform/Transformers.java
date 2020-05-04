package tk.netindev.scuti.core.transform;

import tk.netindev.scuti.core.transform.obfuscation.ControlFlowTransformer;
import tk.netindev.scuti.core.transform.obfuscation.ClassEncryptTransformer;
import tk.netindev.scuti.core.transform.obfuscation.HideCodeTransformer;
import tk.netindev.scuti.core.transform.obfuscation.InvokeDynamicTransformer;
import tk.netindev.scuti.core.transform.obfuscation.MiscellaneousObfuscationTransformer;
import tk.netindev.scuti.core.transform.obfuscation.NumberObfuscationTransformer;
import tk.netindev.scuti.core.transform.obfuscation.RenameMembersTransformer;
import tk.netindev.scuti.core.transform.obfuscation.ShuffleMembersTransformer;
import tk.netindev.scuti.core.transform.obfuscation.StringEncryptionTransformer;
import tk.netindev.scuti.core.transform.optimization.ConstantTransformer;
import tk.netindev.scuti.core.transform.optimization.DeadCodeTransformer;
import tk.netindev.scuti.core.transform.optimization.LoopTransformer;
import tk.netindev.scuti.core.transform.optimization.NoOperationTransformer;
import tk.netindev.scuti.core.transform.optimization.PeepholeTransformer;
import tk.netindev.scuti.core.transform.optimization.RedundantTransformer;
import tk.netindev.scuti.core.transform.shrinking.InnerClassTransformer;
import tk.netindev.scuti.core.transform.shrinking.UnusedMemberTransformer;

/**
 *
 * @author netindev
 *
 */
public interface Transformers {

	/**
	 *
	 * @author netindev
	 *
	 */
	public static final class Obfuscation {
		public static final Class<? extends Transformer> CLASS_ENCRYPT_TRANSFORMER = ClassEncryptTransformer.class;
		public static final Class<? extends Transformer> CONTROL_FLOW_TRANSFORMER = ControlFlowTransformer.class;
		public static final Class<? extends Transformer> HIDE_CODE_TRANSFORMER = HideCodeTransformer.class;
		public static final Class<? extends Transformer> INVOKE_DYNAMIC_TRANSFORMER = InvokeDynamicTransformer.class;
		public static final Class<? extends Transformer> MISCELLANEOUS_OBFUSCATION_TRANSFORMER = MiscellaneousObfuscationTransformer.class;
		public static final Class<? extends Transformer> NUMBER_OBFUSCATION_TRANSFORMER = NumberObfuscationTransformer.class;
		public static final Class<? extends Transformer> RENAME_MEMBERS_TRANSFORMER = RenameMembersTransformer.class;
		public static final Class<? extends Transformer> SHUFFLE_MEMBERS_TRANSFORMER = ShuffleMembersTransformer.class;
		public static final Class<? extends Transformer> STRING_ENCRYPTION_TRANSFORMER = StringEncryptionTransformer.class;
	}

	/**
	 *
	 * @author netindev
	 *
	 */
	public static final class Optimization {
		public static final Class<? extends Transformer> CONSTANT_TRANSFORMER = ConstantTransformer.class;
		public static final Class<? extends Transformer> DEAD_CODE_TRANSFORMER = DeadCodeTransformer.class;
		public static final Class<? extends Transformer> LOOP_TRANSFORMER = LoopTransformer.class;
		public static final Class<? extends Transformer> PEEPHOLE_TRANSFORMER = PeepholeTransformer.class;
		public static final Class<? extends Transformer> NO_OPERATION_TRANSFORMER = NoOperationTransformer.class;
		public static final Class<? extends Transformer> REDUNDANT_TRANSFORMER = RedundantTransformer.class;
	}

	/**
	 *
	 * @author netindev
	 *
	 */
	public static final class Shrinking {
		public static final Class<? extends Transformer> INNER_CLASS_TRANSFORMER = InnerClassTransformer.class;
		public static final Class<? extends Transformer> UNUSED_MEMBER_TRANSFORMER = UnusedMemberTransformer.class;
	}

}
