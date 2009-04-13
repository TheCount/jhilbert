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

package jhilbert.scanners.impl;

import jhilbert.scanners.Token;

/**
 * Implementation of the {@link jhilbert.scanners.Token} interface.
 */
final class TokenImpl implements Token {

	/**
	 * Token class.
	 */
	private final Token.Class tokenClass;

	/**
	 * Token string.
	 */
	private final String tokenString;

	/**
	 * Creates a new <code>TokenImpl</code> from the specified string
	 * of the specified class.
	 *
	 * @param s token string.
	 * @param c token class.
	 */
	TokenImpl(final String s, final Token.Class c) {
		assert (s != null): "Supplied token string is null.";
		assert (c != null): "Supplied token class is null.";
		this.tokenClass = c;
		this.tokenString = s;
	}

	public String getTokenString() {
		return tokenString;
	}

	public Token.Class getTokenClass() {
		return tokenClass;
	}

	public @Override String toString() {
		return tokenString;
	}

}
