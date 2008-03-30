package jhilbert.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jhilbert.data.Interface;
import jhilbert.data.ModuleData;
import jhilbert.data.Statement;
import jhilbert.data.Symbol;
import jhilbert.exceptions.DataException;

/**
 * Data collected in an {@link Interface}.
 * This data structure is meant to be put on top of a {@link ModuleData} structure.
 * <p>
 * The parameters to the methods and constructors of this class must never be <code>null</code>.
 * Otherwise, the behavior of the whole class is undefined.
 */
public abstract class InterfaceData extends ModuleData {

	/**
	 * Interface prefix.
	 */
	protected final String prefix;

	/**
	 * Interface parameters.
	 */
	protected final Iterator<Interface> parameters;

	/**
	 * Local kind map (including kinds from {@link Interface} parameters.
	 */
	protected final Map<String, String> localKindMap;

	/**
	 * Local kind map (kinds from the current {@link Interface} only).
	 */
	protected final Map<String, String> strictlyLocalKindMap;

	/**
	 * Local symbols.
	 */
	protected final Map<String, Symbol> localSymbols;

	/**
	 * Map mapping local term names to global ones.
	 */
	protected final Map<String, String> termNameMap;

	/**
	 * Set of term names defined in the current interface.
	 */
	protected final Set<String> newTermNames;

	/**
	 * Creates new InterfaceData on top of ModuleData.
	 *
	 * @param prefix interface prefix.
	 * @param parameters Iterator over interface parameters.
	 * @param moduleData module data.
	 */
	protected InterfaceData(final String prefix, final Iterator<Interface> parameters, final ModuleData moduleData) {
		super(moduleData);
		assert (prefix != null): "Supplied prefix is null.";
		assert (parameters != null): "Supplied parameters are null.";
		this.prefix = prefix;
		this.parameters = parameters;
		localKindMap = new HashMap();
		strictlyLocalKindMap = new HashMap();
		localSymbols = new HashMap();
		termNameMap = new HashMap();
		newTermNames = new HashSet();
	}

	/**
	 * Makes sure the definition of the specified kind is satisfied.
	 * The action of this method depends on whether the new kind is imported or exported.
	 * When the kind is imported, it will be defined. When it is exported, it will be checked whether it is defined.
	 *
	 * @param kind kind whose definition is to be satisfied.
	 *
	 * @throws DataException if this is not possible.
	 */
	public abstract void defineKind(final String kind) throws DataException;

	/**
	 * Performs checks on kinds to be bound in interfaces.
	 *
	 * @param oldKind old kind.
	 * @param newKind new kind.
	 *
	 * @throws DataException if the kinds are not locally defined.
	 */
	protected final void checkBindKind(final String oldKind, final String newKind) throws DataException {
		assert (oldKind != null): "Supplied old kind is null.";
		assert (newKind != null): "Supplied new kind is null.";
		if (!localKindMap.containsKey(oldKind))
			throw new DataException("Old kind not defined", oldKind);
		if (!localKindMap.containsKey(newKind))
			throw new DataException("New kind not defined", newKind);
	}

	public @Override abstract void bindKind(final String oldKind, final String newKind) throws DataException;

	/**
	 * Checks whether the specified local kind is defined.
	 *
	 * @param kind kind to check for.
	 *
	 * @return <code>true</code> if the kind is defined, <code>false</code> otherwise.
	 */
	public final @Override boolean containsLocalKind(final String kind) {
		assert (kind != null): "Supplied kind is null.";
		return localKindMap.containsKey(kind);
	}

	/**
	 * Defines a kind locally.
	 *
	 * @param name kind name.
	 * @param kind kind the specified name should map to.
	 *
	 * @throws DataException if a kind with the specified name already exists.
	 */
	public void defineLocalKind(final String name, final String kind) throws DataException {
		assert (name != null): "Specified name is null.";
		assert (kind != null): "Specified kind is null.";
		if (localKindMap.containsKey(name))
			throw new DataException("Local kind already defined", name);
		localKindMap.put(name, kind);
	}

	/**
	 * Returns the specified local kind.
	 *
	 * @param kind kind to be returned.
	 *
	 * @return the specified local kind, or the kind it is bound to, or <code>null</code>, if the specified kind was never defined.
	 */
	public final @Override String getLocalKind(final String kind) {
		return localKindMap.get(kind);
	}

