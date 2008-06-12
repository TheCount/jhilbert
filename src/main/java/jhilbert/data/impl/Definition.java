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
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jhilbert.data.Data;
import jhilbert.data.DataFactory;
import jhilbert.data.Kind;
import jhilbert.data.Term;
import jhilbert.data.TermExpression;
import jhilbert.data.Variable;
import jhilbert.data.impl.DummyVariable;
import jhilbert.data.impl.UnnamedVariable;
import jhilbert.exceptions.DataException;
import jhilbert.util.DataInputStream;
import jhilbert.util.DataOutputStream;
import org.apache.log4j.Logger;

/**
 * A Definition.
 * That is a {@link Term} defining another Term, in dependence on a list of variables.
 */
final class Definition extends ComplexTerm implements Externalizable {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(Definition.class);

	/**
	 * Variable list.
	 */
	private List<Variable> varList;

	/**
	 * The definiens.
	 */
	private TermExpressionImpl definiens;

	/**
	 * Definition depth of this definition.
	 */
	private int definitionDepth;

	/**
	 * Creates a new definition.
	 *
	 * @param name Name of this definition (must not be <code>null</code>).
	 * @param varList List of Variables of this definition (must not be <code>null</code>).
	 * @param definiens The definiens (must not be <code>null</code>).
	 */
	public Definition(final String name, final LinkedHashSet<Variable> varList, final TermExpression definiens) {
		super(name, definiens.getKind());
		assert (varList != null): "Supplied variable list is null.";
		assert (definiens instanceof TermExpressionImpl): "Definiens not of this implementation.";
		DataFactory df = DataFactory.getInstance();
		// create consistent dummy/unnamed variable mapping
		final Map<Variable, TermExpression> varMap = new HashMap();
		this.varList = new ArrayList(varList.size());
		for (Variable var: varList) {
			UnnamedVariable u = new UnnamedVariable(var.getKind());
			this.varList.add(u);
			varMap.put(var, df.createTermExpression(u));
		}
		final Set<Variable> extraVars = definiens.variables();
		extraVars.removeAll(varList);
		for (Variable var: extraVars)
			varMap.put(var, df.createTermExpression(new DummyVariable(var.getKind())));
		this.definiens = (TermExpressionImpl) definiens.subst(varMap);
		// calculate definition depth, see description of #definitionDepth()
		final Term value = this.definiens.getValue();
		if (value.isVariable())
			definitionDepth = 1;
		else
			definitionDepth = ((ComplexTerm) value).definitionDepth() + 1;
	}

	/**
	 * Creates a new definition from the specified input stream.
	 *
	 * @param name definition name.
	 * @param kind result kind.
	 * @param placeCount number of arguments to this definition.
	 * @param in data input stream.
	 * @param data interface data.
	 * @param nameList list of names.
	 * @param kindsLower lower bound for kind names.
	 * @param kindsUpper upper bound for kind names.
	 * @param termsLower lower bound for term names.
	 * @param termsUpper upper bound for term names.
	 *
	 * @throws EOFException upon unexpected end of stream.
	 * @throws IOException if an I/O error occurs.
	 * @throws DataException if the input stream is inconsistent.
	 */
	Definition(final String name, final Kind kind, final int placeCount, final DataInputStream in, final DataImpl data,
		final List<String> nameList, final int kindsLower, final int kindsUpper, final int termsLower,
		final int termsUpper)
	throws EOFException, IOException, DataException {
		super(name, kind);
		assert (placeCount >= 0): "Supplied place count is negative.";
		if (kind == null) { // definition result kinds are always known
			logger.error("Unknown result kind in definition " + name);
			throw new DataException("Unknown result kind in definition", name);
		}
		varList = new ArrayList(placeCount);
		// load variable kinds
		final int kindListSize = in.readNonNegativeInt();
		if (kindListSize < placeCount) {
			logger.error("Kind list size of definition " + name
				+ " is smaller than the number of its arguments.");
			logger.error("Kind list size: " + kindListSize);
			logger.error("Place count: " + placeCount);
			throw new DataException("Kind list size of definition is smaller than the number of its arguments",
						name);
		}
		final ArrayList<Kind> kindList = new ArrayList(kindListSize);
		for (int i = 0; i != kindListSize; ++i)
			kindList.add(data.getKind(nameList.get(in.readInt(kindsLower, kindsUpper))));
		// create extended variable list for expression parsing
		final ArrayList extendedVarList = new ArrayList(kindListSize);
		for (int i = 0; i != placeCount; ++i)
			varList.add(new UnnamedVariable(kindList.get(i)));
		extendedVarList.addAll(varList);
		for (int i = placeCount; i != kindListSize; ++i)
			extendedVarList.add(new UnnamedVariable(kindList.get(i)));
		// load definiens
		definiens = TermExpressionImpl.create(in, data, nameList, extendedVarList, termsLower, termsUpper);
		final Kind defKind = definiens.getKind();
		final Term value = definiens.getValue();
		if (defKind == null)
			((ComplexTerm) value).ensureKind(kind);
		else if (!kind.toString().equals(defKind.toString())) {
			logger.error("Definiendum/definiens kind mismatch in definition " + name);
			logger.error("Definiendum kind: " + kind);
			logger.error("Definiens kind: " + defKind);
			throw new DataException("Definiendum/definiens kind mismatch in definition", name);
		}
		// definition depth
		if (value.isVariable())
			definitionDepth = 1;
		else
			definitionDepth = ((ComplexTerm) value).definitionDepth() + 1;
	}

