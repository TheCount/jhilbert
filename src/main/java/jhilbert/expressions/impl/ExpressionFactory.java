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

package jhilbert.expressions.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import jhilbert.data.Kind;
import jhilbert.data.Module;
import jhilbert.data.Functor;
import jhilbert.data.Variable;

import jhilbert.expressions.Expression;
import jhilbert.expressions.ExpressionException;

import jhilbert.scanners.TokenScanner;

import jhilbert.utils.TreeNode;

/**
 * {@link jhilbert.expressions.ExpressionFactory} implementation.
 */
public final class ExpressionFactory extends jhilbert.expressions.ExpressionFactory {

	// default constructed
	
	public @Override ExpressionImpl createExpression(final Module module, final TokenScanner tokenScanner) throws ExpressionException {
		assert (module != null): "Supplied module is null";
		assert (tokenScanner != null): "Supplied token scanner is null";
		return new ExpressionImpl(module, tokenScanner);
	}

	public @Override ExpressionImpl createExpression(final Module module, final TreeNode<String> tree)
	throws ExpressionException {
		assert (module != null): "Supplied module is null";
		assert (tree != null): "Supplied LISP tree is null";
		return new ExpressionImpl(module, tree);
	}

	public @Override ExpressionImpl createExpression(final Variable var) {
		assert (var != null): "Supplied variable is null";
		return new ExpressionImpl(var);
	}

	public @Override ExpressionImpl createExpression(final Functor functor, final List<Expression> children) {
		assert (functor != null): "Supplied functor is null";
		final ExpressionImpl result = new ExpressionImpl(functor);
		assert (children != null): "Supplied children are null";
		final int size = children.size();
		assert (functor.getInputKinds().size() == size): "Wrong number of children";
		for (int i = 0; i != size; ++i) {
			assert (children.get(i).getKind().equals(functor.getInputKinds().get(i))): "Kind mismatch";
			result.addChild(children.get(i));
		}
		return result;
	}

	public @Override MatcherImpl createMatcher() {
		return new MatcherImpl();
	}

	public @Override SubstituterImpl createSubstituter(final Map<Variable, Expression> v2eMap) {
		assert (v2eMap != null): "Supplied variable to expression map is  null";
		return new SubstituterImpl(v2eMap);
	}

	public @Override AnonymiserImpl createAnonymiser(final Set<Variable> varSet) {
		assert (varSet != null): "Supplied variable set is null";
		return new AnonymiserImpl(varSet);
	}

	public @Override TranslatorImpl createTranslator(final Map<Kind, Kind> kindMap, final Map<Functor, Functor> functorMap) {
		assert (kindMap != null): "Supplied kind map is null";
		assert (functorMap != null): "Supplied functor map is null";
		return new TranslatorImpl(kindMap, functorMap);
	}

}
