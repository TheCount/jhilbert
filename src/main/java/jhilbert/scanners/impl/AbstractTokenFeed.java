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

package jhilbert.scanners.impl;

import jhilbert.data.Functor;

import jhilbert.scanners.ScannerException;
import jhilbert.scanners.Token;
import jhilbert.scanners.TokenFeed;

import org.apache.log4j.Logger;

/**
 * Abstract implementation of a {@link TokenFeed}.
 */
abstract class AbstractTokenFeed extends AbstractScanner<Token> implements TokenFeed {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(AbstractTokenFeed.class);

	/**
	 * Start token.
	 */
	public static final Token BEGIN_EXP = new TokenImpl("(", Token.Class.BEGIN_EXP);

	/**
	 * End token.
	 */
	public static final Token END_EXP = new TokenImpl(")", Token.Class.END_EXP);

	public final String getAtom() throws ScannerException {
		try {
			final Token result = getToken();
			if (result.getTokenClass() != Token.Class.ATOM) {
				logger.error("Expected LISP atom, received " + result.getTokenClass());
				logger.debug("Current scanner context: " + getContextString());
				reject("Expected LISP atom");
				throw new ScannerException("Expected LISP atom", this);
			}
			return result.getTokenString();
		} catch (NullPointerException e) {
			logger.error("Expected LISP atom, got unexpected end of input");
			logger.debug("Current scanner context: " + getContextString());
			throw new ScannerException("Expected LISP atom", this, e);
		}
	}

	public final void beginExp() throws ScannerException {
		try {
			final Token result = getToken();
			if (result.getTokenClass() != Token.Class.BEGIN_EXP) {
				logger.error("Expected beginning of a LISP s-expression, received "
						+ result.getTokenClass());
				logger.debug("Current scanner context: " + getContextString());
				reject("Expected expression start");
				throw new ScannerException("Expected beginning of LISP s-expression", this);
			}
		} catch (NullPointerException e) {
			logger.error("Expected beginning of a LISP s-expression, got unexpected end of input");
			logger.debug("Current scanner context: " + getContextString());
			throw new ScannerException("Expected beginning of a LISP s-expression", this, e);
		}
	}

	public final void endExp() throws ScannerException {
		try {
			final Token result = getToken();
			if (result.getTokenClass() != Token.Class.END_EXP) {
				logger.error("Expected end of a LISP s-expression, received " + result.getTokenClass());
				logger.debug("Current scanner context: " + getContextString());
				reject("Expected expression end");
				throw new ScannerException("Expected end of LISP s-expression", this);
			}
		} catch (NullPointerException e) {
			logger.error("Expected end of a LISP s-expression, got unexpected end of input");
			logger.debug("Current scanner context: " + getContextString());
			throw new ScannerException("Expected end of a LISP s-expression", this, e);
		}
	}

	public final String getString() throws ScannerException {
		try {
			Token result = getToken();
			if (result.getTokenClass() == Token.Class.ATOM)
				return result.getTokenString();
			if (result.getTokenClass() != Token.Class.BEGIN_EXP) {
				logger.error("Expected LISP atom or empty s-expression, got " + result.getTokenClass());
				logger.debug("Current scanner context: " + getContextString());
				reject("Expected ATOM or Expression start");
				throw new ScannerException("Exected LISP atom or empty s-expression", this);
			}
			confirmBeginExp();
			result = getToken();
			if (result.getTokenClass() == Token.Class.END_EXP)
				return "";
			logger.error("Expected empty LISP s-expression, got " + result.getTokenClass());
			logger.debug("Current scanner context: " + getContextString());
			reject("Expected empty expression");
			throw new ScannerException("Expected empty LISP s-expression", this);
		} catch (NullPointerException e) {
			logger.error("Expected LISP atom or empty s-expression, got unexpected end of input", e);
			logger.debug("Current scanner context: " + getContextString());
			throw new ScannerException("Expected LISP atom or empty s-expression", this, e);
		}
	}

	public abstract void confirm(String msg) throws ScannerException;

	public abstract void reject(String msg) throws ScannerException;

	public final void confirmKeyword() throws ScannerException {
		confirm(TokenFeed.KEYWORD);
	}

	public final void confirmBeginExp() throws ScannerException {
		confirm(TokenFeed.BEGIN_EXP);
	}

	public final void confirmEndExp() throws ScannerException {
		confirm(TokenFeed.END_EXP);
	}

	public abstract void confirmEndCmd() throws ScannerException;

	public final void confirmKind() throws ScannerException {
		confirm(TokenFeed.KIND);
	}

	public final void confirmVar() throws ScannerException {
		confirm(TokenFeed.VARIABLE);
	}

	public final void confirmTerm() throws ScannerException {
		confirm(TokenFeed.TERM);
	}

	public final void confirmDef() throws ScannerException {
		confirm(TokenFeed.DEFINITION);
	}

	public final void confirmFunctor(final Functor functor) throws ScannerException {
		if (functor.definitionDepth() == 0)
			confirmTerm();
		else
			confirmDef();
	}

	public final void confirmStatement() throws ScannerException {
		confirm(TokenFeed.STATEMENT);
	}

	public final void confirmLabel() throws ScannerException {
		confirm(TokenFeed.LABEL);
	}

	public final void confirmParameter() throws ScannerException {
		confirm(TokenFeed.PARAMETER);
	}

	public final void confirmLocator() throws ScannerException {
		confirm(TokenFeed.LOCATOR);
	}

	public final void confirmString() throws ScannerException {
		confirm(TokenFeed.STRING);
	}

}
