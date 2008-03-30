package jhilbert.data;

import java.util.Map;
import java.util.Set;
import jhilbert.commands.Command;
import jhilbert.data.InterfaceData;
import jhilbert.exceptions.DataException;
import jhilbert.exceptions.InputException;
import jhilbert.exceptions.ScannerException;
import jhilbert.exceptions.VerifyException;
import jhilbert.util.CommandScanner;
import jhilbert.util.InputSource;
import jhilbert.util.InputSourceFactory;
import org.apache.log4j.Logger;

/**
 * An Interface.
 * Each interface comes with a namespace prefix, its newly defined kinds and the names of its newly defined terms.
 * All other data are added to the {@link ModuleData} while the interface is loaded.
 */
public class Interface extends AbstractName {

	/**
	 * Locator.
	 */
	private final String locator;

	/**
	 * Namespace prefix.
	 */
	private final String prefix;

	/**
	 * Kind map of this interface.
	 */
	private final Map<String, String> kindMap;

	/**
	 * The names of the terms defined in this interface.
	 */
	private final Set<String> termNames;

	/**
	 * Creates a new Interface.
	 *
	 * @param name name of the new Interface
	 * @param locator String determining the location of the {@link jhilbert.util.InputSource} for the interface.
	 * @param data Interface data structure to collect data in and create the interface from.
	 *
	 * @throws DataException if the interface cannot be created.
	 */
	public Interface(final String name, final String locator, final InterfaceData data) throws DataException {
		super(name);
		assert (locator != null): "Supplied locator is null.";
		assert (data != null): "Supplied data are null.";
		this.locator = locator;
		Logger logger = Logger.getLogger(getClass());
		prefix = data.getPrefix();
		try {
			logger.info("Scanning interface " + locator);
			final InputSource inputSource = InputSourceFactory.createInterfaceInputSource(locator);
			final CommandScanner commandScanner = new CommandScanner(inputSource, data, Command.INTERFACE_COMMANDS);
			for (Command c = commandScanner.getToken(); c != null; c = commandScanner.getToken()) {
				if (logger.isDebugEnabled())
					logger.debug(commandScanner.getContextString());
				c.execute();
				commandScanner.resetContext();
			}
			data.finalizeInterface();
			kindMap = data.getStrictlyLocalKindMap();
			termNames = data.getNewTermNames();
		} catch (InputException e) {
			throw new DataException("Unable to access input source", locator, e);
		} catch (ScannerException e) {
			throw new DataException("Scanner error", "interface " + name, e);
		} catch (VerifyException e) {
			throw new DataException("A command in the interface source failed to verify", locator, e);
		}
	}

	/**
	 * Returns the locator from which this interface was loaded.
	 *
	 * @return locator from which this interface was loaded.
	 */
	public String getLocator() {
		return locator;
	}

	/**
	 * Returns the namespace prefix of this interface.
	 *
	 * @return namespace prefix of this interface.
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * Returns the kind map of this interface.
	 *
	 * @return the kind map of this interface.
	 */
	public Map<String, String> getKindMap() {
		return kindMap;
	}

	/**
	 * Returns the set of new term names of this interface.
	 *
	 * @return the set of new term names of this interface.
	 */
	public Set<String> getTermNames() {
		return termNames;
	}

}