	public @Override void defineVariable(final Variable variable) throws DataException {
		assert (variable != null): "Supplied variable is null.";
		final String name = variable.getName();
		if (localSymbols.containsKey(name))
			throw new DataException("Variable already defined", name);
		localSymbols.put(name, variable);
	}

	public final @Override boolean containsLocalTerm(final String name) {
		final String term = termNameMap.get(name);
		if (term == null)
			return containsTerm(name);
		else
			return containsTerm(term);
	}

	/**
	 * Makes sure the definition of the specified new term is satisfied.
	 * The action of this method depends on whether the new term is imported or exported.
	 * When the term is imported, it will be defined. When it is exported, it will be checked whether it is properly defined.
	 *
	 * @param name name of the new term.
	 * @param kind result kind of the new term.
	 * @param inputKindList list of input kinds.
	 *
	 * @throws DataException if this is not possible.
	 */
	public abstract void defineTerm(final String name, final String kind, final List<String> inputKindList) throws DataException;

	/**
	 * Defines a new local term.
	 *
	 * @param name name of new local term.
	 * @param term name of target term.
	 *
	 * @throws DataException if term is not defined, or name is already defined.
	 */
	public void defineLocalTerm(final String name, final String term) throws DataException {
		assert (name != null): "Supplied name is null.";
		if (!containsTerm(term))
			throw new DataException("Term not defined", term);
		if (termNameMap.containsKey(name))
			throw new DataException("Local term already defined", name);
		termNameMap.put(name, term);
	}

	public final @Override AbstractComplexTerm getLocalTerm(final String name) {
		final String term = termNameMap.get(name);
		if (term == null)
			return getTerm(name);
		else
			return getTerm(term);
	}

	/**
	 * Makes sure the definition of the specified statement is satisfied.
	 * The action of this method depends on whether the statement is imported or exported.
	 * When the statement is imported, it will be defined. When it is exported, it will be checked whether it is properly defined.
	 *
	 * @param statement statement whose definition is to be ensured.
	 *
	 * @throws DataException if this is not possible.
	 */
	public abstract void defineStatement(final Statement statement) throws DataException;

	/**
	 * Returns the namespace prefix of the current interface.
	 *
	 * @return namespace prefix of the current interface.
	 */
	public final String getPrefix() {
		return prefix;
	}

	/**
	 * Returns the next parameter.
	 *
	 * @return next parameter.
	 *
	 * @throws DataException if there are no more parameters.
	 */
	public final Interface getNextParameter() throws DataException {
		if (!parameters.hasNext())
			throw new DataException("Interface loaded with too few parameters", getClass().getName());
		return parameters.next();
	}

	/**
	 * Returns the strictly local kind map.
	 * Package access only. Used by {@link Interface}.
	 *
	 * @return kinds defined in the current interface only.
	 */
	final Map<String, String> getStrictlyLocalKindMap() {
		return strictlyLocalKindMap;
	}

	/**
	 * Returns the newly defined term names.
	 * Package access only. Used by {@link Interface}.
	 *
	 * @return names of the new terms defined in the current interface.
	 */
	final Set<String> getNewTermNames() {
		return newTermNames;
	}

	public @Override boolean containsLocalSymbol(final String name) {
		return localSymbols.containsKey(name);
	}

	public @Override Symbol getLocalSymbol(final String name) {
		return localSymbols.get(name);
	}

	/**
	 * Finalize this interface.
	 * Call this method before calling {@link #getStrictlyLocalKindMap()} or {@link #getNewTermNames()}.
	 * Package access only. Used by {@link Interface}.
	 *
	 * @throws DataException if a problem occurs.
	 */
	void finalizeInterface() throws DataException {
		if (parameters.hasNext())
			throw new DataException("Interface loaded with too many parameters", getClass().getName());
	}

	public @Override String toString() {
		return	  super.toString()
			+ ", Prefix: " + prefix
			+ ", Parameter iterator: " + parameters.toString()
			+ ", Local kind map: " + localKindMap.toString()
			+ ", Strictly local kind map: " + strictlyLocalKindMap.toString()
			+ ", Local symbols: " + localSymbols.toString()
			+ ", Term name map: " + termNameMap.toString()
			+ ", New term names: " + newTermNames.toString();
	}

}
