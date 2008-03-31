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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SortedSet;
import jhilbert.data.AbstractName;
import jhilbert.data.DVConstraints;
import jhilbert.data.Symbol;
import jhilbert.data.TermExpression;
import jhilbert.data.Variable;
import org.apache.log4j.Logger;

/**
 * A statement.
 */
public class Statement extends AbstractName implements Symbol {

	/**
	 * Logger.
	 */
	private static final Logger logger = Logger.getLogger(Statement.class);

	/**
	 * Distinct variable constraints.
	 */
	private final DVConstraints dvConstraints;

	/**
	 * Hypotheses.
	 */
	private final List<TermExpression> hypotheses;

	/**
	 * Consequent
	 */
	private final TermExpression consequent;

	/**
	 * Mandatory variables (those occurring in the conclusion but not in the hypotheses).
	 */
	private final List<Variable> mandatoryVariables;

	/**
	 * Create a new statement.
	 * The parameters must not be <code>null</code>.
	 *
	 * @param name name of the statement.
	 * @param rawDV raw distinct variable constraints.
	 * @param hypotheses list of hypotheses.
	 * @param consequent the consequent.
	 */
	public Statement(final String name, final List<SortedSet<Variable>> rawDV, final List<TermExpression> hypotheses, final TermExpression consequent) {
		super(name);
		assert (rawDV != null): "Supplied distinct variable constraints are null.";
		assert (hypotheses != null): "Supplied list of hypotheses is null.";
		assert (consequent != null): "Supplied conclusion is null.";
		dvConstraints = new DVConstraints();
		for (SortedSet<Variable> varSet: rawDV)
			dvConstraints.add(varSet);
		this.hypotheses = hypotheses;
		this.consequent = consequent;
		// compute all variables occurring in hypotheses and consequent for dv restriction
		final LinkedHashSet<Variable> allHypVars = new LinkedHashSet();
		for (TermExpression hypothesis: hypotheses)
			allHypVars.addAll(hypothesis.variables());
		final LinkedHashSet<Variable> allVars = consequent.variables();
		allVars.addAll(allHypVars);
		final int oldDVSize = dvConstraints.size();
		dvConstraints.restrict(allVars);
		if (oldDVSize != dvConstraints.size())
			logger.warn("Disjoint variable restrictions for variables not appearing in hypotheses or consequent removed from statement " + name);
		// mandatory variables
		allVars.removeAll(allHypVars);
		mandatoryVariables = new ArrayList(allVars);
	}

	/**
	 * Returns the distinct variable constraints of this statement.
	 *
	 * @return the distinct variable constraints of this statement.
	 */
	public DVConstraints getDVConstraints() {
		return dvConstraints;
	}

	/**
	 * Returns the hypotheses of this statement.
	 *
	 * @return list of hypotheses of this statement.
	 */
	public List<TermExpression> getHypotheses() {
		return hypotheses;
	}

	/**
	 * Returns the consequent of this statement.
	 *
	 * @return the consequent of this statement.
	 */
	public TermExpression getConsequent() {
		return consequent;
	}

	/**
	 * Returns the mandatory variables of this statement.
	 *
	 * @return list of mandatory variables of this statement.
	 */
	public List<Variable> getMandatoryVariables() {
		return mandatoryVariables;
	}

}
