package jhilbert.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jhilbert.data.AbstractComplexTerm;
import jhilbert.data.DummyVariable;
import jhilbert.data.TermExpression;
import jhilbert.data.Variable;

/**
 * A Definition.
 * That is a {@link Term} defining another Term, in dependence on a list of variables.
 */
public class Definition extends AbstractComplexTerm {

	/**
	 * Variable list.
	 */
	private final List<Variable> varList;

	/**
	 * The definiens.
	 */
	private final TermExpression definiens;

	/**
	 * Definiton depth of this definition.
	 */
	final int definitionDepth;

	/**
	 * Creates a new definition.
	 *
	 * @param name Name of this definition (must not be <code>null</code>).
	 * @param varList List of Variables of this definition (must not be <code>null</code>).
	 * @param definiens The definiens (must not be <code>null</code>).
	 *
	 * @throws IllegalArgumentException if varList contains duplicate entries.
	 */
	public Definition(final String name, final List<Variable> varList, final TermExpression definiens) {
		// FIXME: We want varList to be an insetion-ordered set instead
		super(name, definiens.getKind());
		assert (varList != null): "Supplied variable list is null.";
		if ((new HashSet(varList)).size() != varList.size())
			throw new IllegalArgumentException("Variable list contains duplicate entries: " + varList.toString());
		this.varList = varList;
		// create consistent dummy variable mapping
		final Map<Variable, TermExpression> varMap = new HashMap();
		final Set<Variable> extraVars = definiens.variables();
		extraVars.removeAll(varList);
		for (Variable var: extraVars)
			varMap.put(var, new TermExpression(new DummyVariable(var.getKind())));
		this.definiens = definiens.subst(varMap);
		// calculate definition def, see description of #definitionDepth()
		final Term value = this.definiens.getValue();
		if (value instanceof Variable)
			definitionDepth = 1;
		else
			definitionDepth = ((AbstractComplexTerm) value).definitionDepth() + 1;
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
	 * @throws NullPointerException if exprList is <code>null</code>.
	 * @throws IllegalArgumentException if the size of the specified list does not match the number of parameters of this Definition.
	 */
	public TermExpression unfold(final List<TermExpression> exprList) {
		assert (exprList != null): "Supplied expression list is null.";
		final int size = exprList.size();
		if (varList.size() != size)
			throw new IllegalArgumentException("Wrong number of parameters supplied: " + exprList.toString());
		final Map<Variable, TermExpression> varAssignments = new HashMap();
		for (int i = 0; i != size; ++i)
			varAssignments.put(varList.get(i), exprList.get(i));
		return definiens.subst(varAssignments);
	}

	public @Override int placeCount() {
		return varList.size();
	}

	public @Override String getInputKind(final int i) {
		return varList.get(i).getKind();
	}

}
