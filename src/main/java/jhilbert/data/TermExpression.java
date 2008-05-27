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

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import jhilbert.data.AbstractComplexTerm;
import jhilbert.data.Data;
import jhilbert.data.Term;
import jhilbert.data.Token;
import jhilbert.data.TreeNode;
import jhilbert.data.Variable;
import jhilbert.exceptions.DataException;
import jhilbert.exceptions.ScannerException;
import jhilbert.exceptions.UnifyException;
import jhilbert.util.DataInputStream;
import jhilbert.util.DataOutputStream;
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
public final class TermExpression extends TreeNode<Term, TermExpression> {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(TermExpression.class);

	/**
	 * Creates a new TermExpression from the specified data input stream.
	 *
	 * @param in data input stream.
	 * @param data context data.
	 * @param nameList name list for IDs.
	 * @param varList variable list.
	 * @param termsLower lower bound for terms.
	 * @param termsUpper upper bound for terms.
	 *
	 * @throws EOFException upon unexpected end of stream.
	 * @throws IOException if an I/O error occurs.
	 * @throws DataException if the input stream is inconsistent.
	 */
	static TermExpression create(final DataInputStream in, final Data data, final List<String> nameList,
		final List<Variable> varList, final int termsLower, final int termsUpper)
	throws EOFException, IOException, DataException {
		// load expression stack
		final Stack<Integer> exprStack = new Stack();
		for (int id = in.readInt(); id != 0; id = in.readInt())
			exprStack.push(id);
		// create term expression
		final TermExpression result = createPrivate(exprStack, data, nameList, varList, termsLower, termsUpper);
		if (!exprStack.empty()) {
			logger.error("Stack not empty after loading term " + result.getValue());
			logger.debug("Stack: " + exprStack);
			logger.debug("Variable list: " + varList);
			logger.trace("Name list: " + nameList);
			throw new DataException("Stack not empty after parsing term", result.getValue().toString());
		}
		return result;
	}

	private static TermExpression createPrivate(final Stack<Integer> exprStack, final Data data, final List<String> nameList,
		final List<Variable> varList, final int termsLower, final int termsUpper)
	throws DataException {
		if (exprStack.empty()) {
			logger.error("Stack underflow while loading term expression.");
			logger.debug("Variable list: " + varList);
			throw new DataException("Stack underflow while loading term expression.", "none");
		}
		final int leadingID = exprStack.pop();
		assert (leadingID != 0): "Zero id in expression stack: this cannot happen.";
		if (leadingID < 0) {
			if (~leadingID >= varList.size()) {
				logger.error("Invalid variable ID: " + leadingID);
				logger.debug("Valid IDs: -1 >= ID > " + ~varList.size());
				throw new DataException("Invalid variable ID", Integer.toString(leadingID));
			}
			return new TermExpression(varList.get(~leadingID));
		} else {
			if ((leadingID < termsLower) || (leadingID >= termsUpper)) {
				logger.error("Invalid term ID: " + leadingID);
				logger.debug("Valid IDs: " + termsLower + " <= ID < " + termsUpper);
				throw new DataException("Invalid term ID", Integer.toString(leadingID));
			}
			final AbstractComplexTerm term = data.getTerm(nameList.get(leadingID));
			if (term == null) {
				logger.error("Term not found: " + nameList.get(leadingID));
				throw new DataException("Term not found", nameList.get(leadingID));
			}
			final TermExpression result = new TermExpression(term);
			final int placeCount = term.placeCount();
			assert (placeCount >= 0): "Unknown term place count while loading term (this cannot happen)";
			for (int i = 0; i != placeCount; ++i) {
				final TermExpression child = createPrivate(exprStack, data, nameList, varList, termsLower, termsUpper);
				// mutual kind assurance
				final Term value = child.getValue();
				term.ensureInputKind(i, value.getKind());
				if (!value.isVariable())
					((AbstractComplexTerm) value).ensureKind(term.getInputKind(i));
				result.addChild(child);
			}
			return result;
		}
	}

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
	public TermExpression(final TokenScanner scanner, final Data data) throws DataException {
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
					AbstractComplexTerm term = data.getTerm(atom);
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
						final TermExpression termExpression = new TermExpression(scanner, data);
						context.append(termExpression.toString());
						// mutual kind assurance
						final Term paramTerm = termExpression.getValue();
						term.ensureInputKind(placeCount, paramTerm.getKind());
						if (!paramTerm.isVariable())
							((AbstractComplexTerm) paramTerm)
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
	 * Stores this TermExpression to the specified output stream.
	 *
	 * @param out data output stream.
	 * @param termNameTable name to ID table for storing terms.
	 * @param varNameTable name to ID table for storing variables.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	void store(final DataOutputStream out, final Map<String, Integer> termNameTable,
			final Map<String, Integer> varNameTable) throws IOException {
		// store zero terminated term
		storePrivate(out, termNameTable, varNameTable);
		out.writeInt(0);
	}

	void storePrivate(final DataOutputStream out, final Map<String, Integer> termNameTable,
			final Map<String, Integer> varNameTable) throws IOException {
		// store TermExpression in RPN format
		for (int i = childCount() - 1; i >= 0; --i)
			getChild(i).storePrivate(out, termNameTable, varNameTable);
		final Term value = getValue();
		if (value.isVariable())
			out.writeInt(varNameTable.get(value.getName()));
		else
			out.writeInt(termNameTable.get(value.getName()));
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
