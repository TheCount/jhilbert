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

package jhilbert.verifier.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import jhilbert.data.DataException;
import jhilbert.data.DataFactory;
import jhilbert.data.DVConstraints;
import jhilbert.data.Kind;
import jhilbert.data.Module;
import jhilbert.data.Namespace;
import jhilbert.data.Statement;
import jhilbert.data.Symbol;
import jhilbert.data.Variable;

import jhilbert.expressions.Expression;
import jhilbert.expressions.ExpressionFactory;
import jhilbert.expressions.Substituter;
import jhilbert.expressions.UnifyException;

import jhilbert.verifier.Verifier;
import jhilbert.verifier.VerifyException;

import org.apache.log4j.Logger;

/**
 * {@link Verifier} implementation.
 */
final class VerifierImpl implements Verifier {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(VerifierImpl.class);

	/**
	 * Proof.
	 */
	private final List<Object> proof;

	// local proof environment follows:

	/**
	 * Proof stack.
	 */
	private Stack<Expression> proofStack;

	/**
	 * Mandatory stack.
	 */
	private Stack<Expression> mandatoryStack;

	/**
	 * Data factory.
	 */
	private DataFactory dataFactory;

	/**
	 * Expression factory.
	 */
	private ExpressionFactory expressionFactory;

	/**
	 * Kind namespace.
	 */
	private Namespace<? extends Kind> kindNamespace;

	/**
	 * Symbol namespace.
	 */
	private Namespace<? extends Symbol> symbolNamespace;

	/**
	 * DV constraints which must be checked at the end of the proof.
	 */
	private DVConstraints requiredDVConstraints;

	/**
	 * Hypotheses.
	 */
	private Map<String, Expression> hypotheses;

	// constructors & methods:

	/**
	 * Creates a new <code>VerifierImpl</code> for the specified proof.
	 *
	 * @param proof the proof, which is a {@link List} of {@link Object}s
	 * 	each of which must be convertible to either {@link String} or
	 * 	{@link Expression}.
	 */
	VerifierImpl(final List<Object> proof) {
		assert (proof != null): "Supplied proof is null";
		this.proof = proof;
	}

	public void verify(final Module module, final DVConstraints dvConstraints, final Map<String, Expression> hypotheses, final Expression consequent)
	throws VerifyException {
		assert (module != null): "Supplied module is null";
		assert (dvConstraints != null): "Supplied DV constraints are null";
		assert (hypotheses != null): "Supplied hypotheses are null";
		assert (consequent != null): "Supplied consequent is null";
		proofStack = new Stack();
		mandatoryStack = new Stack();
		dataFactory = DataFactory.getInstance();
		expressionFactory = ExpressionFactory.getInstance();
		kindNamespace = module.getKindNamespace();
		assert (kindNamespace != null): "Supplied kind namespace is null";
		symbolNamespace = module.getSymbolNamespace();
		assert (symbolNamespace != null): "Module provided null symbol namespace";
		requiredDVConstraints = dataFactory.createDVConstraints();
		this.hypotheses = hypotheses;
		// check proof steps
		for (final Object proofStep: proof) {
			checkProofStep(proofStep);
			if (logger.isTraceEnabled()) {
				logger.trace("Proof stack:     " + proofStack);
				logger.trace("Mandatory stack: " + mandatoryStack);
			}
		}
		// check stacks
		if (proofStack.empty()) {
			logger.error("Proof stack empty at end of proof; expected precisely one element");
			throw new VerifyException("Proof stack empty at end of proof");
		}
		final Expression proofResult = proofStack.pop();
		if (!(proofStack.empty() && mandatoryStack.empty())) {
			logger.error("Proof stacks not empty after popping final result");
			logger.debug("Mandatory stack: " + mandatoryStack);
			logger.debug("Proof stack: " + proofStack);
			throw new VerifyException("Proof stacks not empty after popping final result");
		}
		// have we proven what we promised to prove?
		final Set<Variable> hypVars = new HashSet();
		for (final Expression hypothesis: hypotheses.values())
			hypVars.addAll(hypothesis.variables());
		final Set<Variable> blacklist = new HashSet(hypVars);
		blacklist.addAll(consequent.variables());
		try {
			if (!expressionFactory.createMatcher().checkVEquality(consequent, proofResult, blacklist)) {
				logger.error("Consequent of theorem does not match proof result");
				logger.debug("Consequent:   " + consequent);
				logger.debug("Proof result: " + proofResult);
				throw new VerifyException("Consequent does not match proof result");
			}
		} catch (UnifyException e) {
			logger.error("Attempt to prove result by illegal dummy assignment", e);
			logger.debug("Source expression: " + e.getSource());
			logger.debug("Target expression: " + e.getTarget());
			throw new VerifyException("Attempt to prove result by illegal dummy assignment", e);
		}
		// do we fulfill all the required DV constraints?
		dvConstraints.restrict(blacklist);
		if (logger.isTraceEnabled()) {
			logger.trace("Final DV check:");
			logger.trace("Required constraints: " + requiredDVConstraints);
			logger.trace("Actual constraints:   " + dvConstraints);
		}
		final Set<Variable> resultVars = hypVars;
		resultVars.addAll(proofResult.variables());
		requiredDVConstraints.restrict(resultVars); // dummies bye bye (are always considered distinct)
		if (!dvConstraints.contains(requiredDVConstraints)) {
			logger.error("Required distinct variable constraints are not a subset of actual distinct variable constraints");
			logger.debug("Required constraints: " + requiredDVConstraints);
			logger.debug("Actual constraints:   " + dvConstraints);
			throw new VerifyException("Required distinct variable constraints are not a subset of actual distinct variable constraints");
		}
	}

