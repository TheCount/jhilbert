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

    You may contact the author on these Wiki pages:
    http://planetx.cc.vt.edu/AsteroidMeta//GrafZahl (preferred)
    http://en.wikisource.org/wiki/User_talk:GrafZahl
*/

package jhilbert.scanners.impl;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import jhilbert.Server;

import jhilbert.scanners.ScannerException;
import jhilbert.scanners.Token;

/**
 * A token feed for conversation with MediaWiki.
 */
final class MediaWikiTokenFeed extends AbstractTokenFeed {

	/**
	 * Parser state.
	 */
	private static enum ParserState {

		/**
		 * Initial state (not in comment, not reading an ATOM).
		 */
		INITIAL,

		/**
		 * Currently reading an ATOM.
		 */
		ATOM,

		/**
		 * Currently within a comment.
		 */
		COMMENT
		
	}

	/**
	 * Input stream.
	 */
	private final InputStream in;

	/**
	 * Output stream.
	 */
	private final BufferedOutputStream out;

	/**
	 * Current char buffer.
	 */
	private CharBuffer charBuffer;

	/**
	 * Current buffer position.
	 */
	private int charBufferPos;

	/**
	 * Current buffer size.
	 */
	private int charBufferSize;

	/**
	 * Current token.
	 */
	private final StringBuilder currentToken;

	/**
	 * Escapes special HTML characters.
	 *
	 * @param c character.
	 *
	 * @return string containing only the character <code>c</code> or an
	 * 	escaped version thereof.
	 */
	private static String escapeHTML(final char c) {
		switch (c) {
			case '\n':
			case '\r':
				return "<br />\n";
			case '\t':
				return "&#9;";
			case ' ':
				return "&nbsp;";
			case '&':
				return "&amp;";
			case '"':
				return "&quot;";
			case '<':
				return "&lt;";
			case '>':
				return "&gt;";
			default:
				return Character.toString(c);
		}
	}

	/**
	 * Escapes special HTML characters in the specified
	 * {@link CharSequence}
	 *
	 * @param s character sequence.
	 *
	 * @return escaped string.
	 */
	private static String escapeHTML(final CharSequence s) {
		assert (s != null): "Supplied character sequence is null";
		final StringBuilder result = new StringBuilder();
		final int length = s.length();
		for (int i = 0; i != length; ++i)
			result.append(escapeHTML(s.charAt(i)));
		return result.toString();
	}

	/**
	 * Creates a new <code>MediaWikiTokenFeed</code> for the provided
	 * streams.
	 *
	 * @param in input stream.
	 * @param out buffered output stream.
	 */
	MediaWikiTokenFeed(final InputStream in, final BufferedOutputStream out) {
		assert (in != null): "Supplied input stream is null";
		assert (out != null): "Supplied output stream is null";
		this.in = in;
		this.out = out;
		charBuffer = null;
		charBufferPos = -1;
		currentToken = new StringBuilder();
	}

	protected @Override Token getNewToken() throws ScannerException {
		try {
			currentToken.setLength(0);
			if (charBuffer == null) {
				// get new text
				Server.writeAnswer(out, Server.MORE_RC, getContextString());
				resetContext();
				int msgSize = Server.readMessageSize(in);
				if (msgSize <= 0)
					throw new ScannerException("Bad message size", this);
				final int command = in.read();
				if (command == -1)
					throw new ScannerException("EOF while reading command", this);
				final byte[] msg = new byte[--msgSize];
				if (in.read(msg) < msgSize)
					throw new ScannerException("EOF from client while reading text", this);
				switch (command) {
					case Server.QUIT_CMD:
						Server.writeAnswer(out, Server.GOODBYE_RC, "");
						throw new ScannerException("Client suddenly wants to quit", this);
					case Server.TEXT_CMD:
						charBuffer = Server.CHARSET.newDecoder().decode(ByteBuffer.wrap(msg));
						charBufferSize = charBuffer.length();
						charBufferPos = 0;
						break;
					case Server.FINISH_CMD:
						return null;
					default:
						Server.writeAnswer(out, Server.CLIENT_ERR_RC, "Command not allowed here");
				}
			}
			ParserState parserState = ParserState.INITIAL;
			while (charBufferPos < charBufferSize) {
				final char c = charBuffer.get(charBufferPos++);
				final Char.Class charClass = (new Char(c)).getCharClass();
				if (charClass == Char.Class.INVALID)
					throw new ScannerException("Invalid character '" + c + "'", this);
				switch (parserState) {
					case INITIAL:
						switch (charClass) {
							case OPEN_PAREN:
								currentToken.append(c);
								return BEGIN_EXP;
							case CLOSE_PAREN:
								currentToken.append(c);
								return END_EXP;
							case HASHMARK:
								parserState = ParserState.COMMENT;
								appendToContext("<span class=\"comment\">#");
								break;
							case ATOM:
								parserState = ParserState.ATOM;
								currentToken.append(c);
								break;
							default:
								appendToContext(escapeHTML(c));
								break;
						}
						break;
					case ATOM:
						switch (charClass) {
							case ATOM:
								currentToken.append(c);
								break;
							default:
								--charBufferPos; // parser backup, ATOM -> INITIAL implied
								return new TokenImpl(currentToken.toString(), Token.Class.ATOM);
						}
						break;
					case COMMENT:
						switch (charClass) {
							case NEWLINE:
								parserState = ParserState.INITIAL;
								appendToContext("</span><br />\n");
								break;
							default:
								appendToContext(escapeHTML(c));
								break;
						}
						break;
					default:
						throw new AssertionError("This should not happen");
				}
			}
			charBuffer = null;
			if (parserState == ParserState.ATOM)
				return new TokenImpl(currentToken.toString(), Token.Class.ATOM);
			if (parserState == ParserState.COMMENT)
				appendToContext("</span>");
			return getNewToken(); // FIXME: Hmm, tail recursion... how smart is the java compiler here?
		} catch (IOException e) {
			throw new ScannerException("I/O error", this, e);
		}
	}

	public @Override void confirm(final String msg) {
		assert (msg != null): "Supplied message is null";
		appendToContext("<span class=\"" + msg + "\">" + escapeHTML(currentToken) + "</span>");
	}

	public @Override void reject(final String msg) {
		assert (msg != null): "Supplied message is null";
		appendToContext("<span class=\"invalid\">" + escapeHTML(currentToken) + "</span> <span class=\"error\">" + escapeHTML(msg) + "</span>");
	}

	public @Override void confirmEndCmd() throws ScannerException {
		confirmEndExp();
	}

}
