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

import java.util.ArrayList;
import java.util.List;
import jhilbert.commands.Command;
import jhilbert.data.Interface;
import jhilbert.data.InterfaceData;
import jhilbert.data.ModuleData;
import jhilbert.data.Token;
import jhilbert.exceptions.DataException;
import jhilbert.exceptions.ScannerException;
import jhilbert.exceptions.SyntaxException;
import jhilbert.exceptions.VerifyException;
import jhilbert.util.TokenScanner;

/**
 * Command template for loading/declaring interfaces.
 * <p>
 * The format of such commands is always the same:
 * <br>
 * name locator (param1 &hellip; paramN) prefix
 */
public abstract class InterfaceCommand extends Command {

	/**
	 * Locator.
	 */
	protected final String locator;

	/**
	 * List of parameter names.
	 */
	protected final List<String> paramNameList;

	/**
	 * Prefix.
	 */
	protected final String prefix;

	/**
	 * Scans a new InterfaceCommand from a TokenScanner.
	 * The parameters must not be <code>null</code>.
	 *
	 * @param commandName name of command.
	 * @param tokenScanner TokenScanner to scan from.
	 * @param data ModuleData.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 */
	protected InterfaceCommand(final String commandName, final TokenScanner tokenScanner, final ModuleData data) throws SyntaxException {
		super(data);
		assert (tokenScanner != null): "Supplied token scanner is null.";
		assert (commandName != null): "Supplied commandName is null.";
		StringBuilder context = new StringBuilder(commandName);
		context.append(' ');
		try {
			name = tokenScanner.getAtom();
			context.append(name);
			locator = tokenScanner.getAtom();
			context.append(" located at ").append(locator).append(" with parameters (");
			paramNameList = new ArrayList();
			tokenScanner.beginExp();
			Token token = tokenScanner.getToken();
			while (token.tokenClass == Token.TokenClass.ATOM) {
				final String atom = token.toString();
				context.append(atom).append(", ");
				paramNameList.add(atom);
				token = tokenScanner.getToken();
			}
			final int length = context.length();
			context.delete(length - 2, length);
			context.append(')');
			if (token.tokenClass != Token.TokenClass.END_EXP)
				throw new SyntaxException("Expected \")\"", context.toString());
			prefix = tokenScanner.getString();
			context.append(" with prefix ").append(prefix);
		} catch (NullPointerException e) {
			throw new SyntaxException("Unexpected end of input", context.toString(), e);
		} catch (ScannerException e) {
			throw new SyntaxException("Scanner error", context.toString(), e);
		}
	}

	/**
	 * Copy constructor.
	 * Data are copied deeply, everything else is copied shallowly.
	 * The String &quot;&nbsp;(copy)&quot; is appended to the interface name.
	 *
	 * @param interfaceCommand InterfaceCommand to be copied. Must not be <code>null</code>.
	 */
	protected InterfaceCommand(final InterfaceCommand interfaceCommand) {
		super(interfaceCommand.data.clone());
		name = interfaceCommand.name + " (copy)";
		locator = interfaceCommand.locator;
		paramNameList = interfaceCommand.paramNameList;
		prefix = interfaceCommand.prefix;
	}

	/**
	 * Creates the {@link jhilbert.data.InterfaceData} needed during creation of the new interface.
	 *
	 * @param parameters interface parameters.
	 */
	protected InterfaceData createInterfaceData(final List<Interface> parameters) {
		throw new UnsupportedOperationException("Subclasses must override this method if they intend to use it.");
	}

	public @Override void execute() throws VerifyException { // NB: ParamCommand overrides this method
		try {
			if (data.containsInterface(name))
				throw new VerifyException("Interface already defined", name);
			List<Interface> parameters = new ArrayList();
			for (String paramName: paramNameList) {
				if (!data.containsInterface(paramName))
					throw new VerifyException("Interface not found", paramName);
				parameters.add(data.getInterface(paramName));
			}
			data.defineInterface(new Interface(name, locator, createInterfaceData(parameters)));
		} catch (DataException e) {
			throw new VerifyException("Data error", name, e);
		}
	}

}
