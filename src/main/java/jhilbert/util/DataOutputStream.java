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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Class which slightly enhances the DataOutputStream provided by the JDK.
 */
public class DataOutputStream extends java.io.DataOutputStream {

	/**
	 * Creates a new data output stream to write data to the specified underlying output stream.
	 *
	 * @param out the underlying output stream, to be saved for later use.
	 */
	public DataOutputStream(final OutputStream out) {
		super(out);
	}

	/**
	 * Writes the specified string as character array, terminated by a zero character.
	 *
	 * @param string a string to be written.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public final void writeString(final String string) throws IOException {
		super.writeChars(string);
		super.writeChar(0);
	}

}
