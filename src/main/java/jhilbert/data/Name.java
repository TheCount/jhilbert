package jhilbert.data;

/**
 * Name to be used as unique identifier in a namespace.
 * FIXME: enhance this description.
 */
public interface Name extends Comparable<Name> {

	/**
	 * Returns this name as a String.
	 *
	 * @return name as a String.
	 *
	 * @see #toString()
	 */
	public String getName();

	/**
	 * Returns a String representation of this name.
	 * This is an alias for {@link #getName()}.
	 *
	 * @see #getName()
	 */
	public String toString();

	/**
	 * Checks whether this Name is equal to another object.
	 *
	 * @param o Object to be compared with this Name.
	 *
	 * @return <code>true</code> if and only if o is an instance of Name, and <code>getName().equals(o.toString())</code>.
	 */
	public boolean equals(final Object o);

	/**
	 * Returns a hash code for this name.
	 * Reimplemented so that equal names return equal hash codes.
	 */
	public int hashCode();

}
