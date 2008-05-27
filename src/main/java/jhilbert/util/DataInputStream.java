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

import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;
import jhilbert.exceptions.DataException;

/**
 * Class which slightly enhances the DataInputStream provided by the JDK.
 */
public class DataInputStream extends java.io.DataInputStream {

	/**
	 * Creates a DataInputStream that uses the specified underlying InputStream.
	 *
	 * @param in the specified input stream.
	 */
	public DataInputStream(final InputStream in) {
		super(in);
	}

	/**
	 * Reads a non-negative integer.
	 *
	 * @return a non-negative integer.
	 *
	 * @throws EOFException if this input stream reaches the end before reading four bytes.
	 * @throws IOException the stream has been closed and the contained input stream does not support reading after
	 * 	close, or another I/O error occurs.
	 * @throws DataException if the integer read is negative.
	 */
	public final int readNonNegativeInt() throws EOFException, IOException, DataException {
		final int result = super.readInt();
		if (result < 0)
			throw new DataException("Read negative integer", Integer.toString(result));
		return result;
	}

	/**
	 * Reads a negative integer.
	 *
	 * @return a negative integer.
	 *
	 * @throws EOFException if this input stream reaches the end before reading four bytes.
	 * @throws IOException the stream has been closed and the contained input stream does not support reading after
	 * 	 close, or another I/O error occurs.
	 * @throws DataException if the integer read is negative.
	 */
	public final int readNegativeInt() throws EOFException, IOException, DataException {
		final int result = super.readInt();
		if (result >= 0)
			throw new DataException("Read non-negative integer", Integer.toString(result));
		return result;
	}

	/**
	 * Reads an integer which lies within the specified bounds.
	 *
	 * @param lower lower bound.
	 * @param upper upper bound.
	 *
	 * @return an integer i with the property lower &lt;= i &lt; upper.
	 *
	 * @throws EOFException if this input stream reaches the end before reading four bytes.
	 * @throws IOException the stream has been closed and the contained input stream does not support reading after
	 * 	close, or another I/O error occurs.
	 * @throws DataException if the integer is not within the specified bounds.
	 * 	This includes the case lower &gt; upper.
	 */
	public final int readInt(final int lower, final int upper) throws EOFException, IOException, DataException {
		final int result = super.readInt();
		if ((result < lower) || (result >= upper))
			throw new DataException("Read out-of-bounds integer", Integer.toString(result));
		return result;
	}

	/**
	 * Reads an integer which lies within the specified bounds, or is zero.
	 *
	 * @param lower lower bound.
	 * @param upper upper bound.
	 *
	 * @return an integer i which is zero or has the property lower &lt;= i &lt; upper.
	 *
	 * @throws EOFException if this input stream reaches the end before reading four bytes.
	 * @throws IOException the stream has been closed and the contained input stream does not support reading after
	 * 	close, or another I/O error occurs.
	 * @throws DataException if the integer is not within the specified bounds, or zero.
	 */
	public final int readIntOr0(final int lower, final int upper) throws EOFException, IOException, DataException {
		final int result = super.readInt();
		if (((result < lower) || (result >= upper)) && (result != 0))
			throw new DataException("Read non-zero out-of-bounds integer", Integer.toString(result));
		return result;
	}

	/**
	 * Reads a string which is stored as a zero-terminated character sequence.
	 *
	 * @return a string.
	 *
	 * @throws EOFException if this input stream reaches the end before reading a zero character.
	 * @throws IOException the stream has been closed and the contained input stream does not support reading after
	 * 	close, or another I/O error occurs.
	 * @throws DataException if improper surrogate characters are detected.
	 */
	public final String readString() throws EOFException, IOException, DataException {
		final StringBuilder result = new StringBuilder();
		boolean expectLowSurrogate = false;
		for (char c = super.readChar(); c != 0; c = super.readChar()) {
			if (expectLowSurrogate) {
				if (!Character.isLowSurrogate(c))
					throw new DataException("Expected low surrogate character", Character.toString(c));
				expectLowSurrogate = false;
			} else {
				if (Character.isLowSurrogate(c))
					throw new DataException("Unexpected low surrogate character",
								Character.toString(c));
				if (Character.isHighSurrogate(c))
					expectLowSurrogate = true;
			}
			result.append(c);
		}
		if (expectLowSurrogate)
			throw new DataException("String terminated in mid-character", result.toString());
		return result.toString();
	}

}
