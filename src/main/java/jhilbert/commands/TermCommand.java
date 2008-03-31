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
import jhilbert.data.InterfaceData;
import jhilbert.data.Token;
import jhilbert.exceptions.DataException;
import jhilbert.exceptions.ScannerException;
import jhilbert.exceptions.SyntaxException;
import jhilbert.exceptions.VerifyException;
import jhilbert.util.TokenScanner;

/**
 * Command introducing a new term.
 * <p>
 * The format of this command is:
 * <br>
 * kindName (name kindName1 &hellip; kindNameN)
 */
public final class TermCommand extends Command {

	/**
	 * Kind.
	 */
	private final String kind;

	/**
	 * List of input kinds.
	 */
	private final List<String> inputKindList;

	/**
	 * Scans a new TermCommand from a TokenScanner.
	 * The parameters must not be <code>null</code>.
	 *
	 * @param tokenScanner TokenScanner to scan from.
	 * @param data InterfaceData.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 */
	public TermCommand(final TokenScanner tokenScanner, final InterfaceData data) throws SyntaxException {
		super(data);
		assert (tokenScanner != null): "Supplied token scanner is null.";
		StringBuilder context = new StringBuilder("term (");
		try {
			kind = tokenScanner.getAtom();
			context.append(kind).append(' ');
			tokenScanner.beginExp();
			context.append('(');
			name = tokenScanner.getAtom();
			context.append(name).append(' ');
			inputKindList = new ArrayList();
			Token token = tokenScanner.getToken();
			while (token.tokenClass == Token.TokenClass.ATOM) {
				String atom = token.toString();
				context.append(atom).append(' ');
				inputKindList.add(atom);
				token = tokenScanner.getToken();
			}
			final int length = context.length();
			context.delete(length - 1, length);
			context.append(')');
			if (token.tokenClass != Token.TokenClass.END_EXP)
				throw new SyntaxException("Expected \")\"", context.toString());
		} catch (NullPointerException e) {
			throw new SyntaxException("Unexpected end of input", context.toString());
		} catch (ScannerException e) {
			throw new SyntaxException("Scanner error", context.toString());
		}
	}

	public @Override void execute() throws VerifyException {
		InterfaceData data = (InterfaceData) this.data;
		try {
			data.defineTerm(name, kind, inputKindList);
		} catch (DataException e) {
			throw new VerifyException("Data error", name, e);
		}
	}

}
