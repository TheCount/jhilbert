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

import java.io.FileInputStream;
import java.io.IOException;

import java.util.HashMap;

import jhilbert.commands.Command;

import jhilbert.data.DataFactory;
import jhilbert.data.Module;

import jhilbert.scanners.CommandScanner;
import jhilbert.scanners.ScannerFactory;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Main class.
 */
public final class Main {

	/**
	 * Version.
	 */
	public static final int VERSION = 4;

	/**
	 * Program entry point.
	 *
	 * @param args command line arguments.
	 *
	 * @throws Exception if anything out of the ordinary happens.
	 */
	public static void main(String... args) throws Exception {
		BasicConfigurator.configure();
		final Logger logger = Logger.getRootLogger();
		boolean startDaemon = false;
		try {
			logger.setLevel(Level.INFO);
			String inputFileName = null;
			for (String arg: args) {
				logger.info("Command line argument: " + arg);
				if (arg.startsWith("-l"))
					logger.setLevel(Level.toLevel(arg.substring(2)));
				else if (arg.equals("-d"))
					startDaemon = true;
				else if (arg.equals("--license"))
					showLicense();
				else
					inputFileName = arg;
			}
			if (startDaemon == true)
				startDaemon();
			if (inputFileName == null) {
				printUsage();
				System.exit(1);
			}
			logger.info("Processing file " + inputFileName);
			final Module mainModule = DataFactory.getInstance().createModule("");
			final CommandScanner cs = ScannerFactory.getInstance()
				.createCommandScanner(new FileInputStream(inputFileName), mainModule);
			for (Command c = cs.getToken(); c != null; c = cs.getToken()) {
				if (logger.isDebugEnabled())
					logger.debug("Current command: " + cs.getContextString());
				c.execute();
				cs.resetContext();
			}
			logger.info("File processes successfully");
			return;
		} catch (JHilbertException e) {
			logger.fatal("Exiting due to unrecoverable error", e);
			System.exit(1);
		} catch (Exception e) {
			logger.fatal("Caught unexpected exception");
			throw e;
		}
	}

	/**
	 * Prints command line usage help.
	 */
	private static void printUsage() {
		System.out.println("Usage: java -jar jhilbert.jar [ OPTIONS ] file.jh");
		System.out.println("Runs the JHilbert proof verifier.");
		System.out.println();
		System.out.println("Available options:");
		System.out.println();
		System.out.println("  -lLOGLEVEL  Sets the least severe log level which is still reported.");
		System.out.println("              Available loglevels are:");
		System.out.println();
		System.out.println("                FATAL");
		System.out.println("                ERROR");
		System.out.println("                WARN");
		System.out.println("                INFO");
		System.out.println("                DEBUG");
		System.out.println("                TRACE");
		System.out.println();
		System.out.println("              If the -l option is not specified, the log level defaults");
		System.out.println("              to INFO. If the log level is specified, but not from the above");
		System.out.println("              list, the log level is set to TRACE.");
		System.out.println();
		System.out.println("  -d          Start in daemon mode. Creates a PHP/Java bridge on port 8080.");
		System.out.println();
		System.out.println("  --license   Displays license information and exits.");
		System.out.println();
		System.out.println("Please report bugs to <Graf." + "Zahl" + '@' + "gmx." + "net>.");
	}

	/**
	 * Displays the license (short form).
	 */
	private static void showLicense() {
		System.out.println("JHilbert version " + VERSION);
		System.out.println();
		System.out.println("    JHilbert is a verifier for collaborative theorem proving.");
		System.out.println("    Copyright © 2008 Alexander Klauer");
		System.out.println();
		System.out.println("    This program is free software: you can redistribute it and/or modify");
		System.out.println("    it under the terms of the GNU General Public License as published by");
		System.out.println("    the Free Software Foundation, either version 3 of the License, or");
		System.out.println("    (at your option) any later version.");
		System.out.println();
		System.out.println("    This program is distributed in the hope that it will be useful,");
		System.out.println("    but WITHOUT ANY WARRANTY; without even the implied warranty of");
		System.out.println("    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the");
		System.out.println("    GNU General Public License for more details.");
		System.out.println();
		System.out.println("    You should have received a copy of the GNU General Public License");
		System.out.println("    along with this program.  If not, see <http://www.gnu.org/licenses/>.");
		System.exit(0);
	}

	/**
	 * Starts a PHP/Java bridge daemon.
	 */
	private static void startDaemon() throws IOException { // FIXME: catch & log IOException!
		final php.java.bridge.JavaBridgeRunner runner = php.java.bridge.JavaBridgeRunner.getRequiredInstance("INET_LOCAL:8080");
		while (true) {
			try {
				Thread.sleep(Long.MAX_VALUE);
			} catch (InterruptedException e) {
				// No, I don't wanna!
			}
		}
	}

}
