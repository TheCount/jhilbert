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
