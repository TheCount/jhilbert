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

package jhilbert;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;

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

import org.apache.log4j.Logger;

/**
 * JHilbert server thread.
 * This class contains only the outer shell of the server implementation.
 * Module and Interface conversation is handled by an appropriate feed.
 *
 * Client and server converse by exchanging <em>messages</em>. Each message
 * consists of:
 * <ol>
 *   <li>
 *     Three bytes indicating the total message length in bytes, minus three
 *     (i.e. the message length without those three bytes). The three bytes
 *     are to be interpreted as an integer in network byte order. In
 *     particular, the message size is limited to roughly 16MB.
 *   </li>
 *   <li>
 *     A command byte (client) or a response byte (server).
 *   </li>
 *   <li>
 *     Possibly further data whose interpretation depends on the command byte
 *     or the response byte.
 *   </li>
 * </ol>
 *
 * The server may use the following response bytes:
 * <ul>
 *   <li>
 *     <code>0x00</code>: GOODBYE
 *     No further data. Sent as a response to a QUIT command.
 *   </li>
 *   <li>
 *     <code>0x20</code>: OK
 *     May be followed by an UTF-8 encoded text string. Used to indicate that
 *     a command issued by the client was completed successfully.
 *     An OK command is sent by the server upon connection before receiving
 *     any data from the client.
 *   </li>
 *   <li>
 *     <code>0x30</code>: MORE
 *     May be followed by an UTF-8 encoded text string. Used to indicate that
 *     a command issued by the client was initiated, but further input from
 *     the client is required to complete the command successfully.
 *   </li>
 *   <li>
 *     <code>0x40</code>: CLIENT ERROR
 *     Followed by an UTF-8 encoded error message. Used to indicate that a
 *     command issued by the client cannot be completed due to an error on
 *     behalf of the client.
 *   </li>
 *   <li>
 *     <code>0x50</code>: SERVER ERROR
 *     Followed by an UTF-8 encoded error message. Used to indicate that a
 *     command issued by the client cannot be completed due to a server side
 *     error.
 *   </li>
 * </ul>
 * All other response bytes are reserved for future use.
 *
 * The client may use the following command bytes:
 * <ul>
 *   <li>
 *     <code>0x00</code>: QUIT
 *     No further data. The server will respond with GOODBYE and close the
 *     connection.
 *   </li>
 *   <li>
 *     <code>0x01</code>: MOD
 *     No further data. Used to indicate that the client wants to stream
 *     JHilbert proof module text to the server. The server will respond
 *     with MORE. This command is illegal if the last server response was
 *     MORE.
 *   </li>
 *   <li>
 *     <code>0x02</code>: IFACE
 *     Followed by a UTF-8 encoded interface name which in turn is followed
 *     by an integral revision number, encoded in 8 bytes in network byte
 *     order. Used to indicate that the client wants to stream JHilbert
 *     interface module text to the server. The server will respond with MORE
 *     or with CLIENT ERROR.
 *     This command is illegal if the last server response was MORE.
 *   </li>
 *   <li>
 *     <code>0x03</code>: TEXT
 *     Followed by UTF-8 encoded JHilbert text. The server will respond with
 *     MORE, CLIENT ERROR or SERVER ERROR. The accompanying text will be valid
 *     HTML snippets to be output by the client. This command is only legal if
 *     the last server response was MORE and the previous client command was
 *     TEXT, MOD or IFACE.
 *   </li>
 *   <li>
 *     <code>0x10</code>: FINISH
 *     No further data. Used to indicate that all JHilbert text has been sent.
 *     The server will respond with OK, CLIENT ERROR or SERVER ERROR. The
 *     accompanying text will be valid HTML snippets to be output by the
 *     client. This command is only legal if the last server response was MORE
 *     and the previous client command was TEXT, MOD or IFACE.
 *   </li>
 *   <li>
 *     <code>0x20</code>: DEL
 *     Followed by a UTF-8 encoded interface name which in turn is followed
 *     by an integral revision number, encoded in 8 bytes in network byte
 *     order. The server will delete the thus specified module from its
 *     storage and respond with OK on success. On error it will respond with
 *     CLIENT ERROR or SERVER ERROR. The accompanying text will be valid HTML
 *     snippets to be output by the client. This command is illegal if the
 *     last server response was MORE.
 *   </li>
 * </ul>
 * All other command bytes are reserved for further use.
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
	 * Charset.
	 */
	public static final Charset CHARSET = Charset.forName(ENCODING);

	/**
	 * Default socket timeout, in milliseconds.
	 */
	private static final int TIMEOUT = 5000;

	/**
	 * Maximum message size.
	 */
	private static final int MAX_MSG_SIZE = (1 << 24) - 1;

	/**
	 * Welcome message.
	 */
	private static final String WELCOME_MSG = "JHilbert version " + Main.VERSION + " ready";

	/**
	 * Proof module successfully parsed.
	 */
	private static final String PROOF_MSG = "Proof module parsed successfully";

	/**
	 * Interface module successfully parsed.
	 */
	private static final String INTERFACE_MSG = "Interface module parsed successfully";

	/**
	 * Unknown command message.
	 */
	private static final String UNKNOWN_MSG = "Unknown command";

	/**
	 * Bad interface message.
	 */
	private static final String BAD_IFACE_MSG = "Bad interface";

	/**
	 * Deletion failed message.
	 */
	private static final String DELETION_FAILED_MSG = "Deletion failed";

	/**
	 * Goodbye response code.
	 */
	public static final byte GOODBYE_RC = 0x00;

	/**
	 * OK response code.
	 */
	public static final byte OK_RC = 0x20;

	/**
	 * Need more response code.
	 */
	public static final byte MORE_RC = 0x30;

	/**
	 * Client error response code.
	 */
	public static final byte CLIENT_ERR_RC = 0x40;

	/**
	 * Server error respknse code.
	 */
	public static final byte SERVER_ERR_RC = 0x50;

	/**
	 * Quit command.
	 */
	public static final byte QUIT_CMD = 0x00;

	/**
	 * Module command.
	 */
	public static final byte MOD_CMD = 0x01;

	/**
	 * Interface command.
	 */
	public static final byte IFACE_CMD = 0x02;

	/**
	 * JHilbert text command.
	 */
	public static final byte TEXT_CMD = 0x03;

	/**
	 * Finish command.
	 */
	public static final byte FINISH_CMD = 0x10;

	/**
	 * Delete command.
	 */
	public static final byte DEL_CMD = 0x20;

	/**
	 * Reads a long from the specified byte array at the specified
	 * position in network byte order.
	 *
	 * @param a input array.
	 * @param pos position in the byte array.
	 *
	 * @return decoded long.
	 */
	public static long decodeLong(final byte[] a, int pos) {
		assert (a != null): "Supplied byte array is null";
		assert ((pos >= 0) && (pos <= a.length - 8)): "Bad array position";
		long result = 0;
		for (int i = 0; i != 8; ++i) {
			result <<= 8;
			result |= a[pos++] & 0xff;
		}
		return result;
	}

	/**
	 * Reads the size of the next message from the specified
	 * {@link InputStream}.
	 *
	 * @param in input stream.
	 *
	 * @return the message size, or <code>-1</code> if the end of the
	 * 	stream has been reached.
	 *
	 * @throws IOException on error.
	 */
	public static int readMessageSize(final InputStream in) throws IOException {
		assert (in != null): "Supplied input stream is null";
		final byte[] buf = new byte[3];
		int rc = in.read(buf);
		if (rc < 3)
			return -1;
		int result = 0;
		for (int i = 0; i != 3; ++i) {
			result <<= 8;
			result |= buf[i] & 0xff;
		}
		return result;
	}

	/**
	 * Leave answer on the specified {@link BufferedOutputStream} with the
	 * specified return code.
	 * The message is sent in UTF-8 format.
	 *
	 * @param out output stream.
	 * @param rc return code.
	 * @param msg message.
	 *
	 * @throws IOException on error.
	 */
	public static void writeAnswer(final BufferedOutputStream out, final byte rc, final String msg) throws IOException {
		assert (out != null): "Supplied output stream is null";
		assert (msg != null): "Supplied message is null";
		if (logger.isTraceEnabled())
			logger.trace("Sending answer code " + rc + " with message: " + msg);
		final byte[] msgBytes = msg.getBytes(/* FIXME: 1.5 compat CHARSET */ ENCODING);
		final int size = msgBytes.length + 1;
		if (size >= MAX_MSG_SIZE)
			throw new IOException("Message is too large to send (" + size + " bytes)");
		out.write(size >>> 16);
		out.write(size >>> 8);
		out.write(size);
		out.write(rc);
		out.write(msgBytes);
		out.flush();
	}

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
		try {
			final InputStream in = socket.getInputStream();
			final BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
			// send welcome
			writeAnswer(out, OK_RC, WELCOME_MSG);
			// execute commands
			for(;;) {
				int msgSize = readMessageSize(in);
				if (msgSize <= -1) {
					logger.warn("EOF from client while reading message size");
					return;
				}
				if (msgSize == 0) {
					logger.warn("Zero message size");
					return;
				}
				int command = in.read();
				if (command == -1) {
					logger.warn("EOF from client while reading command");
					return;
				}
				final byte[] msg = new byte[--msgSize];
				if (in.read(msg) < msgSize) {
					logger.warn("EOF from client while reading message");
					return;
				}
				switch (command) {
					case QUIT_CMD:
						writeAnswer(out, GOODBYE_RC, "");
						return;
					case MOD_CMD:
						final Module proofModule = DataFactory.getInstance().createModule("", -1);
						final TokenFeed proofFeed = ScannerFactory.getInstance().createTokenFeed(in, out);
						try {
							CommandFactory.getInstance().processCommands(proofModule, proofFeed);
							writeAnswer(out, OK_RC, PROOF_MSG);
						} catch (CommandException e) {
							writeAnswer(out, CLIENT_ERR_RC, e.getMessage());
						}
						break;
					case IFACE_CMD:
						if (msgSize <= 8) {
							writeAnswer(out, CLIENT_ERR_RC, BAD_IFACE_MSG);
							break;
						}
						final String param = new String(msg, 0, msgSize - 8, /* FIXME: 1.5 compat CHARSET */ ENCODING);
						if (!Token.VALID_ATOM.matcher(param).matches()) {
							writeAnswer(out, CLIENT_ERR_RC, BAD_IFACE_MSG);
							break;
						}
						final long version = decodeLong(msg, msgSize - 8);
						final Module interfaceModule = DataFactory.getInstance().createModule(param, version);
						final TokenFeed interfaceFeed = ScannerFactory.getInstance().createTokenFeed(in, out);
						try {
							CommandFactory.getInstance().processCommands(interfaceModule, interfaceFeed);
							writeAnswer(out, OK_RC, INTERFACE_MSG);
						} catch (CommandException e) {
							writeAnswer(out, CLIENT_ERR_RC, e.getMessage());
						}
						break;
					case DEL_CMD:
						if (msgSize <= 8) {
							writeAnswer(out, CLIENT_ERR_RC, DELETION_FAILED_MSG);
							break;
						}
						final String locator = new String(msg, 0, msgSize - 8, /* FIXME: 1.5 compat CHARSET */ ENCODING);
						if (!Token.VALID_ATOM.matcher(locator).matches()) {
							writeAnswer(out, CLIENT_ERR_RC, DELETION_FAILED_MSG);
							break;
						}
						final long revision = decodeLong(msg, msgSize - 8);
						try {
							Storage.getInstance().deleteModule(locator, revision);
							writeAnswer(out, OK_RC, "");
						} catch (StorageException e) {
							writeAnswer(out, SERVER_ERR_RC, e.getMessage());
						}
						break;
					case FINISH_CMD: // be lenient and forgive a misplaced finish command
						writeAnswer(out, OK_RC, "");
						break;
					default:
						writeAnswer(out, CLIENT_ERR_RC, UNKNOWN_MSG);
				}
			}
		} catch (UnsupportedEncodingException e) {
			logger.error("UTF-8 encoding not supported", e);
		} catch (SocketException e) {
			logger.error("Socket error on port " + socket.getPort(), e);
		} catch (IOException e) {
			logger.error("I/O error", e);
		} catch (DataException e) {
			logger.error("Unable to create module", e);
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				logger.warn("Unable to properly close socket on exit", e);
			}
		}
	}

}
