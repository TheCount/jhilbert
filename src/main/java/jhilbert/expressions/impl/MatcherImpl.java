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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;

import jhilbert.data.Definition;
import jhilbert.data.Functor;
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
		final Term sourceTerm = source.getValue();
		final Term targetTerm = target.getValue();
		if (sourceTerm.isVariable())
			return sourceTerm.equals(targetTerm);
		if (targetTerm.isVariable())
			return false;
		final List<Expression> sourceChildren = source.getChildren();
		final List<Expression> targetChildren = target.getChildren();
		label:
		if (sourceTerm.equals(targetTerm)) {
			final int size = sourceChildren.size();
			assert (size == targetChildren.size()): "Place count mismatch";
			for (int i = 0; i != size; ++i)
				if (!checkDEquality(sourceChildren.get(i), targetChildren.get(i)))
					break label; // no backup needed as this method does not alter the translation map
			return true;
		}
		final Functor sourceFunctor = (Functor) sourceTerm;
		final Functor targetFunctor = (Functor) targetTerm;
		final int sourceDepth = sourceFunctor.definitionDepth();
		final int targetDepth = targetFunctor.definitionDepth();
		if ((sourceDepth == 0) && (targetDepth == 0))
			return false;
		if (sourceDepth == targetDepth)
			return checkDEquality(((Definition) sourceFunctor).unfold(sourceChildren),
				((Definition) targetFunctor).unfold(targetChildren));
		if (sourceDepth < targetDepth)
			return checkDEquality(source, ((Definition) targetFunctor).unfold(targetChildren));
		else
			return checkDEquality(((Definition) sourceFunctor).unfold(sourceChildren), target);
	}

	public boolean checkVEquality(final Expression source, final Expression target, final Set<Variable> blacklist) throws UnifyException {
		assert (source != null): "Supplied source is null";
		assert (target != null): "Supplied target is null";
		assert (blacklist != null): "Supplied blacklist is null";
		final Term sourceTerm = source.getValue();
		final Term targetTerm = target.getValue();
		if (sourceTerm.isVariable()) {
			if (!targetTerm.isVariable())
				return false;
			final Variable sourceVariable = (Variable) sourceTerm;
			final Variable targetVariable = (Variable) targetTerm;
			if (!translationMap.containsKey(sourceVariable)) {
				if (sourceVariable.isDummy() && blacklist.contains(targetVariable)) {
					logger.error("Attempt to map dummy variable to blacklisted variable");
					throw new UnifyException("Attempt to map dummy variable to blacklisted variable", source, target);
				}
				translationMap.put(sourceVariable, targetVariable);
			}
			return targetVariable.equals(translationMap.get(sourceVariable));
		}
		if (targetTerm.isVariable())
			return false;
		final List<Expression> sourceChildren = source.getChildren();
		final List<Expression> targetChildren = target.getChildren();
		try {
			label:
			if (sourceTerm.equals(targetTerm)) {
				final Map<Variable, Variable> backup = new HashMap(translationMap);
				final int size = sourceChildren.size();
				assert (size == targetChildren.size()): "Place count mismatch";
				for (int i = 0; i != size; ++i)
					if (!checkVEquality(sourceChildren.get(i), targetChildren.get(i), blacklist)) {
						translationMap = backup;
						break label;
					}
				return true;
			}
			final Functor sourceFunctor = (Functor) sourceTerm;
			final Functor targetFunctor = (Functor) targetTerm;
			final int sourceDepth = sourceFunctor.definitionDepth();
			final int targetDepth = targetFunctor.definitionDepth();
			if ((sourceDepth == 0) && (targetDepth == 0))
				return false;
			if (sourceDepth == targetDepth)
				return checkVEquality(((Definition) sourceFunctor).unfold(sourceChildren),
					((Definition) targetFunctor).unfold(targetChildren), blacklist);
			if (sourceDepth < targetDepth)
				return checkVEquality(source, ((Definition) targetFunctor).unfold(targetChildren), blacklist);
			else
				return checkVEquality(((Definition) sourceFunctor).unfold(sourceChildren), target, blacklist);
		} catch (UnifyException e) {
			logger.error("Invalid dummy assignment", e);
			logger.debug("Source expression: " + e.getSource());
			logger.debug("Target expression: " + e.getTarget());
			throw new UnifyException("Invalid dummy assignment", source, target, e);
		}
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
