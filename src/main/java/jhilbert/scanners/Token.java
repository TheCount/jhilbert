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

    You may contact the author on this Wiki page:
    http://www.wikiproofs.de/w/index.php?title=User_talk:GrafZahl
*/

package jhilbert.scanners;

import java.util.regex.Pattern;

/**
 * A LISP token.
 */
public interface Token {

	/**
	 * Token classes.
	 */
	public static enum Class {

		/**
		 * Beginning of a LISP list.
		 */
		BEGIN_EXP,

		/**
		 * End of a LISP list.
		 */
		END_EXP,

		/**
		 * Atomic LISP symbolic expression.
		 */
		ATOM

	}

	/**
	 * Valid ATOM tokens.
	 */
	public static final Pattern VALID_ATOM
		= Pattern.compile("[^\\p{Cn}\\p{Cf}\\p{Cc}\\p{Zs}\\p{Zl}\\p{Zp}\\t\\(\\)\\r\\n\\#]+");

	/**
	 * Obtains a string representation of this token.
	 *
	 * @return string representation of this token.
	 */
	public String getTokenString();

	/**
	 * Obtains the class of this token.
	 *
	 * @return class of this token.
	 */
	public Class getTokenClass();

}
