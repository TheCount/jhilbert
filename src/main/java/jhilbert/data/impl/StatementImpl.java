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
import org.apache.log4j.Logger;

/**
 * A statement.
 */
final class StatementImpl extends NameImpl implements Statement {

	/**
	 * Serialization ID.
	 */
	private static final long serialVersionUID = jhilbert.Main.VERSION;

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(StatementImpl.class);

	/**
	 * Disjoint variable constraints.
	 */
	private DVConstraintsImpl dvConstraints;

	/**
	 * Hypotheses.
	 */
	private List<TermExpression> hypotheses;

	/**
	 * Consequent.
	 */
	private TermExpressionImpl consequent;

	/**
	 * Mandatory variables (those occurring in the conclusion but not in the hypotheses).
	 */
	private List<Variable> mandatoryVariables;

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
		final Map<Variable, TermExpressionImpl> varMap = new HashMap();
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
		for (final TermExpression hypothesis: hypotheses)  {
			assert (hypothesis instanceof TermExpressionImpl): "Hypothesis not from this implementation.";
			this.hypotheses.add(((TermExpressionImpl) hypothesis).substImpl(varMap));
		}
		this.consequent = ((TermExpressionImpl) consequent).substImpl(varMap);
		// calculate mandatory variables
		allVars.removeAll(allHypVars);
		this.mandatoryVariables = new ArrayList(allVars); // NB: order is important here, make sure not to break anything
	}

	/**
	 * Creates an uninitialized Statement.
	 * Used by serialization.
	 */
	public StatementImpl() {
		super();
		dvConstraints = null;
		hypotheses = null;
		consequent = null;
		mandatoryVariables = null;
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
