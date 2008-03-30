package jhilbert.data;

import java.util.HashMap;
import java.util.Map;
import jhilbert.data.Name;

/**
 * Name to be used as unique identifier in a namespace.
 * FIXME: enhance this description.
 */
public abstract class AbstractName implements Name {

	/**
	 * Name.
	 */
	private final String name;

	/**
	 * Create a new AbstractName.
	 *
	 * @param name the Name.
	 */
	protected AbstractName(final String name) {
		assert (name != null): "Supplied name is null.";
		this.name = name;
	}

	/**
	 * Returns this name as a String.
	 *
	 * @return name as a String.
	 *
	 * @see #toString()
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Returns a String representation of this name.
	 * This is an alias for {@link #getName()}.
	 *
	 * @see #getName()
	 */
	public final String toString() {
		return name;
	}

	public int hashCode() {
		return name.hashCode();
	}

	public boolean equals(final Name n) {
		return name.equals(n.getName());
	}

	public int compareTo(final Name n) {
		return name.compareTo(n.getName());
	}

}
