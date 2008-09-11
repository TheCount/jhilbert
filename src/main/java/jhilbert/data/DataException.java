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

package jhilbert.data;

import jhilbert.JHilbertException;

/**
 * An {@link java.lang.Exception} thrown when an error in the context of data
 * handling occurs.
 */
public class DataException extends JHilbertException {

	/**
	 * Constructs a new <code>DataException</code> with the specified
	 * detail message.
	 *
	 * @param message the detail message.
	 */
	public DataException(final String message) {
		this(message, null);
	}

	/**
	 * Constructs a new <code>DataException</code> with the specified
	 * detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause (A <code>null</code> value is permitted, and
	 * 	indicates that the cause is nonexistent or unknown.)
	 */
	public DataException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
