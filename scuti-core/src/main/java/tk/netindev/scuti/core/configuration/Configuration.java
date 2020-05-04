package tk.netindev.scuti.core.configuration;

import java.io.File;
import java.util.List;
import java.util.Set;

import tk.netindev.scuti.core.configuration.option.obfuscation.ClassEncrypt;
import tk.netindev.scuti.core.configuration.option.obfuscation.MiscellaneousObfuscation;
import tk.netindev.scuti.core.configuration.option.obfuscation.NumberObfuscation;
import tk.netindev.scuti.core.configuration.option.obfuscation.RenameMembers;
import tk.netindev.scuti.core.configuration.option.obfuscation.StringEncryption;
import tk.netindev.scuti.core.configuration.option.shrinking.UnusedMembers;
import tk.netindev.scuti.core.transform.Transformer;

/**
 *
 * @author netindev
 *
 */
public interface Configuration {

	File getInput();

	File getOutput();

	List<File> getDependencies();

	Set<Class<? extends Transformer>> getTransformers();

	boolean corruptCRC32();
	
	boolean corruptNames();

	ClassEncrypt getClassEncrypt();

	MiscellaneousObfuscation getMiscellaneousObfuscation();

	NumberObfuscation getNumberObfuscation();

	RenameMembers getRenameMembers();

	StringEncryption getStringEncryption();

	UnusedMembers getUnusedMembers();

}
