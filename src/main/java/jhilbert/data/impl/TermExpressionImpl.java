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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import jhilbert.data.Data;
import jhilbert.data.DataException;
import jhilbert.data.Kind;
import jhilbert.data.Term;
import jhilbert.data.TermExpression;
import jhilbert.data.UnifyException;
import jhilbert.data.Variable;
import jhilbert.data.impl.DummyVariable;
import jhilbert.util.ScannerException;
import jhilbert.util.Token;
import jhilbert.util.TokenScanner;
import jhilbert.util.TreeNode;
import org.apache.log4j.Logger;

/**
 * A symbolic LISP expression constituting a term.
 * A term is either
 * <ul>
 * <li>a LISP atom which is a {@link Variable}, or
 * <li>a LISP list with:
 * <ul>
 * <li>A {@link ComplexTerm} as first item.
 * <li>The subsequent items are {@link Term}s matching the input terms of the first item.
 * </ul>
 * </ul>
 */
final class TermExpressionImpl extends TreeNode<Term, TermExpressionImpl> implements TermExpression {

	/**
	 * Serialization ID.
	 */
	private static final long serialVersionUID = jhilbert.Main.VERSION;

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(TermExpressionImpl.class);

	/**
	 * Scans a new TermExpression.
	 * The TermExpression is scanned with the specified TokenScanner,
	 * the names being retrieved from the specified ModuleData.
	 *
	 * @param scanner the TokenScanner to scan the LISP expression.
	 * @param data the data to obtain {@link Variable}s and {@link AbstractComplexTerm}s.
	 *
	 * @throws DataException if a problem with the scanner occurs, or if the scanned expression is invalid.
	 */
	public TermExpressionImpl(final TokenScanner scanner, final DataImpl data) throws DataException {
		String atom = null;
		StringBuilder context = null;
		try {
			final Token token = scanner.getToken();
			switch (token.tokenClass) {
				case ATOM:
					atom = token.toString();
					final Variable variable = data.getVariable(atom);
					if (variable == null) {
						logger.error("Variable " + atom + " not found.");
						logger.debug(data);
						throw new DataException("Variable not found", atom);
					}
					setValue(variable);
					return;
				case BEGIN_EXP:
					context = new StringBuilder('(');
					atom = scanner.getAtom();
					context.append(atom);
					ComplexTerm term = data.getTerm(atom);
					if (term == null) {
						logger.error("Term " + atom + " not found.");
						throw new DataException("Term not found", atom);
					}
					setValue(term);
					int placeCount = 0;
					for (Token nextToken = scanner.getToken();
							nextToken.tokenClass != Token.TokenClass.END_EXP;
							nextToken = scanner.getToken()) {
						scanner.putToken(nextToken);
						context.append(' ');
						final TermExpressionImpl termExpression
							= new TermExpressionImpl(scanner, data);
						context.append(termExpression.toString());
						// mutual kind assurance
						final Term paramTerm = termExpression.getValue();
						term.ensureInputKind(placeCount, paramTerm.getKind());
						if (!paramTerm.isVariable())
							((ComplexTerm) paramTerm)
								.ensureKind(term.getInputKind(placeCount));
						addChild(termExpression);
						++placeCount;
					}
					context.append(')');
					term.ensurePlaceCount(placeCount);
					if (logger.isTraceEnabled())
						logger.trace("Complex term " + term + " has " + placeCount + " places");
					return;
				default:
					logger.error("Expected ATOM or BEGIN_EXP while scanning term expression.");
					logger.error("Expression scanned thus far: " + context);
					logger.error("Next token: " + token.tokenClass);
					throw new DataException("Token error: expected ATOM or BEGIN_EXP", token.tokenClass.toString());
			}
		} catch (NullPointerException e) {
			logger.error("Unexpected end of input.", e);
			throw new DataException("Unexpected end of input", context.toString(), e);
		} catch (ScannerException e) {
			logger.error("Error scanning term expression.", e);
			throw new DataException("Error scanning term expression", context.toString(), e);
		} catch (DataException e) {
			logger.error("Error caused by previous error.", e);
			logger.error("Expression scanned thus far: " + context);
			throw new DataException("Token error or term not found", context.toString(), e);
		}
	}

	/**
	 * Creates a TermExpression consisting of a single variable.
	 *
	 * @param var variable to be converted to TermExpression.
	 */
	public TermExpressionImpl(final Variable var) {
		super(var);
	}

	/**
	 * Creates a TermExpression consisting of a single {@link AbstractComplexTerm}.
	 * <p>
	 * <strong>Warning:</strong>
	 * If the specified term has non-zero place count, the correct number of children
	 * must be added.
	 *
	 * @param term term to be converted to TermExpression.
	 */
	private TermExpressionImpl(final Term term) {
		super(term);
	}