	/**
	 * Creates an uninitalized definition.
	 * Used by serialization.
	 */
	public Definition() {
		super();
		varList = null;
		definiens = null;
		definitionDepth = 0;
	}

	/**
	 * Returns the definition depth of this term.
	 * The definition depth is <code>1</code>, if the definiens is just a variable.
	 * Otherwise, the definition depth is one larger than the definition depth of
	 * the leading term of the definiens.
	 *
	 * @return definiton depth of this definition.
	 *
	 * @see AbstractComplexTerm#definitionDepth()
	 */
	public int definitionDepth() {
		return definitionDepth;
	}

	/**
	 * Returns an unfolded version of this Definition with the specified list of TermExpressions.
	 *
	 * @param exprList list of TermExpressions to unfold this definition with.
	 *
	 * @return unfolded version of this Definition.
	 *
	 * @throws IllegalArgumentException if the size of the specified list does not match the number of parameters of this Definition.
	 */
	public TermExpressionImpl unfold(final List<TermExpressionImpl> exprList) {
		assert (exprList != null): "Supplied expression list is null.";
		final int size = exprList.size();
		if (varList.size() != size)
			throw new IllegalArgumentException("Wrong number of parameters supplied: " + exprList.toString());
		final Map<Variable, TermExpressionImpl> varAssignments = new HashMap();
		for (int i = 0; i != size; ++i)
			varAssignments.put(varList.get(i), exprList.get(i));
		return definiens.subst(varAssignments);
	}

	public @Override int placeCount() {
		return varList.size();
	}

	protected @Override void setPlaceCount(final int count) {
		logger.error("Unsupported request for setting place count in definition.");
		throw new UnsupportedOperationException("Setting place count not supported in definitions.");
	}

	public @Override Kind getInputKind(final int i) {
		return varList.get(i).getKind();
	}

	protected @Override void setInputKind(final int i, final Kind kind) {
		logger.error("Unsupported request for setting input kind in definition.");
		throw new UnsupportedOperationException("Setting input kind not supported in definitions.");
	}

	/**
	 * Returns the raw definiens of this definition.
	 *
	 * @return definiens of this definition.
	 */
	TermExpressionImpl getDefiniens() {
		return definiens;
	}

	@Override void store(final DataOutputStream out, final Map<String, Integer> kindNameTable,
			final Map<String, Integer> termNameTable) throws IOException {
		super.store(out, kindNameTable, termNameTable);
		out.writeInt(~varList.size());
		// create variable name map
		final Map<String, Integer> varNameTable = new HashMap();
		int id = -1;
		for (Variable var: varList)
			varNameTable.put(var.toString(), id--);
		final Set<Variable> remainingVars = definiens.variables();
		remainingVars.removeAll(varList);
		for (Variable var: remainingVars)
			varNameTable.put(var.toString(), id--);
		// variable list
		out.writeInt(varNameTable.size());
		for (Variable var: varList)
			out.writeInt(kindNameTable.get(var.getKind()));
		for (Variable var: remainingVars)
			out.writeInt(kindNameTable.get(var.getKind()));
		// definiens
		definiens.store(out, termNameTable, varNameTable);
	}

	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
		try {
			setName((String) in.readObject());
			ensureKind((Kind) in.readObject());
			varList = (List<Variable>) in.readObject();
			definiens = (TermExpressionImpl) in.readObject();
			definitionDepth = in.readInt();
		} catch (ClassCastException e) {
			logger.error("Wrong class during definition deserialization.");
			throw new ClassNotFoundException("Wrong class during definition deserialization.", e);
		} catch (DataException e) {
			final Error err = new AssertionError("Result kind already set during deserialization. "
				+ "This should not happen.");
			err.initCause(e);
			throw err;
		}
	}

	public void writeExternal(final ObjectOutput out) throws IOException {
		out.writeObject(this.toString());
		out.writeObject(getKind());
		out.writeObject(varList);
		out.writeObject(definiens);
		out.writeInt(definitionDepth);
	}

}
