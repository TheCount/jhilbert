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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import jhilbert.exceptions.InputException;
import jhilbert.util.StreamInputSource;

/**
 * A {@link StreamInputSource} associated to a file.
 */
public class FileInputSource extends StreamInputSource {

	/**
	 * Creates a new {@link FileInputStream} from a filename.
	 *
	 * @param filename the file name (must not be <code>null</code>).
	 *
	 * @return the new <code>FileInputStream</code>.
	 *
	 * @throws InputException if the file cannot be found.
	 */
	private static FileInputStream createFileInputStream(final String filename) throws InputException {
		assert (filename != null): "Supplied filename is null.";
		try {
			return new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			throw new InputException("File not found", filename, e);
		}
	}

	/**
	 * Creates a new <code>FileInputSource</code> using the file with the specfied file name.
	 *
	 * @param filename the file name.
	 *
	 * @throws NullPointerException if filename is <code>null</code>.
	 * @throws InputException if the file cannot be found or an I/O problem occurs.
	 */
	public FileInputSource(final String filename) throws InputException {
		super(createFileInputStream(filename), filename);
	}

}
