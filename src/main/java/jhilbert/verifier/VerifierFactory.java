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

package jhilbert.verifier;

import jhilbert.data.Module;

import jhilbert.scanners.TokenFeed;

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
	 * Creates a new {@link Verifier} for the specified module and token
	 * feed.
	 *
	 * @param module data module.
	 * @param tokenFeed token feed.
	 *
	 * @return the new verifier.
	 */
	public abstract Verifier createVerifier(Module module, TokenFeed tokenFeed);

}
