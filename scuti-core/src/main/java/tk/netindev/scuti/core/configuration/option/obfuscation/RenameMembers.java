package tk.netindev.scuti.core.configuration.option.obfuscation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import tk.netindev.scuti.core.configuration.Option;
import tk.netindev.scuti.core.dictionary.Types;

/**
 *
 * @author netindev
 *
 */
public class RenameMembers extends Option {

	private List<String> excludeClasses, excludeMethods, excludeFields;
	private File packagesDictionary, classesDictionary, methodsDictionary, fieldsDictionary;
	private boolean renameClasses, renameMethods, renameFields, removePackages;
	private Types randomize;

	public List<String> getExcludeClasses() {
		return this.excludeClasses;
	}

	public void setExcludeClasses(final List<String> excludeClasses) {
		this.excludeClasses = excludeClasses;
	}

	public List<String> getExcludeMethods() {
		return this.excludeMethods;
	}

	public void setExcludeMethods(final List<String> excludeMethods) {
		this.excludeMethods = excludeMethods;
	}

	public List<String> getExcludeFields() {
		return this.excludeFields;
	}

	public void setExcludeFields(final List<String> excludeFields) {
		this.excludeFields = excludeFields;
	}

	public File getPackagesDictionary() {
		return this.packagesDictionary;
	}

	public void setPackagesDictionary(final File packagesDictionary) {
		this.packagesDictionary = packagesDictionary;
	}

	public File getClassesDictionary() {
		return this.classesDictionary;
	}

	public void setClassesDictionary(final File classesDictionary) {
		this.classesDictionary = classesDictionary;
	}

	public File getMethodsDictionary() {
		return this.methodsDictionary;
	}

	public void setMethodsDictionary(final File methodsDictionary) {
		this.methodsDictionary = methodsDictionary;
	}

	public File getFieldsDictionary() {
		return this.fieldsDictionary;
	}

	public void setFieldsDictionary(final File fieldsDictionary) {
		this.fieldsDictionary = fieldsDictionary;
	}

	public boolean isRenameClasses() {
		return this.renameClasses;
	}

	public void setRenameClasses(final boolean renameClasses) {
		this.renameClasses = renameClasses;
	}

	public boolean isRenameMethods() {
		return this.renameMethods;
	}

	public void setRenameMethods(final boolean renameMethods) {
		this.renameMethods = renameMethods;
	}

	public boolean isRenameFields() {
		return this.renameFields;
	}

	public void setRenameFields(final boolean renameFields) {
		this.renameFields = renameFields;
	}

	public boolean isRemovePackages() {
		return this.removePackages;
	}

	public void setRemovePackages(final boolean removePackages) {
		this.removePackages = removePackages;
	}

	public Types getRandomize() {
		return this.randomize;
	}

	public void setRandomize(final Types randomize) {
		this.randomize = randomize;
	}

	/* default config */ {
		this.setRandomize(Types.ALPHABET);

		this.setExcludeClasses(new ArrayList<>());
		this.setExcludeMethods(new ArrayList<>());
		this.setExcludeFields(new ArrayList<>());
	}

}
