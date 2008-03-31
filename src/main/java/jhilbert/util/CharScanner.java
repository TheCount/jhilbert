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

import jhilbert.data.Char;
import jhilbert.exceptions.InputException;
import jhilbert.exceptions.ScannerException;
import jhilbert.util.InputSource;
import jhilbert.util.Scanner;

/**
 * Scanner for characters.
 * This class is specially tuned for scanning LISP-like input.
 */
public class CharScanner extends Scanner<Char> {

	/**
	 * Input source.
	 */
	private final InputSource inputSource;

	/**
	 * Creates a new CharScanner with the specified {@link InputSource}.
	 *
	 * @param inputSource the input source.
	 */
	public CharScanner(final InputSource inputSource) {
		assert (inputSource != null): "Supplied input source is null.";
		this.inputSource = inputSource;
	}

	/**
	 * Creates a new Char token.
	 * If a hashmark is encountered, this and all subsequent characters are ignored, until a line separator occurs.
	 * Also sets the context of this scanner to a String representation of the {@link Char.CharClass}, followed by
	 * a string representation of the character.
	 *
	 * @return a new Char token, or <code>null</code> if the input source is exhausted.
	 *
	 * @throws ScannerException if the character is invalid, or an {@link InputException} occurs with the input source.
	 */
	protected Char getNewToken() throws ScannerException {
		try {
			Char c = new Char(inputSource.read());
			if (c.charClass == Char.CharClass.HASHMARK) // ignore comment
				do {
					c = new Char(inputSource.read());
				} while (c.charClass != Char.CharClass.NEWLINE && c.charClass != Char.CharClass.EOF);
			getContext().append(c.toCharArray());
			if (c.charClass == Char.CharClass.EOF)
				return null;
			if (c.charClass == Char.CharClass.INVALID)
				throw new ScannerException("Invalid character", this);
			return c;
		} catch (InputException e) {
			throw new ScannerException("Error scanning character", this, e);
		}
	}

}
