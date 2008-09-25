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

package jhilbert.expressions;

import java.util.List;
import java.util.Map;
import java.util.Set;

import jhilbert.data.Functor;
import jhilbert.data.Kind;
import jhilbert.data.Module;
import jhilbert.data.Variable;

import jhilbert.scanners.TokenScanner;

import jhilbert.utils.TreeNode;

/**
 * Factory class for {@link Expression}s and expression related tools.
 */
public abstract class ExpressionFactory {

	/**
	 * Instance.
	 */
	private static final ExpressionFactory instance = new jhilbert.expressions.impl.ExpressionFactory();

	/**
	 * Obtains an instance of a <code>ExpressionFactory</code>.
	 *
	 * @return expression factory instance.
	 */
	public static ExpressionFactory getInstance() {
		return instance;
	}

	/**
	 * Scans a new {@link Expression} from the specified
	 * {@link TokenScanner} using data from the specified {@link Module}.
	 *
	 * @param module data module.
	 * @param tokenScanner token scanner.
	 *
	 * @return the new expression.
	 *
	 * @throws ExpressionException if an error occurs.
	 */
	public abstract Expression createExpression(Module module, TokenScanner tokenScanner) throws ExpressionException;

	/**
	 * Creates a new {@link Expression} from the specified LISP expression
	 * tree using data from the specified {@link Module}.
	 *
	 * @param module data module.
	 * @param tree LISP tree.
	 *
	 * @return the new expression.
	 *
	 * @throws ExpressionException if an error occurs.
	 */
	public abstract Expression createExpression(Module module, TreeNode<String> tree) throws ExpressionException;

	/**
	 * Creates a new {@link Expression} consisting just of the specified
	 * {@link Variable}.
	 *
	 * @param var variable.
	 *
	 * @return a new expression consisting just of <code>var</code>.
	 */
	public abstract Expression createExpression(Variable var);

	/**
	 * Creates a new {@link Expression} from the specified functor
	 * and children.
	 *
	 * @param functor functor.
	 * @param children list of children.
	 */
	public abstract Expression createExpression(Functor functor, List<Expression> children);

	/**
	 * Creates a new {@link Matcher}.
	 *
	 * @return new matcher.
	 */
	public abstract Matcher createMatcher();

	/**
	 * Creates a new {@link Substituter} from the specified substitution
	 * map.
	 *
	 * @param v2eMap substitution map.
	 *
	 * @return the new substituter.
	 */
	public abstract Substituter createSubstituter(Map<Variable, Expression> v2eMap);

	/**
	 * Creates a new {@link Anonymiser} from the spcified variable set.
	 *
	 * @param varSet variable set.
	 *
	 * @return the new anonymiser.
	 */
	public abstract Anonymiser createAnonymiser(Set<Variable> varSet);

	/**
	 * Creates a new {@link Translator} for the specified {@link Kind} and
	 * {@link Functor} mappings.
	 *
	 * @param kindMap kind map.
	 * @param functorMap functor map.
	 *
	 * @return the new translator.
	 */
	public abstract Translator createTranslator(Map<Kind, Kind> kindMap, Map<Functor, Functor> functorMap);

}
