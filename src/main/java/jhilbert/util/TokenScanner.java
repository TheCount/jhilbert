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

import jhilbert.util.Char;
import jhilbert.util.CharScanner;
import jhilbert.util.InputSource;
import jhilbert.util.Scanner;
import jhilbert.util.ScannerException;
import jhilbert.util.Token;

/**
 * Class to carve up a stream of {@link Char} values into {@link Token}s.
 * Whitespace may be used to separate tokens, but is otherwise ignored.
 *
 * @see jhilbert.data.Token.TokenClass
 */
public class TokenScanner extends Scanner<Token> {

	/**
	 * Token starting a symbolic expression.
	 */
	private static final Token BEGIN_EXP_TOKEN = new Token("(", Token.TokenClass.BEGIN_EXP);

	/**
	 * Token concluding a symbolic expression.
	 */
	private static final Token END_EXP_TOKEN = new Token("(", Token.TokenClass.END_EXP);

	/**
	 * Character scanner.
	 */
	private final CharScanner charScanner;

	/**
	 * Creates a new token scanner with the specified {@link InputSource}.
	 *
	 * @param inputSource the input source.
	 */
	public TokenScanner(final InputSource inputSource) {
		charScanner = new CharScanner(inputSource);
	}

	/**
	 * Creates a new {@link Token}.
	 *
	 * @return a new token.
	 *
	 * @throws ScannerException if the underlying character scanner creates such an Exception.
	 */
	protected Token getNewToken() throws ScannerException {
		charScanner.resetContext();
		StringBuilder context = getContext();
		StringBuilder repr = new StringBuilder();
		try {
			context.append(' ');
			Char c;
			// consume whitespace
			try {
				do {
					c = charScanner.getToken();
				} while ((c.charClass == Char.CharClass.SPACE) || (c.charClass == Char.CharClass.NEWLINE));
			} catch (NullPointerException e) { // EOF
				return null;
			}
			// what have we got?
			switch (c.charClass) {
				case OPEN_PAREN:
					context.append('(');
					return BEGIN_EXP_TOKEN;
				case CLOSE_PAREN:
					context.append(')');
					return END_EXP_TOKEN;
				case ATOM_CHAR:
					break;
				default:
					assert false: "Invalid character type " + c.charClass + " (this should not happen)";
			}
			// scan whole ATOM
			try {
				do {
					repr.append(c.toCharArray());
					c = charScanner.getToken();
				} while (c.charClass == Char.CharClass.ATOM_CHAR);
			} catch (NullPointerException e) { // EOF
				// break
			}
			charScanner.putToken(c);
			context.append(repr);
			return new Token(repr.toString(), Token.TokenClass.ATOM);
		} catch (ScannerException e) {
			throw new ScannerException("Error scanning token", this, e);
		}
	}

	/**
	 * Reads an ATOM string.
	 * Reads a token and returns it if it is an atom, otherwise a ScannerException is thrown.
	 *
	 * @return a new token of class ATOM.
	 *
	 * @throws ScannerException if the next token is not an ATOM, or if reading the token fails.
	 */
	public String getAtom() throws ScannerException {
		try {
			final Token result = getToken();
			if (result.tokenClass != Token.TokenClass.ATOM)
				throw new ScannerException("Expected ATOM", this);
			return result.toString();
		} catch (NullPointerException e) {
			throw new ScannerException("Unexpected end of input", this, e);
		}
	}

	/**
	 * Reads a BEGIN_EXP.
	 * Reads a token and ensures it's a BEGIN_EXP.
	 *
	 * @throws ScannerException if the next token is not a BEGIN_EXP, or if reading the token fails.
	 */
	public void beginExp() throws ScannerException {
		try {
			if (getToken().tokenClass != Token.TokenClass.BEGIN_EXP)
				throw new ScannerException("Expected BEGIN_EXP", this);
		} catch (NullPointerException e) {
			throw new ScannerException("Unexpected end of input", this, e);
		}
	}

	/**
	 * Reads an END_EXP.
	 * Reads a token and ensures it's an END_EXP.
	 *
	 * @throws ScannerException if the next token is not a BEGIN_EXP, or if reading the token fails.
	 */
	public void endExp() throws ScannerException {
		try {
			if (getToken().tokenClass != Token.TokenClass.END_EXP)
				throw new ScannerException("Expected END_EXP", this);
		} catch (NullPointerException e) {
			throw new ScannerException("Unexpected end of input", this, e);
		}
	}

	/**
	 * Reads a string.
	 * A string is either an atom or a BEGIN_EXP immediately followed by an END_EXP.
	 * In the latter case, the string is empty.
	 *
	 * @throws ScannerException if a string as specified could not be found, or if reading the token(s) fails.
	 */
	public String getString() throws ScannerException {
		try {
			Token token = getToken();
			if (token.tokenClass == Token.TokenClass.ATOM)
				return token.toString();
			if (token.tokenClass != Token.TokenClass.BEGIN_EXP)
				throw new ScannerException("Error scanning string", this);
			token = getToken();
			if (token.tokenClass == Token.TokenClass.END_EXP)
				return "";
			throw new ScannerException("Error scanning string", this);
		} catch (NullPointerException e) {
			throw new ScannerException("Unexpected end of input", this, e);
		}
	}

}
