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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InvalidClassException;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;

import jhilbert.commands.CommandException;
import jhilbert.commands.CommandFactory;

import jhilbert.data.DataException;
import jhilbert.data.DataFactory;
import jhilbert.data.Module;

import jhilbert.scanners.ScannerException;
import jhilbert.scanners.ScannerFactory;
import jhilbert.scanners.TokenFeed;

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
		final File interfaceFile = null;//new File(locator);
		// create library

                System.out.println("locator is" + locator);
if (true) return null;
		Module module;
		try {
			module = DataFactory.getInstance().createModule(locator, revision);
		} catch (DataException e) {
			final AssertionError err = new AssertionError("Bad revision number. This cannot happen");
			err.initCause(e);
			throw err;
		}
		try {
			final TokenFeed tokenFeed = ScannerFactory.getInstance()
				.createTokenFeed(new FileInputStream(interfaceFile));
			CommandFactory.getInstance().processCommands(module, tokenFeed);
		} catch (ScannerException e) {
			logger.error("Scanner error while scanning interface " + locator, e);
			logger.debug("Scanner context: " + e.getScanner().getContextString());
			throw new StorageException("Scanner error while scanning interface", e);
		} catch (CommandException e) {
			logger.error("Command failed to execute while loading interface " + locator, e);
			throw new StorageException("Command failed to execute while loading interface", e);
		} catch (FileNotFoundException e) {
			logger.warn("Unable to open library file for writing while creating library for interface " + locator, e);
		} catch (IOException e) {
			logger.warn("Unable to write library file while creating library for interface " + locator, e);
		}
		return module;
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
