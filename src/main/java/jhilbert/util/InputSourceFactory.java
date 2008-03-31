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
import jhilbert.util.FileInputSource;
import jhilbert.util.InputSource;

/**
 * Factory class for {@link InputSource}s.
 *
 * FIXME: Right now, only files are supported
 */
public abstract class InputSourceFactory {

	/**
	 * Create an interface input source from a locator String.
	 *
	 * @param locator interface specifier.
	 *
	 * @return an InputSource for the specified interface.
	 *
	 * @throws InputException if the input source cannot be created.
	 */
	public static InputSource createInterfaceInputSource(final String locator) throws InputException {
		return new FileInputSource(locator + ".ghi");
	}

}
