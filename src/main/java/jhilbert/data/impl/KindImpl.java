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

package jhilbert.data.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import jhilbert.data.Kind;
import jhilbert.data.impl.NameImpl;
import org.apache.log4j.Logger;

/**
 * Implementation of {@link Kind}.
 */
final class KindImpl extends NameImpl implements Kind, Externalizable {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(KindImpl.class);

	/**
	 * Creates a new kind with the specified name.
	 *
	 * @param name name of the new kind.
	 */
	KindImpl(final String name) {
		super(name);
	}

	/**
	 * Creates an uninitalized kind.
	 * Used by serialization.
	 */
	public KindImpl() {
		super();
	}

	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
		try {
			setName((String) in.readObject());
		} catch (ClassCastException e) {
			logger.error("Wrong class while deserializing kind.");
			throw new ClassNotFoundException("Wrong class while deserializing kind.", e);
		}
	}

	public void writeExternal(final ObjectOutput out) throws IOException {
		out.writeObject(super.toString());
	}

}
