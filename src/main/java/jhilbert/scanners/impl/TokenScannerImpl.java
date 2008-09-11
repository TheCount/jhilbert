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

package jhilbert.scanners.impl;

import java.io.InputStream;

import jhilbert.scanners.ScannerException;
import jhilbert.scanners.Token;
import jhilbert.scanners.TokenScanner;

import org.apache.log4j.Logger;

/**
 * Implementation of the {@link jhilbert.scanners.TokenScanner} interface.
 */
final class TokenScannerImpl extends AbstractScanner<Token> implements TokenScanner {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(TokenScannerImpl.class);

	/**
	 * Token starting a symbolic expression.
	 */
	private static final Token BEGIN_EXP_TOKEN = new TokenImpl("(", Token.Class.BEGIN_EXP);

	/**
	 * Token concluding a symbolic expression.
	 */
	private static final Token END_EXP_TOKEN = new TokenImpl(")", Token.Class.END_EXP);

	/**
	 * Character scanner.
	 */
	private final CharScanner charScanner;

	/**
	 * Creates a new <code>TokenScannerImpl</code> from the specified
	 * {@link java.io.InputStream}.
	 *
	 * @param in input stream.
	 *
	 * @throws ScannerException if the underlying character scanner cannot
	 * 	be set up.
	 */
	TokenScannerImpl(final InputStream in) throws ScannerException {
		assert (in != null): "Supplied input stream is null";
		try {
			charScanner = new CharScanner(in);
		} catch (ScannerException e) {
			logger.error("Unable to set up character scanner " + e.getScanner(), e);
			throw new ScannerException("Unable to set up character scanner", this, e);
		}
	}

	protected @Override Token getNewToken() throws ScannerException {
		charScanner.resetContext();
		final StringBuilder repr = new StringBuilder();
		try {
			appendToContext(' ');
			Char c;
			Char.Class cc;
			// consume whitespace
			try {
				do {
					c = charScanner.getToken();
					cc = c.getCharClass();
				} while ((cc == Char.Class.SPACE) || (cc == Char.Class.NEWLINE));
			} catch (NullPointerException e) { // EOF
				return null;
			}
			// what have we got?
			switch (cc) {
				case OPEN_PAREN:
					appendToContext('(');
					return BEGIN_EXP_TOKEN;
				case CLOSE_PAREN:
					appendToContext(')');
					return END_EXP_TOKEN;
				case ATOM:
					break;
				default:
					assert false: "Invalid character type (this should not happen)";
			}
			// scan whole ATOM
			try {
				do {
					repr.append((char) c.getCodepoint());
					c = charScanner.getToken();
				} while (c.getCharClass() == Char.Class.ATOM);
			} catch (NullPointerException e) { // EOF
				// break
			}
			charScanner.putToken(c);
			appendToContext(repr);
			return new TokenImpl(repr.toString(), Token.Class.ATOM);
		} catch (ScannerException e) {
			logger.error("Error scanning token", e);
			logger.debug("Context of causing scanner: " + e.getScanner().getContextString());
			logger.debug("Context of this scanner: " + getContextString());
			throw new ScannerException("Error scanning token", this, e);
		}
	}

	public String getAtom() throws ScannerException {
		try {
			final Token result = getToken();
			if (result.getTokenClass() != Token.Class.ATOM) {
				logger.error("Expected LISP atom, received " + result.getTokenClass());
				logger.debug("Current scanner context: " + getContextString());
				throw new ScannerException("Expected LISP atom", this);
			}
			return result.getTokenString();
		} catch (NullPointerException e) {
			logger.error("Expected LISP atom, got unexpected end of input", e);
			logger.debug("Current scanner context: " + getContextString());
			throw new ScannerException("Expected LISP atom", this, e);
		} catch (ScannerException e) {
			logger.error("Scanner error while scanning LISP atom", e);
			logger.debug("Context of causing scanner: " + e.getScanner().getContextString());
			// Note: (this == e.getScanner()) should be true here
			throw new ScannerException("Scanner error while scanning LISP atom", this, e);
		}
	}

	public void beginExp() throws ScannerException {
		try {
			final Token result = getToken();
			if (result.getTokenClass() != Token.Class.BEGIN_EXP) {
				logger.error("Expected beginning of a LISP s-expression, received "
					+ result.getTokenClass());
				logger.debug("Current scanner context: " + getContextString());
				throw new ScannerException("Expected beginning of LISP s-expression", this);
			}
		} catch (NullPointerException e) {
			logger.error("Expected beginning of a LISP s-expression, got unexpected end of input", e);
			logger.debug("Current scanner context: " + getContextString());
			throw new ScannerException("Expected beginning of a LISP s-expression", this, e);
		} catch (ScannerException e) {
			logger.error("Scanner error while scanning beginning of LISP s-expression", e);
			logger.debug("Context of causing scanner: " + e.getScanner().getContextString());
			// Note: (this == e.getScanner()) should be true here
			throw new ScannerException("Scanner error while scanning beginning of LISP s-expression", this, e);
		}
	}

	public void endExp() throws ScannerException {
		try {
			final Token result = getToken();
			if (result.getTokenClass() != Token.Class.END_EXP) {
				logger.error("Expected end of a LISP s-expression, received "
					+ result.getTokenClass());
				logger.debug("Current scanner context: " + getContextString());
				throw new ScannerException("Expected end of LISP s-expression", this);
			}
		} catch (NullPointerException e) {
			logger.error("Expected end of a LISP s-expression, got unexpected end of input", e);
			logger.debug("Current scanner context: " + getContextString());
			throw new ScannerException("Expected end of a LISP s-expression", this, e);
		} catch (ScannerException e) {
			logger.error("Scanner error while scanning end of LISP s-expression", e);
			logger.debug("Context of causing scanner: " + e.getScanner().getContextString());
			// Note: (this == e.getScanner()) should be true here
			throw new ScannerException("Scanner error while scanning end of LISP s-expression", this, e);
		}
	}

	public String getString() throws ScannerException {
		try {
			Token result = getToken();
			if (result.getTokenClass() == Token.Class.ATOM)
				return result.getTokenString();
			if (result.getTokenClass() != Token.Class.BEGIN_EXP) {
				logger.error("Expected LISP atom or empty s-expression, got " + result.getTokenClass());
				logger.debug("Current scanner context: " + getContextString());
				throw new ScannerException("Exected LISP atom or empty s-expression", this);
			}
			result = getToken();
			if (result.getTokenClass() == Token.Class.END_EXP)
				return "";
			logger.error("Expected empty LISP s-expression, got " + result.getTokenClass());
			logger.debug("Current scanner context: " + getContextString());
			throw new ScannerException("Expected empty LISP s-expression", this);
		} catch (NullPointerException e) {
			logger.error("Expected LISP atom or empty s-expression, got unexpected end of input", e);
			logger.debug("Current scanner context: " + getContextString());
			throw new ScannerException("Expected LISP atom or empty s-expression", this, e);
		} catch (ScannerException e) {
			logger.error("Scanner error while scanning string", e);
			logger.debug("Context of causing scanner: " + e.getScanner().getContextString());
			// Note: (this == e.getScanner()) should be true here
			throw new ScannerException("Scanner error while scanning string", this, e);
		}
	}

}
