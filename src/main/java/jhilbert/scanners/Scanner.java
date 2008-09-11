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
 * Scanner interface.
 * A Scanner provides a stream of tokens whose type is of the specified type
 * parameter.
 * <p>
 * Classes implementing this interface should provide a constructor which
 * is provided with some kind of input source (e.g.
 * {@link java.io.InputStream}) to create the token stream.
 * <p>
 * Scanners also provide a method of keeping track of <em>context</em>.
 * A context is simply a string representing already consumed input. It can be
 * used to create user-friendly error messages in case a
 * {@link ScannerException} occurs.
 *
 * @param E token type.
 */
public interface Scanner<E> {

	/**
	 * Resets the context of this scanner.
	 * Clears the context. That is, a call to {@link #getContextString}
	 * immediately following a call to this method will return an empty
	 * string.
	 */
	public void resetContext();

	/**
	 * Returns the current context of this scanner.
	 *
	 * @return a String representation of the current context.
	 */
	public String getContextString();

	/**
	 * Obtains the next token.
	 *
	 * @return the token, or <code>null</code> if there are no more tokens.
	 * 
	 * @throws ScannerException if this scanner is unable to produce a
	 * 	token due to either an input problem or because of a syntax
	 * 	error in the input source.
	 *
	 * @see #putToken
	 */
	public E getToken() throws ScannerException;

	/**
	 * Put a token back.
	 * This method provides a means of &quot;unwinding&quot; the Scanner.
	 * The tokens put back are returned by {@link #getToken()} on a first
	 * in, last out basis.
	 * <p>
	 * The token to be put back must have been obtained by the thus far
	 * last call to to {@link #getToken}, or, if more than token is put back
	 * between calls to {@link #getToken}, the tokens must be provided in
	 * proper reverse order with no omissions. Otherwise the scanner may
	 * end up in an undefined state.
	 *
	 * @param token token to be put back.
	 *
	 * @see #getToken
	 */
	public void putToken(E token);

}
