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

package jhilbert.util;

/**
 * A character.
 * This class is meant to be a token type for {@link jhilbert.util.Scanner}.
 */
public class Char implements Cloneable {

	/**
	 * Character classes.
	 * These character classes are useful for scanning characters occurring when parsing LISP symbolic expressions.
	 */
	public static enum CharClass {

		/**
		 * Characters invalid in a LISP stream.
		 */
		INVALID,

		/**
		 * Space characters other than line separators.
		 */
		SPACE,

		/**
		 * Opening parenthesis (list start).
		 */
		OPEN_PAREN,

		/**
		 * Closing parenthesis (list end).
		 */
		CLOSE_PAREN,

		/**
		 * Line separator.
		 */
		NEWLINE,

		/**
		 * Hashmark, starting comments that stretch to the end of line.
		 */
		HASHMARK,

		/**
		 * Characters which may occur in a LISP atom.
		 */
		ATOM_CHAR,

		/**
		 * End of input.
		 */
		EOF

	}

	/**
	 * Codepoint of opening parenthesis.
	 */
	private static int OPEN_PAREN_CP = Character.codePointAt("(", 0);

	/**
	 * Codepoint of closing parenthesis.
	 */
	private static int CLOSE_PAREN_CP = Character.codePointAt(")", 0);

	/**
	 * Codepoint of horizontal tab.
	 */
	private static int HORIZ_TAB_CP = Character.codePointAt("\t", 0);

	/**
	 * Codepoint of line feed.
	 */
	private static int LINEFEED_CP = Character.codePointAt("\n", 0);

	/**
	 * Codepoint of carriage return.
	 */
	private static int CARR_RET_CP = Character.codePointAt("\r", 0);

	/**
	 * Codepoint of hashmark.
	 */
	private static int HASHMARK_CP = Character.codePointAt("#", 0);

	/**
	 * Character class of this character.
	 */
	public final CharClass charClass;

	/**
	 * Codepoint of this character.
	 */
	public final int codepoint;

	/**
	 * Creates a new character with the specified codepoint.
	 *
	 * @param codepoint codepoint of character.
	 */
	public Char(final int codepoint) {
		this.codepoint = codepoint;
		if (codepoint == -1)
			charClass = CharClass.EOF;
		else if (Character.isSpaceChar(codepoint) || codepoint == HORIZ_TAB_CP)
			charClass = CharClass.SPACE;
		else if (codepoint == LINEFEED_CP || codepoint == CARR_RET_CP)
			charClass = CharClass.NEWLINE;
		else if (codepoint == OPEN_PAREN_CP)
			charClass = CharClass.OPEN_PAREN;
		else if (codepoint == CLOSE_PAREN_CP)
			charClass = CharClass.CLOSE_PAREN;
		else if (codepoint == HASHMARK_CP)
			charClass = CharClass.HASHMARK;
		else if (!Character.isISOControl(codepoint) && Character.isDefined(codepoint))
			charClass = CharClass.ATOM_CHAR;
		else
			charClass = CharClass.INVALID;
	}

	/**
	 * Copy constructor.
	 *
	 * @param character character to be copied.
	 *
	 * @throws NullPointerException if character is <code>null</code>.
	 */
	protected Char(final Char character) {
		charClass = character.charClass;
		codepoint = character.codepoint;
	}

	public Char clone() {
		return new Char(this);
	}

	public int hashCode() {
		return codepoint;
	}

	public boolean equals(Object o) {
		try {
			Char c = (Char) o;
			return (c.codepoint == codepoint);
		} catch (NullPointerException e) {
			return false;
		} catch (ClassCastException e) {
			return false;
		}
	}

	/**
	 * Converts the codepoint to a char array.
	 *
	 * @return a char array representing the codepoint of this Char, or the char array <code>&quot;EOF&quot;</code> if <code>charClass == CharClass.EOF</code>.
	 */
	public char[] toCharArray() {
		if (charClass == CharClass.EOF)
			return "EOF".toCharArray();
		return Character.toChars(codepoint);
	}

	/**
	 * Converts the codepoint to a {@link String}.
	 * 
	 * @return a String representing the codepoint of this Char, or the String <code>&quot;EOF&quot;</code> if <code>charClass == CharClass.EOF</code>.
	 */
	public String toString() {
		return new String(toCharArray());
	}

}
