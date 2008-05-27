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

package jhilbert.data;

import jhilbert.data.Kind;
import jhilbert.data.Variable;

/**
 * Unnamed variable.
 * As soon as a {@link Definition} or a {@link Statement} is created,
 * the names of the variables occurring in their definition become
 * unimportant, as long as proper substitution is guaranteed.
 * This class provides objects with which the explicitly named
 * variables may be replaced with.
 */
public final class UnnamedVariable extends Variable {

	/**
	 * Internal ID of this unnamed variable.
	 */
	private static int id = 0;

	/**
	 * Creates a new unnamed variable with the specified kind.
	 *
	 * @param kind kind of unnamed variable.
	 */
	public UnnamedVariable(final Kind kind) {
		super("(?" + id + ")", kind);
		++id;
	}

	/**
	 * Upgrades the given variable to an unnamed one.
	 * Use with caution.
	 *
	 * @param var variable to be upgraded.
	 */
	private UnnamedVariable(final Variable var) {
		super(var.getName(), var.getKind());
	}

	public @Override UnnamedVariable clone() {
		return new UnnamedVariable(super.clone());
	}

}
