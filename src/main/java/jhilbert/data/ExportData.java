package jhilbert.data;

import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jhilbert.data.AbstractComplexTerm;
import jhilbert.data.DVConstraints;
import jhilbert.data.Interface;
import jhilbert.data.InterfaceData;
import jhilbert.data.ModuleData;
import jhilbert.data.Statement;
import jhilbert.exceptions.DataException;
import org.apache.log4j.Logger;

/**
 * Data collected in an {@link Interface} when it is exported.
 */
public class ExportData extends InterfaceData {

	/**
	 * Logger.
	 */
	private static final Logger logger = Logger.getLogger(ExportData.class);

	/**
	 * Creates new ExportData on top of ModuleData.
	 *
	 * @param prefix interface prefix.
	 * @param parameters Iterator over interface parameters.
	 * @param moduleData module data.
	 *
	 * @throws NullPointerException if one of the parameters is <code>null</code>.
	 */
	public ExportData(final String prefix, final Iterator<Interface> parameters, final ModuleData moduleData) {
		super(prefix, parameters, moduleData);
	}

	/**
	 * Checks whether the specified kind is defined and introduces it into the local namespace.
	 *
	 * @param kind kind to be defined.
	 *
	 * @throws NullPointerException if kind is <code>null</code>.
	 * @throws DataException if this kind is already defined.
	 */
	public void defineKind(final String kind) throws DataException {
		assert (kind != null): "Supplied kind is null.";
		final String fqKind = prefix + kind;
		final String definedKind = kindMap.get(fqKind);
		if (definedKind == null)
			throw new DataException("Kind not defined", fqKind);
		localKindMap.put(kind, definedKind);
		strictlyLocalKindMap.put(kind, definedKind);
	}

	public @Override void bindKind(final String oldKind, final String newKind) throws DataException {
		checkBindKind(oldKind, newKind);
		final String fqOldKind = prefix + oldKind;
		final String fqNewKind = prefix + newKind;
		if (!kindMap.get(fqOldKind).equals(kindMap.get(fqNewKind)))
			throw new DataException("Incompatible kindbind", fqOldKind + "/" + fqNewKind);
	}

	public @Override void defineTerm(final String name, final String kind, final List<String> inputKindList) throws DataException {
		assert (name != null): "Supplied name is null.";
		assert (kind != null): "Supplied kind is null.";
		assert (inputKindList != null): "Supplied input kind list is null.";
		final String fqKind = kindMap.get(localKindMap.get(kind));
		if (fqKind == null)
			throw new DataException("Kind not defined", kind);
		final String fqName = prefix + name;
		if (!terms.containsKey(fqName))
			throw new DataException("Term not defined", fqName);
		final AbstractComplexTerm term = terms.get(fqName);
		// now check if result and input kinds match
		final String termKind = term.getKind();
		if (!fqKind.equals(termKind))
			throw new DataException("Result kinds do not match", fqKind + "/" + termKind);
		final int size = inputKindList.size();
		final int placeCount = term.placeCount();
		if (size != placeCount)
			throw new DataException("Wrong number of places in exported term", Integer.toString(size) + " != " + placeCount);
		for (int i = 0; i != size; ++i) {
			final String inputKind = inputKindList.get(i);
			final String fqInputKind = kindMap.get(localKindMap.get(inputKind));
			if (fqInputKind == null)
				throw new DataException("Kind not defined", inputKind);
			final String termInputKind = term.getInputKind(i);
			if (!fqInputKind.equals(termInputKind))
				throw new DataException("Input kinds do not match at place " + (i + 1), fqInputKind + "/" + termInputKind);
		}
		termNameMap.put(name, fqName);
		newTermNames.add(name);
	}

	public @Override void defineStatement(final Statement statement) throws DataException {
		assert (statement != null): "Supplied statement is null.";
		final String name = statement.getName();
		if (!containsSymbol(name)) {
			logger.error("Statement " + name + " not defined");
			logger.debug("Defined symbols: " + symbols);
			throw new DataException("Statement not defined", name);
		}
		Statement dStatement;
		try {
			dStatement = (Statement) getSymbol(name);
		} catch (ClassCastException e) {
			throw new DataException("Defined symbol is not a statement", name);
		}
		// check hypotheses
		final List<TermExpression> hypotheses = statement.getHypotheses();
		final List<TermExpression> dHypotheses = dStatement.getHypotheses();
		final int size = hypotheses.size();
		if (size != dHypotheses.size())
			throw new DataException("Defined statement has different number of hypotheses", name);
		final Map<Variable, Variable> translationMap = new HashMap();
		for (int i = 0; i != size; ++i)
			dHypotheses.get(i).equalityMap(hypotheses.get(i), translationMap);
		// check consequent
		dStatement.getConsequent().equalityMap(statement.getConsequent(), translationMap);
		// check variables
		if (translationMap.keySet().size() != translationMap.values().size())
			throw new DataException("Obtained variable mapping is not bijective", name);
		final DVConstraints dvConstraints = statement.getDVConstraints();
		for (VariablePair p: dStatement.getDVConstraints())
			if (!dvConstraints.contains(new VariablePair(translationMap.get(p.getFirst()), translationMap.get(p.getSecond()))))
				throw new DataException("Missing disjoint variable constraint: (" + translationMap.get(p.getFirst()) + ", "
					+ translationMap.get(p.getSecond()) + ")", name);
	}

}
