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
import jhilbert.data.DataException;
import jhilbert.data.Kind;
import jhilbert.data.Term;
import jhilbert.data.impl.Definition;
import jhilbert.data.impl.Functor;
import jhilbert.data.impl.NameImpl;
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
	 * Creates a new complex term with the specified name and kind.
	 *
	 * @param name term name (must not be <code>null</code>).
	 * @param kind result kind (may be <code>null</code> if unknown).
	 */
	protected ComplexTerm(final String name, final Kind kind) {
		super(name);
		this.kind = kind;
	}

	/**
	 * Creates a new complex term with the specified name and unknown kind.
	 *
	 * @param name term name (must not be <code>null</code>).
	 */
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
	// FIXME
	//public final void ensureKind(final Kind kind) throws DataException {
	//	if (this.kind == null) {
	//		this.kind = kind;
	//		return;
	//	}
	//	if (kind == null)
	//		return;
	//	if (!this.kind.equals(kind)) {
	//		logger.error("Result kind mismatch in term " + this.toString());
	//		logger.error("Required result kind: " + kind);
	//		logger.error("Actual result kind: " + this.kind);
	//		throw new DataException("Result kind mismatch.", this.toString());
	//	}
	//}

	/**
	 * Returns the definition depth of this term.
	 *
	 * @return definition depth of this term.
	 *
	 * @see Definition#definitionDepth()
	 */
	public abstract int definitionDepth();

	/**
	 * Returns the number of places of this term.
	 * That is, the number of {@link TermExpression}s which must follow this term's name in a LISP expression.
	 *
	 * @return number of places of this term, or <code>-1</code> if the number of places is not known yet.
	 */
	public abstract int placeCount();

	/**
	 * Sets the number of places of this term.
	 *
	 * @param count number of places this term should have.
	 */
	// FIXME
	// protected abstract void setPlaceCount(int count);

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
	//public final void ensurePlaceCount(final int placeCount) throws DataException {
	//	if (placeCount == -1)
	//		return;
	//	assert (placeCount >= 0): "Supplied place count is out of bounds.";
	//	final int thisPlaceCount = placeCount();
	//	if (thisPlaceCount == -1) {
	//		setPlaceCount(placeCount);
	//		return;
	//	}
	//	if (thisPlaceCount == placeCount)
	//		return;
	//	logger.error("Place count mismatch in term " + this.toString());
	//	logger.error("Required place count: " + placeCount);
	//	logger.error("Actual place count: " + thisPlaceCount);
	//	throw new DataException("Place count mismatch", this.toString());
	//}

	/**
	 * Returns the kind of the i-th input term.
	 *
	 * @param i input term number (starting from zero).
	 *
	 * @return kind of the i-th input term, or <code>null</code> if that kind is not known yet.
	 */
	public abstract Kind getInputKind(final int i);

	/**
	 * Sets the kind of the i-th input term.
	 *
	 * @param i input term number (starting from zero).
	 * @param kind the kind.
	 */
	// FIXME
	// protected abstract void setInputKind(int i, Kind kind);

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
	//public final void ensureInputKind(final int i, final Kind kind) throws DataException {
	//	final Kind thisInputKind = getInputKind(i);
	//	if (thisInputKind == null) {
	//		setInputKind(i, kind);
	//		return;
	//	}
	//	if (kind == null)
	//		return;
	//	if (!thisInputKind.equals(kind)) {
	//		logger.error("Input kind mismatch in term " + this.toString());
	//		logger.error("Input kind place: " + (i + 1));
	//		logger.error("Required input kind: " + kind);
	//		logger.error("Actual input kind: " + thisInputKind);
	//		throw new DataException("Input kind mismatch", this.toString());
	//	}
	//}

	public final boolean isVariable() {
		return false;
	}

}