	/**
	 * Creates an uninitialized TermExpression.
	 * Used by serialization.
	 */
	public TermExpressionImpl() {
		super();
	}

	/**
	 * Returns the kind of this TermExpression.
	 *
	 * @return kind of this TermExpression.
	 */
	public Kind getKind() {
		return getValue().getKind();
	}

	public LinkedHashSet<Variable> variables() {
		LinkedHashSet<Variable> result = new LinkedHashSet();
		variables(result);
		return result;
	}

	private void variables(final LinkedHashSet<Variable> result) {
		Term value = getValue();
		if (value.isVariable())
			result.add((Variable) value);
		else {
			final int childCount = childCount();
			for (TermExpressionImpl child: getChildren())
				child.variables(result);
		}
	}

	public TermExpressionImpl subst(final Map<Variable, ? extends TermExpression> varAssignments) {
		assert (varAssignments != null): "Supplied variable assignments are null.";
		final Term value = getValue();
		if (value.isVariable()) {
			final Variable variable = (Variable) value;
			if (varAssignments.containsKey(variable))
				return (TermExpressionImpl) varAssignments.get(variable);
			else
				return this;
		}
		final TermExpressionImpl result = new TermExpressionImpl((ComplexTerm) value);
		for (TermExpressionImpl child: getChildren())
			result.addChild(child.subst(varAssignments));
		return result;
	}

	/**
	 * Matches the target against this TermExpression.
	 * This is a conservative match: all terms must be equal,
	 * only definitions may be expanded.
	 *
	 * @param target target expression to match against.
	 *
	 * @return <code>true</code> if this term expression matches the target, <code>false</code> otherwise.
	 *
	 * @throws NullPointerException if target is <code>null</code>.
	 */
	public boolean matches(final TermExpressionImpl target) {
		assert (target != null): "Supplied target is null.";
		final Term sValue = getValue();
		final Term tValue = target.getValue();
		if (sValue.isVariable()) {
			if (!(tValue.isVariable()))
				return false;
			return sValue.equals(tValue);
		}
		if (tValue.isVariable())
			return false;
		if (sValue.equals(tValue)) {
			final int childCount = childCount();
			for (int i = 0; i != childCount; ++i)
				if(!getChild(i).matches(target.getChild(i)))
					return false;
			return true;
		}
		final ComplexTerm sTerm = (ComplexTerm) sValue;
		final ComplexTerm tTerm = (ComplexTerm) tValue;
		final int sDepth = sTerm.definitionDepth();
		final int tDepth = tTerm.definitionDepth();
		if ((sDepth == 0) && (tDepth == 0))
			return false;
		if (sDepth == tDepth)
			return ((Definition) sTerm).unfold(getChildren()).matches(((Definition) tTerm).unfold(target.getChildren()));
		if (sDepth < tDepth)
			return matches(((Definition) tTerm).unfold(target.getChildren()));
		else
			return ((Definition) sTerm).unfold(getChildren()).matches(target);
	}

	public boolean dummyMatches(final TermExpression target, final Set<Variable> blacklist) {
		assert (target != null): "Supplied target is null.";
		assert (target instanceof TermExpressionImpl): "Target not from this implementation.";
		assert (blacklist != null): "Supplied blacklist is null.";
		final Map<Variable, Variable> varMap = new HashMap();
		return dummyMatches((TermExpressionImpl) target, blacklist, varMap);
	}

