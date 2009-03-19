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
 * Provides a token feed. Similar to a {@link Scanner}, a <code>Feed</code>
 * accepts input tokens and creates output tokens from them. However, the
 * input method is not determined by a constructor but directly by the input
 * token type of this interface. This allows for more extraneous control.
 *
 * @param I input token type.
 * @param O output token type.
 */
public interface Feed<I, O> extends Scanner<O> {

	/**
	 * Feeds on the specified input token.
	 *
	 * @param input input token.
	 *
	 * @throws ScannerException if this <code>Feed</code> cannot proceed
	 * 	due to a syntax error.
	 *
	 * @see #finish
	 * @see #getToken
	 */
	public void feed(I input) throws ScannerException;

	/**
	 * Finishes feeding.
	 * Once feeding has been finished, further calls to {@link #feed}
	 * or to <code>finish()</code> will result in undefined behaviour.
	 *
	 * @throws ScannerException if finishing feeding at this point is
	 * 	a syntax error.
	 *
	 * @see #getToken
	 */
	public void finish() throws ScannerException;

	/**
	 * Obtains the next output token.
	 *
	 * @return the token, or <code>null</code> if there are currently no
	 * 	more tokens. If <code>null</code> is returned, you must call
	 * 	{@link #feed} in order to obtain more tokens, or possibly
	 * 	{@link #finish} in order to obtain the last token.
	 *
	 * @see #feed
	 * @see #finish
	 */
	public O getToken();

	/**
	 * Checks whether a token is available.
	 *
	 * @return <code>true</code> if the next call to {@link #getToken}
	 * 	will not return <code>null</code>, <code>false</code>
	 * 	otherwise.
	 *
	 * @see #getToken
	 */
	public boolean hasToken();

}
