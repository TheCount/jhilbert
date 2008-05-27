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
import jhilbert.data.Data;
import jhilbert.data.Parameter;
import jhilbert.data.Token;
import jhilbert.exceptions.DataException;
import jhilbert.exceptions.ScannerException;
import jhilbert.exceptions.SyntaxException;
import jhilbert.exceptions.VerifyException;
import jhilbert.util.TokenScanner;
import org.apache.log4j.Logger;

/**
 * Command template for loading/declaring interfaces.
 * <p>
 * The format of such commands is always the same:
 * <br>
 * name locator (param1 &hellip; paramN) prefix
 */
public abstract class InterfaceCommand extends Command {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(InterfaceCommand.class);

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
	 * Parameter corresponding to this command (initialized by {@link #execute()}.
	 */
	protected Parameter parameter;

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
	protected InterfaceCommand(final String commandName, final TokenScanner tokenScanner, final Data data) throws SyntaxException {
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
			if (token.tokenClass != Token.TokenClass.END_EXP) {
				logger.error("Expected \")\" after parameter list of parameter " + name);
				throw new SyntaxException("Expected \")\"", context.toString());
			}
			prefix = tokenScanner.getString();
			context.append(" with prefix ").append(prefix);
		} catch (NullPointerException e) {
			logger.error("Unexpected end of input while scanning parameter " + name + " in context "
				+ context, e);
			throw new SyntaxException("Unexpected end of input", context.toString(), e);
		} catch (ScannerException e) {
			logger.error("Scanner error while scanning parameter " + name + " in context " + context, e);
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
	 * FIXME: Scrap this method
	 *
	 * @param parameters interface parameters.
	 */
//	protected InterfaceData createInterfaceData(final List<Interface> parameters) {
//		throw new UnsupportedOperationException("Subclasses must override this method if they intend to use it.");
//	}

	public @Override void execute() throws VerifyException { // subclasses extend this method.
//		try {
			// This really belongs in the classes defining the parameter
//			if (data.getParameter(name) != null) {
//				logger.error("A parameter with name " + name + " already exists.");
//				throw new VerifyException("Parameter already defined", name);
//			}
			List<Parameter> parameters = new ArrayList(paramNameList.size());
			for (final String paramName: paramNameList) {
				final Parameter param = data.getParameter(paramName);
				if (param == null) {
					logger.error("Parameter " + paramName + " from parameter list of " + name
						+ " does not exist.");
					throw new VerifyException("Parameter not found", paramName);
				}
				parameters.add(param);
			}
			parameter = new Parameter(name, locator, parameters, prefix);
//		} catch (DataException e) {
//			logger.error("Data error while defining parameter " + name, e);
//			throw new VerifyException("Data error", name, e);
//		}
	}

}
