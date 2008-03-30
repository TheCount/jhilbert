package jhilbert.data;

import jhilbert.data.Name;

/**
 * A Term.
 * A term can either be a {@link Variable} or an {@link AbstractComplexTerm}.
 */
public interface Term extends Name {

	/**
	 * Returns the kind of this term.
	 */
	public String getKind();

}
