/*
    JHilbert, a verifier for collaborative theorem proving
    Copyright © 2008 Alexander Klauer

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    You may contact the author on these Wiki pages:
    http://planetx.cc.vt.edu/AsteroidMeta//GrafZahl (preferred)
    http://en.wikisource.org/wiki/User_talk:GrafZahl
*/

package jhilbert.data;

import java.util.List;
import jhilbert.data.AbstractName;
import jhilbert.data.Term;

/**
 * A term which combines zero or more input terms to a new term.
 */
public abstract class AbstractComplexTerm extends AbstractName implements Term {

	/**
	 * Resulting kind of this term.
	 */
	private final String kind;

	/**
	 * Creates a new complex term with the specified name, kind and input terms.
	 *
	 * @param name term name (must not be <code>null</code>).
	 * @param kind result kind (must not be <code>null</code>).
	 */
	public AbstractComplexTerm(final String name, final String kind) {
		super(name);
		assert (kind != null): "Supplied kind is null.";
		this.kind = kind;
	}

	public String getKind() {
		return kind;
	}

	/**
	 * Returns the definition depth of this term.
	 * This method always returns <code>0</code>. The {@link Definition} subclass
	 * overrides this method.
	 *
	 * @return definition depth of this term.
	 *
	 * @see Definition#definitionDepth()
	 */
	public int definitionDepth() {
		return 0;
	}

	/**
	 * Returns the number of places of this term.
	 * That is, the number of {@link TermExpression}s which must follow this term's name in a LISP expression.
	 *
	 * @return number of places of this term.
	 */
	public abstract int placeCount();

	/**
	 * Returns the kind of the i-th input term.
	 *
	 * @param i input term number (starting from zero).
	 *
	 * @return kind of the i-th input term.
	 *
	 * @throws IndexOutOfBoundsException if i is not positive or greater than {@link #placeCount()}.
	 */
	public abstract String getInputKind(final int i); 

	public final boolean isVariable() {
		return false;
	}

}
