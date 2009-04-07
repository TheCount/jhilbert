/*
    JHilbert, a verifier for collaborative theorem proving
    Copyright © 2009 Alexander Klauer

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

import jhilbert.commands.CommandException;
import jhilbert.commands.CommandFactory;

import jhilbert.data.DataException;
import jhilbert.data.DataFactory;
import jhilbert.data.Module;

import jhilbert.scanners.ScannerFactory;
import jhilbert.scanners.Token;
import jhilbert.scanners.TokenFeed;

import jhilbert.storage.Storage;
import jhilbert.storage.StorageException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import java.net.Socket;
import java.net.SocketException;

import org.apache.log4j.Logger;

/**
 * JHilbert server thread.
 * This class contains only the outer shell of the server implementation.
 * Module and Interface conversation is handled by an appropriate feed.
 */
public class Server extends Thread {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(Server.class);

	/**
	 * Socket to talk with.
	 */
	private final Socket socket;

	/**
	 * Encoding.
	 */
	private static final String ENCODING = "UTF-8";

	/**
	 * Default socket timeout, in milliseconds.
	 */
	private static final int TIMEOUT = 5000;

	/**
	 * Server welcome message.
	 */
	private static final String WELCOME_MSG = "201 JHilbert " + Main.VERSION + " ready\r\n";

	/**
	 * Server goodbye message.
	 */
	private static final String GOODBYE_MSG = "202 Goodbye\r\n";

	/**
	 * Module OK message.
	 */
	private static final String COMPLETE_MSG = "203 Module OK\r\n";

	/**
	 * Module stored message.
	 */
	private static final String STORED_MSG = "204 Interface module stored\r\n";

	/**
	 * Start module message.
	 */
	private static final String MODULE_MSG = "301 Start sending module tokens, finish with FINI\r\n";

	/**
	 * Start interface message.
	 */
	private static final String INTERFACE_MSG = "302 Start sending interface tokens, finish with FINI\r\n";

	/**
	 * Unknown command message.
	 */
	private static final String UNKNOWN_MSG = "401 Unknown command\r\n";

	/**
	 * Bad interface name message.
	 */
	private static final String BAD_IFACE_MSG = "402 Bad interface name\r\n";

	/**
	 * No module to store.
	 */
	private static final String NO_MODULE_MSG = "403 No module to store\r\n";

	/**
	 * Bad module.
	 */
	private static final String BAD_MODULE_MSG = "404 Only interface modules can be stored\r\n";

	/**
	 * Unable to save module message.
	 */
	private static final String SAVE_FAILED_MSG = "501 Storing the module failed\r\n";

	/**
	 * Module command.
	 */
	private static final String MOD_CMD = "MODL";

	/**
	 * Interface command.
	 */
	private static final String IFACE_CMD = "IFCE";

	/**
	 * Store command.
	 */
	private static final String STORE_CMD = "STOR";

	/**
	 * Quit command.
	 */
	private static final String QUIT_CMD = "QUIT";

	/**
	 * Creates a new server thread object on the specified
	 * {@link Socket}.
	 *
	 * @param name thread name.
	 * @param socket socket to talk with.
	 *
	 * @throws SocketException if a socket error occurs.
	 */
	public Server(final String name, final Socket socket) throws SocketException {
		super(name);
		this.socket = socket;
		socket.setSoTimeout(TIMEOUT);
		socket.setTcpNoDelay(true);
	}

	/**
	 * Runs the JHilbert server thread.
	 */
	public @Override void run() {
		BufferedWriter out = null;
		Module module = null;
		String param = null;
		try {
			final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(),
						ENCODING));
			out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), ENCODING));
			// send welcome
			out.write(WELCOME_MSG, 0, WELCOME_MSG.length());
			out.flush();
			// execute commands
			for(;;) {
				final String command = in.readLine();
				if (command == null) {
					logger.warn("EOF from client on port " + socket.getPort());
					return;
				}
				if ("".equals(command))
					continue;
				// which command?
				if (QUIT_CMD.equals(command)) {
					out.write(GOODBYE_MSG, 0, GOODBYE_MSG.length());
					return;
				} else if (MOD_CMD.equals(command)) {
					out.write(MODULE_MSG, 0, MODULE_MSG.length());
					out.flush();
					module = DataFactory.getInstance().createModule("", -1);
					final TokenFeed tokenFeed = ScannerFactory.getInstance().createTokenFeed(in, out);
					CommandFactory.getInstance().processCommands(module, tokenFeed);
					out.write(COMPLETE_MSG, 0, COMPLETE_MSG.length());
					out.flush();
				} else if (command.startsWith(IFACE_CMD)) {
					param = command.substring(IFACE_CMD.length() + 1);
					if (!Token.VALID_ATOM.matcher(param).matches()) {
						out.write(BAD_IFACE_MSG, 0, BAD_IFACE_MSG.length());
						out.flush();
						module = null;
						continue;
					}
					out.write(INTERFACE_MSG, 0, INTERFACE_MSG.length());
					out.flush();
					module = DataFactory.getInstance().createModule(param, -1);
					final TokenFeed tokenFeed = ScannerFactory.getInstance().createTokenFeed(in, out);
					CommandFactory.getInstance().processCommands(module, tokenFeed);
					out.write(COMPLETE_MSG, 0, COMPLETE_MSG.length());
					out.flush();
				} else if (STORE_CMD.equals(command)) {
					if (module == null) {
						out.write(NO_MODULE_MSG, 0, NO_MODULE_MSG.length());
						out.flush();
						continue;
					}
					if ("".equals(module.getName())) {
						out.write(BAD_MODULE_MSG, 0, BAD_MODULE_MSG.length());
						out.flush();
						continue;
					}
					Storage.getInstance().saveModule(module, param, -1);
					out.write(STORED_MSG, 0, STORED_MSG.length());
					out.flush();
				} else {
					out.write(UNKNOWN_MSG, 0, UNKNOWN_MSG.length());
					out.flush();
				}
			}
		} catch (UnsupportedEncodingException e) {
			logger.error("UTF-8 encoding not supported", e);
		} catch (SocketException e) {
			logger.error("Socket error on port " + socket.getPort(), e);
		} catch (IOException e) {
			logger.error("IO error", e);
		} catch (CommandException e) {
			logger.error("Command error", e);
		} catch (DataException e) {
			logger.error("Unable to create module", e);
		} catch (StorageException e) {
			// out is definitely initialized here
			try {
				out.write(SAVE_FAILED_MSG, 0, SAVE_FAILED_MSG.length());
				socket.close();
			} catch (IOException ee) {
				logger.error("Unable to finish conversation with client", ee);
			}
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				logger.warn("Unable to properly close socket on exit", e);
			}
		}
	}

}
