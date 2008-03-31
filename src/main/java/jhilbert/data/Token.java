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

/**
 * A token as they are encountered while scanning a LISP symbolic expression.
 */
public class Token implements Cloneable {

	/**
	 * Token classes.
	 */
	public static enum TokenClass {
		/**
		 * Beginning of a list.
		 * Typically denoted by a single opening parenthesis.
		 */
		BEGIN_EXP,

		/**
		 * End of a list.
		 * Typically denoted by a single closing parenthesis.
		 */
		END_EXP,

		/**
		 * Atomic symbolic expression.
		 * A concatenation of {@link Char.CharClass#ATOM_CHAR} characters.
		 */
		ATOM
	}

	/**
	 * String representation of this token.
	 */
	public final String repr;

	/**
	 * Token class of this token.
	 */
	public final TokenClass tokenClass;

	/**
	 * Creates a new token with the specified String representation and token class.
	 *
	 * @param repr String representation of this token (must not be <code>null</code>.
	 * @param tokenClass token class of this token.
	 */
	public Token(final String repr, final TokenClass tokenClass) {
		assert (repr != null): "Specified representation is null.";
		this.repr = repr;
		this.tokenClass = tokenClass;
	}

	/**
	 * Copy constructor.
	 *
	 * @param token the token to be copied.
	 */
	protected Token(final Token token) {
		this(token.repr, token.tokenClass);
	}

	public Token clone() {
		return new Token(this);
	}

	public int hashCode() {
		return repr.hashCode();
	}

	/**
	 * Equality comparison.
	 * Two non-null tokens are equal if and only if they are equal as Strings and they are in the same class.
	 *
	 * @param o Object to be compared for equality.
	 */
	public boolean equals(final Object o) {
		try {
			final Token t = (Token) o;
			return (repr.equals(t.repr) && (t.tokenClass == tokenClass));
		} catch (NullPointerException e) {
			return false;
		} catch (ClassCastException e) {
			return false;
		}
	}

	/**
	 * Return the string representation of this Token.
	 *
	 * @return String representation of this Token.
	 */
	public String toString() {
		return repr;
	}

}
