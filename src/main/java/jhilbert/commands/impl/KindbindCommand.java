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

package jhilbert.commands.impl;

import jhilbert.commands.CommandException;
import jhilbert.commands.SyntaxException;

import jhilbert.data.DataException;
import jhilbert.data.Kind;
import jhilbert.data.Module;
import jhilbert.data.Namespace;

import jhilbert.scanners.ScannerException;
import jhilbert.scanners.TokenScanner;

import org.apache.log4j.Logger;

/**
 * Command to bind two kinds together.
 */
public final class KindbindCommand extends AbstractCommand {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(KindbindCommand.class);

	/**
	 * Old kind name.
	 */
	private final String oldKindName;

	/**
	 * New kind name.
	 */
	private final String newKindName;

	/**
	 * Creates a new <code>KindbindCommand</code>.
	 *
	 * @param module {@link Module} in which to bind kinds.
	 * @param tokenScanner {@link TokenScanner} to obtain kindbind data.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 */
	public KindbindCommand(final Module module, final TokenScanner tokenScanner) throws SyntaxException {
		super(module);
		try {
			oldKindName = tokenScanner.getAtom();
			newKindName = tokenScanner.getAtom();
		} catch (ScannerException e) {
			logger.error("Error scanning kindbind", e);
			logger.debug("Scanner context: " + e.getScanner().getContextString());
			throw new SyntaxException("Error scanning kindbind", e);
		}
	}

	public @Override void execute() throws CommandException {
		final Namespace<? extends Kind> namespace = getModule().getKindNamespace();
		assert (namespace != null): "Module provided null namespace";
		final Kind oldKind = namespace.getObjectByString(oldKindName);
		if (oldKind == null) {
			logger.error("Kind " + oldKindName + " not found");
			throw new CommandException("Kind not found");
		}
		final Kind newKind = namespace.getObjectByString(newKindName);
		try {
			if (newKind == null)
				namespace.createAlias(oldKind, newKindName);
			else
				namespace.identify(oldKind, newKind);
		} catch (DataException e) {
			throw new AssertionError("Error creating alias/identifying; this should not happen");
		}
	}

}
