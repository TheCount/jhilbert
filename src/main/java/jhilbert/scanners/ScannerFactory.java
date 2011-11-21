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

package jhilbert.scanners;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;

/**
 * A factory class for creating {@link TokenFeed}s.
 */
public abstract class ScannerFactory {

	/**
	 * Instance.
	 */
	private static final ScannerFactory instance = new jhilbert.scanners.impl.ScannerFactory();

	/**
	 * Returns a <code>ScannerFactory</code> instance.
	 *
	 * @return a <code>ScannerFactory</code> instance.
	 */
	public static ScannerFactory getInstance() {
		return instance;
	}

	/**
	 * Creates a new {@link TokenFeed} from the specified
	 * {@link java.io.InputStream}.
	 *
	 * @param in input stream to create the <code>TokenFeed</code> from.
	 *
	 * @return the new <code>TokenFeed</code>.
	 *
	 * @throws ScannerException if the scanner cannot be created.
	 */
	public abstract TokenFeed createTokenFeed(InputStream in) throws ScannerException;

	/**
	 * Creates a new {@link TokenFeed} from the specified input and
	 * output buffers (for server operation).
	 * FIXME: not needed now that c/s conversation is binary
	 *
	 * @param in input reader.
	 * @param out output writer.
	 */
	public @Deprecated abstract TokenFeed createTokenFeed(BufferedReader in, BufferedWriter out);

	/**
	 * Creates a new {@link TokenFeed} from the specified input and output
	 * streams (for server operation).
	 *
	 * @param in input stream.
	 * @param out buffered output stream.
	 */
	public abstract TokenFeed createTokenFeed(InputStream in, BufferedOutputStream out);

}
