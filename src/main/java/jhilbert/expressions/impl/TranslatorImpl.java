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

import java.util.HashMap;
import java.util.Map;

import jhilbert.data.DataFactory;
import jhilbert.data.Functor;
import jhilbert.data.Kind;
import jhilbert.data.Term;
import jhilbert.data.Variable;

import jhilbert.expressions.Expression;
import jhilbert.expressions.ExpressionException;
import jhilbert.expressions.Translator;

import org.apache.log4j.Logger;

/**
 * {@link Translator} implementation.
 */
final class TranslatorImpl implements Translator {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(Translator.class);

	/**
	 * Kind map.
	 */
	private final Map<Kind, Kind> kindMap;

	/**
	 * Functor map.
	 */
	private final Map<Functor, Functor> functorMap;

	/**
	 * Variable map.
	 */
	private final Map<Variable, Variable> variableMap;

	/**
	 * Data factory.
	 */
	private final DataFactory dataFactory;

	/**
	 * Creates a new <code>TranslatorImpl</code> for the specified
	 * {@link Kind} and {@link Functor} mappings.
	 *
	 * @param kindMap kind map.
	 * @param functorMap functor map.
	 */
	TranslatorImpl(final Map<Kind, Kind> kindMap, final Map<Functor, Functor> functorMap) {
		assert (kindMap != null): "Supplied kind map is null";
		assert (functorMap != null): "Supplied functor map is null";
		this.kindMap = kindMap;
		this.functorMap = functorMap;
		variableMap = new HashMap();
		dataFactory = DataFactory.getInstance();
	}

	public Expression translate(final Expression expression) throws ExpressionException {
		assert (expression != null): "Supplied expression is null";
		final Term term = expression.getValue();
		if (term.isVariable())
			return new ExpressionImpl(translate((Variable) term));
		if (!functorMap.containsKey(term)) {
			logger.error("Undefined functor translation");
			logger.debug("Expression: " + expression);
			logger.debug("Functor:    " + term);
			throw new ExpressionException("Undefined functor translation");
		}
		final Expression result = new ExpressionImpl(functorMap.get(term));
		for (final Expression childExpression: expression.getChildren())
			result.addChild(translate(childExpression));
		return result;
	}

	public Variable translate(final Variable variable) throws ExpressionException {
		assert (variable != null): "Supplied variable is null";
		if (!variableMap.containsKey(variable)) {
			final Kind kind = variable.getKind();
			if (!kindMap.containsKey(kind)) {
				logger.error("Undefined kind translation");
				logger.debug("Variable: " + variable);
				logger.debug("Kind:     " + kind);
				throw new ExpressionException("Undefined kind translation");
			}
			if (variable.isDummy())
				variableMap.put(variable, dataFactory.createDummyVariable(kindMap.get(kind)));
			else
				variableMap.put(variable, dataFactory.createUnnamedVariable(kindMap.get(kind)));
		}
		return variableMap.get(variable);
	}

	public Map<Functor, Functor> getFunctorMap() {
		return functorMap;
	}

	public Map<Variable, Variable> getVariableMap() {
		return variableMap;
	}

}
