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

package jhilbert.storage.wiki;

import java.io.IOException;
import java.io.InputStream;

import jhilbert.commands.CommandException;
import jhilbert.commands.CommandFactory;
import jhilbert.data.DataException;
import jhilbert.data.DataFactory;
import jhilbert.data.Module;
import jhilbert.scanners.ScannerException;
import jhilbert.scanners.ScannerFactory;
import jhilbert.scanners.TokenFeed;
import jhilbert.scanners.WikiInputStream;
import jhilbert.storage.StorageException;

import org.apache.log4j.Logger;

/**
 * File based {@link Storage} implementation.
 */
public final class Storage extends jhilbert.storage.Storage {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(Storage.class);

	// default constructed
	
	public @Override boolean isVersioned() {
		return false;
	}

	protected @Override String getCanonicalName(final String locator) {
		return locator;
	}

	protected synchronized @Override Module retrieveModule(final String locator, final long revision)
	throws StorageException {
		// bottleneck alert: the file storage implementation is not really meant for multithreading use
		assert (locator != null): "Supplied locator is null";
		assert (!"".equals(locator)): "No storage for a proof module";
		if (revision != -1) {
			logger.error("File based storage does not support versioning");
			logger.debug("Supplied locator:        " + locator);
			logger.debug("Supplied version number: " + revision);
			throw new StorageException("File based storage does not support versioning");
		}
		Module interfaceModule;
		try {
			interfaceModule = DataFactory.getInstance().createInterface(locator, revision);
		} catch (DataException e) {
			final AssertionError err = new AssertionError("Bad revision number. This cannot happen");
			err.initCause(e);
			throw err;
		}
		try {
			String fileName = fileName(locator);
			logger.debug("file name is " + fileName);
			final InputStream interfaceFile = WikiInputStream.create(fileName);
			final TokenFeed tokenFeed = ScannerFactory.getInstance()
				.createTokenFeed(interfaceFile);
			CommandFactory.getInstance().processCommands(interfaceModule, tokenFeed);
		} catch (ScannerException e) {
			logger.error("Scanner error while scanning interface " + locator, e);
			logger.debug("Scanner context: " + e.getScanner().getContextString());
			throw new StorageException("Scanner error while scanning interface", e);
		} catch (CommandException e) {
			logger.error("Command failed to execute while loading interface " + locator, e);
			throw new StorageException("Command failed to execute while loading interface", e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return interfaceModule;
	}

	protected @Override void storeModule(final Module module, final String locator, long version) {
		throw new UnsupportedOperationException("Storing not supported in this implementation");
	}

	protected @Override void eraseModule(final String locator, final long version) {
		throw new UnsupportedOperationException("Erasing not supportted in this implementation");
	}

	protected @Override long getCurrentRevision(final String locator) {
		return -1;
	}

	/**
	 * Given the name of a file, jhilbertName, as it appears in an import
	 * or params statement (for example Interface:Some_file), return the
	 * filename as it is modified by mediawiki and levitation (for example,
	 * "Interface/S/o/m/Some file").
	 */
	public static String fileName(String jhilbertName) {
		String[] parts = jhilbertName.split(":");
		String namespace = parts[0];
		String underscoredName = parts[1];

		// Can't remember how mediawiki handles multibyte characters here, but
		// we can fix that later.
		CharSequence first = underscoredName.subSequence(0, 1);
		CharSequence second = underscoredName.subSequence(1, 2);
		CharSequence third = underscoredName.subSequence(2, 3);
		return namespace + "/" +
		  convertCharacter(first) + "/" +
		  convertCharacter(second) + "/" +
		  convertCharacter(third) + "/" +
		  underscoredName.replace("_", " ");
	}

	public static CharSequence convertCharacter(CharSequence character) {
		if ("_".equals(character)) {
			return ".20";
		}
		else {
			return character;
		}
	}

}
