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

import java.io.InputStream;

import jhilbert.data.Module;

/**
 * A factory class for creating {@link TokenScanner}s and {@link CommandScanner}s.
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
	 * Creates a new {@link TokenScanner} from the specified
	 * {@link java.io.InputStream}.
	 *
	 * @param in input stream to create the <code>TokenScanner</code> from.
	 *
	 * @return the new <code>TokenScanner</code>.
	 *
	 * @throws ScannerException if the scanner cannot be created.
	 */
	public abstract TokenScanner createTokenScanner(final InputStream in) throws ScannerException;

	/**
	 * Creates a new {@link CommandScanner} from the specified
	 * {@link InputStream} for the for the specified {@link Module}.
	 *
	 * @param in input stream.
	 * @param module data module.
	 *
	 * @throws ScannerException if the underlying token scanner cannot
	 * 	be set up.
	 */
	public abstract CommandScanner createCommandScanner(final InputStream in, final Module module) throws ScannerException;

}
