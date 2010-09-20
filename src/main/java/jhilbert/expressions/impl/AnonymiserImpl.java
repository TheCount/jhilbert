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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jhilbert.data.DataException;
import jhilbert.data.DataFactory;
import jhilbert.data.DVConstraints;
import jhilbert.data.Term;
import jhilbert.data.Variable;

import jhilbert.expressions.Anonymiser;
import jhilbert.expressions.Expression;

/**
 * {@link Anonymiser} implementation.
 */
final class AnonymiserImpl implements Anonymiser {

	/**
	 * Variable to unnamed variable mapping.
	 */
	private final Map<Variable, Variable> varMap;

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
		dataFactory = DataFactory.getInstance();
		for (final Variable var: varSet) {
			assert (varSet != null): "Set of variables contains null variable";
			varMap.put(var, dataFactory.createUnnamedVariable(var.getKind()));
		}
	}

	public Variable anonymise(final Variable var) {
		assert (var != null): "Supplied variable is null";
		if (!varMap.containsKey(var))
			varMap.put(var, dataFactory.createDummyVariable(var.getKind()));
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

}
