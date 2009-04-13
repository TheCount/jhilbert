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

package jhilbert.verifier;

import jhilbert.JHilbertException;

/**
 * Thrown when a proof fails to verify.
 */
public class VerifyException extends JHilbertException {

	/**
	 * Creates a new <code>VerifyException</code> with the specified detail
	 * message.
	 *
	 * @param message detail message.
	 */
	public VerifyException(final String message) {
		this(message, null);
	}

	/**
	 * Creates a new <code>VerifyException</code> with the specified detail
	 * message and cause.
	 *
	 * @param message detail message.
	 * @param cause the cause.
	 */
	public VerifyException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
