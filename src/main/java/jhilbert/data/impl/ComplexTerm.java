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
import java.util.List;
import java.util.Map;
import jhilbert.data.Data;
import jhilbert.data.Kind;
import jhilbert.data.Term;
import jhilbert.data.impl.Definition;
import jhilbert.data.impl.Functor;
import jhilbert.data.impl.NameImpl;
import jhilbert.exceptions.DataException;
import jhilbert.util.DataInputStream;
import jhilbert.util.DataOutputStream;
import org.apache.log4j.Logger;

/**
 * A term which combines zero or more input terms to a new term.
 */
abstract class ComplexTerm extends NameImpl implements Term {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(ComplexTerm.class);

	/**
	 * Resulting kind of this term.
	 */
	private Kind kind;

	/**
	 * Loads a new complex term from the specified input stream.
	 *
	 * @param name term name.
	 * @param in data input stream.
	 * @param data interface data.
	 * @param nameList list of names.
	 * @param kindsLower lower bound for kind names.
	 * @param kindsUpper upper bound for kind names.
	 * @param termsLower lower bound for term names (optional).
	 * @param termsUpper upper bound for term names (mandatory iff termsLower is provided).
	 *
	 * @throws EOFException upon unexpected end of stream.
	 * @throws IOException if an I/O error occurs.
	 * @throws DataException if the input stream is inconsistent.
	 */
	// FIXME
	static ComplexTerm create(final String name, final DataInputStream in, final DataImpl data,
		final List<String> nameList, final int kindsLower, final int kindsUpper)
	throws EOFException, IOException, DataException {
		return create(name, in, data, nameList, kindsLower, kindsUpper, -1, -1);
	}
	static ComplexTerm create(final String name, final DataInputStream in, final DataImpl data,
		final List<String> nameList, final int kindsLower, final int kindsUpper, final int termsLower,
		final int termsUpper)
	throws EOFException, IOException, DataException {
		assert (name != null): "Specified name is null.";
		assert (in != null): "Specified data input stream is null.";
		assert (data != null): "Specified data are null.";
		assert (nameList != null): "Specified name list is null.";
		assert (kindsLower > 0): "Specified lower kinds bound is not positive.";
		assert (kindsUpper >= kindsLower): "Specified upper kinds bound is smaller than lower bound.";
		assert (termsUpper >= termsLower): "Specified upper terms bound is smaller than lower bound.";
		final Kind kind = data.getKind(nameList.get(in.readIntOr0(kindsLower, kindsUpper)));
		final int placeCount = in.readInt();
		if (placeCount >= 0)
			return new Functor(name, kind, placeCount, in, data, nameList, kindsLower, kindsUpper);
		else if (termsLower != -1)
			return new Definition(name, kind, ~placeCount, in, data, nameList, kindsLower, kindsUpper,
					termsLower, termsUpper);
		else {
			logger.error("No term bounds provided for loading definition " + name);
			throw new DataException("No term bounds provided for loading definition", name);
		}
	}

	/**
	 * Creates a new complex term with the specified name and kind.
	 *
	 * @param name term name (must not be <code>null</code>).
	 * @param kind result kind (may be <code>null</code> if unknown).
	 */
	// FIXME
	protected ComplexTerm(final String name, final Kind kind) {
		super(name);
		this.kind = kind;
	}

	/**
	 * Creates a new complex term with the specified name and unknown kind.
	 *
	 * @param name term name (must not be <code>null</code>).
	 */
	// FIXME
	protected ComplexTerm(final String name) {
		this(name, null);
	}

	/**
	 * Creates an uninitialized complex term.
	 * Used by serialization.
	 */
	protected ComplexTerm() {
		super();
		kind = null;
	}

	/**
	 * Returns the resulting kind of this term.
	 *
	 * @return resulting kind of this term, or <code>null</code> if the resulting kind is unknown.
	 */
	// FIXME
	public Kind getKind() {
		return kind;
	}

