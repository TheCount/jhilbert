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

package jhilbert.commands;

import jhilbert.exceptions.GeneralException;

/**
 * Execption occurring during {@link jhilbert.commands.Command} execution.
 *
 * @see jhilbert.commands.Command#execute()
 */
public final class VerifyException extends GeneralException {

	/**
	 * Creates a new VerifyException with specified detail message, context, and cause.
	 *
	 * @param message detail message.
	 * @param context context String.
	 * @param cause Throwable which caused this exception.
	 */
	public VerifyException(final String message, final String context, final Throwable cause) {
		super(message, context, cause);
	}

	/**
	 * Creates a new VerifyException with specified detail message and context.
	 *
	 * @param message detail message.
	 * @param context context String.
	 */
	public VerifyException(final String message, final String context) {
		super(message, context);
	}

}
