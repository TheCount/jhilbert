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

package jhilbert.scanners.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import jhilbert.scanners.ScannerException;
import jhilbert.scanners.Token;
import jhilbert.scanners.TokenFeed;

import org.apache.log4j.Logger;

/**
 * A token feed for server-like IO.
 */
final class IOTokenFeed extends AbstractTokenFeed {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(IOTokenFeed.class);

	/**
	 * Input stream.
	 */
	private final BufferedReader in;

	/**
	 * Output stream.
	 */
	private final BufferedWriter out;

	/**
	 * Creates a new <code>IOTokenFeed</code> for the provided streams.
	 *
	 * @param in input stream.
	 * @param out output stream.
	 */
	IOTokenFeed(final BufferedReader in, final BufferedWriter out) {
		assert (in != null): "Supplied input stream is null";
		assert (out != null): "Supplied output stream is null";
		this.in = in;
		this.out = out;
	}

	protected @Override Token getNewToken() throws ScannerException {
		try {
			final String s = in.readLine();
			if (s == null) {
				logger.error("Unexpected end of input");
				throw new ScannerException("Unexpected end of input", this);
			}
			if ("(".equals(s))
				return BEGIN_EXP;
			if (")".equals(s))
				return END_EXP;
			if (!Token.VALID_ATOM.matcher(s).matches()) {
				logger.error("Invalid token: " + s);
				throw new ScannerException("Invalid token", this);
			}
			return new TokenImpl(s, Token.Class.ATOM);
		} catch (IOException e) {
			logger.error("I/O error while reading input");
			throw new ScannerException("I/O error while reading input", this, e);
		}
	}

	/**
	 * Replies to the last token.
	 *
	 * @param msg reply message.
	 *
	 * @throws ScannerException if the reply fails.
	 */
	private void reply(final String msg) throws ScannerException {
		try {
			out.write(msg, 0, msg.length());
			out.flush();
		} catch (IOException e) {
			logger.error("Unable to send reply");
			logger.debug("Reply content: " + msg.trim());
			throw new ScannerException("Unable to send reply", this, e);
		}
	}

	public @Override void confirm(final String msg) throws ScannerException {
		assert (msg != null): "Supplied message is null";
		reply("321 " + msg + "\r\n");
	}

	public @Override void reject(final String msg) throws ScannerException {
		assert (msg != null): "Supplied message is null";
		reply("421 " + msg + "\r\n");
	}

	public @Override void confirmEndCmd() throws ScannerException {
		reply("221 " + TokenFeed.END_EXP + "\r\n");
	}

}
