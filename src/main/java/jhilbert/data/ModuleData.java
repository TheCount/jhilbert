package jhilbert.data;

import java.util.HashMap;
import java.util.Map;
import jhilbert.data.AbstractComplexTerm;
import jhilbert.data.Definition;
import jhilbert.data.Interface;
import jhilbert.data.Symbol;
import jhilbert.data.Variable;
import jhilbert.exceptions.DataException;
import org.apache.log4j.Logger;

/**
 * Data collected in a proof module.
 */
public class ModuleData implements Cloneable {

	/**
	 * Kind map.
	 */
	protected final Map<String, String> kindMap;

	/**
	 * Complex terms.
	 */
	protected final Map<String, AbstractComplexTerm> terms;

	/**
	 * Symbols.
	 */
	protected final Map<String, Symbol> symbols;

	/**
	 * Interfaces.
	 */
	protected final Map<String, Interface> interfaces;
	
	/**
	 * Creates a new instance of module data.
	 */
	public ModuleData() {
		kindMap = new HashMap();
		terms = new HashMap();
		symbols = new HashMap();
		interfaces = new HashMap();
	}

	/**
	 * Copy constructor.
	 * Creates a shallow copy.
	 *
	 * @param moduleData module data to be copied (must not be <code>null</code>).
	 */
	protected ModuleData(final ModuleData moduleData) {
		assert (moduleData != null): "Supplied module data is null.";
		kindMap = moduleData.kindMap;
		terms = moduleData.terms;
		symbols = moduleData.symbols;
		interfaces = moduleData.interfaces;
	}

	/**
	 * Creates a deep copy of the maps in this object.
	 * Keys and values are copied shallowly.
	 */
	public ModuleData clone() {
		ModuleData result = new ModuleData();
		result.kindMap.putAll(kindMap);
		result.terms.putAll(terms);
		result.symbols.putAll(symbols);
		result.interfaces.putAll(interfaces);
		return result;
	}

	/**
	 * Checks whether the specified kind is defined.
	 *
	 * @param kind kind to check for (must not be <code>null</code>).
	 *
	 * @return <code>true</code> if the kind is defined, <code>false</code> otherwise.
	 */
	public boolean containsKind(final String kind) {
		assert (kind != null): "Supplied kind is null.";
		final Logger logger = Logger.getLogger(getClass());
		if (logger.isTraceEnabled())
			logger.trace("Current kind map: " + kindMap);
		return kindMap.containsKey(kind);
	}

	/**
	 * Returns the specified kind.
	 * <p>
	 * For kinds defined with a {@link jhilbert.commands.KindCommand}, this method
	 * simply returns the specified kind.
	 * <p>
	 * For kind defined with a {@link jhilbert.commands.KindbindCommand}, this methid
	 * returns the kind which the specified kind was bound to.
	 *
	 * @param kind kind to be returned.
	 *
	 * @return the specified kind, or the kind it is bound to, or <code>null</code>, if the specified kind was never defined.
	 */
	public String getKind(final String kind) {
		return kindMap.get(kind);
	}

	/**
	 * Defines a new kind by binding it to an old one.
	 * The parameters must not be <code>null</code>.
	 *
	 * @param oldKind an already defined kind.
	 * @param newKind kind to be bound to oldKind.
	 *
	 * @throws DataException if oldKind does not exist, or newKind already exists.
	 */
	public void bindKind(final String oldKind, final String newKind) throws DataException {
		if (!containsKind(oldKind))
			throw new DataException("Kind not found", oldKind);
		if (containsKind(newKind))
			throw new DataException("Kind already exists", newKind);
		kindMap.put(newKind, kindMap.get(oldKind));
	}

	/**
	 * Checks whether the specified local kind is defined.
	 * This method is identical to {@link #containsKind()}.
	 * However, subclasses may override this method.
	 *
	 * @param kind kind to check for (must not be <code>null</code>).
	 *
	 * @return <code>true</code> if the kind is defined, <code>false</code> otherwise.
	 */
	public boolean containsLocalKind(final String kind) {
		return containsKind(kind);
	}

	/**
	 * Returns the specified local kind.
	 * This method is identical to {@link #getKind()}.
	 * However, subclasses may override this method.
	 *
	 * @param kind kind to be returned.
	 *
	 * @return the specified local kind, or the kind it is bound to, or <code>null</code>, if the specified kind was never defined.
	 */
	public String getLocalKind(final String kind) {
		return getKind(kind);
	}

	/**
	 * Checks whether a {@link AbstractComplexTerm} with the specified name is defined.
	 *
	 * @param name term name (must not be <code>null</code>).
	 *
	 * @return <code>true</code> if a complex term with this name is defined, <code>false</code> otherwise.
	 */
	public boolean containsTerm(final String name) {
		assert (name != null): "Supplied term name is null.";
		return terms.containsKey(name);
	}

	/**
	 * Checks whether a {@link AbstractComplexTerm} with the specified local name is defined.
	 * For proof modules, this method is identical to {@link #containsTerm()}.
	 * In {@link InterfaceData}, a local translation is tried before falling back to containsTerm().
	 *
	 * @param name local term name (must not be <code>null</code>).
	 *
	 * @return <code>true</code> if a complex term with this local name is defined, <code>false</code> otherwise.
	 */
	public boolean containsLocalTerm(final String name) {
		return containsTerm(name);
	}

