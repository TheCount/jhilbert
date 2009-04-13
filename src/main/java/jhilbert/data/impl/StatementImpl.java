/*
    JHilbert, a verifier for collaborative theorem proving
    Copyright © 2008, 2009 Alexander Klauer

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

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

import jhilbert.data.DVConstraints;
import jhilbert.data.Statement;
import jhilbert.data.Variable;

import jhilbert.expressions.Anonymiser;
import jhilbert.expressions.Expression;
import jhilbert.expressions.ExpressionFactory;

import org.apache.log4j.Logger;

/**
 * {@link Statement} implementation.
 */
final class StatementImpl extends SymbolImpl implements Statement, Serializable {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = jhilbert.Main.VERSION;

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(StatementImpl.class);

	/**
	 * DV constraints.
	 */
	private final DVConstraints dvConstraints;

	/**
	 * Hypotheses.
	 */
	private final List<Expression> hypotheses;

	/**
	 * Consequent.
	 */
	private final Expression consequent;

	/**
	 * Mandatory variables.
	 */
	private final List<Variable> mandatoryVariables;

	/**
	 * Default constructor, for serialisation use only!
	 */
	public StatementImpl() {
		super();
		dvConstraints = null;
		hypotheses = null;
		consequent = null;
		mandatoryVariables = null;
	}

	/**
	 * Creates a new <code>StatementImpl</code> with the specified name,
	 * disjoint variable constraints, hypotheses and consequent.
	 *
	 * @param name name of new statement.
	 * @param dv disjoint variable constraints.
	 * @param hypotheses {@link List} of hypotheses.
	 * @param consequent consequent of new statement.
	 */
	StatementImpl(final String name, final DVConstraints dv, final List<Expression> hypotheses, final Expression consequent) {
		this(name, null, -1, dv, hypotheses, consequent);
	}

	/**
	 * Creates a new <code>StatementImpl</code> derived from the specified
	 * original statement with the specified name, disjoint variable
	 * constraints, hypotheses and consequent.
	 *
	 * @param name name of new statement.
	 * @param orig statement this statement is derived from.
	 * @param parameterIndex index of parameter of <code>orig</code>.
	 * @param dv disjoint variable constraints.
	 * @param hypotheses {@link List} of hypotheses.
	 * @param consequent consequent of new statement.
	 */
	StatementImpl(final String name, final StatementImpl orig, final int parameterIndex, final DVConstraints dv, final List<Expression> hypotheses,
			final Expression consequent) {
		super(name, orig, parameterIndex);
		assert (dv != null): "Supplied DV constraints are null";
		assert (hypotheses != null): "Supplied hypotheses are null";
		assert (consequent != null): "Supplied consequent is null";
		// variables appearing in the hypotheses
		final Set<Variable> hypVars = new HashSet();
		for (final Expression hyp: hypotheses)
			hypVars.addAll(hyp.variables());
		// variables appearing in the consequent
		final LinkedHashSet<Variable> consVars = consequent.variables();
		// all variables
		final Set<Variable> allVars = new HashSet(hypVars);
		allVars.addAll(consVars);
		// named mandatory variables
		final LinkedHashSet<Variable> namedMandVars = new LinkedHashSet(consVars);
		namedMandVars.removeAll(hypVars);
		// restrict DV constraints
		dv.restrict(allVars);
		// create an anonymiser for the variables and set fields
		final Anonymiser anonymiser = ExpressionFactory.getInstance().createAnonymiser(allVars);
		dvConstraints = anonymiser.anonymise(dv);
		final List<Expression> unnamedHyps = new ArrayList(hypotheses.size());
		for (final Expression hyp: hypotheses)
			unnamedHyps.add(anonymiser.anonymise(hyp));
		this.hypotheses = Collections.unmodifiableList(unnamedHyps);
		this.consequent = anonymiser.anonymise(consequent);
		final List<Variable> unnamedMandVars = new ArrayList(namedMandVars.size());
		for (final Variable namedMandVar: namedMandVars)
			unnamedMandVars.add(anonymiser.anonymise(namedMandVar));
		this.mandatoryVariables = Collections.unmodifiableList(unnamedMandVars);
	}

	public DVConstraints getDVConstraints() {
		return dvConstraints;
	}

	public List<Expression> getHypotheses() {
		return hypotheses;
	}

	public Expression getConsequent() {
		return consequent;
	}

	public List<Variable> getMandatoryVariables() {
		return mandatoryVariables;
	}

	public final boolean isVariable() {
		return false;
	}

	public @Override String toString() {
		return getNameString() + "((" + dvConstraints + ", " + hypotheses + ") -> " + consequent + ")";
	}

}
