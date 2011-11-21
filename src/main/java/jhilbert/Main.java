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

package jhilbert;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import jhilbert.commands.CommandException;
import jhilbert.commands.CommandFactory;
import jhilbert.data.DataFactory;
import jhilbert.data.Module;
import jhilbert.scanners.ScannerException;
import jhilbert.scanners.ScannerFactory;
import jhilbert.scanners.TokenFeed;
import jhilbert.scanners.WikiInputStream;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * Main class.
 */
public final class Main {

	/**
	 * Version.
	 */
	public static final int VERSION = 9;

	/**
	 * Logger.
	 */
	private static final Logger logger;

	/**
	 * Daemon port.
	 */
	private static final int DAEMON_PORT = 3141;

	/**
	 * MediaWiki API default location.
	 */
	private static final String MEDIAWIKI_API = "http://127.0.0.1:80/w/api.php";

	/**
	 * Default location for hashstore.
	 */
	private static final String HASHSTORE_DEFAULT_PATH = "/var/local/lib/jhilbert/hashstore";

	/**
	 * Hashstore location.
	 */
	private static String hashstorePath;

	/**
	 * Is DAEMON?
	 */
	private static boolean isDaemon;

	/**
	 * Are we reading wiki-format pages from files (--wiki)?
	 */
	private static boolean isWiki;

	/**
	 * Static initialiser.
	 *
	 * Initialises the logger.
	 */
	static {
		BasicConfigurator.configure(new ConsoleAppender(
					new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} [%t] %-5p %c - %m%n")));
		logger = Logger.getRootLogger();
		logger.setLevel(Level.INFO);
	}

	/**
	 * Program entry point.
	 *
	 * @param args command line arguments.
	 *
	 * @throws Exception if anything out of the ordinary happens.
	 */
	public static void main(String... args) throws Exception {
		isDaemon = false;
		isWiki = false;
		hashstorePath = null;
		try {
			String inputFileName = null;
			for (String arg: args) {
				logger.info("Command line argument: " + arg);
				if (arg.startsWith("-l")) {
					logger.setLevel(Level.toLevel(arg.substring(2)));
				} else if (arg.startsWith("-p")) {
					if (arg.length() > 2) {
						hashstorePath = arg.substring(2);
					} else {
						hashstorePath = HASHSTORE_DEFAULT_PATH;
					}
				} else if (arg.equals("-d")) {
					isDaemon = true;
				} else if (arg.equals("--wiki")) {
					isWiki = true;
				} else if (arg.equals("--license")) {
					showLicense();
				} else {
					inputFileName = arg;
				}
			}
			if (isDaemon == true) {
				startDaemon();
				return;
			}
			if (inputFileName == null) {
				printUsage();
				System.exit(1);
			}
			if (isWiki) {
				processWikiFile(inputFileName);
			}
			else {
				processProofModule(inputFileName);
			}
			return;
		} catch (JHilbertException e) {
			logger.fatal("Exiting due to unrecoverable error", e);
			System.exit(1);
		} catch (Exception e) {
			logger.fatal("Caught unexpected exception");
			throw e;
		}
	}

	private static void processWikiFile(String inputFileName)
	  throws IOException, ScannerException, CommandException {
		if (isInterface(inputFileName)) {
			logger.info("Processing interface " + inputFileName);

			final Module mainInterface = DataFactory.getInstance().createModule(inputFileName);
			final TokenFeed tokenFeed = ScannerFactory
				.getInstance().createTokenFeed(WikiInputStream.create(inputFileName));
			CommandFactory.getInstance().processCommands(mainInterface, tokenFeed);
			logger.info("File processed successfully");
		}
		else if (isProofModule(inputFileName)) {
			logger.info("Processing proof module " + inputFileName);

			final Module mainModule = DataFactory.getInstance().createModule("");
			final TokenFeed tokenFeed = ScannerFactory
				.getInstance().createTokenFeed(WikiInputStream.create(inputFileName));
			CommandFactory.getInstance().processCommands(mainModule, tokenFeed);
			logger.info("File processed successfully");
		}
		else {
			logger.fatal("Not sure whether file is an interface or a proof module");
			logger.fatal("File was " + inputFileName);
		}
	}

	public static boolean isProofModule(String fileName) {
		return fileName.contains("Main/") ||
		  fileName.contains("User module/");
	}

	public static boolean isInterface(String fileName) {
		return fileName.contains("Interface/") ||
		  fileName.contains("User interface/");
	}

	private static void processProofModule(String inputFileName)
			throws ScannerException, FileNotFoundException, CommandException {
		logger.info("Processing file " + inputFileName);
		final Module mainModule = DataFactory.getInstance().createModule("");
		final TokenFeed tokenFeed = ScannerFactory
			.getInstance().createTokenFeed(new FileInputStream(inputFileName));
		CommandFactory.getInstance().processCommands(mainModule, tokenFeed);
		logger.info("File processed successfully");
	}

	/**
	 * Prints command line usage help.
	 */
	private static void printUsage() {
		System.out.println("Usage: java -jar jhilbert.jar [ OPTIONS ] [ file.jh ]");
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
		System.out.println("  -d          Start in daemon mode. Creates a JHilbert daemon on port " + DAEMON_PORT);
		System.out.println();
		System.out.println("  --wiki      Operate on wiki-formatted pages stored locally in files");
		System.out.println();
		System.out.println("  -pPATH      Uses hashstore storage instead of file storage. Useful in daemon");
		System.out.println("              mode. The PATH is the base directory used for storage. If PATH is");
		System.out.println("              not specified, it defaults to " + HASHSTORE_DEFAULT_PATH);
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
		System.out.println("    Copyright © 2008, 2009 Alexander Klauer");
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
	 * Starts a JHilbert daemon.
	 */
	private static void startDaemon() throws JHilbertException {
		final byte[] localHost = { 127, 0, 0, 1 };
		int transactionCounter = 0;
		try {
			final ServerSocket listener = new ServerSocket(DAEMON_PORT, 50, InetAddress.getByAddress(localHost));
			for (;;) {
				final Socket conn = listener.accept();
				final Server thread = new Server("JHilbert transaction " + ++transactionCounter, conn);
				thread.start();
			}
		} catch (UnknownHostException e) {
			logger.error("No localhost. Is your networking configured correctly?");
			throw new JHilbertException("No localhost", e);
		} catch (IOException e) {
			logger.error("Unable to create socket: " + e.getMessage());
			throw new JHilbertException("Unable to create socket", e);
		}
	}

	/**
	 * Retrieves the hashstore path.
	 *
	 * @return the hashstore path.
	 */
	public static String getHashstorePath() {
		return hashstorePath;
	}

	/**
	 * Is JHilbert being used as a daemon?
	 *
	 * @return <code>true</code> if JHilbert is being used as a daemin,
	 * 	<code>false</code> otherwise.
	 */
	public static boolean isDaemon() {
		return isDaemon;
	}

	public static boolean isWiki() {
		return isWiki;
	}

	/**
	 * Retrieves the MediaWiki API location.
	 */
	public static String getMediaWikiApi() {
		return MEDIAWIKI_API; // FIXME: Make this configurable
	}

}