	private boolean dummyMatches(final TermExpressionImpl target, final Set<Variable> blacklist, final Map<Variable, Variable> varMap) {
		if (logger.isTraceEnabled())
			logger.trace("Dummy match: matching " + this + " against " + target);
		final Term sValue = getValue();
		final Term tValue = target.getValue();
		if (sValue.isVariable()) {
			if (!(tValue.isVariable())) {
				logger.error("Error matching " + sValue + " against " + tValue);
				return false;
			}
			final Variable sVar = (Variable) sValue;
			final Variable tVar = (Variable) tValue;
			if (!varMap.containsKey(sVar)) {
				if ((sVar instanceof DummyVariable) && blacklist.contains(tVar)) {
					logger.error("Error: " + tVar + " is blacklisted");
					return false;
				}
				varMap.put(sVar, tVar);
			}
			return varMap.get(sVar).equals(tVar);
		}
		if (tValue.isVariable()) {
			logger.error("Error matching " + sValue + " against " + tValue);
			return false;
		}
		if (sValue.equals(tValue)) {
			final int childCount = childCount();
			for (int i = 0; i != childCount; ++i)
				if(!getChild(i).dummyMatches(target.getChild(i), blacklist, varMap)) {
					logger.error("Dummy match error: " + getChild(i) + " does not match " + target.getChild(i));
					logger.debug("Blacklisted variables:        " + blacklist);
					logger.debug("Current variable assignments: " + varMap);
					return false;
				}
			return true;
		}
		final ComplexTerm sTerm = (ComplexTerm) sValue;
		final ComplexTerm tTerm = (ComplexTerm) tValue;
		final int sDepth = sTerm.definitionDepth();
		final int tDepth = tTerm.definitionDepth();
		if (logger.isTraceEnabled()) {
			logger.trace("Preparing to unfold definitions.");
			logger.trace("Source: " + this);
			logger.trace("Target: " + target);
			logger.trace("Source definition depth: " + sDepth);
			logger.trace("Target definition depth: " + tDepth);
		}
		if ((sDepth == 0) && (tDepth == 0)) {
			logger.error("Error matching " + sValue + " against " + tValue);
			return false;
		}
		if (sDepth == tDepth)
			return ((Definition) sTerm).unfold(getChildren()).dummyMatches(((Definition) tTerm).unfold(target.getChildren()), blacklist, varMap);
		if (sDepth < tDepth)
			return dummyMatches(((Definition) tTerm).unfold(target.getChildren()), blacklist, varMap);
		else
			return ((Definition) sTerm).unfold(getChildren()).dummyMatches(target, blacklist, varMap);
	}

	public <E extends TermExpression> void unify(final E target, final Map<Variable, E> varMap) throws UnifyException {
		assert (target != null): "Supplied target is null.";
		assert (target instanceof TermExpressionImpl): "Target not from this implementation.";
		assert (varMap != null): "Supplied variable map is null.";
		final TermExpressionImpl targetImpl = (TermExpressionImpl) target;
		final Term sValue = getValue();
		if (sValue.isVariable()) {
			final Variable sVar = (Variable) sValue;
			if (varMap.containsKey(sVar))
				if (!((TermExpressionImpl) varMap.get(sVar)).matches(targetImpl)) {
					logger.error("Invalid change of variable assignment.");
					logger.error("Variable:            " + sVar);
					logger.error("Previous assignment: " + varMap.get(sVar));
					logger.error("Invalid assignment:  " + targetImpl);
					throw new UnifyException("Invalid change of variable assignment " + varMap.get(sVar), this, targetImpl);
				} else
					return;
			varMap.put(sVar, target);
			return;
		}
		final Term tValue = targetImpl.getValue();
		if (logger.isTraceEnabled()) {
			logger.trace("Unification: sValue: " + sValue);
			logger.trace("Unification: tValue: " + tValue);
		}
		if (tValue.isVariable())
			throw new UnifyException("Trying to unify complex term against variable", this, targetImpl);
		try {
			if (sValue.equals(tValue)) {
				final int childCount = childCount();
				for (int i = 0; i != childCount; ++i)
					getChild(i).unify((E) targetImpl.getChild(i), varMap);
				return;
			}
		} catch (UnifyException e) {
			throw new UnifyException("Unification error in subterm", this, targetImpl, e);
		}
		final ComplexTerm sTerm = (ComplexTerm) sValue;
		final ComplexTerm tTerm = (ComplexTerm) tValue;
		final int sDepth = sTerm.definitionDepth();
		final int tDepth = tTerm.definitionDepth();
		if ((sDepth == 0) && (tDepth == 0)) {
			logger.error("Unification error: terms do not match.");
			logger.error("Unifying: " + this);
			logger.error("Target:   " + targetImpl);
			throw new UnifyException("Terms do not match", this, targetImpl);
		}
		try {
			if (sDepth == tDepth)
				((Definition) sTerm).unfold(getChildren()).unify((E) ((Definition) tTerm).unfold(targetImpl.getChildren()), varMap);
			if (sDepth < tDepth)
				unify((E) ((Definition) tTerm).unfold(targetImpl.getChildren()), varMap);
			else
				((Definition) sTerm).unfold(getChildren()).unify(target, varMap);
		} catch (UnifyException e) {
			logger.error("Unification error after unfolding definition.");
			throw new UnifyException("Unification error after unfolding definition", this, targetImpl, e);
		}
	}

