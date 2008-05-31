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

package jhilbert.commands;

import jhilbert.commands.Command;
import jhilbert.data.Data;
import jhilbert.data.Kind;
import jhilbert.exceptions.DataException;
import jhilbert.exceptions.ScannerException;
import jhilbert.exceptions.SyntaxException;
import jhilbert.exceptions.VerifyException;
import jhilbert.util.TokenScanner;
import org.apache.log4j.Logger;

/**
 * Command binding one kind to another.
 * <p>
 * The format of the command is:
 * <br>
 * oldKindName name
 */
public final class KindbindCommand extends Command {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(KindbindCommand.class);

	/**
	 * Data.
	 */
	private final Data data;

	/**
	 * Old kind name.
	 */
	private final String oldKindName;

	/**
	 * New kind name.
	 */
	private final String newKindName;

	/**
	 * Scans a new KindbindCommand from a TokenScanner.
	 * The parameters must not be <code>null</code>.
	 *
	 * @param tokenScanner TokenScanner to scan from.
	 * @param data data.
	 * 
	 * @throws SyntaxException if a syntax error occurs.
	 */
	public KindbindCommand(final TokenScanner tokenScanner, final Data data) throws SyntaxException {
		assert (tokenScanner != null): "Supplied token scanner is null.";
		assert (data != null): "Supplied data are null.";
		this.data = data;
		try {
			oldKindName = tokenScanner.getAtom();
			newKindName = tokenScanner.getAtom();
		} catch (ScannerException e) {
			logger.error("Scanner error in context " + tokenScanner.getContextString());
			throw new SyntaxException("Scanner error", tokenScanner.getContextString(), e);
		}
	}

	public @Override void execute() throws VerifyException {
		try {
			final Kind oldKind = data.getKind(oldKindName);
			if (oldKind == null) {
				logger.error("kindbind error: old kind " + oldKindName + " does not exist.");
				throw new VerifyException("kindbind error", oldKindName);
			}
			data.bindKind(oldKind, newKindName);
		} catch (DataException e) {
			logger.error("kindbind error binding " + newKindName + " to " + oldKindName);
			throw new VerifyException("kindbind error", oldKindName + "/" + newKindName, e);
		}
	}

}
