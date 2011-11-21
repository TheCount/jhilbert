/*
    JHilbert, a verifier for collaborative theorem proving

    Copyright © 2008, 2009, 2011 The JHilbert Authors
      See the AUTHORS file for the list of JHilbert authors.
      See the commit logs ("git log") for a list of individual contributions.

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

    You may contact the author on this Wiki page:
    http://www.wikiproofs.de/w/index.php?title=User_talk:GrafZahl
*/

package jhilbert.scanners.impl;

/**
 * Class to represent a character.
 * The <code>Char</code> class is used by {@link CharScanner}s as token type.
 */
final class Char {

	/**
	 * Possible character classes.
	 * These character classes resemble th purpose of {@link CharScanner}s,
	 * which are meant to parse characters occurring in a LISP symbolic
	 * expression (an <em>s-expression</em>).
	 */
	static enum Class {

		/**
		 * Characters which may never appear in any input.
		 */
		INVALID,

		/**
		 * Space characters other than line separators.
		 */
		SPACE,

		/**
		 * Opening parenthesis.
		 */
		OPEN_PAREN,

		/**
		 * Closing parenthesis.
		 */
		CLOSE_PAREN,

		/**
		 * Line separator.
		 */
		NEWLINE,

		/**
		 * Hash marks.
		 */
		HASHMARK,

		/**
		 * Atom characters.
		 */
		ATOM,

		/**
		 * End of input.
		 */
		EOF

	}

	/**
	 * Some codepoints of importance.
	 */
	private static enum Codepoint {

		/**
		 * Opening parenthesis.
		 */
		OPEN_PAREN(Character.codePointAt("(", 0)),

		/**
		 * Closing parenthesis.
		 */
		CLOSE_PAREN(Character.codePointAt(")", 0)),

		/**
		 * Horizontal tabulator.
		 */
		HORIZ_TAB(Character.codePointAt("\t", 0)),

		/**
		 * Linefeed.
		 */
		LINEFEED(Character.codePointAt("\n", 0)),

		/**
		 * Carriage return.
		 */
		CARR_RET(Character.codePointAt("\r", 0)),

		/**
		 * Hashmark.
		 */
		HASHMARK(Character.codePointAt("#", 0)),

		/**
		 * End of input.
		 */
		EOF(-1);

		/**
		 * Codepoint.
		 */
		private int codepoint;

		/**
		 * Creates a new instance of the <code>Codepoints</code> enum.
		 *
		 * @param codepoint integer representation of this codepoint.
		 */
		private Codepoint(final int codepoint) {
			this.codepoint = codepoint;
		}

		/**
		 * Returns an integer representation of this codepoint.
		 *
		 * @return integer representation of this codepoint.
		 */
		public int toInt() {
			return codepoint;
		}

	}

	/**
	 * Character class of this character.
	 */
	private final Class charClass;

	/**
	 * Codepoint of this character.
	 */
	private final int codepoint;

	/**
	 * Creates a new <code>Char</code> character with the specified
	 * codepoint. Unless the character is in the Basic Multilingual
	 * Plane (0–0xffff), the word "codepoint" is incorrect, as it might
	 * be half of a UTF-16 surrogate pair (if surrogate pairs even work).
	 * codepoint can also be <code>-1</code>, signifying end of input.
	 *
	 * @param codepoint codepoint of this <code>Char</code>.
	 */
	Char(final int codepoint) {
		assert ((codepoint >= -1) && (codepoint <= 65535)): "Invalid codepoint.";
		this.codepoint = codepoint;
		if (Character.isSpaceChar(codepoint) || codepoint == Codepoint.HORIZ_TAB.toInt())
			this.charClass = Class.SPACE;
		else if (codepoint == Codepoint.OPEN_PAREN.toInt())
			this.charClass = Class.OPEN_PAREN;
		else if (codepoint == Codepoint.CLOSE_PAREN.toInt())
			this.charClass = Class.CLOSE_PAREN;
		else if (codepoint == Codepoint.LINEFEED.toInt() || codepoint == Codepoint.CARR_RET.toInt())
			this.charClass = Class.NEWLINE;
		else if (codepoint == Codepoint.HASHMARK.toInt())
			this.charClass = Class.HASHMARK;
		else if (codepoint == Codepoint.EOF.toInt())
			this.charClass = Class.EOF;
		else if (Character.isISOControl(codepoint))
			this.charClass = Class.INVALID;
		else
			this.charClass = Class.ATOM;
	}

	/**
	 * Obtains the character class of this character.
	 *
	 * @return character class of this character.
	 */
	Class getCharClass() {
		return charClass;
	}

	/**
	 * Obtains the codepoint of this character.
	 *
	 * @return codepoint of this character.
	 */
	int getCodepoint() {
		return codepoint;
	}

	public @Override String toString() {
		return charClass.toString();
	}

}