	/**
	 * Defines a new complex term.
	 *
	 * @param term AbstractComplexTerm to be defined (must not be <code>null</code>).
	 *
	 * @throws DataException if the specified complex term already exists.
	 */
	public void defineTerm(final Definition term) throws DataException {
		final String name = term.getName();
		if (containsTerm(name))
			throw new DataException("Term already defined", name);
		terms.put(name, term);
	}

	/**
	 * Returns the {@link AbstractComplexTerm} with the specified name.
	 *
	 * @param name name of the term.
	 *
	 * @return the complex term with the specified name, or <code>null</code> if no complex term with that name exists.
	 */
	public AbstractComplexTerm getTerm(final String name) {
		return terms.get(name);
	}

	/**
	 * Returns the {@link AbstractComplexTerm} with the specified local name.
	 * For proof modules, this method is identical to {@link #getTerm()}.
	 * In {@link InterfaceData}, a local translation is tried before falling back to getTerm().
	 *
	 * @param name local name of the term.
	 *
	 * @return the complex term with the specified local name, or <code>null</code> if no complex term with that name exists.
	 */
	public AbstractComplexTerm getLocalTerm(final String name) {
		return getTerm(name);
	}

	/**
	 * Checks whether a {@link Symbol} with the specified name is defined.
	 *
	 * @param name symbol name (must not be <code>null</code>).
	 *
	 * @return <code>true</code> if a symbol with this name is defined, <code>false</code> otherwise.
	 */
	public boolean containsSymbol(final String name) {
		assert (name != null): "Supplied symbol name is null.";
		return symbols.containsKey(name);
	}

	/**
	 * Defines a new symbol.
	 *
	 * @param symbol Symbol to be defined (must not be <code>null</code>).
	 *
	 * @throws DataException if the specified symbol already exists.
	 */
	public void defineSymbol(final Symbol symbol) throws DataException {
		final String name = symbol.getName();
		if (containsSymbol(name))
			throw new DataException("Symbol already defined", name);
		symbols.put(name, symbol);
	}

	/**
	 * Defines a new variable.
	 * Variables are special in that they are defined only locally.
	 *
	 * @param variable Variable to be defined (must not be <code>null</code>).
	 *
	 * @throws DataException if a symbol with the name of the specified variable already exists.
	 */
	public void defineVariable(final Variable variable) throws DataException {
		defineSymbol(variable);
	}

	/**
	 * Returns the {@link Symbol} with the specified name.
	 *
	 * @param name name of the symbol.
	 *
	 * @return the symbol with the specified name, or <code>null</code> if no symbol with that name exists.
	 */
	public Symbol getSymbol(final String name) {
		return symbols.get(name);
	}

	/**
	 * Checks whether a local {@link Symbol} with the specified name is defined.
	 * This method is identical to {@link #containsSymbol()}. Subclasses may override this method to provide access to
	 * local symbols.
	 *
	 * @param name symbol name (must not be <code>null</code>).
	 *
	 * @return <code>true</code> if a local symbol with this name is defined, <code>false</code> otherwise.
	 */
	public boolean containsLocalSymbol(final String name) {
		return containsSymbol(name);
	}

	/**
	 * Returns the local {@link Symbol} with the specified name.
	 * This method is identical to {@link #getSymbol()}. Subclasses may override this method to provide access to
	 * local symbols.
	 *
	 * @param name name of the symbol.
	 *
	 * @return the local symbol with the specified name, or <code>null</code> if no local symbol with that name exists.
	 */
	public Symbol getLocalSymbol(final String name) {
		return getSymbol(name);
	}

	/**
	 * Checks whether the named variable is defined.
	 * This is a convenience method which checks if the local symbol is defined
	 * and then checks whether it is a variable.
	 *
	 * @param name variable name (must not be <code>null</code>).
	 *
	 * @return <code>true</code> if a variable with the specified name is defined, <code>false</code> otherwise.
	 */
	public boolean containsVariable(final String name) {
		assert (name != null): "Supplied name is null.";
		Symbol s = getLocalSymbol(name);
		return (s instanceof Variable);
	}

	/**
	 * Checks whether an {@link Interface} with the specified name is defined.
	 *
	 * @param name name of interface (must not be <code>null</code>).
	 *
	 * @return <code>true</code> if an Interface with the specified name is defined, <code>false</code> otherwise.
	 */
	public boolean containsInterface(final String name) {
		assert (name != null): "Supplied name is null.";
		return interfaces.containsKey(name);
	}

	/**
	 * Defines a new Interface.
	 *
	 * @param iface Interface to define (must not be <code>null</code>).
	 *
	 * @throws DataException if the interface is already defined.
	 */
	public void defineInterface(final Interface iface) throws DataException {
		assert (iface != null): "Supplied interface is null.";
		final String name = iface.getName();
		if (containsInterface(name))
			throw new DataException("Interface already defined", name);
		interfaces.put(name, iface);
	}

	/**
	 * Returns the {@link Interface} with the specified name.
	 *
	 * @param name name of the interface.
	 *
	 * @return Interface with the specified name, or <code>null</code> if no interface with this name is defined.
	 */
	public Interface getInterface(final String name) {
		return interfaces.get(name);
	}

	/**
	 * Returns the namespace prefix.
	 * For proof modules, this is always the empty string.
	 * However, subclasses of {@link ModuleData} may override this method.
	 *
	 * @return empty string.
	 */
	public String getPrefix() {
		return "";
	}

	public @Override String toString() {
		return	  "Kind map: " + kindMap.toString()
			+ "\nTerms: " + terms.toString()
			+ "\nSymbols: " + symbols.toString()
			+ "\nInterfaces: " + interfaces.toString();
	}

}
