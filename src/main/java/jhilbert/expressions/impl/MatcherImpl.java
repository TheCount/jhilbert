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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jhilbert.data.Term;
import jhilbert.data.Variable;
import jhilbert.expressions.Expression;
import jhilbert.expressions.Matcher;
import jhilbert.expressions.UnifyException;

import org.apache.log4j.Logger;

/**
 * {@link Matcher} implementation.
 */
final class MatcherImpl implements Matcher {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(MatcherImpl.class);

	/**
	 * Variable translation map.
	 */
	private Map<Variable, Variable> translationMap;

	/**
	 * Creates a new <code>MatcherImpl</code> with an empty translation
	 * map.
	 */
	MatcherImpl() {
		this(new HashMap());
	}

	/**
	 * Creates a new <code>MatcherImpl</code> with the specified initial
	 * translation map.
	 *
	 * @param translationMap variable translation map.
	 */
	MatcherImpl(final Map<Variable, Variable> translationMap) {
		assert (translationMap != null): "Supplied translation map is null";
		this.translationMap = translationMap;
	}

	public boolean checkDEquality(final Expression source, final Expression target) {
		assert (source != null): "Supplied source is null";
		assert (target != null): "Supplied target is null";
		final Expression unfoldedSource = source.totalUnfold();
		final Expression unfoldedTarget = target.totalUnfold();
		final Term sourceTerm = unfoldedSource.getValue();
		final Term targetTerm = unfoldedTarget.getValue();
		if (sourceTerm != targetTerm)
			return false;
		if (sourceTerm.isVariable())
			return true;
		final List<Expression> sourceChildren = unfoldedSource.getChildren();
		final List<Expression> targetChildren = unfoldedTarget.getChildren();
		final int size = sourceChildren.size();
		assert (size == targetChildren.size()): "Place count mismatch";
		for (int i = 0; i != size; ++i)
			if (!checkDEquality(sourceChildren.get(i), targetChildren.get(i)))
				return false;
		return true;
	}

	public boolean checkVEquality(final Expression source, final Expression target, final Set<Variable> blacklist) throws UnifyException {
		assert (source != null): "Supplied source expression is null";
		assert (target != null): "Supplied target expression is null";
		assert (blacklist != null): "Supplied variable blacklist is null";
		if (logger.isTraceEnabled()) {
			logger.trace("VEquality check:");
			logger.trace("Source expression: " + source);
			logger.trace("Target expression: " + target);
		}
		final Expression unfoldedSource = source.totalUnfold();
		final Expression unfoldedTarget = target.totalUnfold();
		if (logger.isTraceEnabled()) {
			logger.trace("Unfolded source:   " + unfoldedSource);
			logger.trace("Unfolded target:   " + unfoldedTarget);
			logger.trace("Blacklist:         " + blacklist);
		}
		if (!checkVEqualityHelper(unfoldedSource, unfoldedTarget, blacklist))
			return false;
		if (logger.isTraceEnabled()) {
			logger.trace("First stage equality check succeeded");
			logger.trace("Translation map: " + translationMap);
		}
		// check if translation map is one-to-one
		final Set<Variable> keySet = translationMap.keySet();
		final Set<Variable> valueSet = new HashSet(translationMap.values());
		if (keySet.size() == valueSet.size()) {
			if (logger.isTraceEnabled())
				logger.trace("Second stage equality check succeeded");
			return true;
		}
		logger.debug("Translation map not one-to-one after VEquality check");
		return false;
	}

	private boolean checkVEqualityHelper(final Expression source, final Expression target, final Set<Variable> blacklist) throws UnifyException {
		if (logger.isTraceEnabled()) {
			logger.trace("Now comparing: ");
			logger.trace("Source: " + source);
			logger.trace("Target: " + target);
		}
		final Term sourceTerm = source.getValue();
		final Term targetTerm = target.getValue();
		if (sourceTerm.isVariable()) {
			// simple variable equality
			if (sourceTerm == targetTerm)
				return true;
			final Variable sourceVariable = (Variable) sourceTerm;
			if (!targetTerm.isVariable())
				return false;
			final Variable targetVariable = (Variable) targetTerm;
			if (blacklist.contains(sourceVariable) || blacklist.contains(targetVariable))
				throw new UnifyException("Cannot map " + sourceVariable + " to " + targetVariable + " due to blacklist " + blacklist, source, target);
			if (!sourceVariable.isDummy() && targetVariable.isDummy())
				return false;
			if (translationMap.containsKey(sourceVariable))
				return (translationMap.get(sourceVariable) == targetVariable);
			if (logger.isTraceEnabled())
				logger.trace("Adding mapping " + sourceVariable + " -> " + targetVariable + " to translation map");
			translationMap.put(sourceVariable, targetVariable);
			return true;
		}
		// complex expression equality
		if (sourceTerm != targetTerm)
			return false;
		final List<Expression> sourceChildren = source.getChildren();
		final List<Expression> targetChildren = target.getChildren();
		final int size = sourceChildren.size();
		assert (size == targetChildren.size()): "Place count mismatch";
		for (int i = 0; i != size; ++i)
			if (!checkVEqualityHelper(sourceChildren.get(i), targetChildren.get(i), blacklist))
				return false;
		return true;
	}

	public boolean checkVEquality(final Expression source, final Expression target) {
		try {
			return checkVEquality(source, target, Collections.<Variable>emptySet());
		} catch (UnifyException e) {
			throw new AssertionError("Invalid dummy assignment with empty blacklist. This cannot happen");
		}
	}

	public Map<Variable, Variable> getAssignmentMap() {
		return Collections.unmodifiableMap(translationMap);
	}

}
