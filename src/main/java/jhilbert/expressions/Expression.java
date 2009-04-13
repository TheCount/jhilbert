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

package jhilbert.expressions;

import java.io.Serializable;

import java.util.LinkedHashSet;
import java.util.List;

import jhilbert.data.Kind;
import jhilbert.data.Term;
import jhilbert.data.Variable;

import jhilbert.utils.TreeNode;

/**
 * A JHilbert expression.
 * See the JHilbert documentation for details.
 */
public interface Expression extends TreeNode<Term>, Serializable {

	/**
	 * Returns the kind of this <code>Expression</code>.
	 *
	 * @return kind of this expression.
	 */
	public Kind getKind();

	/**
	 * Returns the sub-expressions of this <code>Expression</code> in
	 * proper order.
	 *
	 * @return list of subexpressions.
	 */
	public List<Expression> getChildren();

	/**
	 * Returns the {@link Variable}s occurring in this
	 * <code>Expression</code>, in order of first appearance.
	 *
	 * @return variables occurring in this expression.
	 */
	public LinkedHashSet<Variable> variables();

}
