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

package jhilbert.expressions.impl;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import jhilbert.data.*;

import jhilbert.expressions.Expression;
import jhilbert.expressions.ExpressionException;
import jhilbert.expressions.KindMismatchException;
import jhilbert.expressions.PlaceCountMismatchException;

import jhilbert.scanners.ScannerException;
import jhilbert.scanners.Token;
import jhilbert.scanners.TokenScanner;

import jhilbert.utils.ArrayTreeNode;
import jhilbert.utils.TreeNode;

import org.apache.log4j.Logger;

/**
 * {@link Expression} implementation.
 */
final class ExpressionImpl extends ArrayTreeNode<Term> implements Expression, Serializable {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = jhilbert.Main.VERSION;

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(ExpressionImpl.class);

	/**
	 * Default constructor, for serialisation use only!
	 */
	public ExpressionImpl() {
		super();
	}

	/**
	 * Scans a new <code>ExpressionImpl</code> from the specified
	 * {@link TokenScanner} using data from the specified {@link Module}.
	 *
	 * @param module data module.
	 * @param tokenScanner token scanner.
	 *
	 * @throws KindMismatchException if an input {@link Kind} and a result
	 * 	kind do not match during parsing.
	 * @throws ExpressionException if some other error (such as a scanner
	 * 	related error) occurs.
	 */
	ExpressionImpl(final Module module, final TokenScanner tokenScanner)
	throws KindMismatchException, ExpressionException {
		assert (module != null): "Supplied module is null";
		assert (tokenScanner != null): "Supplied token scanner is null";
		try {
			final Token token = tokenScanner.getToken();
			switch (token.getTokenClass()) {
				case ATOM: // variable name
				setVariable(module, token.getTokenString());
				break;

				case BEGIN_EXP: // subexpression
				final Functor functor = setFunctor(module, tokenScanner.getAtom());
				for (final Kind inputKind: functor.getInputKinds())
					addExpression(new ExpressionImpl(module, tokenScanner), inputKind);
				tokenScanner.endExp();
				break;

				default:
				logger.error("Expected expression");
				logger.debug("Current scanner context: " + tokenScanner.getContextString());
				throw new ExpressionException("Expected expression");
			}
		} catch (NullPointerException e) {
			logger.error("Unexpected end of input while scanning expression", e);
			throw new ExpressionException("Unexpected end of input", e);
		} catch (ScannerException e) {
			logger.error("Scanner error", e);
			logger.debug("Scanner context: " + e.getScanner().getContextString());
			throw new ExpressionException("Scanner error", e);
		} catch (ExpressionException e) {
			logger.debug("Current scanner context: " + tokenScanner.getContextString());
			throw e;
		}
		if (logger.isTraceEnabled())
			logger.trace("Expression complete: " + toString());
	}

	ExpressionImpl(final Module module, final TreeNode<String> tree) throws ExpressionException {
		assert (module != null): "Supplied module is null";
		assert (tree != null): "Supplied LISP tree is null";
		try {
			if (tree.isLeaf()) // variable
				setVariable(module, tree.getValue());
			else { // complex expression
				final List<? extends TreeNode<String>> children = tree.getChildren();
				final Functor functor = setFunctor(module, children.get(0).getValue());
				final List<? extends Kind> inputKindList = functor.getInputKinds();
				final int size = inputKindList.size();
				if (size != children.size() - 1) {
					logger.error("Wrong place count for functor " + children.get(0).getValue());
					logger.debug("Expected place count: " + size);
					logger.debug("Actual place count:   " + (children.size() - 1));
					throw new PlaceCountMismatchException("Wrong place count");
				}
				for (int i = 1; i <= size; ++i)
					addExpression(new ExpressionImpl(module, children.get(i)),
						inputKindList.get(i - 1));
			}
		} catch (RuntimeException e) {
			logger.error("Invalid expression: " + tree);
			throw new ExpressionException("Invalid expression");
		}
	}

	private void setVariable(final Module module, final String varName) throws ExpressionException {
		final Symbol sym = module.getSymbolNamespace().getObjectByString(varName);
		if (sym == null) {
			logger.error("Symbol not found: " + varName);
			throw new ExpressionException("Symbol not found");
		}
		if (!sym.isVariable()) {
			logger.error("Non-variable symbol found in expression: " + varName);
			throw new ExpressionException("Non-variable symbol found in expression");
		}
		setValue((Variable) sym);
	}

	private Functor setFunctor(final Module module, final String functorName) throws ExpressionException {
		final Functor functor = module.getFunctorNamespace().getObjectByString(functorName);
		if (functor == null) {
			logger.error("Term not found: " + functorName);
			throw new ExpressionException("Term not found");
		}
		setValue(functor);
		return functor;
	}

	private void addExpression(final ExpressionImpl expr, final Kind targetKind) throws ExpressionException {
		if (!expr.getKind().equals(targetKind)) {
			logger.error("Kind mismatch");
			logger.debug("Expected kind: " + targetKind);
			logger.debug("Found kind:    " + expr.getKind());
			throw new KindMismatchException("Kind mismatch");
		}
		addChild(expr);
	}

	/**
	 * Creates a new <code>ExpressionImpl</code> with the specified
	 * {@link Term} and no children.
	 *
	 * @param term the term.
	 */
	ExpressionImpl(final Term term) {
		super(term);
	}

	public Kind getKind() {
		return getValue().getKind();
	}

	public @Override List<Expression> getChildren() {
		final Expression[] fooArray = new Expression[0];
		return Collections.unmodifiableList(Arrays.asList(super.getChildren().toArray(fooArray)));
	}

	/**
	 * For internal use by this impementation, this method allows changing
	 * the term of this <code>ExpressionImpl</code>, if it is a
	 * {@link Variable}.
	 *
	 * @param value the value.
	 */
	void setVariable(final Variable value) {
		assert (value != null): "Supplied value is null";
		assert (getValue() instanceof Variable): "Attempt to replace non-variable with variable";
		setValue(value);
	}

	/**
	 * For internal use by this implementation, this method allows
	 * supplanting this <code>ExpressionImpl</code>.
	 * This may be useful for implementing a substitution algorithm.
	 *
	 * @param functor new functor.
	 * @param children new list of children.
	 */
	void supplant(final Functor functor, final List<ExpressionImpl> children) {
		assert (functor != null): "Supplied functor is null";
		assert (children != null): "Supplied children are null";
		assert (functor.getInputKinds().size() == children.size()): "Place count mismatch";
		// FIXME: more asserts...
		setValue(functor);
		final ExpressionImpl[] fooArray = new ExpressionImpl[0];
		setChildren(children.toArray(fooArray));
	}

	public LinkedHashSet<Variable> variables() {
		final LinkedHashSet<Variable> varSet = new LinkedHashSet();
		variables(varSet);
		return varSet;
	}

	private void variables(final LinkedHashSet<Variable> varSet) {
		final Term term = getValue();
		if (term.isVariable()) {
			varSet.add((Variable) term);
			return;
		}
		for (final Expression childExp: getChildren())
			((ExpressionImpl) childExp).variables(varSet);
	}

	public @Override String toString() {
		final Term term = getValue();
		if (term.isVariable())
			return term.toString();
		final StringBuilder result = new StringBuilder();
		result.append('(')
			.append(term.toString());
		for (final Expression childExp: getChildren())
			result.append(' ')
				.append(childExp.toString());
		result.append(')');
		return result.toString();
	}

}
