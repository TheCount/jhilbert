package jhilbert.data;

import jhilbert.data.Pair;

/**
 * A Pair of {@link Variable}s.
 * Variable Pairs are Cloneable and Comparable.
 */
public class VariablePair extends Pair<Variable, Variable> implements Comparable<VariablePair> {

	/**
	 * Creates a new VariablePair.
	 *
	 * @param first first Variable.
	 * @param second second Variable.
	 *
	 * @throws NullPointerException if one of the parameters is <code>null</code>.
	 */
	public VariablePair(final Variable first, final Variable second) {
		super(first, second);
	}

	/**
	 * Returns a new VariablePair with reversed values.
	 *
	 * @return new VariablePair with reversed values.
	 */
	public VariablePair reverse() {
		return new VariablePair(getSecond(), getFirst());
	}

	/**
	 * Implements lexicographic order as natural order.
	 *
	 * @param p the VariablePair this object should be compared with.
	 *
	 * @return an integer less than, equal to, or greater than zero as this object is less than, equal to, or greater than the specified object.
	 */
	public int compareTo(final VariablePair p) {
		final int firstComp = getFirst().compareTo(p.getFirst());
		if (firstComp != 0)
			return firstComp;
		return getSecond().compareTo(p.getSecond());
	}

}
