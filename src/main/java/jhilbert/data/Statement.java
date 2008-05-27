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

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import jhilbert.data.AbstractStatement;
import jhilbert.data.TemporaryStatement;
import jhilbert.data.TermExpression;
import jhilbert.data.UnnamedVariable;
import jhilbert.exceptions.DataException;
import jhilbert.exceptions.InputException;
import jhilbert.util.DataInputStream;
import jhilbert.util.DataOutputStream;

/**
 * A statement.
 * <p>
 * The difference to {@link TemporaryStatement} is that variables
 * are replaced by their unnamed counterparts immediately.
 */
public final class Statement extends AbstractStatement {

	/**
	 * Creates a new Statement.
	 * The parameters must not be <code>null</code>.
	 *
	 * @param name name of the statement.
	 * @param rawDV raw distinct variable constraints.
	 * @param hypotheses list of hypotheses.
	 * @param consequent the consequent.
	 */
	public Statement(final String name, final List<SortedSet<Variable>> rawDV, final List<TermExpression> hypotheses,
			final TermExpression consequent) {
		super(new TemporaryStatement(name, rawDV, hypotheses, consequent));
	}

	/**
	 * Upgrades the specified TemporaryStatement to a statement.
	 *
	 * @param statement temporary statement.
	 */
	public Statement(final TemporaryStatement statement) {
		super(statement);
		// unname all variables
		final Set<Variable> allVars = consequent.variables();
		for (TermExpression hypothesis: hypotheses)
			allVars.addAll(hypothesis.variables());
		final Map<Variable, TermExpression> varMap = new HashMap();
		for (Variable var: allVars)
			varMap.put(var, new TermExpression(new UnnamedVariable(var.getKind())));
		// replace all named variables with unnamed ones
		final DVConstraints newDV = new DVConstraints();
		for (VariablePair p: dvConstraints)
			newDV.add(new VariablePair((Variable) varMap.get(p.getFirst()).getValue(),
						(Variable) varMap.get(p.getSecond()).getValue()));
		dvConstraints = newDV;
		final List<TermExpression> newHyp = new ArrayList(hypotheses.size());
		for (TermExpression hypothesis: hypotheses)
			newHyp.add(hypothesis.subst(varMap));
		consequent = consequent.subst(varMap);
		final List<Variable> newMand = new ArrayList(mandatoryVariables.size());
		for (Variable mandVar: mandatoryVariables)
			newMand.add((Variable) varMap.get(mandVar).getValue());
		mandatoryVariables = newMand;
	}

	/**
	 * Loads a new statement from the specified input stream.
	 *
	 * @param name statement name.
	 * @param in input stream.
	 * @param data interface data.
	 * @param nameList list of names.
	 * @param kindsLower lower bound for kinds.
	 * @param kindsUpper upper bound for kinds.
	 * @param termsLower lower bound for terms.
	 * @param termsUpper upper bound for terms.
	 *
	 * @throws EOFException upon unexpected end of input.
	 * @throws IOException if an I/O-Error occurs.
	 * @throws DataException if the input stream is inconsistent.
	 */
	Statement(final String name, final DataInputStream in, final Data data, final List<String> nameList,
		final int kindsLower, final int kindsUpper, final int termsLower, final int termsUpper)
	throws EOFException, IOException, DataException {
		super(name);
		assert (in != null): "Supplied data input stream is null.";
		assert (data != null): "Supplied data are null.";
		assert (nameList != null): "Supplied name list is null.";
		assert (kindsLower > 0): "Supplied lower kinds bound is not positive.";
		assert (kindsUpper >= kindsLower): "Supplied upper kinds bound is smaller than lower bound.";
		assert (termsLower > 0): "Supplied lower terms bound is not positive.";
		assert (termsUpper >= termsLower): "Supplied upper terms bound is smaller than lower bound.";
		// load variables
		final int numVars = in.readNonNegativeInt();
		final List<Variable> varList = new ArrayList(numVars);
		for (int i = 0; i != numVars; ++i)
			varList.add(new UnnamedVariable(nameList.get(in.readInt(kindsLower, kindsUpper))));
		// load DV constraints
		final int numDVC = in.readNonNegativeInt();
		dvConstraints = new DVConstraints();
		for (int i = 0; i != numDVC; ++i) {
			final int first = in.readInt(~numVars, 0);
			final int second = in.readInt(~numVars, 0);
			dvConstraints.add(new VariablePair(varList.get(~first), varList.get(~second)));
		}
		// load hypotheses
		final LinkedHashSet<Variable> allHypVars = new LinkedHashSet();
		final int numHyp = in.readNonNegativeInt();
		hypotheses = new ArrayList(numHyp);
		for (int i = 0; i != numHyp; ++i) {
			hypotheses.add(TermExpression.create(in, data, nameList, varList, termsLower, termsUpper));
			allHypVars.addAll(hypotheses.get(i).variables());
		}
		// load consequent
		consequent = TermExpression.create(in, data, nameList, varList, termsLower, termsUpper);
		// mandatory variables
		final LinkedHashSet<Variable> mandVars = consequent.variables();
		mandVars.removeAll(allHypVars);
		mandatoryVariables = new ArrayList(mandVars);
	}

	/**
	 * Stores this statement in the specified output stream.
	 *
	 * @param out data output stream.
	 * @param kindNameTable name to ID map for kinds.
	 * @param termNameTable name to ID map for terms.
	 *
	 * @throws IOException if an I/O-Error occurs.
	 */
	void store(final DataOutputStream out, final Map<String, Integer> kindNameTable, final Map<String, Integer> termNameTable) throws IOException {
		// get all variables
		final Set<Variable> varSet = new HashSet(consequent.variables());
		for (TermExpression hypothesis: hypotheses)
			varSet.addAll(hypothesis.variables());
		// create variable to ID mapping and store kinds
		out.writeInt(varSet.size());
		int id = -1;
		final Map<String, Integer> varNameTable = new HashMap();
		for (Variable var: varSet) {
			varNameTable.put(var.getName(), id--);
			out.writeInt(kindNameTable.get(var.getKind()));
		}
		// store DV constraints
		out.writeInt(dvConstraints.size());
		for (VariablePair p: dvConstraints) {
			out.writeInt(varNameTable.get(p.getFirst()));
			out.writeInt(varNameTable.get(p.getSecond()));
		}
		// store hypotheses
		out.writeInt(hypotheses.size());
		for (TermExpression hypothesis: hypotheses)
			hypothesis.store(out, termNameTable, varNameTable);
		// store consequent
		consequent.store(out, termNameTable, varNameTable);
	}

	/**
	 * Checks whether this statement and the specified one can be used in the same way.
	 * To this end, this method checks whether the hypotheses, the consequent, and the
	 * disjoint variable constraints are compatible.
	 *
	 * @param statement statement this statement should be checked against (must not be <code>null</code>).
	 *
	 * @return <code>true</code> if this statement and the specified one are compatible, <code>false</code> otherwise.
	 *
	 * FIXME: not ready yet
	 */
	public boolean equalsSuperficially(final Statement statement) {
		assert (statement != null): "Supplied statement is null.";
		// FIXME
	}

}
