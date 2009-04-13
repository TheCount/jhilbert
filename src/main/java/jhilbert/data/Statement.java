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

package jhilbert.data;

import java.util.List;

import jhilbert.expressions.Expression;

/**
 * A statement.
 */
public interface Statement extends Symbol {

	/**
	 * Returns the {@link DVConstraints} of this <code>Statement</code>.
	 *
	 * @return disjoint variable constraints.
	 */
	public DVConstraints getDVConstraints();

	/**
	 * Returns the hypotheses of this <code>Statement</code>.
	 *
	 * @return hypotheses of this statement.
	 */
	public List<Expression> getHypotheses();

	/**
	 * Returns the consequent of this statement.
	 */
	public Expression getConsequent();

	/**
	 * Returns the mandatory variables of this statement.
	 */
	public List<Variable> getMandatoryVariables();

}
