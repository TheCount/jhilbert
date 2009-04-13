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

import java.util.Stack;
import jhilbert.scanners.Scanner;
import jhilbert.scanners.ScannerException;

/**
 * Basic implementation of the scanner interface.
 * This class implements methods to handle the putting back of tokens
 * as well as context manipulation.
 *
 * @param E token type.
 */
abstract class AbstractScanner<E> implements Scanner<E> {

	/**
	 * Stack for putting back tokens.
	 */
	private final Stack<E> tokenStack;

	/**
	 * Current scanner context.
	 */
	private final StringBuilder context;

	/**
	 * Creates a new <code>AbstractScanner</code> with empty
	 * context.
	 */
	protected AbstractScanner() {
		tokenStack = new Stack();
		context = new StringBuilder();
	}

	public void resetContext() {
		context.setLength(0);
	}

	/**
	 * Appends the specified character to the current context.
	 *
	 * @param c character to append.
	 */
	protected void appendToContext(final char c) {
		context.append(c);
	}

	/**
	 * Appends the specified character sequence to the current context.
	 *
	 * @param s character sequence to append.
	 */
	protected void appendToContext(final CharSequence s) {
		context.append(s);
	}

	public String getContextString() {
		return context.toString();
	}

	/**
	 * Obtains a new token.
	 * This method is called by this class's implementation of the
	 * {@link #getToken} method whenever all tokens which have been put
	 * back are exhausted.
	 * <p>
	 * This method must be implemented by subclasses.
	 *
	 * @return the new token, or <code>null</code> if there are no more
	 * 	tokens.
	 *
	 * @throws ScannerException for the same reasons as {@link #getToken}
	 * 	does.
	 *
	 * @see #getToken
	 */
	protected abstract E getNewToken() throws ScannerException;

	public final E getToken() throws ScannerException {
		if (tokenStack.empty())
			return getNewToken();
		return tokenStack.pop();
	}

	public final void putToken(final E token) {
		tokenStack.push(token);
	}

}
