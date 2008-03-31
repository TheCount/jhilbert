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

package jhilbert.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import jhilbert.commands.AbstractStatementCommand;
import jhilbert.data.DVConstraints;
import jhilbert.data.ModuleData;
import jhilbert.data.Statement;
import jhilbert.data.Symbol;
import jhilbert.data.TermExpression;
import jhilbert.data.Token;
import jhilbert.data.Variable;
import jhilbert.data.VariablePair;
import jhilbert.exceptions.DataException;
import jhilbert.exceptions.ScannerException;
import jhilbert.exceptions.SyntaxException;
import jhilbert.exceptions.UnifyException;
import jhilbert.exceptions.VerifyException;
import jhilbert.util.TokenScanner;
import org.apache.log4j.Logger;

/**
 * Command introducing a new {@link jhilbert.data.Theorem}.
 * <p>
 * The hypotheses for a theorem have the following form:
 * <br>
 * (label1 {@link TermExpression}1) &hellip; (labelN {@link TermExpression}N)
 * <p>
 * A proof is a LISP list each item of which is either
 * <ul>
 * <li>a {@link jhilbert.data.Symbol}, or
 * <li>a {@link jhilbert.data.Hypothesis}, or
 * <li>an {@link jhilbert.data.AbstractComplexTerm}.
 * </ul>
 */
public final class TheoremCommand extends AbstractStatementCommand {

	/**
	 * Logger.
	 */
	private final Logger logger;

	/**
	 * Map mapping labels to hypotheses
	 */
	private Map<String, TermExpression> hypothesisMap;

	/**
	 * Proof.
	 */
	private final List<Object> proof;

	/**
	 * Proof stack.
	 */
	private final Stack<TermExpression> proofStack;

	/**
	 * Mandatory variable stack.
	 */
	private final Stack<TermExpression> mandatoryStack;

	/**
	 * Distinct variable constraints which must be checked at the end of the proof.
	 */
	private final DVConstraints requiredDVConstraints;

	protected @Override void scanHypotheses(final TokenScanner tokenScanner, final ModuleData data) throws SyntaxException, ScannerException, DataException {
		StringBuilder context = new StringBuilder("hypotheses: ");
		hypothesisMap = new HashMap();
		try {
			Token token = tokenScanner.getToken();
			while (token.tokenClass == Token.TokenClass.BEGIN_EXP) {
				context.append('(');
				final String label = tokenScanner.getAtom();
				context.append(label).append(' ');
				TermExpression expr = new TermExpression(tokenScanner, data);
				context.append(expr.toString()).append(')');
				tokenScanner.endExp();
				hypotheses.add(expr);
				hypothesisMap.put(label, expr);
				token = tokenScanner.getToken();
			}
			tokenScanner.putToken(token);
		} catch (NullPointerException e) {
			throw new SyntaxException("Unexpected end of input", context.toString(), e);
		}
	}

	/**
	 * Scans a new TheoremCommand from a TokenScanner.
	 * The parameters must not be <code>null</code>.
	 *
	 * @param tokenScanner TokenScanner to scan from.
	 * @param data ModuleData.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 */
	public TheoremCommand(final TokenScanner tokenScanner, final ModuleData data) throws SyntaxException {
		super("theorem", tokenScanner, data);
		logger = Logger.getLogger(getClass());
		// scan proof
		StringBuilder context = new StringBuilder("proof: ");
		try {
			proof = new ArrayList();
			tokenScanner.beginExp();
			Token token = tokenScanner.getToken();
			while (token.tokenClass != Token.TokenClass.END_EXP) {
				switch (token.tokenClass) {
					case ATOM:
					final String atom = token.toString();
					context.append(atom).append(' ');
					proof.add(atom);
					break;

					case BEGIN_EXP:
					tokenScanner.putToken(token);
					final TermExpression expr = new TermExpression(tokenScanner, data);
					context.append(expr.toString()).append(' ');
					proof.add(expr);
					break;

					default:
					assert false: "This cannot happen";
					break;
				}
				token = tokenScanner.getToken();
			}
		} catch (NullPointerException e) {
			throw new SyntaxException("Unexpected end of input", context.toString(), e);
		} catch (DataException e) {
			throw new SyntaxException("Error scanning term", context.toString(), e);
		} catch (ScannerException e) {
			throw new SyntaxException("Scanner error", context.toString(), e);
		}
		proofStack = new Stack();
		mandatoryStack = new Stack();
		requiredDVConstraints = new DVConstraints();
	}

	public @Override void execute() throws VerifyException {
		super.execute();
		checkProof();
		try {
			data.defineSymbol(statement);
		} catch (DataException e) {
			throw new VerifyException("Data error while defining theorem", statement.getName(), e);
		}
	}

