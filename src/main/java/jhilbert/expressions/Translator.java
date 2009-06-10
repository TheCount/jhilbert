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

package jhilbert.expressions;

import java.util.Map;

import jhilbert.data.Functor;
import jhilbert.data.Variable;

/**
 * Translates {@link Expression}s under given {@link jhilbert.data.Kind} and
 * {@link jhilbert.data.Functor} maps. Implementations must provide a
 * constructor which accepts these mappings.
 */
public interface Translator {

	/**
	 * Translates the specified {@link Expression}.
	 * This method does not check for translation sanity, such as matching
	 * place count and kinds, checking for namespace sanity, etc.
	 *
	 * @param expression expression.
	 *
	 * @throws ExpressionException if a kind or a functor is encountered
	 * 	for which no mapping exists.
	 */
	public Expression translate(final Expression expression) throws ExpressionException;

	/**
	 * Translates the specified {@link Variable}.
	 * Behaves just as {@link #translate(Expression)} with a
	 * single-variable expression as argument.
	 *
	 * @param variable variable.
	 *
	 * @throws Expression if a kind or a functor is encountered
	 * 	for which no mapping exists.
	 */
	public Variable translate(final Variable variable) throws ExpressionException;

	/**
	 * Returns the functor map provided to this <code>Translator</code>.
	 *
	 * @return the functor map.
	 */
	public Map<Functor, Functor> getFunctorMap();

	/**
	 * Returns the current variable map of this <code>Translator</code>.
	 *
	 * @return current variable map.
	 */
	public Map<Variable, Variable> getVariableMap();

}
