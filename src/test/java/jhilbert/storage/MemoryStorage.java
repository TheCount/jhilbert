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

package jhilbert.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import jhilbert.commands.CommandException;
import jhilbert.commands.CommandFactory;
import jhilbert.data.DataException;
import jhilbert.data.DataFactory;
import jhilbert.data.Module;
import jhilbert.scanners.ScannerException;
import jhilbert.scanners.ScannerFactory;
import jhilbert.scanners.TokenFeed;

import org.apache.log4j.Logger;

/**
 * In memory {@link Storage} implementation. This is for tests, so they can
 * set up interfaces and import them without having to write files.
 */
public final class MemoryStorage extends jhilbert.storage.Storage {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(Storage.class);

	// default constructed

	Map<String, String> storedInterfaces = new HashMap<String, String>();

	public @Override boolean isVersioned() {
		return false;
	}

	protected @Override String getCanonicalName(final String locator) {
		return locator;
	}

	public void store(final String locator, final String contents) {
		storedInterfaces.put(locator, contents);
	}

	protected synchronized @Override Module retrieveModule(final String locator, final long revision)
	throws StorageException {
		assert (locator != null): "Supplied locator is null";
		assert (!"".equals(locator)): "No storage for a proof module";
		if (revision != -1) {
			logger.error("Memory based storage does not support versioning");
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
			final InputStream interfaceFile = new ByteArrayInputStream(
				storedInterfaces.get(locator).getBytes("UTF-8"));
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

}
