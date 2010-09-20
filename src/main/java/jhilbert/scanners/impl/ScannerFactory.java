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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;

import jhilbert.data.Module;

import jhilbert.scanners.ScannerException;

/**
 * Scanner factory for this implementation.
 */
public final class ScannerFactory extends jhilbert.scanners.ScannerFactory {

	// instances are default-constructed.

	public @Override StreamTokenFeed createTokenFeed(final InputStream in) throws ScannerException {
		assert (in != null): "Supplied input stream is null";
		return new StreamTokenFeed(in);
	}

	public @Override @Deprecated IOTokenFeed createTokenFeed(final BufferedReader in, final BufferedWriter out) {
		assert (in != null): "Supplied input reader is null";
		assert (out != null): "Supplied output writer is null";
		return new IOTokenFeed(in, out);
	}

	public @Override MediaWikiTokenFeed createTokenFeed(final InputStream in, final BufferedOutputStream out) {
		assert (in != null): "Supplied input stream is null";
		assert (out != null): "Supplied output stream is null";
		return new MediaWikiTokenFeed(in, out);
	}

}
