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
	 * @param kind kind to be defined.
	 *
	 * @throws NullPointerException if kind is <code>null</code>.
	 * @throws DataException if this kind is already defined.
	 */
	public void defineKind(final String kind) throws DataException {
		if (kind == null)
			throw new NullPointerException("Supplied kind is null.");
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
		if (name == null)
			throw new NullPointerException("Specified name is null.");
		if (kind == null)
			throw new NullPointerException("Specified kind is null.");
		if (inputKindList == null)
			throw new NullPointerException("Specified input kind list is null.");
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
		defineSymbol(statement);
	}

}
