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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jhilbert.data.AbstractComplexTerm;
import jhilbert.data.ModuleData;
import jhilbert.data.Term;
import jhilbert.data.Token;
import jhilbert.data.TreeNode;
import jhilbert.data.Variable;
import jhilbert.exceptions.DataException;
import jhilbert.exceptions.ScannerException;
import jhilbert.exceptions.UnifyException;
import jhilbert.util.TokenScanner;
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
public class TermExpression extends TreeNode<Term, TermExpression> {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(TermExpression.class);

	/**
	 * Scans a new TermExpression.
	 * The TermExpression is scanned with the specified TokenScanner,
	 * the names being retrieved from the specified ModuleData.
	 *
	 * @param scanner the TokenScanner to scan the LISP expression.
	 * @param data the data to obtain {@link Variable}s and {@link ComplexTerm}s.
	 *
	 * @throws DataException if a problem with the scanner occurs, or if the scanned expression is invalid.
	 */
	public TermExpression(final TokenScanner scanner, final ModuleData data) throws DataException {
		String atom = null;
		StringBuilder context = null;
		try {
			final Token token = scanner.getToken();
			switch (token.tokenClass) {
				case ATOM:
					atom = token.toString();
					if (!data.containsLocalSymbol(atom)) {
						logger.debug(data);
						throw new DataException("Symbol not found", atom);
					}
					setValue((Variable) data.getLocalSymbol(atom));
					return;
				case BEGIN_EXP:
					context = new StringBuilder('(');
					atom = scanner.getAtom();
					context.append(atom);
					if (!data.containsLocalTerm(atom))
						throw new DataException("ComplexTerm not found", atom);
					AbstractComplexTerm complexTerm = data.getLocalTerm(atom);
					setValue(complexTerm);
					final int placeCount = complexTerm.placeCount();
					if (logger.isTraceEnabled())
						logger.trace("Complex term " + complexTerm + " has " + placeCount + " places");
					for (int i = 0; i != placeCount; ++i) {
						context.append(' ');
						final TermExpression termExpression = new TermExpression(scanner, data);
						context.append(termExpression.toString());
						final String wantKind = data.getKind(complexTerm.getInputKind(i));
						final String haveKind = data.getKind(termExpression.getKind());
						if (!wantKind.equals(haveKind))
							throw new DataException("Kind mismatch", wantKind + "/" + haveKind);
						addChild(termExpression);
					}
					scanner.endExp();
					context.append(')');
					return;
				default:
					throw new DataException("Token error: expected ATOM or BEGIN_EXP", token.tokenClass.toString());
			}
		} catch (ClassCastException e) {
			throw new DataException("Symbol is not a variable", atom, e);
		} catch (ScannerException e) {
			throw new DataException("Error scanning term expression", context.toString(), e);
		} catch (DataException e) {
			throw new DataException("Token error or term not found", context.toString(), e);
		}
	}

