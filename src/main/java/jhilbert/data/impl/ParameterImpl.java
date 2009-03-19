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

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jhilbert.data.DataException;
import jhilbert.data.Module;
import jhilbert.data.Parameter;

import jhilbert.scanners.ScannerException;
import jhilbert.scanners.Token;
import jhilbert.scanners.TokenScanner;

import jhilbert.utils.TreeNode;

import org.apache.log4j.Logger;

/**
 * {@link Parameter} implementation.
 */
final class ParameterImpl implements Parameter, Serializable {

	/**
	 * Serialisation ID.
	 */
	private static final long serialVersionUID = jhilbert.Main.VERSION;

	/**
	 * Logger for this class.
	 */
	private static final Logger logger = Logger.getLogger(ParameterImpl.class);

	/**
	 * Parameter name.
	 */
	private final String name;

	/**
	 * Module locator.
	 */
	private final String locator;

	/**
	 * Parameter list.
	 */
	private final List<Parameter> parameterList;

	/**
	 * Namespace prefix.
	 */
	private final String prefix;

	/**
	 * Default constructor, for serialisation use only!
	 */
	ParameterImpl() {
		name = null;
		locator = null;
		parameterList = null;
		prefix = null;
	}

	/**
	 * Creates a new <code>ParameterImpl</code> with the specified name,
	 * locator, parameter list and prefix.
	 *
	 * @param name name of new parameter.
	 * @param locator module locator of new parameter.
	 * @param parameterList parameter list.
	 * @param prefix namespace prefix.
	 */
	ParameterImpl(final String name, final String locator, final List<Parameter> parameterList, final String prefix) {
		assert (name != null): "Supplied name is null";
		assert (locator != null): "Supplied locator is null";
		assert (parameterList != null): "Supplied parameter list is null";
		assert (prefix != null): "Supplied prefix is null";
		this.name = name;
		this.locator = locator;
		this.parameterList = Collections.unmodifiableList(parameterList);
		this.prefix = prefix;
	}

	/**
	 * Scans a new <code>ParameterImpl</code> from the specified token
	 * scanner using data from the specified module.
	 *
	 * @param module data module.
	 * @param tokenScanner {@link TokenScanner} to obtain parameter data.
	 *
	 * @throws DataException if a syntax error occurs.
	 */
	ParameterImpl(final Module module, final TokenScanner tokenScanner) throws DataException {
		assert (module != null): "Supplied module is null";
		assert (tokenScanner != null): "Supplied tokenScanner is null";
		try {
			name = tokenScanner.getAtom();
			locator = tokenScanner.getAtom();
			final List<Parameter> parameterList = new ArrayList();
			tokenScanner.beginExp();
			Token token = tokenScanner.getToken();
			while (token.getTokenClass() == Token.Class.ATOM) {
				final Parameter parameter = module.getParameter(token.getTokenString());
				if (parameter == null) {
					logger.error("Parameter " + token.getTokenString() + " unknown");
					logger.debug("Current scanner context: " + tokenScanner.getContextString());
					throw new DataException("Parameter unknown");
				}
				parameterList.add(parameter);
				token = tokenScanner.getToken();
			}
			if (token.getTokenClass() != Token.Class.END_EXP) {
				logger.error("Expected end of parameter list");
				logger.debug("Current scanner context: " + tokenScanner.getContextString());
				throw new DataException("Expected end of parameter list");
			}
			this.parameterList = Collections.unmodifiableList(parameterList);
			prefix = tokenScanner.getString();
		} catch (NullPointerException e) {
			logger.error("Unexpected end of input while scanning parameter", e);
			throw new DataException("Unexpected end of input while scanning parameter", e);
		} catch (ScannerException e) {
			logger.error("Scanner error while scanning parameter", e);
			logger.debug("Scanner context: " + e.getScanner().getContextString());
			throw new DataException("Scanner error while scanning parameter", e);
		}
	}

	/**
	 * Creates a new <code>ParameterImpl</code> from the specified syntax
	 * tree using data from the specified module.
	 *
	 * @param module data module.
	 * @param tree syntax tree.
	 *
	 * @throws DataException if a syntax error occurs.
	 */
	ParameterImpl(final Module module, final TreeNode<String> tree) throws DataException {
		assert (module != null): "Supplied module is null";
		assert (tree != null): "Supplied syntax tree is null";
		try {
			if (tree.getValue() != null)
				throw new NullPointerException();
			final List<? extends TreeNode<String>> children = tree.getChildren();
			if (children.size() != 4)
				throw new IndexOutOfBoundsException();
			name = children.get(0).getValue();
			if (name == null)
				throw new NullPointerException();
			locator = children.get(1).getValue();
			if (locator == null)
				throw new NullPointerException();
			// parameter list
			final TreeNode<String> parameterTree = children.get(2);
			if (parameterTree.getValue() != null)
				throw new NullPointerException();
			final List<? extends TreeNode<String>> parameterTreeList = parameterTree.getChildren();
			final List<Parameter> parameterList = new ArrayList(parameterTreeList.size());
			for (final TreeNode<String> listItem: parameterTreeList) {
				final String parameterName = listItem.getValue();
				if (parameterName == null)
					throw new NullPointerException();
				final Parameter parameter = module.getParameter(parameterName);
				if (parameter == null) {
					logger.error("Parameter " + parameterName + " unknown");
					throw new DataException("Parameter unknown");
				}
				parameterList.add(parameter);
			}
			this.parameterList = Collections.unmodifiableList(parameterList);
			// prefix
			final TreeNode<String> prefixTree = children.get(3);
			String prefix = prefixTree.getValue();
			if (prefix == null) {
				prefix = "";
				if (prefixTree.getChildren().size() != 0)
					throw new IndexOutOfBoundsException();
			}
			this.prefix = prefix;
		} catch (RuntimeException e) {
			logger.error("Syntax error in parameter specification, expected (newparam locator ([ param1 [ param2 [ ... [ paramN ] ... ] ] ]) prefix), got " + tree);
			throw new DataException("Syntax error in parameter specification", e);
		}
	}

	public String getName() {
		return name;
	}

	public String getLocator() {
		return locator;
	}

	public long getRevision() {
		return -1; // FIXME: always returns -1 right now
	}

	public List<Parameter> getParameterList() {
		return parameterList;
	}

	public String getPrefix() {
		return prefix;
	}

	public @Override String toString() {
		return name + '(' + locator + ')' + parameterList + '+' + prefix;
	}

}
