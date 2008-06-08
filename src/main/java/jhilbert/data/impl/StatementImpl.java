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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import jhilbert.data.Data;
import jhilbert.data.DVConstraints;
import jhilbert.data.Statement;
import jhilbert.data.TermExpression;
import jhilbert.data.Variable;
import jhilbert.data.VariablePair;
import jhilbert.data.impl.UnnamedVariable;
import jhilbert.exceptions.DataException;
import jhilbert.exceptions.InputException;
import jhilbert.util.DataInputStream;
import jhilbert.util.DataOutputStream;
import org.apache.log4j.Logger;

/**
 * A statement.
 */
final class StatementImpl extends NameImpl implements Statement {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(StatementImpl.class);

	/**
	 * Disjoint variable constraints.
	 */
	private final DVConstraintsImpl dvConstraints;

	/**
	 * Hypotheses.
	 */
	private final List<TermExpression> hypotheses;

	/**
	 * Consequent.
	 */
	private final TermExpressionImpl consequent;

	/**
	 * Mandatory variables (those occurring in the conclusion but not in the hypotheses).
	 */
	private final List<Variable> mandatoryVariables;

	/**
	 * Creates a new Statement.
	 * The parameters must not be <code>null</code>.
	 *
	 * @param name name of the statement.
	 * @param rawDV raw distinct variable constraints.
	 * @param hypotheses list of hypotheses.
	 * @param consequent the consequent.
	 */
	public StatementImpl(final String name, final List<SortedSet<Variable>> rawDV,
			final List<TermExpression> hypotheses, final TermExpression consequent) {
		super(name);
		assert (rawDV != null): "Supplied distinct variable constraints are null.";
		assert (hypotheses != null): "Supplied list of hypotheses is null.";
		assert (consequent != null): "Supplied conclusion is null.";
		assert (consequent instanceof TermExpressionImpl): "Consequent not from this implementation.";
		// unname variables of raw DV
		final Map<Variable, TermExpression> varMap = new HashMap();
		final List<SortedSet<Variable>> unnamedDV = new ArrayList(rawDV.size());
		for (final SortedSet<Variable> rawDVGroup: rawDV) {
			final SortedSet<Variable> unnamedDVGroup = new TreeSet();
			for (final Variable namedVar: rawDVGroup) {
				if (!varMap.containsKey(namedVar))
					varMap.put(namedVar,
						new TermExpressionImpl(new UnnamedVariable(namedVar.getKind())));
				unnamedDVGroup.add((Variable) varMap.get(namedVar).getValue());
			}
			unnamedDV.add(unnamedDVGroup);
		}
		// obtain hypothesis and consequent variables and enhance varMap for unnaming
		final LinkedHashSet<Variable> allHypVars = new LinkedHashSet();
		for (final TermExpression hypothesis: hypotheses)
			for (final Variable namedVar: hypothesis.variables()) {
				if (!varMap.containsKey(namedVar))
					varMap.put(namedVar,
						new TermExpressionImpl(new UnnamedVariable(namedVar.getKind())));
				allHypVars.add((Variable) varMap.get(namedVar).getValue());
			}
		final LinkedHashSet<Variable> allVars = new LinkedHashSet(allHypVars); // NB: order ltr first appearance
		for (final Variable namedVar: consequent.variables()) {
			if (!varMap.containsKey(namedVar))
				varMap.put(namedVar, new TermExpressionImpl(new UnnamedVariable(namedVar.getKind())));
			allVars.add((Variable) varMap.get(namedVar).getValue());
		}
		// calculate unnamed restricted DV contraints
		this.dvConstraints = new DVConstraintsImpl();
		for (final SortedSet<Variable> varSet: unnamedDV)
			dvConstraints.add(varSet);
		final int oldDVSize = dvConstraints.size();
		dvConstraints.restrict(allVars);
		if (oldDVSize != dvConstraints.size()) {
			logger.warn("Disjoint variable restrictions for variables not appearing in hypotheses or "
				+ "consequent removed from statement " + name);
			logger.debug("DV constraints now: " + dvConstraints);
		}
		// calculate hypotheses and consequent
		this.hypotheses = new ArrayList(hypotheses.size());
		for (final TermExpression hypothesis: hypotheses)
			this.hypotheses.add(hypothesis.subst(varMap));
		this.consequent = (TermExpressionImpl) consequent.subst(varMap);
		// calculate mandatory variables
		allVars.removeAll(allHypVars);
		this.mandatoryVariables = new ArrayList(allVars); // NB: order is important here, make sure not to break anything
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
	// FIXME
	StatementImpl(final String name, final DataInputStream in, final DataImpl data, final List<String> nameList,
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
			varList.add(new UnnamedVariable(data.getKind(nameList.get(in.readInt(kindsLower, kindsUpper)))));
		// load DV constraints
		final int numDVC = in.readNonNegativeInt();
		dvConstraints = new DVConstraintsImpl();
		for (int i = 0; i != numDVC; ++i) {
			final int first = in.readInt(~numVars, 0);
			final int second = in.readInt(~numVars, 0);
			dvConstraints.add(new VariablePairImpl(varList.get(~first), varList.get(~second)));
		}
		// load hypotheses
		final LinkedHashSet<Variable> allHypVars = new LinkedHashSet();
		final int numHyp = in.readNonNegativeInt();
		hypotheses = new ArrayList(numHyp);
		for (int i = 0; i != numHyp; ++i) {
			hypotheses.add(TermExpressionImpl.create(in, data, nameList, varList, termsLower, termsUpper));
			allHypVars.addAll(hypotheses.get(i).variables());
		}
		// load consequent
		consequent = TermExpressionImpl.create(in, data, nameList, varList, termsLower, termsUpper);
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
	// FIXME
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
			varNameTable.put(var.toString(), id--);
			out.writeInt(kindNameTable.get(var.getKind().toString()));
		}
		// store DV constraints
		out.writeInt(dvConstraints.size());
		for (VariablePair p: dvConstraints) {
			out.writeInt(varNameTable.get(p.getFirst().toString()));
			out.writeInt(varNameTable.get(p.getSecond().toString()));
		}
		// store hypotheses
		out.writeInt(hypotheses.size());
		for (TermExpression hypothesis: hypotheses) {
			assert (hypothesis instanceof TermExpressionImpl): "hypothesis not from this implementation.";
			((TermExpressionImpl) hypothesis).store(out, termNameTable, varNameTable);
		}
		// store consequent
		assert (consequent instanceof TermExpressionImpl): "consequent not from this implementation.";
		((TermExpressionImpl) consequent).store(out, termNameTable, varNameTable);
	}

	public DVConstraintsImpl getDVConstraints() {
		return dvConstraints;
	}

	public List<TermExpression> getHypotheses() {
		return hypotheses;
	}

	public TermExpressionImpl getConsequent() {
		return consequent;
	}

	public List<Variable> getMandatoryVariables() {
		return mandatoryVariables;
	}

	public boolean isVariable() {
		return false;
	}

}
