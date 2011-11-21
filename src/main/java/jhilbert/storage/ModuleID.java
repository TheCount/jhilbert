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

package jhilbert.storage;

import jhilbert.data.Module;

/**
 * Class which encapsulates the two pieces of data to uniquely identify a
 * {@link Module}: its locator and its revision number.
 */
final class ModuleID {

	/**
	 * Locator.
	 */
	private final String locator;

	/**
	 * Revision number.
	 */
	private final long version;

	/**
	 * Creates a new <code>ModuleID</code> with the specified locator and
	 * the specified revision.
	 *
	 * @param locator module locator.
	 * @param version revision number.
	 */
	public ModuleID(final String locator, final long version) {
		assert (locator != null): "Supplied locator is null";
		assert (version >= -1): "Invalid revision number supplied";
		this.locator = locator;
		this.version = version;
	}

	/**
	 * Creates a new <code>ModuleID</code> from the specified
	 * {@link Module}, using the module name as locator.
	 *
	 * @param module module.
	 */
	public ModuleID(final Module module) {
		assert (module != null): "Specified module is null";
		locator = module.getName();
		version = module.getRevision();
	}

	public @Override boolean equals(final Object o) {
		try {
			final ModuleID id = (ModuleID) o;
			return (id.locator.equals(locator) && (id.version == version));
		} catch (ClassCastException e) {
			return false;
		}
	}

	public @Override int hashCode() {
		return (locator.hashCode() ^ ((int) (version ^ (version >>> 32))));
	}

	public @Override String toString() {
		return (locator + " " + version);
	}

}
