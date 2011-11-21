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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jhilbert.data.DVConstraints;
import jhilbert.data.DataException;
import jhilbert.data.DataFactory;
import jhilbert.data.Term;
import jhilbert.data.Variable;
import jhilbert.expressions.Anonymiser;
import jhilbert.expressions.Expression;

import org.apache.log4j.Logger;

/**
 * {@link Anonymiser} implementation.
 */
final class AnonymiserImpl implements Anonymiser {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(AnonymiserImpl.class);

	/**
	 * Variable to unnamed variable mapping.
	 */
	private final Map<Variable, Variable> varMap;

	/**
	 * Set of dummies.
	 */
	private final Set<Variable> dummySet;

	/**
	 * Data factory.
	 */
	private final DataFactory dataFactory;

	/**
	 * Creates a new <code>AnonymiserImpl</code> using the spcified set
	 * for variable unnaming.
	 *
	 * @param varSet set of variables to unname.
	 */
	AnonymiserImpl(final Set<Variable> varSet) {
		assert (varSet != null): "Supplied set of variables is null";
		varMap = new HashMap();
		dummySet = new HashSet();
		dataFactory = DataFactory.getInstance();
		for (final Variable var: varSet) {
			assert (varSet != null): "Set of variables contains null variable";
			if (var.isDummy())
				varMap.put(var, var);
			else
				varMap.put(var, dataFactory.createUnnamedVariable(var.getKind()));
		}
	}

	public Variable anonymise(final Variable var) {
		assert (var != null): "Supplied variable is null";
		if (!varMap.containsKey(var)) {
			final Variable dummy = dataFactory.createDummyVariable(var.getKind());
			if (logger.isDebugEnabled()) {
				logger.debug("New dummy variable created: " + dummy);
				if (logger.isTraceEnabled())
					Thread.dumpStack();
			}
			dummySet.add(dummy);
			varMap.put(var, dummy);
		}
		return varMap.get(var);
	}

	public DVConstraints anonymise(final DVConstraints dv) {
		assert (dv != null): "Supplied DVConstraints are null";
		try {
			final DVConstraints result = dataFactory.createDVConstraints();
			for (final Variable[] constraint: dv)
				result.add(anonymise(constraint[0]), anonymise(constraint[1]));
			return result;
		} catch (DataException e) {
			final AssertionError err = new AssertionError("Unexpected DataException while anonymising DV constraints. This should not happen.");
			err.initCause(e);
			throw err;
		}
	}

	public Expression anonymise(final Expression expr) {
		assert (expr != null): "Supplied expression is null";
		final Term term = expr.getValue();
		if (term.isVariable())
			return new ExpressionImpl(anonymise((Variable) term));
		// expression is a functor
		final ExpressionImpl result = new ExpressionImpl(term);
		for (final Expression childExp: expr.getChildren())
			result.addChild(anonymise(childExp));
		return result;
	}

	public Set<Variable> getDummyVariables() {
		return Collections.unmodifiableSet(dummySet);
	}

}
