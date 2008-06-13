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

package jhilbert;

import jhilbert.commands.Command;
import jhilbert.data.DataFactory;
import jhilbert.data.ModuleData;
import jhilbert.exceptions.GeneralException;
import jhilbert.util.FileInputSource;
import jhilbert.util.CommandScanner;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Main class.
 */
public class Main
{

	/**
	 * Program entry point.
	 *
	 * @param args command line arguments.
	 *
	 * @throws Exception if anything out of the ordinary happens.
	 */
	public static void main(String[] args) throws Exception {
		BasicConfigurator.configure();
		final Logger logger = Logger.getRootLogger();
		try {
			logger.setLevel(Level.INFO);
			String inputFileName = null;
			for (String arg: args) {
				logger.info("Command line argument: " + arg);
				if (arg.startsWith("-l"))
					logger.setLevel(Level.toLevel(arg.substring(2)));
				else
					inputFileName = arg;
			}
			if (inputFileName == null) {
				logger.fatal("No input file name specified.");
				System.exit(1);
			}
			logger.info("Scanning file " + inputFileName);
			ModuleData md = DataFactory.getInstance().createModuleData();
			CommandScanner cs = new CommandScanner(new FileInputSource(inputFileName), md, Command.MODULE_COMMANDS);
			for (Command c = cs.getToken(); c != null; c = cs.getToken()) {
				if (logger.isDebugEnabled())
					logger.debug("Current context: " + cs.getContextString());
				c.execute();
				cs.resetContext();
			}
		} catch (GeneralException e) {
			logger.fatal("Exiting due to unrecoverable error:", e);
			System.exit(1);
		} catch (Exception e) {
			logger.fatal("Caught unexpected exception:");
			throw e;
		}
	}

}
