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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import jhilbert.scanners.ScannerException;

import org.apache.log4j.Logger;

/**
 * Scanner for characters.
 * This class is specially tuned for scanning LISP-like input, as LISP
 * comments (initiated by hashmarks) are stripped, and the {@link Char} class
 * automatically classifies LISP characters using {@link Char.Class}.
 */
class CharScanner extends AbstractScanner<Char> {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(CharScanner.class);

	/**
	 * Input encoding.
	 */
	private static final String ENCODING = "UTF-8";

	/**
	 * Reader used as input source.
	 */
	private final Reader reader;

	/**
	 * Creates a new <code>CharScanner</code> from the specified
	 * {@link java.io.InputStream}.
	 *
	 * @param in input stream.
	 *
	 * @throws ScannerException if the {@link #ENCODING} is not supported.
	 */
	CharScanner(final InputStream in) throws ScannerException {
		assert (in != null): "Supplied input stream is null";
		try {
			reader = new InputStreamReader(in, ENCODING);
		} catch (UnsupportedEncodingException e) {
			logger.error("Encoding " + ENCODING + " not supported while trying to create character scanner "
				+ " from input stream " + in, e);
			throw new ScannerException("Encoding not supported", this, e);
		}
	}

	protected @Override Char getNewToken() throws ScannerException {
		try {
			Char c = new Char(reader.read());
			if (c.getCharClass() == Char.Class.HASHMARK) // ignore comment
				do {
					 c = new Char(reader.read());
				} while (c.getCharClass() != Char.Class.NEWLINE
					&& c.getCharClass() != Char.Class.EOF);
			if (c.getCharClass() == Char.Class.EOF)
				return null;
			if (c.getCharClass() == Char.Class.INVALID) {
				logger.error("Invalid character with codepoint " + c.getCodepoint() + " encountered.");
				throw new ScannerException("Invalid character", this);
			}
			appendToContext((char) c.getCodepoint());
			return c;
		} catch (IOException e) {
			logger.error("I/O error while trying to create character token", e);
			throw new ScannerException("I/O error: " + e.getMessage(), this, e);
		}
	}

}
