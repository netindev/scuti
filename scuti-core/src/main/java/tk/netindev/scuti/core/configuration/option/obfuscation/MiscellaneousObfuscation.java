package tk.netindev.scuti.core.configuration.option.obfuscation;

import java.util.ArrayList;
import java.util.List;

import tk.netindev.scuti.core.configuration.Option;

/**
 *
 * @author netindev
 *
 */
public class MiscellaneousObfuscation extends Option {

	private List<String> variableDescriptorList;

	private boolean invalidAnnotation, massiveSource, massiveSignature, pushTransient, pushVarargs, variableDescritor,
			duplicateVariables, randomExceptions;

	public List<String> getVariableDescriptorList() {
		return this.variableDescriptorList;
	}

	public void setVariableDescriptorList(final List<String> variableDescriptorList) {
		this.variableDescriptorList = variableDescriptorList;
	}

	public boolean isInvalidAnnotation() {
		return this.invalidAnnotation;
	}

	public void setInvalidAnnotation(final boolean invalidAnnotation) {
		this.invalidAnnotation = invalidAnnotation;
	}

	public boolean isMassiveSource() {
		return this.massiveSource;
	}

	public void setMassiveSource(final boolean massiveSource) {
		this.massiveSource = massiveSource;
	}

	public boolean isMassiveSignature() {
		return this.massiveSignature;
	}

	public void setMassiveSignature(final boolean massiveSignature) {
		this.massiveSignature = massiveSignature;
	}

	public boolean isPushTransient() {
		return this.pushTransient;
	}

	public void setPushTransient(final boolean pushTransient) {
		this.pushTransient = pushTransient;
	}

	public boolean isPushVarargs() {
		return this.pushVarargs;
	}

	public void setPushVarargs(final boolean pushVarargs) {
		this.pushVarargs = pushVarargs;
	}

	public boolean isVariableDescritor() {
		return this.variableDescritor;
	}

	public void setVariableDescritor(final boolean variableDescritor) {
		this.variableDescritor = variableDescritor;
	}

	public boolean isDuplicateVariables() {
		return this.duplicateVariables;
	}

	public void setDuplicateVariables(final boolean duplicateVariables) {
		this.duplicateVariables = duplicateVariables;
	}

	public boolean isRandomExceptions() {
		return this.randomExceptions;
	}

	public void setRandomExceptions(final boolean randomExceptions) {
		this.randomExceptions = randomExceptions;
	}

	/* default config */ {
		this.setVariableDescriptorList(new ArrayList<>());
	}

}
