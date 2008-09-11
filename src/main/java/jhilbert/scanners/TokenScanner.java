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

package jhilbert.scanners;

/**
 * A {@link Scanner} for {@link Token}s.
 */
public interface TokenScanner extends Scanner<Token> {

	/**
	 * Reads a LISP atom.
	 * This convenience method reads a token and returns it if it is an
	 * atom, otherwise a {@link ScannerException} is thrown.
	 *
	 * @return a new {@link Token} whose {@link Token.Class} is
	 * 	{@link Token.Class#ATOM}.
	 *
	 * @throws ScannerException if the next token is not an atom, or if
	 * 	reading the token fails.
	 */
	public String getAtom() throws ScannerException;

	/**
	 * Reads the beginning of a LISP s-expression.
	 * This convenience method reads a token and returns normally it if it
	 * is the beginning of a LISP s-expression (that is, an opening
	 * parenthesis), otherwise a {@link ScannerException} is thrown.
	 *
	 * @throws ScannerException if the next token is not of class
	 * 	{@link Token.Class#BEGIN_EXP}, or if reading the token fails.
	 */
	public void beginExp() throws ScannerException;

	/**
	 * Reads the end of a LISP s-expression.
	 * This convenience method reads a token and returns normally it if it
	 * is the end of a LISP s-expression (that is, a closing
	 * parenthesis), otherwise a {@link ScannerException} is thrown.
	 *
	 * @throws ScannerException if the next token is not of class
	 * 	{@link Token.Class#END_EXP}, or if reading the token fails.
	 */
	public void endExp() throws ScannerException;

	/**
	 * Reads a string.
	 * This convenience method reads a string, that is either an atom or
	 * a {@link Token} of class {@link Token.Class#BEGIN_EXP} immediately
	 * followed by a <code>Token</code> of class
	 * {@link Token.Class#END_EXP}, in which case the string is empty.
	 *
	 * @return a string as specified.
	 *
	 * @throws ScannerException if a string as specified could not be
	 * 	found, or if reading the token(s) fails.
	 */
	public String getString() throws ScannerException;

}
