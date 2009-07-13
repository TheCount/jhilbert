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

    You may contact the author on this Wiki page:
    http://www.wikiproofs.de/w/index.php?title=User_talk:GrafZahl
*/

package jhilbert.expressions.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jhilbert.data.Definition;
import jhilbert.data.Functor;
import jhilbert.data.Term;
import jhilbert.data.Variable;

import jhilbert.expressions.Expression;
import jhilbert.expressions.ExpressionException;
import jhilbert.expressions.Substituter;
import jhilbert.expressions.Translator;
import jhilbert.expressions.UnifyException;

import org.apache.log4j.Logger;

/**
 * {@link Substituter} implementation.
 */
final class SubstituterImpl implements Substituter {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(SubstituterImpl.class);

	/**
	 * Variable to expression map.
	 */
	private Map<Variable, Expression> v2eMap;

	/**
	 * Matcher.
	 */
	private final MatcherImpl matcher;

	/**
	 * Creates a new <code>Substituter</code> for the specified variable to
	 * expression map.
	 *
	 * @param v2eMap variable to expression map.
	 */
	SubstituterImpl(final Map<Variable, Expression> v2eMap) {
		assert (v2eMap != null): "Supplied variable to expression map is null";
		this.v2eMap = v2eMap;
		matcher = new MatcherImpl();
	}

	public Map<Variable, Expression> getAssignments() {
		return v2eMap;
	}

	public Expression substitute(final Expression expr) {
		assert (expr != null): "Supplied expression is null";
		final Term term = expr.getValue();
		if (term.isVariable()) {
			final Expression subst = v2eMap.get((Variable) term);
			if (subst == null)
				return expr;
			else
				return subst;
		}
		// expression is a functor
		final ExpressionImpl result = new ExpressionImpl(term);
		for (final Expression childExp: expr.getChildren())
			result.addChild(substitute(childExp));
		return result;
	}

	public void unify(final Expression source, final Expression target) throws UnifyException {
		assert (source != null): "Supplied source expression is null";
		assert (target != null): "Supplied target expression is null";
		if (logger.isTraceEnabled()) {
			logger.trace("Unifying expressions");
			logger.trace("Source: " + source);
			logger.trace("Target: " + target);
		}
		final Term sourceTerm = source.getValue();
		if (sourceTerm.isVariable()) {
			final Variable sourceVariable = (Variable) sourceTerm;
			if (v2eMap.containsKey(sourceVariable))
				if(!matcher.checkDEquality(v2eMap.get(sourceVariable), target)) {
					logger.error("Invalid change of variable assignment");
					logger.debug("Variable:            " + sourceVariable);
					logger.debug("Previous assignment: " + v2eMap.get(sourceVariable));
					logger.debug("Invalid assignment:  " + target);
					throw new UnifyException("Invalid change of variable assignment", source, target);
				} else
					return;
			v2eMap.put(sourceVariable, target);
			return;
		}
		final Term targetTerm = target.getValue();
		if (targetTerm.isVariable()) {
			logger.error("Unable to unify complex expression with variable");
			throw new UnifyException("Unable to unify complex expression with variable", source, target);
		}
		final List<Expression> sourceChildren = source.getChildren();
		final List<Expression> targetChildren = target.getChildren();
		final Map<Variable, Expression> backup = new HashMap(v2eMap);
		try {
			if (sourceTerm.equals(targetTerm)) {
				final int size = sourceChildren.size();
				assert (size == targetChildren.size()): "Place count mismatch";
				for (int i = 0; i != size; ++i)
					unify(sourceChildren.get(i), targetChildren.get(i));
				return;
			}
		} catch (UnifyException e) {
			// hmm, better luck after unfolding?
			if (logger.isTraceEnabled()) {
				logger.trace("Backtracking after unification error", e);
				logger.trace("Source: " + e.getSource());
				logger.trace("Target: " + e.getTarget());
			}
			v2eMap = backup;
		}
		final Functor sourceFunctor = (Functor) sourceTerm;
		final Functor targetFunctor = (Functor) targetTerm;
		final int sourceDepth = sourceFunctor.definitionDepth();
		final int targetDepth = targetFunctor.definitionDepth();
		if ((sourceDepth == 0) && (targetDepth == 0)) {
			logger.error("Terms do not match");
			throw new UnifyException("Terms do not match", source, target);
		}
		try {
			if (sourceDepth == targetDepth) {
				unify(((Definition) sourceFunctor).unfold(sourceChildren), ((Definition) targetFunctor).unfold(targetChildren));
				return;
			}
			if (sourceDepth < targetDepth)
				unify(source, ((Definition) targetFunctor).unfold(targetChildren));
			else
				unify(((Definition) sourceFunctor).unfold(sourceChildren), target);
			return;
		} catch (UnifyException e) {
			logger.error("Unification error after unfolding definition", e);
			logger.debug("Source expression: " + e.getSource());
			logger.debug("Target expression: " + e.getTarget());
			throw new UnifyException("Unification error after unfolding definition", source, target, e);
		}
	}

	public void crossUnify(final Expression source, final Expression target, final Translator translator) throws UnifyException {
		assert (source != null): "Supplied source expression is null";
		assert (target != null): "Supplied target expression is null";
		assert (translator != null): "Supplied translator is null";
		try {
			unify(translator.translate(ExpressionImpl.totalUnfold(source)), target);
		} catch (ExpressionException e) {
			logger.error("Unable to translate " + source);
			throw new UnifyException("Source translation error", source, target, e);
		}
	}

}
