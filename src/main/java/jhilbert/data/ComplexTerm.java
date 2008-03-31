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
