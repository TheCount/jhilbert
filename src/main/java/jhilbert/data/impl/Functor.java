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

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import jhilbert.data.Kind;
import jhilbert.data.impl.ComplexTerm;
import jhilbert.exceptions.DataException;
import jhilbert.util.DataInputStream;
import jhilbert.util.DataOutputStream;
import jhilbert.util.Collections;
// import org.apache.log4j.Logger;

/**
 * A term which combines zero or more input terms to a new term.
 */
final class Functor extends ComplexTerm {

	/**
	 * Logger for this class.
	 */
	// private static final Logger logger = Logger.getLogger(ComplexTerm.class);

	/**
	 * Place count.
	 */
	private int placeCount;

	/**
	 * Input kinds.
	 */
	private final List<Kind> inputKinds;

	/**
	 * Creates a new complex term with the specified name, kind and input terms.
	 *
	 * @param name term name (must not be <code>null</code>).
	 * @param kind result kind (must not be <code>null</code>).
	 * @param inputKinds list of input kinds (must not be <code>null</code>).
	 */
	// FIXME
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
	// FIXME
	public Functor (final String name) {
		super(name);
		placeCount = -1;
		inputKinds = new LinkedList();
	}

	/**
	 * Loads a new complex term with the specified name, kind and place count from the specified input stream.
	 *
	 * @param name name of this term.
	 * @param kind kind of this term.
	 * @param placeCount number of places of this term.
	 * @param in data input stream.
	 * @param data data.
	 * @param nameList list of names.
	 * @param kindsLower lower bound for kind names.
	 * @param kindsUpper upper bound for kind names.
	 *
	 * @throws EOFException upon unexpected end of stream.
	 * @throws IOException if an I/O error occurs.
	 * @throws DataException if the input stream is inconsistent.
	 */
	Functor(final String name, final Kind kind, final int placeCount, final DataInputStream in, final DataImpl data,
		final List<String> nameList, final int kindsLower, final int kindsUpper)
	throws EOFException, IOException, DataException {
		super(name, kind);
		assert (placeCount >= 0): "Negative place count specified.";
		this.placeCount = placeCount;
		inputKinds = new ArrayList(placeCount);
		for (int i = 0; i != placeCount; ++i)
			inputKinds.add(data.getKind(nameList.get(in.readIntOr0(kindsLower, kindsUpper))));
	}

	// FIXME
	public @Override int placeCount() {
		return placeCount;
	}

	// FIXME
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

	// FIXME
	@Override void store(final DataOutputStream out, final Map<String, Integer> kindNameTable,
			final Map<String, Integer> termNameTable) throws IOException {
		assert (placeCount != -1): "Attempted to store complex term with unknown place count";
		super.store(out, kindNameTable, termNameTable);
		out.writeInt(placeCount);
		for(final Kind inputKind: inputKinds)
			if (inputKind == null)
				out.writeInt(0);
			else
				out.writeInt(kindNameTable.get(inputKind.toString()));
	}

	// FIXME
	//public @Override ComplexTerm clone() {
	//	List<String> clonedList = new ArrayList(inputKinds.size());
	//	Collections.clone(clonedList, inputKinds);
	//	return new ComplexTerm(getName().clone(), kind.clone(), clonedList);
	//}

}
