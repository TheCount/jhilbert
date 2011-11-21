/*
    JHilbert, a verifier for collaborative theorem proving

    Copyright © 2008, 2009, 2011 The JHilbert Authors
      See the AUTHORS file for the list of JHilbert authors.
      See the commit logs ("git log") for a list of individual contributions.

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

    You may contact the author on this Wiki page:
    http://www.wikiproofs.de/w/index.php?title=User_talk:GrafZahl
*/

package jhilbert.verifier.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import jhilbert.data.ConstraintException;
import jhilbert.data.DVConstraints;
import jhilbert.data.DataFactory;
import jhilbert.data.Kind;
import jhilbert.data.Module;
import jhilbert.data.Namespace;
import jhilbert.data.Statement;
import jhilbert.data.Symbol;
import jhilbert.data.Variable;
import jhilbert.expressions.Expression;
import jhilbert.expressions.ExpressionException;
import jhilbert.expressions.ExpressionFactory;
import jhilbert.expressions.Matcher;
import jhilbert.expressions.Substituter;
import jhilbert.expressions.UnifyException;
import jhilbert.scanners.ScannerException;
import jhilbert.scanners.Token;
import jhilbert.scanners.TokenFeed;
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
	 * Data module.
	 */
	private final Module module;

	/**
	 * Token feed.
	 */
	private final TokenFeed feed;

	// local proof environment follows:

	/**
	 * Proof stack.
	 */
	private final Stack<Expression> proofStack;

	/**
	 * Mandatory stack.
	 */
	private final Stack<Expression> mandatoryStack;

	/**
	 * Data factory.
	 */
	private final DataFactory dataFactory;

	/**
	 * Expression factory.
	 */
	private final ExpressionFactory expressionFactory;

	/**
	 * Kind namespace.
	 */
	private final Namespace<? extends Kind> kindNamespace;

	/**
	 * Symbol namespace.
	 */
	private final Namespace<? extends Symbol> symbolNamespace;

	/**
	 * DV constraints which must be checked at the end of the proof.
	 */
	private final DVConstraints requiredDVConstraints;

	/**
	 * Hypotheses.
	 */
	private Map<String, Expression> hypotheses;

	// constructors & methods:

	/**
	 * Creates a new <code>VerifierImpl</code> for the specified data
	 * module and token feed.
	 *
	 * @param module data module;
	 * @param tokenFeed token feed;
	 */
	VerifierImpl(final Module module, final TokenFeed tokenFeed) {
		assert (module != null): "Supplied data module is null";
		assert (tokenFeed != null): "Supplied token feed is null";
		this.module = module;
		feed = tokenFeed;
		proofStack = new Stack();
		mandatoryStack = new Stack();
		dataFactory = DataFactory.getInstance();
		expressionFactory = ExpressionFactory.getInstance();
		kindNamespace = module.getKindNamespace();
		assert (kindNamespace != null): "Module supplied null kind namespace";
		symbolNamespace = module.getSymbolNamespace();
		assert (symbolNamespace != null): "Module supplied null symbol namespace";
		requiredDVConstraints = dataFactory.createDVConstraints();
	}

	public void verify(final DVConstraints dvConstraints, final Map<String, Expression> hypotheses,
			final Expression consequent)
	throws VerifyException {
		assert (dvConstraints != null): "Supplied DV constraints are null";
		assert (hypotheses != null): "Supplied hypotheses are null";
		assert (consequent != null): "Supplied consequent is null";
		this.hypotheses = hypotheses;
		try {
			feed.beginExp();
			feed.confirmBeginExp();
			// check proof steps
			Token token = feed.getToken();
			while (token.getTokenClass() != Token.Class.END_EXP) {
				checkProofStep(token);
				if (logger.isTraceEnabled()) {
					logger.trace("Proof stack:     " + proofStack);
					logger.trace("Mandatory stack: " + mandatoryStack);
				}
				token = feed.getToken();
			}
			// check stacks
			if (proofStack.empty()) {
				feed.reject("Proof stack empty at end of proof; expected precisely one element");
				throw new VerifyException("Proof stack empty at end of proof");
			}
			final Expression proofResult = proofStack.pop();
			if (!(proofStack.empty() && mandatoryStack.empty())) {
				feed.reject("Proof stacks not empty after popping final result");
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
			for (final Iterator<Variable> i = blacklist.iterator(); i.hasNext();) {
				if (i.next().isDummy())
					i.remove();
			}
			final Matcher matcher = expressionFactory.createMatcher();
			if (!matcher.checkVEquality(consequent, proofResult, blacklist)) {
				logger.debug("Consequent:   " + consequent);
				logger.debug("Proof result: " + proofResult);
				feed.reject("Consequent of theorem does not match proof result");
				throw new VerifyException("Consequent does not match proof result");
			}
			// do we fulfill all the required DV constraints?
			final DVConstraints actualDVConstraints = dataFactory.createDVConstraints();
			final Map<Variable, Variable> assignmentMap = matcher.getAssignmentMap();
			for (final Variable[] dv: dvConstraints) {
				Variable var0 = assignmentMap.get(dv[0]);
				Variable var1 = assignmentMap.get(dv[1]);
				if (var0 == null)
					var0 = dv[0];
				if (var1 == null)
					var1 = dv[1];
				actualDVConstraints.add(var0, var1);
			}
			for (final Expression hypothesis: hypotheses.values()) {
				for (final Variable[] dv: hypothesis.dvConstraints()) {
					Variable var0 = assignmentMap.get(dv[0]);
					Variable var1 = assignmentMap.get(dv[1]);
					if (var0 == null)
						var0 = dv[0];
					if (var1 == null)
						var1 = dv[1];
					 actualDVConstraints.add(var0, var1);
				}
			}
			for (final Variable[] dv: consequent.dvConstraints()) {
				Variable var0 = assignmentMap.get(dv[0]);
				Variable var1 = assignmentMap.get(dv[1]);
				if (var0 == null)
					var0 = dv[0];
				if (var1 == null)
					var1 = dv[1];
				actualDVConstraints.add(var0, var1);
			}
			if (logger.isTraceEnabled()) {
				logger.trace("Final DV check:");
				logger.trace("Required constraints: " + requiredDVConstraints);
				logger.trace("Actual constraints:   " + actualDVConstraints);
			}
			if (!actualDVConstraints.contains(requiredDVConstraints)) {
				feed.reject("Required distinct variable constraints are not a subset of actual distinct "
						+ "variable constraints");
				logger.debug("Required constraints: " + requiredDVConstraints);
				logger.debug("Actual constraints:   " + actualDVConstraints);
				throw new VerifyException("Required distinct variable constraints are not a subset of "
						+ "actual distinct variable constraints");
			}
			feed.confirmEndExp();
		} catch (NullPointerException e) {
			logger.error("Unexpected end of input while scanning proof");
			throw new VerifyException("Unexpected end of input", e);
		} catch (ScannerException e) {
			throw new VerifyException("Feed error", e);
		} catch (UnifyException e) {
			try {
				feed.reject("Attempt to prove result by illegal dummy assignment");
			} catch (ScannerException ignored) {
				logger.error("Attempt to prove result by illegal dummy assignment");
			}
			logger.debug("Source expression: " + e.getSource());
			logger.debug("Target expression: " + e.getTarget());
			throw new VerifyException("Attempt to prove result by illegal dummy assignment", e);
		} catch (ExpressionException e) {
			throw new VerifyException("Unable to scan expression", e);
		} catch (ConstraintException e) {
			throw new VerifyException("Illegal DV constraints on statement", e);
		}
	}

	private void checkProofStep(final Token token) throws ExpressionException, ScannerException, VerifyException {
		// expression?
		if (token.getTokenClass() == Token.Class.BEGIN_EXP) {
			feed.putToken(token);
			mandatoryStack.push(expressionFactory.createExpression(module, feed));
			if (logger.isDebugEnabled())
				logger.debug("Proof object: " + mandatoryStack.peek());
			return;
		}
		assert (token.getTokenClass() == Token.Class.ATOM): "Wrong token class";
		final String label = token.getTokenString();
		// hypothesis?
		if (hypotheses.containsKey(label)) {
			if (!mandatoryStack.empty()) {
				feed.reject("Proof step " + label + " is a hypothesis but mandatory variable stack is not "
						+ "empty. (Remember to place mandatory terms after the hypotheses!)");
				logger.debug("Proof stack:     " + proofStack);
				logger.debug("Mandatory stack: " + mandatoryStack);
				throw new VerifyException("Proof step is a hypothesis but mandatory variable stack is not "
						+ "empty.");
			}
			proofStack.push(hypotheses.get(label).totalUnfold());
			if (logger.isDebugEnabled())
				logger.debug("Proof object: " + proofStack.peek());
			feed.confirmLabel();
			return;
		}
		// label must be symbol now
		final Symbol symbol = symbolNamespace.getObjectByString(label);
		if (logger.isDebugEnabled())
			logger.debug("Proof object: " + symbol);
		if (symbol == null) {
			feed.reject("Proof step is neither a symbol nor a hypothesis: " + label);
			throw new VerifyException("Proof step is neither a symbol nor a hypothesis");
		}
		// variable?
		if (symbol.isVariable()) {
			mandatoryStack.push(expressionFactory.createExpression((Variable) symbol));
			feed.confirmVar();
			return;
		}
		// Aha, Statement!
		checkStatement((Statement) symbol);
		feed.confirmStatement();
	}

	private void checkStatement(final Statement statement) throws VerifyException {
		final Map<Variable, Expression> varAssignments = new HashMap();
		if (logger.isTraceEnabled())
			logger.trace("Inference: " + statement.getHypotheses() + " -> " + statement.getConsequent());
		assignMandatoryVariables(statement.getMandatoryVariables(), varAssignments);
		final Substituter substituter = expressionFactory.createSubstituter(varAssignments);
		assignHypotheses(statement.getHypotheses(), substituter);
		// FIXME
		if (logger.isDebugEnabled()) {
			logger.debug("Statement DV constraints: " + statement.getDVConstraints());
			logger.debug("Var assignments: " + substituter.getAssignments());
		}
		// End FIXME
		updateRequiredConstraints(statement.getDVConstraints(), substituter.getAssignments());
		proofStack.push(substituter.substitute(statement.getConsequent()).totalUnfold());
	}

	private void assignMandatoryVariables(final List<Variable> mandatoryVars,
			final Map<Variable, Expression> varAssignments)
	throws VerifyException {
		final int size = mandatoryStack.size();
		if (mandatoryVars.size() != size) {
			try {
				feed.reject("Statement has wrong number of mandatory variables in proof");
			} catch (ScannerException ignored) {
				logger.error("Statement has wrong number of mandatory variables in proof");
			}
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
				try {
					feed.reject("Kind mismatch");
				} catch (ScannerException ignored) {
					logger.error("Kind mismatch");
				}
				logger.debug("Affected expression: " + expr);
				logger.debug("Kind of expression:  " + exprKind);
				logger.debug("Required kind:       " + varKind);
				throw new VerifyException("Kind mismatch");
			}
			varAssignments.put(var, expr);
		}
		mandatoryStack.clear();
	}

	private void assignHypotheses(final List<Expression> hypotheses, final Substituter substituter)
	throws VerifyException {
		final int size = hypotheses.size();
		final int start = proofStack.size() - size;
		if (start < 0) {
			try {
				feed.reject("Too few hypotheses on stack");
			} catch (ScannerException ignored) {
				logger.error("Too few hypotheses on stack");
			}
			logger.debug("Hypotheses missing: " + -start);
			throw new VerifyException("Too few hypotheses on stack");
		}
		try {
			for (int i = 0; i != size; ++i)
				substituter.unify(hypotheses.get(i).totalUnfold(), proofStack.get(start + i));
			proofStack.setSize(start);
		} catch (UnifyException e) {
			try {
				feed.reject("Unification error while popping hypotheses from proof stack: " + e.getMessage());
			} catch (ScannerException ignored) {
				logger.error("Unification error while popping hypotheses from proof stack: " + e.getMessage());
			}
			logger.debug("Source expression: " + e.getSource());
			logger.debug("Target expression: " + e.getSource());
			throw new VerifyException("Unification error while popping hypotheses from proof stack", e);
		}
	}

	private void updateRequiredConstraints(final DVConstraints dvConstraints,
			final Map<Variable, Expression> varAssignments)
	throws VerifyException {
		for (Variable[] constraint: dvConstraints) {
			assert (constraint.length == 2): "Invalid constraint length";
			final Expression exp1 = varAssignments.get(constraint[0]);
			final Expression exp2 = varAssignments.get(constraint[1]);
			final Set<Variable> varSet1 = (exp1 == null)? Collections.singleton(constraint[0])
				: exp1.variables();
			final Set<Variable> varSet2 = (exp2 == null)? Collections.singleton(constraint[1])
				: exp2.variables();
			try {
				requiredDVConstraints.addProduct(varSet1, varSet2);
			} catch (ConstraintException e) {
				try {
					feed.reject("Distinct variable constraint violation: " + e.getMessage());
				} catch (ScannerException ignored) {
					logger.error("Distinct variable constraint violation: " + e.getMessage());
				}
				logger.debug("First variable set:  " + varSet1);
				logger.debug("Second variable set: " + varSet2);
				logger.debug("Current required DV: " + requiredDVConstraints);
				throw new VerifyException("Distinct variable constraint violation", e);
			}
		}
	}

}
