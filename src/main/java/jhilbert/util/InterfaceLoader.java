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

package jhilbert.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import jhilbert.commands.Command;
import jhilbert.data.InterfaceData;
import jhilbert.exceptions.DataException;
import jhilbert.exceptions.InputException;
import jhilbert.exceptions.ScannerException;
import jhilbert.exceptions.UnknownFormatException;
import jhilbert.exceptions.VerifyException;
import jhilbert.util.CommandScanner;
import jhilbert.util.FileInputSource;
import org.apache.log4j.Logger;

/**
 * Class with static methods to load an interface.
 */
public class InterfaceLoader {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(InterfaceLoader.class);

	/**
	 * File suffixes.
	 */
	private static final String INTERFACE_SUFFIX = ".jhi";
	private static final String LIBRARY_SUFFIX   = ".jhl";

	/**
	 * Loads an interface. An interface library is created if it doesn't exist.
	 *
	 * @param locator interface locator (must not be <code>null</code>).
	 *
	 * @throws InputException if the interface cannot be loaded.
	 */
	public static InterfaceData loadInterface(final String locator) throws InputException {
		assert (locator != null): "Specified interface locator is null";
		final File interfaceFile = new File(locator + INTERFACE_SUFFIX);
		final File libraryFile = new File(locator + LIBRARY_SUFFIX);
		// load from library if interface wasn't modified
		if (libraryFile.lastModified() > interfaceFile.lastModified()) {
			try {
				return new InterfaceData(new FileInputStream(libraryFile));
			} catch (FileNotFoundException e) { // This should not happen
				logger.info("Compiling library for interface " + locator, e);
			} catch (UnknownFormatException e) {
				logger.warn("Obsolete library format. Recreating library.", e);
			} catch (DataException e) {
				logger.warn("Unable to load library. Attempt to recreate library.", e);
			}
		} else
			logger.info("No recent library for " + locator + " detected, creating...");
		// create library
		final InterfaceData result = new InterfaceData();
		try {
			final CommandScanner cs = new CommandScanner(new FileInputSource(interfaceFile), result, Command.INTERFACE_COMMANDS);
			for (Command c = cs.getToken(); c != null; c = cs.getToken())
				c.execute();
			libraryFile.delete();
			result.store(new FileOutputStream(libraryFile));
			logger.info("Library for interface " + locator + " created.");
		} catch (InputException e) {
			logger.error("Unable to open interface file for reading while loading interface " + locator, e);
			throw new InputException("Unable to read interface file", locator, e);
		} catch (ScannerException e) {
			logger.error("Error scanning interface file while loading interface " + locator, e);
			throw new InputException("Error scanning interface file", locator, e);
		} catch (VerifyException e) {
			logger.error("Interface file failed to verify while loading interface " + locator, e);
			throw new InputException("Verification error in interface file", locator, e);
		} catch (FileNotFoundException e) {
			logger.warn("Unable to open library file for writing while creating library for interface " + locator, e);
		} catch (DataException e) {
			logger.warn("Unable to store library for interface " + locator, e);
		}
		return result;
	}

}