	/**
	 * Creates a TermExpression consisting of a single variable.
	 *
	 * @param var variable to be converted to TermExpression.
	 */
	public TermExpression(final Variable var) {
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
	private TermExpression(final AbstractComplexTerm term) {
		super(term);
	}

	/**
	 * Returns the kind of this TermExpression.
	 *
	 * @return kind of this TermExpression.
	 */
	public String getKind() {
		return getValue().getKind();
	}

	/**
	 * Returns the variables occurring in this TermExpression, in order of first occurrence when the term is read from left to right.
	 *
	 * @return ordered set of mutually different variables occurring in this TermExpression.
	 */
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
			for (TermExpression child: getChildren())
				child.variables(result);
		}
	}

	/**
	 * Returns a TermExpression with the specified variable assignments.
	 * This TermExpression will not be altered.
	 *
	 * @param varAssignments variable assignments to apply.
	 *
	 * @return TermExpression with the specified variable assignments applied.
	 *
	 * @throws NullPointerException if varAssignments is <code>null</code>.
	 */
	public TermExpression subst(final Map<Variable, TermExpression> varAssignments) {
		assert (varAssignments != null): "Supplied variable assignments are null.";
		final Term value = getValue();
		if (value.isVariable()) {
			final Variable variable = (Variable) value;
			if (varAssignments.containsKey(variable))
				return varAssignments.get(variable);
			else
				return this;
		}
		final TermExpression result = new TermExpression((AbstractComplexTerm) value);
		for (TermExpression child: getChildren())
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
	public boolean matches(final TermExpression target) {
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
		final AbstractComplexTerm sTerm = (AbstractComplexTerm) sValue;
		final AbstractComplexTerm tTerm = (AbstractComplexTerm) tValue;
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

	/**
	 * Matches the target against this TermExpression.
	 * This is a less conservative match than {@link #matches()}.
	 * Here, dummy variables may be bound to non-dummy variables,
	 * as long as these non-dummy variables are not on the specified blacklist.
	 *
	 * @param target target expression to match against.
	 * @param blacklist list of variables that may not be bound to dummies.
	 *
	 * @return <code>true</code> if this term expression matches the target, <code>false</code> otherwise.
	 *
	 * @throws NullPointerException if one of the parameters is <code>null</code>.
	 */
	public boolean dummyMatches(final TermExpression target, final Set<Variable> blacklist) {
		assert (target != null): "Supplied target is null.";
		assert (blacklist != null): "Supplied blacklist is null.";
		final Map<DummyVariable, Variable> dummyAssignments = new HashMap();
		return dummyMatches(target, blacklist, dummyAssignments);
	}

	private boolean dummyMatches(final TermExpression target, final Set<Variable> blacklist, final Map<DummyVariable, Variable> dummyAssignments) {
		if (logger.isTraceEnabled())
			logger.trace("Dummy match: matching " + this + " against " + target);
		final Term sValue = getValue();
		final Term tValue = target.getValue();
		if (sValue.isVariable()) {
			if (!(tValue.isVariable())) {
				logger.error("Error matching " + sValue + " against " + tValue);
				return false;
			}
			if (sValue instanceof DummyVariable) {
				final DummyVariable dummy = (DummyVariable) sValue;
				if (dummyAssignments.containsKey(dummy))
					return dummyAssignments.get(dummy).equals(tValue);
				final Variable tVar = (Variable) tValue;
				if (blacklist.contains(tVar)) {
					logger.error("Error: " + tVar + " is blacklisted");
					return false;
				}
				dummyAssignments.put(dummy, tVar);
				return true;
			}
			return sValue.equals(tValue);
		}
		if (tValue.isVariable()) {
			logger.error("Error matching " + sValue + " against " + tValue);
			return false;
		}
		if (sValue.equals(tValue)) {
			final int childCount = childCount();
			for (int i = 0; i != childCount; ++i)
				if(!getChild(i).dummyMatches(target.getChild(i), blacklist, dummyAssignments)) {
					logger.error("Dummy match error: " + getChild(i) + " does not match " + target.getChild(i));
					logger.debug("Blacklisted variables: " + blacklist);
					logger.debug("current dummy assignments: " + dummyAssignments);
					return false;
				}
			return true;
		}
		final AbstractComplexTerm sTerm = (AbstractComplexTerm) sValue;
		final AbstractComplexTerm tTerm = (AbstractComplexTerm) tValue;
		final int sDepth = sTerm.definitionDepth();
		final int tDepth = tTerm.definitionDepth();
		if ((sDepth == 0) && (tDepth == 0)) {
			logger.error("Error matching " + sValue + " against " + tValue);
			return false;
		}
		if (sDepth == tDepth)
			return ((Definition) sTerm).unfold(getChildren()).dummyMatches(((Definition) tTerm).unfold(target.getChildren()), blacklist, dummyAssignments);
		if (sDepth < tDepth)
			return dummyMatches(((Definition) tTerm).unfold(target.getChildren()), blacklist, dummyAssignments);
		else
			return ((Definition) sTerm).unfold(getChildren()).dummyMatches(target, blacklist, dummyAssignments);
	}

	/**
	 * Attempts to unify this TermExpression with the specified target.
	 * This TermExpression will not be altered.
	 * Instead, variable to term expression mappings are provided.
	 * If this method returns successfully, the target can be generated by
	 * calling {@link #subst()} with the resulting variable mapping.
	 *
	 * @param target target expression of unification.
	 * @param varMap variable to term expression mapping. This parameter may be altered by this method.
	 *
	 * @throws NullPointerException if one of the specified parameters is <code>null</code>.
	 * @throws UnifyException if unification to the specified target is not possinble.
	 */
	public void unify(final TermExpression target, final Map<Variable, TermExpression> varMap) throws UnifyException {
		assert (target != null): "Supplied target is null.";
		assert (varMap != null): "Supplied variable map is null.";
		final Term sValue = getValue();
		if (sValue.isVariable()) {
			final Variable sVar = (Variable) sValue;
			if (varMap.containsKey(sVar))
				if (!varMap.get(sVar).matches(target))
					throw new UnifyException("Invalid change of variable assignment " + varMap.get(sVar), this, target);
				else
					return;
			varMap.put(sVar, target);
			return;
		}
		final Term tValue = target.getValue();
		if (logger.isTraceEnabled()) {
			logger.trace("Unification: sValue: " + sValue);
			logger.trace("Unification: tValue: " + tValue);
		}
		if (tValue.isVariable())
			throw new UnifyException("Trying to unify complex term against variable", this, target);
		try {
			if (sValue.equals(tValue)) {
				final int childCount = childCount();
				for (int i = 0; i != childCount; ++i)
					getChild(i).unify(target.getChild(i), varMap);
				return;
			}
		} catch (UnifyException e) {
			throw new UnifyException("Unification error in subterm", this, target, e);
		}
		final AbstractComplexTerm sTerm = (AbstractComplexTerm) sValue;
		final AbstractComplexTerm tTerm = (AbstractComplexTerm) tValue;
		final int sDepth = sTerm.definitionDepth();
		final int tDepth = tTerm.definitionDepth();
		if ((sDepth == 0) && (tDepth == 0))
			throw new UnifyException("Terms do not match", this, target);
		try {
			if (sDepth == tDepth)
				((Definition) sTerm).unfold(getChildren()).unify(((Definition) tTerm).unfold(target.getChildren()), varMap);
			if (sDepth < tDepth)
				unify(((Definition) tTerm).unfold(target.getChildren()), varMap);
			else
				((Definition) sTerm).unfold(getChildren()).unify(target, varMap);
		} catch (UnifyException e) {
			throw new UnifyException("Unification error after unfolding definition", this, target, e);
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
	public void equalityMap(final TermExpression target, final Map<Variable, Variable> translationMap) throws DataException {
		assert (target != null) : "Supplied target is null.";
		assert (translationMap != null) : "Supplied variable translation map is null.";
		final Term sValue = getValue();
		final Term tValue = target.getValue();
		if (sValue.isVariable()) {
			if (!(tValue.isVariable()))
				throw new DataException("Expression rhs should be a variable", sValue.toString() + "/" + target);
			final Variable sVar = (Variable) sValue;
			if (translationMap.containsKey(sVar)) {
				if (!translationMap.get(sVar).equals(tValue))
					throw new DataException("Inconsistent variable mapping",
						"(" + sValue + ", " + translationMap.get(sVar) + ") vs. (" + sValue + ", " + tValue + ")");
				else
					return;
			}
			translationMap.put(sVar, (Variable) tValue);
			return;
		}
		if ((tValue.isVariable()) || (!sValue.equals(tValue)))
			throw new DataException("Expressions are not equal", sValue.toString() + "/" + tValue);
		final int childCount = childCount();
		for (int i = 0; i != childCount; ++i)
			getChild(i).equalityMap(target.getChild(i), translationMap);
	}

	/**
	 * Renders this TermExpression as a String.
	 *
	 * @return String representation of this TermExpression.
	 */
	public @Override String toString() {
		final Term value = getValue();
		if (value.isVariable())
			return value.getName();
		final StringBuilder result = new StringBuilder();
		result.append('(').append(value.getName());
		for (TermExpression child: getChildren())
			result.append(' ').append(child.toString());
		result.append(')');
		return result.toString();
	}

}
