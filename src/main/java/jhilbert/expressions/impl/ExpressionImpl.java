/*
    JHilbert, a verifier for collaborative theorem proving
    Copyright © 2008, 2009 Alexander Klauer

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
import jhilbert.scanners.TokenFeed;

import jhilbert.utils.ArrayTreeNode;

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
	 * {@link TokenFeed} using data from the specified {@link Module}.
	 *
	 * @param module data module.
	 * @param tokenFeed token scanner.
	 *
	 * @throws KindMismatchException if an input {@link Kind} and a result
	 * 	kind do not match during parsing.
	 * @throws ExpressionException if some other error (such as a scanner
	 * 	related error) occurs.
	 */
	ExpressionImpl(final Module module, final TokenFeed tokenFeed)
	throws KindMismatchException, ExpressionException {
		assert (module != null): "Supplied module is null";
		assert (tokenFeed != null): "Supplied token feed is null";
		try {
			final Token token = tokenFeed.getToken();
			switch (token.getTokenClass()) {
				case ATOM: // variable name
				setVariable(module, tokenFeed, token.getTokenString());
				tokenFeed.confirmVar();
				break;

				case BEGIN_EXP: // subexpression
				tokenFeed.confirmBeginExp();
				final Functor functor = setFunctor(module, tokenFeed, tokenFeed.getAtom());
				tokenFeed.confirmFunctor(functor);
				for (final Kind inputKind: functor.getInputKinds())
					addExpression(new ExpressionImpl(module, tokenFeed), inputKind);
				tokenFeed.endExp();
				tokenFeed.confirmEndExp();
				break;

				default:
				tokenFeed.reject("Expected expression");
				throw new ExpressionException("Expected expression");
			}
		} catch (NullPointerException e) {
			logger.error("Unexpected end of input while scanning expression");
			throw new ExpressionException("Unexpected end of input", e);
		} catch (ScannerException e) {
			throw new ExpressionException("Feed error", e);
		} catch (ClassCastException e) {
			try {
				tokenFeed.reject("Non-variable symbol in expression");
			} catch (ScannerException ignored) {
				// ignored
			}
			throw new ExpressionException("Non-variable symbol found in expression", e);
		}
		if (logger.isTraceEnabled())
			logger.trace("Expression complete: " + toString());
	}

	private void setVariable(final Module module, final TokenFeed tokenFeed, final String varName)
	throws ExpressionException {
		final Symbol sym = module.getSymbolNamespace().getObjectByString(varName);
		if (sym == null) {
			try {
				tokenFeed.reject("Symbol not found");
			} catch (ScannerException ignored) {
				// ignored
			}
			throw new ExpressionException("Symbol not found");
		}
		setValue((Variable) sym);
	}

	private Functor setFunctor(final Module module, final TokenFeed tokenFeed, final String functorName)
	throws ExpressionException {
		final Functor functor = module.getFunctorNamespace().getObjectByString(functorName);
		if (functor == null) {
			try {
				tokenFeed.reject("Term not found");
			} catch (ScannerException ignored) {
				// ignored
			}
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
