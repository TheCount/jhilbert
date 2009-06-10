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

package jhilbert.scanners;

import jhilbert.JHilbertException;

/**
 * Signals an error with a {@link Scanner}.
 * This may be due to a propagated I/O error or due to a syntax error in the
 * input.
 */
public class ScannerException extends JHilbertException {

	/**
	 * Scanner which created the exception.
	 */
	private final Scanner scanner;

	/**
	 * Creates a new <code>ScannerException</code> with the specified
	 * detail message. This constructor should only be used by a
	 * {@link Scanner}, which the constructor is provided with.
	 *
	 * @param message detail message.
	 * @param scanner the scanner which created the exception.
	 */
	public ScannerException(final String message, final Scanner scanner) {
		this(message, scanner, null);
	}

	/**
	 * Creates a new <code>ScannerException</code> with the specified
	 * detail message and cause.
	 * This constructor should only be used by a {@link Scanner}, which
	 * the constructor is provided with.
	 *
	 * @param message detail message.
	 * @param scanner the scanner which created the exception.
	 * @param cause the cause. (A <code>null</code> value is permitted, and
	 * 	indicates that the cause is nonexistent or unknown.)
	 */
	public ScannerException(final String message, final Scanner scanner, final Throwable cause) {
		super(message, cause);
		this.scanner = scanner;
	}

	/**
	 * Obtains the scanner which caused this exception.
	 *
	 * @return scanner which caused this exception.
	 */
	public final Scanner getScanner() {
		return scanner;
	}

}
