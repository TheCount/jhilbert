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

package jhilbert.verifier;

import java.util.Map;

import jhilbert.data.DVConstraints;
import jhilbert.data.Module;

import jhilbert.expressions.Expression;

/**
 * Verifier interface.
 */
public interface Verifier {

	/**
	 * Verifies the proof in the context of the spcified module with
	 * respect to the specified disjoint variable constraints and
	 * hypotheses.
	 * Succeeds if the proof is correct and its final outcome is compatible
	 * with the specified consequent.
	 *
	 * @param module data module.
	 * @param dvConstraints DV constraints. If necessary, the verifier will
	 * 	restrict the constraints.
	 * @param hypotheses the labelled hypotheses.
	 * @param consequent the consequent.
	 *
	 * @throws VerifyException if the proof does not verify.
	 */
	public void verify(final Module module, DVConstraints dvConstraints, Map<String, Expression> hypotheses, Expression consequent)
	throws VerifyException;

}
