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
import jhilbert.data.Data;
import jhilbert.data.DataFactory;
import jhilbert.data.DVConstraints;
import jhilbert.data.Kind;
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
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(TheoremCommand.class);

	/**
	 * Data factory.
	 */
	private DataFactory df;

	/**
	 * Data.
	 */
	private final ModuleData data;

	/**
	 * Map mapping labels to hypotheses
	 */
	private Map<String, TermExpression> hypothesisMap;

	/**
	 * The statement that is to be proven.
	 */
	private Statement statement;

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

	protected @Override void scanHypotheses(final TokenScanner tokenScanner, final Data data) throws SyntaxException, ScannerException, DataException {
		hypothesisMap = new HashMap();
		df = DataFactory.getInstance();
		try {
			Token token = tokenScanner.getToken();
			while (token.tokenClass == Token.TokenClass.BEGIN_EXP) {
				final String label = tokenScanner.getAtom();
				TermExpression expr = df.scanTermExpression(tokenScanner, data);
				tokenScanner.endExp();
				hypotheses.add(expr);
				hypothesisMap.put(label, expr);
				token = tokenScanner.getToken();
			}
			tokenScanner.putToken(token);
		} catch (NullPointerException e) {
			logger.error("Unexpected end of input while scanning hypotheses in theorem " + getName());
			throw new SyntaxException("Unexpected end of input", tokenScanner.getContextString(), e);
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
		super(tokenScanner, data);
		final String name = getName();
		// scan proof
		try {
			proof = new ArrayList();
			tokenScanner.beginExp();
			Token token = tokenScanner.getToken();
			while (token.tokenClass != Token.TokenClass.END_EXP) {
				switch (token.tokenClass) {
					case ATOM:
					proof.add(token.toString());
					break;

					case BEGIN_EXP:
					tokenScanner.putToken(token);
					proof.add(df.scanTermExpression(tokenScanner, data));
					break;

					default:
					assert false: "This cannot happen";
					break;
				}
				token = tokenScanner.getToken();
			}
		} catch (NullPointerException e) {
			logger.error("Unexpected end of input while scanning proof of theorem " + name);
			throw new SyntaxException("Unexpected end of input", tokenScanner.toString(), e);
		} catch (DataException e) {
			logger.error("Error scanning term in proof of theorem " + name + " in context "
				+ tokenScanner.toString());
			throw new SyntaxException("Error scanning term", tokenScanner.toString(), e);
		} catch (ScannerException e) {
			logger.error("Scanner error in context " + tokenScanner.toString());
			throw new SyntaxException("Scanner error", tokenScanner.toString(), e);
		}
		this.data = data;
		proofStack = new Stack();
		mandatoryStack = new Stack();
		requiredDVConstraints = df.createDVConstraints();
	}

	public @Override void execute() throws VerifyException {
		super.execute();
		statement = data.getStatement(getName());
		checkProof();
	}

	private void checkProof() throws VerifyException {
		logger.info("Verifying proof of " + getName());
		for (final Object proofStep: proof) {
			checkProofStep(proofStep);
			if (logger.isTraceEnabled()) {
				logger.trace("Proof stack: " + proofStack);
				logger.trace("Mandatory stack: " + mandatoryStack);
			}
		}
		if (proofStack.empty()) {
			logger.error("Proof stack of theorem " + getName() + " empty at end of proof; "
				+ "should contain exactly one element.");
			throw new VerifyException("Proof stack empty at end of proof", getName());
		}
		final TermExpression proofResult = proofStack.pop();
		if (!(mandatoryStack.empty() && proofStack.empty())) {
			logger.error("Proof stack of theorem " + getName() + " not empty after popping final result.");
			logger.debug("Mandatory stack: " + mandatoryStack);
			logger.debug("Proof stack: " + proofStack);
			throw new VerifyException("Proof stack not empty after popping final proof result", getName());
		}
		// try matching consequent against the proofResult
		final TermExpression consequent = statement.getConsequent();
		final Set<Variable> hypBlacklist = new HashSet();
		for (TermExpression hypothesis: hypothesisMap.values())
			hypBlacklist.addAll(hypothesis.variables());
		final Set<Variable> blacklist = new HashSet(hypBlacklist);
		blacklist.addAll(consequent.variables());
		if (!consequent.dummyMatches(proofResult, blacklist)) {
			logger.error("Consequent of theorem " + getName() + " does not match proof result.");
			logger.error("Consequent:   " + consequent);
			logger.error("Proof result: " + proofResult);
			throw new VerifyException("Consequent does not match proof result", getName());
		}
		// check required dinstinct variable constraints
		final DVConstraints dvConstraints = getUnanonymizedDVConstraints();
		final Set<Variable> resultBlacklist = hypBlacklist;
		resultBlacklist.addAll(proofResult.variables());
		requiredDVConstraints.restrict(resultBlacklist); // throw out dummies
		if (!dvConstraints.containsAll(requiredDVConstraints)) {
			logger.error("Required distinct variable constraints for theorem " + getName()
				+ " are not a subset of actual distinct variable constraints.");
			logger.error("Required constraints: " + requiredDVConstraints);
			logger.error("Actual constraints:   " + dvConstraints);
			throw new VerifyException("Required distinct variable constraints are not "
				+ "a subset of actual distinct variable constraints " + dvConstraints, getName());
		}
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
				logger.error("Proof step is a hypothesis but mandatory variable stack is not empty.");
				logger.error("(Remember to place mandatory terms after the hypotheses!)");
				logger.error("Proof step: " + label);
				logger.debug("Proof stack: " + proofStack);
				logger.debug("Mandatory stack: " + mandatoryStack);
				throw new VerifyException("Proof step is a hypothesis but mandatory variable stack "
					+ "is not empty", label);
			}
			proofStack.push(hypothesisMap.get(label));
			return;
		}
		final Symbol symbol = data.getSymbol(label);
		if (symbol == null) {
			logger.error("Proof step is neither a symbol nor a hypothesis: " + label);
			throw new VerifyException("Proof step is neither a symbol nor a hypothesis", label);
		}
		if (getName().equals(label)) {
			logger.error("Invalid self-reference: " + label);
			throw new VerifyException("Invalid self-reference", label);
		}
		// proof step is a variable?
		if (symbol instanceof Variable) {
			mandatoryStack.push(df.createTermExpression((Variable) symbol));
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
		if (pMandatoryVariables.size() != size) {
			logger.error("Statement " + label + " has wrong number of mandatory variables in proof.");
			logger.error("Required number of variables: " + pMandatoryVariables.size());
			logger.error("Terms present:                " + size);
			throw new VerifyException("Statement has wrong number of mandatory variables", label);
		}
		for (int i = 0; i != size; ++i) {
			final Variable var = pMandatoryVariables.get(i);
			final TermExpression expr = mandatoryStack.get(i);
			final Kind varKind = var.getKind();
			final Kind exprKind = expr.getKind();
			if (!varKind.toString().equals(exprKind.toString())) {
				logger.error("Kind mismatch in proof statement " + label);
				logger.error("Affected expression: " + expr);
				logger.error("Kind of expression: " + exprKind);
				logger.error("Required kind:      " + varKind);
				throw new VerifyException("Kind mismatch in proof statement", label);
			}
			varAssignments.put(var, expr);
		}
		mandatoryStack.clear();
		// now pop the hypotheses
		size = pHypotheses.size();
		final int start = proofStack.size() - size;
		if (start < 0) {
			logger.error("Too few hypotheses on stack for proof step " + label);
			logger.error("Hypotheses missing: " + (-start));
			throw new VerifyException("Too few hypotheses on stack", label);
		}
		try {
			for (int i = 0; i != size; ++i)
				pHypotheses.get(i).unify(proofStack.get(start + i), varAssignments);
		} catch (UnifyException e) {
			logger.error("Unification error during proof step " + label);
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
				logger.error("Distinct variable constraint violation during proof step " + label, e);
				logger.error("First variable set:  " + varSet1);
				logger.error("Second variable set: " + varSet2);
				varSet1.removeAll(varSet2);
				logger.error("Meet: " + varSet1);
				throw new VerifyException("Distinct variable constraint violated", label, e);
			}
			if (logger.isTraceEnabled())
				logger.trace("constraint " + p + " expands to (" + varSet1 + ", " + varSet2 + "); required DV constraints now: " + requiredDVConstraints);
		}
		// push the new, shiny result on the stack
		proofStack.push(pStatement.getConsequent().subst(varAssignments));
	}

}