	private void checkProofStep(final Object proofStep) throws VerifyException {
		// expression?
		if (proofStep instanceof Expression) {
			mandatoryStack.push((Expression) proofStep);
			if (logger.isDebugEnabled())
				logger.debug("Proof object: " + mandatoryStack.peek());
			return;
		}
		assert (proofStep instanceof String): "Wrong proof step type";
		final String label = (String) proofStep;
		// hypothesis?
		if (hypotheses.containsKey(label)) {
			if (!mandatoryStack.empty()) {
				logger.error("Proof step is a hypothesis but mandatory variable stack is not empty.");
				logger.error("(Remember to place mandatory terms after the hypotheses!)");
				logger.error("Proof step:      " + label);
				logger.debug("Proof stack:     " + proofStack);
				logger.debug("Mandatory stack: " + mandatoryStack);
				throw new VerifyException("Proof step is a hypothesis but mandatory variable stack is not empty.");
			}
			proofStack.push(hypotheses.get(label));
			if (logger.isDebugEnabled())
				logger.debug("Proof object: " + proofStack.peek());
			return;
		}
		// label must be symbol now
		final Symbol symbol = symbolNamespace.getObjectByString(label);
		if (logger.isDebugEnabled())
			logger.debug("Proof object: " + symbol);
		if (symbol == null) {
			logger.error("Proof step is neither a symbol nor a hypothesis: " + label);
			throw new VerifyException("Proof step is neither a symbol nor a hypothesis");
		}
		// variable?
		if (symbol.isVariable()) {
			mandatoryStack.push(expressionFactory.createExpression((Variable) symbol));
			return;
		}
		// Aha, Statement!
		checkStatement((Statement) symbol);
	}

	private void checkStatement(final Statement statement) throws VerifyException {
		final Map<Variable, Expression> varAssignments = new HashMap();
		if (logger.isTraceEnabled())
			logger.trace("Inference: " + statement.getHypotheses() + " -> " + statement.getConsequent());
		assignMandatoryVariables(statement.getMandatoryVariables(), varAssignments);
		final Substituter substituter = expressionFactory.createSubstituter(varAssignments);
		assignHypotheses(statement.getHypotheses(), substituter);
		updateRequiredConstraints(statement.getDVConstraints(), substituter.getAssignments());
		proofStack.push(substituter.substitute(statement.getConsequent()));
	}

	private void assignMandatoryVariables(final List<Variable> mandatoryVars, final Map<Variable, Expression> varAssignments) throws VerifyException {
		final int size = mandatoryStack.size();
		if (mandatoryVars.size() != size) {
			logger.error("Statement has wrong number of mandatory variables in proof.");
			logger.debug("Required number of variables: " + mandatoryVars.size());
			logger.debug("Terms present:                " + size);
			throw new VerifyException("Statement has wrong number of mandatory variables");
		}
		for (int i = 0; i != size; ++i) {
			final Variable var = mandatoryVars.get(i);
			final Expression expr = mandatoryStack.get(i);
			final Kind varKind = var.getKind();
			final Kind exprKind = expr.getKind();
			if (!varKind.equals(exprKind)) {
				logger.error("Kind mismatch");
				logger.debug("Affected expression: " + expr);
				logger.debug("Kind of expression:  " + exprKind);
				logger.debug("Required kind:       " + varKind);
				throw new VerifyException("Kind mismatch");
			}
			varAssignments.put(var, expr);
		}
		mandatoryStack.clear();
	}

	private void assignHypotheses(final List<Expression> hypotheses, final Substituter substituter) throws VerifyException {
		final int size = hypotheses.size();
		final int start = proofStack.size() - size;
		if (start < 0) {
			logger.error("Too few hypotheses on stack");
			logger.debug("Hypotheses missing: " + -start);
			throw new VerifyException("Too few hypotheses on stack");
		}
		try {
			for (int i = 0; i != size; ++i)
				substituter.unify(hypotheses.get(i), proofStack.get(start + i));
			proofStack.setSize(start);
		} catch (UnifyException e) {
			logger.error("Unification error while popping hypotheses from proof stack", e);
			logger.debug("Source expression: " + e.getSource());
			logger.debug("Target expression: " + e.getSource());
			throw new VerifyException("Unification error while popping hypotheses from proof stack", e);
		}
	}

	private void updateRequiredConstraints(final DVConstraints dvConstraints, final Map<Variable, Expression> varAssignments) throws VerifyException {
		for (Variable[] constraint: dvConstraints) {
			assert (constraint.length == 2): "Invalid constraint length";
			assert (varAssignments.containsKey(constraint[0]) && varAssignments.containsKey(constraint[1]))
				: "Unrestricted contraints in statement";
			final Set<Variable> varSet1 = varAssignments.get(constraint[0]).variables();
			final Set<Variable> varSet2 = varAssignments.get(constraint[1]).variables();
			try {
				requiredDVConstraints.addProduct(varSet1, varSet2);
			} catch (DataException e) {
				logger.error("Distinct variable constraint violation", e);
				logger.debug("First variable set:  " + varSet1);
				logger.debug("Second variable set: " + varSet2);
				logger.debug("Current required DV: " + requiredDVConstraints);
				throw new VerifyException("Distinct variable constraint violation", e);
			}
		}
	}

}
