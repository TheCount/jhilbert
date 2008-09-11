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

import java.util.List;

/**
 * Factory class for {@link Verifier}s.
 */
public abstract class VerifierFactory {

	/**
	 * Instance.
	 */
	private static final VerifierFactory instance = new jhilbert.verifier.impl.VerifierFactory();

	/**
	 * Returns a <code>VerifierFactory</code> instance.
	 */
	public static VerifierFactory getInstance() {
		return instance;
	}

	// default constructed
	
	/**
	 * Creates a new {@link Verifier} for the specified proof.
	 *
	 * @param proof the proof, which is a {@link List} of {@link Object}s
	 * 	each of which must be convertible to either {@link String} or
	 * 	{@link jhilbert.expressions.Expression}.
	 *
	 * @return the new verifier.
	 */
	public abstract Verifier createVerifier(List<Object> proof);

}
