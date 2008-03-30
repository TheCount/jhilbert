package jhilbert.data;

import java.util.List;
import jhilbert.data.AbstractComplexTerm;

/**
 * A term which combines zero or more input terms to a new term.
 */
public class ComplexTerm extends AbstractComplexTerm {

	/**
	 * Input kinds.
	 */
	private final List<String> inputKinds;

	/**
	 * Creates a new complex term with the specified name, kind and input terms.
	 *
	 * @param name term name (must not be <code>null</code>).
	 * @param kind result kind (must not be <code>null</code>).
	 * @param inputKinds list of input kinds (must not be <code>null</code>).
	 */
	public ComplexTerm(final String name, final String kind, final List<String> inputKinds) {
		super(name, kind);
		assert (inputKinds != null): "Supplied list of input kinds is null.";
		this.inputKinds = inputKinds;
	}

	public @Override int placeCount() {
		return inputKinds.size();
	}

	public @Override String getInputKind(final int i) {
		return inputKinds.get(i);
	}

}
