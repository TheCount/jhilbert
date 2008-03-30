package jhilbert.data;

/**
 * A pair (2-tuple) of values.
 * <code>null</code> values are not allowed.
 *
 * @param E1 type of first entry.
 * @param E2 type of second entry.
 */
public class Pair<E1, E2> {

	/**
	 * First value.
	 */
	private final E1 first;

	/**
	 * Second value.
	 */
	private final E2 second;

	/**
	 * Creates a new Pair.
	 *
	 * @param first first value.
	 * @param second second value.
	 *
	 * @throws NullPointerException if one of the parameters is <code>null</code>.
	 */
	public Pair(final E1 first, final E2 second) {
		if ((first == null) || (second == null))
			throw new NullPointerException("Null values are not allowed in Pairs.");
		this.first = first;
		this.second = second;
	}

	/**
	 * Copy constructor.
	 * Creates a shallow copy of this Pair.
	 *
	 * @param p Pair to be copied.
	 */
	public Pair(final Pair<E1, E2> p) {
		first = p.first;
		second = p.second;
	}

	/**
	 * Returns the first value.
	 *
	 * @return first value.
	 */
	public E1 getFirst() {
		return first;
	}

	/**
	 * Returns the second value.
	 *
	 * @return second value.
	 */
	public E2 getSecond() {
		return second;
	}

	public @Override int hashCode() {
		return first.hashCode() + (~second.hashCode());
	}

	public @Override boolean equals(final Object o) {
		Pair<E1, E2> p;
		try {
			p = (Pair<E1, E2>) o;
		} catch (ClassCastException e) {
			return false;
		}
		return (first.equals(p.first) && second.equals(p.second));
	}

	public @Override String toString() {
		return "(" + first.toString() + ", " + second.toString() + ")";
	}

}
