package jhilbert.util;

import java.io.InputStream;
import java.util.Stack;
import jhilbert.exceptions.InputException;
import jhilbert.exceptions.ScannerException;
import jhilbert.util.InputSource;

/**
 * Scanner class.
 * Consumes characters from an input source and returns tokens of specified type.
 *
 * @param E token type
 */
public abstract class Scanner<E> {

	/**
	 * stack for tokens put back.
	 */
	private final Stack<E> tokenStack;

	/**
	 * context.
	 */
	private StringBuilder context;

	/**
	 * Creates a new scanner with empty token stack and empty context.
	 */
	protected Scanner() {
		tokenStack = new Stack();
		context = new StringBuilder();
	}

	/**
	 * Resets the context of this scanner.
	 * Clears the context. That is, a call to {@link #getContextString()} immediately following a call to this method
	 * will return an empty string.
	 */
	public void resetContext() {
		context.delete(0, context.length());
	}

	/**
	 * Sets the context of this scanner.
	 *
	 * @param context StringBuilder context.
	 *
	 * @throws NullPointerException if context is <code>null</code>.
	 */
	protected void setContext(final StringBuilder context) {
		if (context == null)
			throw new NullPointerException("Null context supplied.");
		this.context = context;
	}

	/**
	 * Returns the context of this Scanner.
	 *
	 * @return scanner context StringBuilder.
	 */
	protected StringBuilder getContext() {
		return context;
	}

	/**
	 * Returns the context of this scanner as a String.
	 * The returned string is immutable.
	 *
	 * @return an immutable String representation of the current context.
	 */
	public String getContextString() {
		return context.toString();
	}

	/**
	 * Creates a new token.
	 * Subclasses must override this method. It is called by {@link #getToken()} whenever a new token is required.
	 *
	 * @return the new token, or <code>null</code> if there are no more tokens.
	 *
	 * @throws ScannerException if there is a problem creating the new token.
	 */
	protected abstract E getNewToken() throws ScannerException;

	/**
	 * Obtains the next token.
	 *
	 * @return the token, or <code>null</code> if there are no more tokens.
	 * 	This promise may be broken if {@link #putToken()} is called with tokens
	 * 	not returned by this method, or tokens returned by this method, but not
	 * 	in the correct (reverse) order.
	 *
	 * @throws ScannerException if there is a problem returning the next token.
	 *
	 * @see #putToken()
	 */
	public final E getToken() throws ScannerException {
		if (tokenStack.empty())
			return getNewToken();
		return tokenStack.pop();
	}

	/**
	 * Put a token back.
	 * This method provides a means of &quot;unwinding&quot; the Scanner.
	 * The tokens put back are returned by {@link #getToken()} on a first in, last out basis.
	 * <p>
	 * This method accepts <code>null</code> tokens. However, a <code>null</code> token should only be put back
	 * if it was obtained via the most recent call to {@link #getToken()} on this object.
	 *
	 * @param token token to be put back.
	 *
	 * @see #getToken()
	 */
	public final void putToken(final E token) {
		tokenStack.push(token);
	}

}
