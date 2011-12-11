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

import java.io.InputStream;

import jhilbert.scanners.ScannerException;
import jhilbert.scanners.Token;

import org.apache.log4j.Logger;

/**
 * A token feed for stream I/O.
 */
class StreamTokenFeed extends AbstractTokenFeed {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(StreamTokenFeed.class);

	/**
	 * Character scanner.
	 */
	private final CharScanner charScanner;

	/**
	 * Creates a new <code>StreamTokenFeed</code> for the specified input
	 * stream.
	 *
	 * @param in input stream.
	 *
	 * @throws ScannerException if the underlying character scanner cannot
	 * 	be set up.
	 */
	StreamTokenFeed(final InputStream in) throws ScannerException {
		assert (in != null): "Supplied input stream is null";
		try {
			charScanner = new CharScanner(in);
		} catch (ScannerException e) {
			logger.error("Unable to set up character scanner " + e.getScanner(), e);
			throw new ScannerException("Unable to set up character scanner", this, e);
		}
	}

	protected @Override Token getNewToken() throws ScannerException {
		charScanner.resetContext();
		final StringBuilder repr = new StringBuilder();
		try {
			appendToContext(' ');
			Char c;
			Char.Class cc;
			// consume whitespace
			try {
				do {
					c = charScanner.getToken();
					cc = c.getCharClass();
				} while ((cc == Char.Class.SPACE) || (cc == Char.Class.NEWLINE));
			} catch (NullPointerException e) { // EOF
				return null;
			}
			// what have we got?
			switch (cc) {
				case OPEN_PAREN:
					appendToContext('(');
					return BEGIN_EXP;
				case CLOSE_PAREN:
					appendToContext(')');
					return END_EXP;
				case ATOM:
					break;
				default:
					assert false: "Invalid character type (this should not happen)";
			}
			// scan whole ATOM
			try {
				do {
					repr.append((char) c.getCodepoint());
					c = charScanner.getToken();
				} while (c.getCharClass() == Char.Class.ATOM);
			} catch (NullPointerException e) { // EOF
				// break
			}
			charScanner.putToken(c);
			appendToContext(repr);
			return new TokenImpl(repr.toString(), Token.Class.ATOM);
		} catch (ScannerException e) {
			logger.error("Error scanning token");
			logger.debug("Context of causing scanner: " + e.getScanner().getContextString());
			logger.debug("Context of this scanner: " + getContextString());
			throw new ScannerException("Error scanning token", this, e);
		}
	}

	public @Override void confirm(final String msg) {
		assert (msg != null): "Supplied message is null";
		if (logger.isTraceEnabled())
			logger.trace("Read " + msg + " token");
	}

	public @Override void reject(final String msg) {
		assert (msg != null): "Supplied message is null";
		logger.error(msg);
	}

	public @Override void confirmEndCmd() {
		if (logger.isDebugEnabled())
			logger.debug("Command complete");
		resetContext();
	}

}
