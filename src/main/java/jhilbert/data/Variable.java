package jhilbert.data;

import jhilbert.data.AbstractName;
import jhilbert.data.Symbol;

/**
 * A variable.
 * FIXME: enhance description.
 */
public class Variable extends AbstractName implements Term, Symbol {

	/**
	 * Kind of this variable.
	 */
	private final String kind;

	/**
	 * Create a new Variable with the specified name and kind.
	 *
	 * @param name name of this variable.
	 * @param kind kind of this variable.
	 */
	public Variable(final String name, final String kind) {
		super(name);
		assert (kind != null): "Supplied kind is null.";
		this.kind = kind;
	}

	/**
	 * Returns the kind of this variable.
	 *
	 * @return the kind of this variable.
	 */
	public String getKind() {
		return kind;
	}

}