	/**
	 * Checks that the specified target expression is equal to this one,
	 * except possibly for a different naming of variables.
	 * If they are equal, a translation map for the variables is returned.
	 * If they are not equal, an exception is thrown.
	 * <strong>Warning:</strong>
	 * {@link Definition}s are not unfolded!
	 *
	 * @param target target expression to check for equality.
	 * @param translationMap equality translation map for the variables.
	 * 	This map is enhanced as the equality test proceeds.
	 * 	That way, several TermExpressions can be tested for simultaneous variable translation.
	 *
	 * @throws NullPointerException if one of the parameters is <code>null</code>.
	 * @throws DataException if this expression and the target expression are not equal.
	 */
	public void equalityMap(final TermExpressionImpl target, final Map<Variable, Variable> translationMap) throws DataException {
		assert (target != null) : "Supplied target is null.";
		assert (translationMap != null) : "Supplied variable translation map is null.";
		final Term sValue = getValue();
		final Term tValue = target.getValue();
		if (sValue.isVariable()) {
			if (!(tValue.isVariable())) {
				logger.error("Equality check failed: expression rhs should be a variable.");
				logger.error("lhs: " + this);
				logger.error("rhs: " + target);
				throw new DataException("Expression rhs should be a variable", sValue.toString() + "/" + target);
			}
			final Variable sVar = (Variable) sValue;
			if (translationMap.containsKey(sVar)) {
				if (!translationMap.get(sVar).equals(tValue)) {
					logger.error("Equality check failed: inconsistent variable mapping.");
					logger.error("Expected mapping: " + sValue + " -> " + translationMap.get(sVar));
					logger.error("Received mapping: " + sValue + " -> " + tValue);
					throw new DataException("Inconsistent variable mapping", sValue.toString());
				} else
					return;
			}
			translationMap.put(sVar, (Variable) tValue);
			return;
		}
		if ((tValue.isVariable()) || (!sValue.equals(tValue))) {
			logger.error("Equality check failed: expressions are not equal.");
			logger.error("lhs: " + this);
			logger.error("rhs: " + target);
			throw new DataException("Expressions are not equal", sValue.toString() + "/" + tValue);
		}
		final int childCount = childCount();
		for (int i = 0; i != childCount; ++i)
			getChild(i).equalityMap(target.getChild(i), translationMap);
	}

	/**
	 * Returns a copy of this expression which is adapted to
	 * the specified module data.
	 * This method is used to implement importing and exporting.
	 *
	 * @param data the module data this expression should be adapted to (must not be <code>null</code>).
	 * 	The data must contain all necessary data to adapt this expression.
	 * @param kindNameMap map mapping interface kind names to module kind names (must not be <code>null</code>).
	 * @param termNameMap map mapping interface term names to module term names (must not be <code>null</code>).
	 * @param varMap variable mapping (must not be <code>null</code>).
	 * 	This mapping is enhanced as the adaption progresses.
	 *
	 * @return data-adapted expression.
	 */
	TermExpressionImpl adapt(final ModuleDataImpl data, final Map<String, String> kindNameMap,
			final Map<String, String> termNameMap, final Map<Variable, Variable> varMap) {
		assert (data != null): "Supplied data are null.";
		assert (kindNameMap != null): "Supplied kind name map is null.";
		assert (termNameMap != null): "Supplied term name map is null.";
		assert (varMap != null): "Supplied variable map is null.";
		final Term value = getValue();
		if (value.isVariable()) {
			final Variable variable = (Variable) value;
			if (!varMap.containsKey(variable)) {
				final Kind kind = data.getKind(kindNameMap.get(variable.getKind().toString()));
				assert (kind != null): "Kind data missing during expression adaption.";
				if (variable instanceof DummyVariable)
					varMap.put(variable, new DummyVariable(kind));
				else if (variable instanceof UnnamedVariable)
					varMap.put(variable, new UnnamedVariable(kind));
				else
					assert false: "Attempt to adapt unanonymized expression.";
			}
			return new TermExpressionImpl(varMap.get(variable));
		}
		// value is a complex term
		final ComplexTerm term = data.getTerm(termNameMap.get(value.toString()));
		final TermExpressionImpl result = new TermExpressionImpl(term);
		final int placeCount = term.placeCount();
		try {
			for (int i = 0; i != placeCount; ++i)
				result.addChild(getChild(i).adapt(data, kindNameMap, termNameMap, varMap));
		} catch (IndexOutOfBoundsException e) {
			assert false: "Place counts do not match.";
			throw e;
		}
		return result;
	}

	/**
	 * Renders this TermExpression as a String.
	 *
	 * @return String representation of this TermExpression.
	 */
	public @Override String toString() {
		final Term value = getValue();
		if (value.isVariable())
			return value.toString();
		final StringBuilder result = new StringBuilder();
		result.append('(').append(value.toString());
		for (TermExpression child: getChildren())
			result.append(' ').append(child.toString());
		result.append(')');
		return result.toString();
	}

}