	/**
	 * Ensures the specified kind is the resulting kind of this term.
	 * <ul>
	 * <li>If the specified kind is <code>null</code>, no operation is performed, otherwise
	 * <li>if the resulting kind of this term is unknown, it is set to the specified kind, otherwise
	 * <li>if the specified kind equals the resulting kind of this term, no operation is performed, otherwise
	 * <li>a DataException is thrown.
	 * </ul>
	 *
	 * @param kind kind to check the resulting kind of this term against.
	 *
	 * @throws DataException if the resulting kind of this term is known and the specified kind is not
	 * 	<code>null</code>, yet they are not equal.
	 */
	public final void ensureKind(final Kind kind) throws DataException {
		if (this.kind == null) {
			this.kind = kind;
			return;
		}
		if (kind == null)
			return;
		if (!this.kind.equals(kind)) {
			logger.error("Result kind mismatch in term " + this.toString());
			logger.error("Required result kind: " + kind);
			logger.error("Actual result kind: " + this.kind);
			throw new DataException("Result kind mismatch.", this.toString());
		}
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
	// FIXME
	public int definitionDepth() {
		return 0;
	}

	/**
	 * Returns the number of places of this term.
	 * That is, the number of {@link TermExpression}s which must follow this term's name in a LISP expression.
	 *
	 * @return number of places of this term, or <code>-1</code> if the number of places is not known yet.
	 */
	// FIXME
	public abstract int placeCount();

	/**
	 * Sets the number of places of this term.
	 *
	 * @param count number of places this term should have.
	 */
	// FIXME
	protected abstract void setPlaceCount(int count);

	/**
	 * Ensures the specified place count equals the place count of this term.
	 * <ul>
	 * <li>If the specified place count is <code>-1</code>, no operation is performed, otherwise
	 * <li>if the place count of this term is unknown, it is set to the specified place count, otherwise
	 * <li>if the place count of this term equals the specified place count, no operation is performed, otherwise
	 * <li>a DataException is thrown.
	 * </ul>
	 *
	 * @param placeCount place count to check the place count of this term agains.
	 *
	 * @throws DataException as described above.
	 *
	 * @see #placeCount()
	 * @see #ensureInputKind()
	 */
	// FIXME
	public final void ensurePlaceCount(final int placeCount) throws DataException {
		if (placeCount == -1)
			return;
		assert (placeCount >= 0): "Supplied place count is out of bounds.";
		final int thisPlaceCount = placeCount();
		if (thisPlaceCount == -1) {
			setPlaceCount(placeCount);
			return;
		}
		if (thisPlaceCount == placeCount)
			return;
		logger.error("Place count mismatch in term " + this.toString());
		logger.error("Required place count: " + placeCount);
		logger.error("Actual place count: " + thisPlaceCount);
		throw new DataException("Place count mismatch", this.toString());
	}

	/**
	 * Returns the kind of the i-th input term.
	 *
	 * @param i input term number (starting from zero).
	 *
	 * @return kind of the i-th input term, or <code>null</code> if that kind is not known yet.
	 */
	// FIXME
	public abstract Kind getInputKind(final int i);

	/**
	 * Sets the kind of the i-th input term.
	 *
	 * @param i input term number (starting from zero).
	 * @param kind the kind.
	 */
	// FIXME
	protected abstract void setInputKind(int i, Kind kind);

	/**
	 * Ensures the input kind of the place with the specified number equals the specified input kind.
	 * The criteria are the same as for {@link #ensureKind()}.
	 *
	 * @param i input term number (starting from zero).
	 * @param kind kind the kind of the i-th input term is to be checked against.
	 *
	 * @throws DataException as described in {@link #ensureKind()}.
	 *
	 * @see #ensureKind()
	 */
	// FIXME
	public final void ensureInputKind(final int i, final Kind kind) throws DataException {
		final Kind thisInputKind = getInputKind(i);
		if (thisInputKind == null) {
			setInputKind(i, kind);
			return;
		}
		if (kind == null)
			return;
		if (!thisInputKind.equals(kind)) {
			logger.error("Input kind mismatch in term " + this.toString());
			logger.error("Input kind place: " + (i + 1));
			logger.error("Required input kind: " + kind);
			logger.error("Actual input kind: " + thisInputKind);
			throw new DataException("Input kind mismatch", this.toString());
		}
	}

	// FIXME
	public final boolean isVariable() {
		return false;
	}

	/**
	 * Stores this term to the specified data output stream.
	 *
	 * @param out data output stream.
	 * @param kindNameTable name to ID table for storing kinds.
	 * @param termNameTable name to ID table for storing term names.
	 *
	 * @throws IOException if an I/O-Error occurs.
	 */
	// FIXME
	void store(final DataOutputStream out, final Map<String, Integer> kindNameTable,
			final Map<String, Integer> termNameTable)
	throws IOException {
		if (kind == null)
			out.writeInt(0);
		else
			out.writeInt(kindNameTable.get(kind.toString()));
		// further storage done by subclasses
	}

	/**
	 * Checks whether this term's result kind and input kinds match that of the specified one.
	 * This may only be called on fully defined terms.
	 *
	 * @param term AbstractComplexTerm to compare against. Must be fully defined!
	 * @param data1 data context for this term.
	 * @param data2 data context for specified term.
	 *
	 * @return <code>true</code> if the result kind and input kinds of this term and the specified term match.
	 *
	 * FIXME: We should, at some point, add a containsKind() method to {@link Data} which does not add undefined kinds.
	 * FIXME: broken, don't use
	 */
	// FIXME
	//public boolean equalsSuperficially(final ComplexTerm term, final Data data1, final Data data2) {
	//	assert (term != null): "Supplied term is null.";
	//	assert ((data1 != null) && (data2 != null)): "Supplied data are null.";
	//	assert (this.kind != null): "Result kind undefined.";
	//	final String kind = data1.getKind(this.kind);
	//	assert (kind != null): "No such result kind.";
	//	if (!kind.equals(data2.getKind(term.kind)))
	//		return false;
	//	final int placeCount = getPlaceCount();
	//	assert (placeCount >= 0): "Place count undefined.";
	//	if (placeCount != term.getPlaceCount())
	//		return false;
	//	for (int i = 0; i != placeCount; ++i) {
	//		String inputKind = getInputKind(i);
	//		assert (inputKind != null): "Input kind undefined.";
	//		inputKind = data1.getKind(inputKind);
	//		assert (inputKind != null): "No such input kind.";
	//		if (!inputKind.equals(data2.getKind(term.getInputKind(i))))
	//			return false;
	//	}
	//	return true;
	//}

	// FIXME
	//public abstract @Override ComplexTerm clone();

}
