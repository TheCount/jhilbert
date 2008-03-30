package jhilbert.data;

import jhilbert.data.Variable;

/**
 * Dummy variable.
 * Dummy variables are never equal to non-dummy variables.
 */
public class DummyVariable extends Variable {

	/**
	 * Internal id of this dummy.
	 */
	private static int id;

	/**
	 * Initialize id with zero.
	 */
	static {
		id = 0;
	}

	/**
	 * Creates a new dummy variable with the specified kind.
	 * This dummy variable will be unequal to all other currently existing variables.
	 *
	 * @param kind kind of dummy variable.
	 *
	 * @throws NullPointerException if kind is <code>null</code>.
	 */
	public DummyVariable(final String kind) {
		super("(dummy" + id + ")", kind);
		++id;
	}

}
