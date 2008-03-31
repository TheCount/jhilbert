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

import jhilbert.exceptions.InputException;

/**
 * An input source, such as one provided by an InputStream.
 */
public interface InputSource {

	/**
	 * Returns Unicode codepoint of the next character.
	 * If there are no more characters, <code>-1</code> will be returned.
	 * Once this happens, all subsequent calls will also return <code>-1</code>.
	 *
	 * @return codepoint of the next character, or -1 if there are no more characters.
	 *
	 * @throws InputException if a problem, such as an I/O error, occurs.
	 */
	public int read() throws InputException;

}
