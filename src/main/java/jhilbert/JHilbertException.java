/*
    JHilbert, a verifier for collaborative theorem proving

    Copyright © 2008, 2009, 2011 The JHilbert Authors
      See the AUTHORS file for the list of JHilbert authors.
      See the commit logs ("git log") for a list of individual contributions.

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

package jhilbert;

/**
 * Instances of the class <code>JHilbertException</code> are
 * {@link java.lang.Exception}s created by JHilbert, but otherwise general.
 */
public class JHilbertException extends Exception {

	/**
	 * Constructs a new <code>JhilbertException</code> with the specified
	 * detail message.
	 *
	 * @param message the detail message.
	 */
	public JHilbertException(final String message) {
		this(message, null);
	}

	/**
	 * Constructs a new <code>JhilbertException</code> with the specified
	 * detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause the cause. (A <code>null</code> value is permitted, and
	 * 	indicates that the cause is nonexistent or unknown.)
	 */
	public JHilbertException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public boolean messageMatches(String expectedError) {
		if (expectedError.equals(getMessage())) {
			return true;
		}
		else if (getCause() instanceof JHilbertException) {
			return ((JHilbertException) getCause()).messageMatches(expectedError);
		}
		else if (null != getCause()) {
			return expectedError.equals(getCause().getMessage());
		}
		else {
			return false;
		}
	}

}
