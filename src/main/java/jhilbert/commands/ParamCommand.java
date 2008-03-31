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

import java.util.Map;
import jhilbert.commands.ExportCommand;
import jhilbert.commands.InterfaceCommand;
import jhilbert.data.Interface;
import jhilbert.data.InterfaceData;
import jhilbert.exceptions.DataException;
import jhilbert.exceptions.SyntaxException;
import jhilbert.exceptions.VerifyException;
import jhilbert.util.TokenScanner;
import org.apache.log4j.Logger;

/**
 * Command declaring a new parameter.
 *
 * @see InterfaceCommand
 */
public final class ParamCommand extends InterfaceCommand {

	/**
	 * Scans a new ParamCommand from a TokenScanner.
	 * The parameters must not be <code>null</code>.
	 *
	 * @param tokenScanner TokenScanner to scan from.
	 * @param data InterfaceData.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 *
	 * @see InterfaceCommand#InterfaceCommand(TokenScanner, ModuleData)
	 */
	public ParamCommand(final TokenScanner tokenScanner, final InterfaceData data) throws SyntaxException {
		super("param", tokenScanner, data);
	}

	public @Override void execute() throws VerifyException {
		InterfaceData data = (InterfaceData) this.data;
		final Logger logger = Logger.getLogger(getClass());
		try {
			Interface param = data.getNextParameter();
			// copy data
			final String paramPrefix = param.getPrefix();
			for (Map.Entry<String, String> entry: param.getKindMap().entrySet())
				data.defineLocalKind(prefix + entry.getKey(), entry.getValue());
			for (String termName: param.getTermNames())
				data.defineLocalTerm(prefix + termName, paramPrefix + termName);
			// Now check if this parameter satisfies the specified interface
			Interface iface = data.getInterface(name);
			if ((iface == null) || (!locator.equals(iface.getLocator()))) {
				logger.info("Checking whether interface " + name + " is satisfied:");
				ExportCommand ec = new ExportCommand(this);
				ec.execute();
				logger.info("Interface " + name + " is satisfied.");
			} else {
				logger.info("Interface " + name + " is satisfied (cached).");
			}
		} catch (VerifyException e) {
			throw new VerifyException("Parameter does not satisfy specified interface", name, e);
		} catch (DataException e) {
			throw new VerifyException("Data error", name, e);
		}
	}

}
