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

package jhilbert.scanners.impl;

import java.io.InputStream;

import jhilbert.data.Module;

import jhilbert.scanners.ScannerException;

/**
 * Scanner factory for this implementation.
 */
public final class ScannerFactory extends jhilbert.scanners.ScannerFactory {

	// instances are default-constructed.

	public @Override TokenScannerImpl createTokenScanner(final InputStream in) throws ScannerException {
		assert (in != null): "Supplied input stream is null";
		return new TokenScannerImpl(in);
	}

	public @Override CommandScannerImpl createCommandScanner(final InputStream in, final Module module) throws ScannerException {
		assert (in != null): "Supplied input stream is null";
		assert (module != null): "Supplied module is null";
		return new CommandScannerImpl(in, module);
	}

}