	private void checkProof() throws VerifyException {
		logger.info("Verifying proof of " + name);
		for (Object proofStep: proof) {
			checkProofStep(proofStep);
			if (logger.isTraceEnabled()) {
				logger.trace("Proof stack: " + proofStack);
				logger.trace("Mandatory stack: " + mandatoryStack);
			}
		}
		if (proofStack.empty())
			throw new VerifyException("Proof stack empty at end of proof", name);
		final TermExpression proofResult = proofStack.pop();
		if (!(mandatoryStack.empty() && proofStack.empty())) {
			logger.debug("Mandatory stack: " + mandatoryStack);
			logger.debug("Proof stack: " + proofStack);
			throw new VerifyException("Proof stack not empty after popping final proof result", name);
		}
		// try matching consequent against the proofResult
		final TermExpression consequent = statement.getConsequent();
		final Set<Variable> hypBlacklist = new HashSet();
		for (TermExpression hypothesis: hypothesisMap.values())
			hypBlacklist.addAll(hypothesis.variables());
		final Set<Variable> blacklist = new HashSet(hypBlacklist);
		blacklist.addAll(consequent.variables());
		if (!consequent.dummyMatches(proofResult, blacklist))
			throw new VerifyException("Consequent does not match proof result: " + consequent + " vs. " + proofResult, name);
		// check required dinstinct variable constraints
		final DVConstraints dvConstraints = statement.getDVConstraints();
		final Set<Variable> resultBlacklist = hypBlacklist;
		resultBlacklist.addAll(proofResult.variables());
		requiredDVConstraints.restrict(resultBlacklist); // throw out dummies
		if (!dvConstraints.containsAll(requiredDVConstraints))
			throw new VerifyException("Required distinct variable constraints " + requiredDVConstraints
				+ " are not a subset of actual distinct variable constraints " + dvConstraints, name);
	}

	private void checkProofStep(final Object proofStep) throws VerifyException {
		if (logger.isDebugEnabled())
			logger.debug("Proof step: " + proofStep);
		// proof step is a TermExpression?
		if (proofStep instanceof TermExpression) {
			mandatoryStack.push((TermExpression) proofStep);
			return;
		}
		final String label = (String) proofStep;
		// proof step is a hypothesis?
		if (hypothesisMap.containsKey(label)) {
			if (!mandatoryStack.empty()) {
				logger.debug("Proof stack: " + proofStack);
				logger.debug("Mandatory stack: " + mandatoryStack);
				throw new VerifyException("Proof step is a hypothesis but mandatory variable stack is not empty "
					+ "(you must place mandatory terms after the hypotheses)", label);
			}
			proofStack.push(hypothesisMap.get(label));
			return;
		}
		if (!data.containsSymbol(label))
			throw new VerifyException("Proof step is neither a symbol nor a hypothesis", label);
		final Symbol symbol = data.getSymbol(label);
		// proof step is a variable?
		if (symbol instanceof Variable) {
			mandatoryStack.push(new TermExpression((Variable) symbol));
			return;
		}
		// OK, only remaining possibility is Statement
		final Statement pStatement = (Statement) symbol;
		final Map<Variable, TermExpression> varAssignments = new HashMap();
		final DVConstraints pDVConstraints = pStatement.getDVConstraints();
		final List<TermExpression> pHypotheses = pStatement.getHypotheses();
		final List<Variable> pMandatoryVariables = pStatement.getMandatoryVariables();
		if (logger.isTraceEnabled())
			logger.trace("Proof step " + proofStep + ": " + pHypotheses + " -> " + pStatement.getConsequent());
		// first pop the mandatory variables
		int size = mandatoryStack.size();
		if (pMandatoryVariables.size() != size)
			throw new VerifyException("Statement has wrong number of mandatory variables", label);
		for (int i = 0; i != size; ++i) {
			final Variable var = pMandatoryVariables.get(i);
			final TermExpression expr = mandatoryStack.get(i);
			final String varKind = var.getKind();
			final String exprKind = expr.getKind();
			if (!varKind.equals(exprKind))
				throw new VerifyException("Kind mismatch in proof statement: " + varKind + " vs. " + exprKind, label);
			varAssignments.put(var, expr);
		}
		mandatoryStack.clear();
		// now pop the hypotheses
		size = pHypotheses.size();
		final int start = proofStack.size() - size;
		if (start < 0)
			throw new VerifyException("Too few hypotheses on stack: " + (-start) + " missing", label);
		try {
			for (int i = 0; i != size; ++i)
				pHypotheses.get(i).unify(proofStack.get(start + i), varAssignments);
		} catch (UnifyException e) {
			throw new VerifyException("Unification error while popping hypotheses from proof stack", label, e);
		}
		proofStack.setSize(start);
		// finally, check which distinct variable constraints we need
		for (VariablePair p: pDVConstraints) {
			SortedSet<Variable> varSet1 = new TreeSet(varAssignments.get(p.getFirst()).variables());
			SortedSet<Variable> varSet2 = new TreeSet(varAssignments.get(p.getSecond()).variables()); // never null because DVConstraints are restricted
			try {
				requiredDVConstraints.addProduct(varSet1, varSet2);
			} catch (DataException e) {
				throw new VerifyException("Distinct variable constraint " + p + " violated: " + varSet1 + " vs. " + varSet2, label, e);
			}
			if (logger.isTraceEnabled())
				logger.trace("constraint " + p + " expands to (" + varSet1 + ", " + varSet2 + "); required DV constraints now: " + requiredDVConstraints);
		}
		// push the new, shiny result on the stack
		proofStack.push(pStatement.getConsequent().subst(varAssignments));
	}

}
