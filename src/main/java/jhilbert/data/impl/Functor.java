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

package jhilbert.data.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import jhilbert.data.Kind;
import jhilbert.data.impl.ComplexTerm;
import jhilbert.data.impl.DataImpl;
import jhilbert.exceptions.DataException;

/**
 * A term which combines zero or more input terms to a new term.
 */
final class Functor extends ComplexTerm {

	/**
	 * Serialization ID.
	 */
	private static final long serialVersionUID = DataImpl.FORMAT_VERSION;

	/**
	 * Place count.
	 */
	private int placeCount;

	/**
	 * Input kinds.
	 */
	private List<Kind> inputKinds;

	/**
	 * Creates a new complex term with the specified name, kind and input terms.
	 *
	 * @param name term name (must not be <code>null</code>).
	 * @param kind result kind (must not be <code>null</code>).
	 * @param inputKinds list of input kinds (must not be <code>null</code>).
	 */
	public Functor(final String name, final Kind kind, final List<Kind> inputKinds) {
		super(name, kind);
		assert (inputKinds != null): "Supplied list of input kinds is null.";
		placeCount = inputKinds.size();
		this.inputKinds = inputKinds;
	}

	/**
	 * Creates a new complex term with the specified name, unknown kind and unknown input terms.
	 *
	 * @param name term name (must not be <code>null</code>).
	 */
	public Functor(final String name) {
		super(name);
		placeCount = -1;
		inputKinds = new LinkedList();
	}

	/**
	 * Creates an uninitialized functor.
	 * Used by serialization.
	 */
	public Functor() {
		super();
		placeCount = 0;
		inputKinds = null;
	}

	public @Override int placeCount() {
		return placeCount;
	}

	protected @Override void setPlaceCount(final int count) {
		assert (placeCount == -1): "Attempted to set already determined place count.";
		placeCount = count;
	}

	public @Override Kind getInputKind(final int i) {
		try {
			return inputKinds.get(i);
		} catch (IndexOutOfBoundsException e) {
			assert (i >= 0): "Negative input kind index.";
			assert (placeCount == -1): "Input kind index out of bounds while checking term with known place "
				+ "count.";
			return null;
		}
	}

	protected @Override void setInputKind(final int i, final Kind kind) {
		assert (i >= 0): "Negative input kind index.";
		if (i >= inputKinds.size()) {
			// assume sequential setting of input kinds
			assert (i == inputKinds.size()): "Internal error: nonsequential initial setting of input kinds.";
			inputKinds.add(kind);
		} else {
			assert (inputKinds.get(i) == null): "Attempted to set already determined input kind.";
			inputKinds.set(i, kind);
		}
	}

	public @Override int definitionDepth() {
		return 0;
	}

}
