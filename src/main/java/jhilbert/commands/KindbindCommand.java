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
	 * Old kind.
	 */
	private final String oldKind;

	/**
	 * Scans a new KindbindCommand from a TokenScanner.
	 * The parameters must not be <code>null</code>.
	 *
	 * @param tokenScanner TokenScanner to scan from.
	 * @param data ModuleData.
	 * 
	 * @throws SyntaxException if a syntax error occurs.
	 */
	public KindbindCommand(final TokenScanner tokenScanner, final Data data) throws SyntaxException {
		super(data);
		assert (tokenScanner != null): "Supplied token scanner is null.";
		StringBuilder context = new StringBuilder("kindbind ");
		try {
			oldKind = tokenScanner.getAtom();
			context.append(oldKind).append(' ');
			name = tokenScanner.getAtom();
			context.append(name);
		} catch (ScannerException e) {
			logger.error("Scanner error in context " + context, e);
			throw new SyntaxException("Scanner error", context.toString(), e);
		}
	}

	public @Override void execute() throws VerifyException {
		try {
			data.bindKind(oldKind, name);
		} catch (DataException e) {
			logger.error("kindbind error binding " + name + " to " + oldKind, e);
			throw new VerifyException("kindbind error", oldKind + "/" + name, e);
		}
	}

}
