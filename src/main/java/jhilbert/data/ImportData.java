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

package jhilbert.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jhilbert.data.Interface;
import jhilbert.data.InterfaceData;
import jhilbert.data.ModuleData;
import jhilbert.data.Statement;
import jhilbert.exceptions.DataException;

/**
 * Data collected in an {@link Interface} when it is imported.
 */
public class ImportData extends InterfaceData {

	/**
	 * Creates new ImportData on top of ModuleData.
	 *
	 * @param prefix interface prefix.
	 * @param parameters Iterator over interface parameters.
	 * @param moduleData module data.
	 *
	 * @throws NullPointerException if one of the parameters is <code>null</code>.
	 */
	public ImportData(final String prefix, final Iterator<Interface> parameters, final ModuleData moduleData) {
		super(prefix, parameters, moduleData);
	}

	/**
	 * Defines a new kind.
	 *
	 * @param kind kind to be defined (must not be <code>null</code>).
	 *
	 * @throws DataException if this kind is already defined.
	 */
	public void defineKind(final String kind) throws DataException {
		assert (kind != null): "Supplied kind is null.";
		final String fqKind = prefix + kind;
		if (kindMap.containsKey(fqKind))
			throw new DataException("Kind already defined", fqKind);
		localKindMap.put(kind, fqKind);
		strictlyLocalKindMap.put(kind, fqKind);
		kindMap.put(fqKind, fqKind);
	}

	public @Override void bindKind(final String oldKind, final String newKind) throws DataException {
		checkBindKind(oldKind, newKind);
		final String fqOldKind = kindMap.get(prefix + oldKind);
		// effect new kind binding in all three kind maps
		kindMap.put(prefix + newKind, fqOldKind);
		localKindMap.put(newKind, fqOldKind);
		strictlyLocalKindMap.put(newKind, fqOldKind);
	}

	public @Override void defineTerm(final String name, final String kind, final List<String> inputKindList) throws DataException {
		assert (name != null): "Specified name is null.";
		assert (kind != null): "Specified kind is null.";
		assert (inputKindList != null): "Specified input kind list is null.";
		if (!localKindMap.containsKey(kind))
			throw new DataException("Kind not defined", kind);
		final String fqKind = localKindMap.get(kind);
		final String fqName = prefix + name;
		if (terms.containsKey(fqName))
			throw new DataException("Term already defined", fqName);
		List<String> fqInputKindList = new ArrayList(inputKindList.size());
		for (String inputKind: inputKindList) {
			if (!localKindMap.containsKey(inputKind))
				throw new DataException("Kind not defined", inputKind);
			fqInputKindList.add(localKindMap.get(inputKind));
		}
		termNameMap.put(name, fqName);
		newTermNames.add(name);
		terms.put(fqName, new ComplexTerm(fqName, fqKind, fqInputKindList));
	}

	public @Override void defineStatement(final Statement statement) throws DataException {
		// prefix voodoo already done by TermExpression and AbstractStatementCommand
		defineSymbol(statement);
	}

}
