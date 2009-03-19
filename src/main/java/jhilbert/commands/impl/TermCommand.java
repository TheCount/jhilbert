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

import java.util.ArrayList;
import java.util.List;

import jhilbert.commands.CommandException;
import jhilbert.commands.SyntaxException;

import jhilbert.data.DataException;
import jhilbert.data.DataFactory;
import jhilbert.data.Functor;
import jhilbert.data.Kind;
import jhilbert.data.Module;
import jhilbert.data.Namespace;

import jhilbert.scanners.ScannerException;
import jhilbert.scanners.Token;
import jhilbert.scanners.TokenScanner;

import jhilbert.utils.TreeNode;

import org.apache.log4j.Logger;

/**
 * Command to introduce a new {@link jhilbert.data.Functor}.
 */
public final class TermCommand extends AbstractCommand {

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(TermCommand.class);

	/**
	 * Term name.
	 */
	private final String name;

	/**
	 * Result kind name.
	 */
	private final String kindName;

	/**
	 * List of input kind names.
	 */
	private final List<String> inputKindNameList;

	/**
	 * Creates a new <code>TermCommand</code>.
	 *
	 * @param module {@link Module} to add functor to.
	 * @param tokenScanner {@link TokenScanner} to obtain functor data.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 */
	public TermCommand(final Module module, final TokenScanner tokenScanner) throws SyntaxException {
		super(module);
		try  {
			kindName = tokenScanner.getAtom();
			tokenScanner.beginExp();
			name = tokenScanner.getAtom();
			inputKindNameList = new ArrayList();
			Token token = tokenScanner.getToken();
			while (token.getTokenClass() == Token.Class.ATOM) {
				inputKindNameList.add(token.getTokenString());
				token = tokenScanner.getToken();
			}
			if (token.getTokenClass() != Token.Class.END_EXP) {
				logger.error("Expected end of LISP s-expression");
				logger.debug("Current scanner context: " + tokenScanner.getContextString());
				throw new SyntaxException("Expected end of expression");
			}
		} catch (NullPointerException e) {
			logger.error("Unexpected end of input while scanning term command", e);
			throw new SyntaxException("Unexpected end of input", e);
		} catch (ScannerException e) {
			logger.error("Scanner error while scanning term command", e);
			logger.debug("Scanner context: " + e.getScanner().getContextString());
			throw new SyntaxException("Scanner error", e);
		}
	}

	/**
	 * Creates a new <code>TermCommand</code>.
	 *
	 * @param module {@link Module} to add functor to.
	 * @param tree synatx tree to obtain functor data from.
	 *
	 * @throws SyntaxException if a syntax error occurs.
	 */
	public TermCommand(final Module module, final TreeNode<String> tree) throws SyntaxException {
		super (module);
		assert (tree != null): "Supplied LISP tree is null";
		final List<? extends TreeNode<String>> children = tree.getChildren();
		try {
			if (children.size() != 2)
				throw new IndexOutOfBoundsException();
			kindName = children.get(0).getValue();
			if (kindName == null)
				throw new NullPointerException();
			final List<? extends TreeNode<String>> termspec = children.get(1).getChildren();
			name = termspec.get(0).getValue();
			if (name == null)
				throw new NullPointerException();
			final int size = termspec.size();
			inputKindNameList = new ArrayList(size - 1);
			for (int i = 1; i != size; ++i) {
				final String kindName = termspec.get(i).getValue();
				if (kindName == null)
					throw new NullPointerException();
				inputKindNameList.add(kindName);
			}
			if (inputKindNameList.size() == 0)
				throw new IndexOutOfBoundsException();
		} catch (RuntimeException e) {
			logger.error("Expected (kind (termName kind1 ... kindN)), got " + children);
			throw new SyntaxException("Expected (kind (termName kind1 ... kindN))", e);
		}
	}

	public @Override void execute() throws CommandException {
		final Module module = getModule();
		final Namespace<? extends Kind> kindNamespace = module.getKindNamespace();
		assert (kindNamespace != null): "Module supplied null kind namespace";
		final Namespace<? extends Functor> functorNamespace = module.getFunctorNamespace();
		assert (functorNamespace != null): "Module supplied null functor namespace";
		try {
			final Kind kind = kindNamespace.getObjectByString(kindName);
			if (kind == null) {
				logger.error("Kind " + kindName + " not found");
				throw new CommandException("Kind not found");
			}
			final List<Kind> inputKindList = new ArrayList(inputKindNameList.size());
			for (final String inputKindName: inputKindNameList) {
				final Kind inputKind = kindNamespace.getObjectByString(inputKindName);
				if (inputKind == null) {
					logger.error("Input kind " + inputKindName + " not found");
					throw new CommandException("Input kind not found");
				}
				inputKindList.add(inputKind);
			}
			final DataFactory dataFactory = DataFactory.getInstance();
			dataFactory.createFunctor(name, kind, inputKindList, functorNamespace);
		} catch (DataException e) {
			logger.error("Unable to define functor " + name, e);
			throw new CommandException("Unable to define functor", e);
		}
	}

}
