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

package jhilbert.expressions.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import jhilbert.data.ConstraintException;
import jhilbert.data.DVConstraints;
import jhilbert.data.DataFactory;
import jhilbert.data.Definition;
import jhilbert.data.Functor;
import jhilbert.data.Kind;
import jhilbert.data.Module;
import jhilbert.data.Namespace;
import jhilbert.data.Symbol;
import jhilbert.data.Term;
import jhilbert.data.Variable;
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
	 * Default constructor, for serialisation or supplanting use only!
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
				parseSubexpression(module, tokenFeed);
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

	private void parseSubexpression(final Module module, final TokenFeed tokenFeed) throws ExpressionException, ScannerException {
		Functor functor = null;
		final Namespace<? extends Functor> functorNamespace = module.getFunctorNamespace();
		final List<ExpressionImpl> children = new ArrayList();
		outer:for (;;) {
			final Token token = tokenFeed.getToken();
			switch (token.getTokenClass()) {
				case ATOM:
				// attempt to read functor
				if (functor == null) {
					functor = functorNamespace.getObjectByString(token.getTokenString());
					if (functor != null) {
						tokenFeed.confirmFunctor(functor);
						break;
					}
				}
				// NO BREAK!

				case BEGIN_EXP:
				tokenFeed.putToken(token);
				children.add(new ExpressionImpl(module, tokenFeed));
				break;

				case END_EXP:
				break outer;

				default:
				throw new AssertionError("This cannot happen");
			}
		}
		// final sanity checks
		if (functor == null) {
			tokenFeed.reject("No functor found in expression");
			throw new ExpressionException("No functor found in expression");
		}
		final int size = children.size();
		final List<? extends Kind> inputKinds = functor.getInputKinds();
		if (size != inputKinds.size()) {
			tokenFeed.reject("Place count mismatch: expected " + size + " arguments to " + functor);
			throw new PlaceCountMismatchException("Place count mismatch: expected " + size);
		}
		for (int i = 0; i != size; ++i) {
			if (!children.get(i).getKind().equals(inputKinds.get(i))) {
				tokenFeed.reject("Kind mismatch after argument " + i + ": expected " + inputKinds.get(i) + ", got " + children.get(i).getKind());
				throw new KindMismatchException("Kind mismatch after argument " + i);
			}
		}
		supplant(functor, children);
		tokenFeed.confirmEndExp();
	}

	private void setVariable(final Module module, final TokenFeed tokenFeed, final String varName)
	throws ExpressionException {
		final Symbol sym = module.getSymbolNamespace().getObjectByString(varName);
		if (sym == null) {
			try {
				tokenFeed.reject("Symbol " + varName + " not found");
			} catch (ScannerException ignored) {
				// ignored
			}
			throw new ExpressionException("Symbol " + varName + " not found");
		}
		setValue((Variable) sym);
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
		final Functor functor = (Functor) term;
		if (functor.definitionDepth() != 0) {
			varSet.addAll(((Definition) functor).getDummyVariables());
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

	public DVConstraints dvConstraints() throws ConstraintException {
		final DVConstraints result = DataFactory.getInstance().createDVConstraints();
		final Term term = getValue();
		if (term.isVariable())
			return result;
		final Functor functor = (Functor) term;
		if (functor.definitionDepth() == 0) {
			for (final Expression childExp: getChildren())
				result.add(childExp.dvConstraints());
			return result;
		}
		final jhilbert.expressions.ExpressionFactory expressionFactory = ExpressionFactory.getInstance();
		final Definition definition = (Definition) functor;
		final List<Variable> arguments = new ArrayList(definition.getArguments());
		final List<Expression> children = getChildren();
		final Expression unfoldedExpression = definition.unfold(children);
		result.add(unfoldedExpression.dvConstraints());
		assert (arguments.size() == children.size()): "args/children count mismatch";
		final int size = arguments.size();
		final Map<Variable, Expression> varMap = new HashMap();
		for (int i = 0; i != size; ++i) {
			varMap.put(arguments.get(i), children.get(i));
		}
		for (final Variable[] dv: definition.getDVConstraints()) {
			assert (dv.length == 2): "Invalid DV length";
			Expression exp0 = varMap.get(dv[0]);
			Expression exp1 = varMap.get(dv[1]);
			if (exp0 == null)
				exp0 = expressionFactory.createExpression(dv[0]);
			if (exp1 == null)
				exp1 = expressionFactory.createExpression(dv[1]);
			result.addProduct(exp0.variables(), exp1.variables());
		}
		return result;
	}

	public Expression totalUnfold() { // recursively unfold expression
		// unfold head
		Expression expr = this;
		for (;;) {
			final Term term = expr.getValue();
			if (term.isVariable()) {
				// strictly speaking, definitions unfolding to a single variable are permissible by the
				// spec, so this check must NOT be moved outside the loop
				return expr;
			}
			final Functor functor = (Functor) term;
			if (functor.definitionDepth() == 0)
				break;
			expr = ((Definition) functor).unfold(expr.getChildren());
		}
		// unfold children
		final List<Expression> children = expr.getChildren();
		final int numChildren = children.size();
		final List<ExpressionImpl> newChildren = new ArrayList(numChildren);
		for (int i = 0; i != numChildren; ++i)
			newChildren.add((ExpressionImpl) children.get(i).totalUnfold());
		final ExpressionImpl result = new ExpressionImpl();
		result.supplant((Functor) expr.getValue(), newChildren);
		return result;
	}

}
