package jhilbert;

import jhilbert.commands.Command;
import jhilbert.data.ModuleData;
import jhilbert.exceptions.GeneralException;
import jhilbert.util.FileInputSource;
import jhilbert.util.CommandScanner;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Main class.
 */
public class Main
{

	/**
	 * FIXME
	 *
	 * @param args command line arguments.
	 *
	 * @throws Exception if anything out of the ordinary happens.
	 */
	public static void main(String[] args) throws Exception {
		BasicConfigurator.configure();
		final Logger logger = Logger.getRootLogger();
		try {
			logger.setLevel(Level.INFO);
			String inputFileName = null;
			for (String arg: args) {
				logger.info("Command line argument: " + arg);
				if (arg.startsWith("-l"))
					logger.setLevel(Level.toLevel(arg.substring(2)));
				else
					inputFileName = arg;
			}
			logger.info("Scanning file " + inputFileName);
			ModuleData md = new ModuleData();
			CommandScanner cs = new CommandScanner(new FileInputSource(inputFileName), md, Command.MODULE_COMMANDS);
			for (Command c = cs.getToken(); c != null; c = cs.getToken()) {
				if (logger.isDebugEnabled())
					logger.debug(cs.getContextString() + " " + c.getName());
				c.execute();
				cs.resetContext();
			}
//			logger.info("Finished. Press Return to exit.");
//			System.in.read();
		} catch (GeneralException e) {
			throw e;
		} catch (Exception e) {
			logger.fatal("Caught unexpected exception:");
			throw e;
		}
	}

}
