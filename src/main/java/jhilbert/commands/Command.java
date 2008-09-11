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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import jhilbert.data.Module;

import jhilbert.scanners.TokenScanner;

/**
 * Basic interface for all commands.
 * Implementing classes <strong>must</strong> provide a constructor which takes
 * two arguments, the first of type {@link Module}, the second of type
 * {@link TokenScanner}. These constructors are then provided to the
 * {@link Command.Class}es and used to create commands.
 */
public interface Command {

	/**
	 * Command classes.
	 */
	public static enum Class {

		DEFINITION("def", true, true),
		EXPORT("export", true, false),
		IMPORT("import", true, false),
		KIND("kind", false, true),
		KINDBIND("kindbind", true, true),
		PARAMETER("param", false, true),
		STATEMENT("stmt", false, true),
		TERM("term", false, true),
		THEOREM("thm", true, false),
		VARIABLE("var", true, true);

		/**
		 * LISP atom signifying this command class.
		 */
		private final String atom;

		/**
		 * May the commands of this class occur in proof modules?
		 */
		private final boolean proofPermissible;

		/**
		 * May the commands of this class occur in interface modules?
		 */
		private final boolean interfacePermissible;

		/**
		 * Constructor for a command belonging to this class.
		 */
		private Constructor<? extends Command> constructor;

		/**
		 * Creates a new command class.
		 *
		 * @param atom LISP atom signifying this command class.
		 * @param proofPermissible <code>boolean</code> indicating
		 * 	whether the commands of this class may occur in
		 * 	proof modules.
		 * @param interfacePermissible <code>boolean</code> indicating
		 * 	whether the commands of this class may occur in
		 * 	interface modules.
		 */
		private Class(final String atom, final boolean proofPermissible, final boolean interfacePermissible) {
			assert(atom != null): "Supplied atom is null";
			this.atom = atom;
			this.proofPermissible = proofPermissible;
			this.interfacePermissible = interfacePermissible;
			constructor = null;
		}

		/**
		 * Obtains the atom of this command class.
		 *
		 * @return LISP atom of this command class.
		 */
		public String getAtom() {
			return atom;
		}

		/**
		 * Checks whether this command class may be used in proof
		 * modules.
		 *
		 * @return <code>true</code> if this command class may be used
		 * 	in proof modules, <code>false</code> otherwise.
		 */
		public boolean isProofPermissible() {
			return proofPermissible;
		}

		/**
		 * Checks whether this command class may be used in interface
		 * modules.
		 *
		 * @return <code>true</code> if this command class may be used
		 * 	in interface modules, <code>false</code> otherwise.
		 */
		public boolean isInterfacePermissible() {
			return interfacePermissible;
		}

		/**
		 * Sets the command constructor.
		 * This method should be called by a static initialiser of the
		 * command implementation.
		 *
		 * @param constructor command constructor. The constructor must
		 * 	accept precisely two parameters. The first parameter is
		 * 	a {@link jhilbert.data.Module}, the second parameter is
		 * 	a {@link jhilbert.scanners.TokenScanner}.
		 */
		public void setConstructor(final Constructor<? extends Command> constructor) {
			assert (constructor != null): "Supplied constructor is null";
			assert (constructor.getParameterTypes().length == 2):
				"Supplied constructor must accept two parameters";
			// FIXME: more assertions...
			this.constructor = constructor;
		}

		/**
		 * Creates a new command from the specified {@link Module} and
		 * the specified {@link TokenScanner}.
		 * This method must not be called before the command
		 * constructor has been initialised. Tjis should not normally
		 * be a problem, as this initialisation should be performed
		 * in a static initialiser of the command package
		 * implementation.
		 *
		 * @param module module the command should store its data to.
		 * @param scanner the token scanner.
		 *
		 * @return a command of this class.
		 *
		 * @throws SyntaxException if a syntax error occurs. Syntax
		 * 	errors may also be caused due to the scanner unable to
		 * 	read a token because of I/O problems.
		 *
		 * @see #setConstructor
		 */
		public Command createCommand(final Module module, final TokenScanner scanner) throws SyntaxException {
			assert (module != null): "Supplied module is null";
			assert (scanner != null): "Supplied scanner is null";
			assert (constructor != null): "Constructor has not been initialised yet";
			try {
				return constructor.newInstance(module, scanner);
			} catch (InvocationTargetException e) {
				final Throwable cause = e.getCause();
				if (cause instanceof SyntaxException)
					throw (SyntaxException) cause;
				else if (cause instanceof RuntimeException)
					throw (RuntimeException) cause;
				else if (cause instanceof Error)
					throw (Error) cause;
				else {
					final AssertionError ae = new AssertionError(
						"Implementation throws invalid exception");
					ae.initCause(e);
					throw ae;
				}
			} catch (IllegalAccessException e) {
				throw new AssertionError("Implementation provides inaccessible constructors");
			} catch (IllegalArgumentException e) {
				throw new AssertionError("Implementation violates contract on constructor arguments");
			} catch (InstantiationException e) {
				throw new AssertionError("Implementation provides constructors of abstract classes");
			}
		}

		/**
		 * Mapping from atoms to their respective command classes.
		 */
		private static final Map<String, Class> classMap = new HashMap();

		/**
		 * Set of all proof permissible command classes.
		 */
		private static final EnumSet<Class> proofPermissibleClasses = EnumSet.noneOf(Class.class);

		/**
		 * Set of all interface permissible command classes.
		 */
		private static final EnumSet<Class> interfacePermissibleClasses = EnumSet.noneOf(Class.class);

		static {
			// start factory
			CommandFactory.getInstance();
			// init static members
			for (Class c: Class.values()) {
				classMap.put(c.getAtom(), c);
				if (c.isProofPermissible())
					proofPermissibleClasses.add(c);
				if (c.isInterfacePermissible())
					interfacePermissibleClasses.add(c);
			}
			// FIXME: we do not seem to need the two EnumSets right now
		}

		/**
		 * Obtains the command class belonging to an atom.
		 *
		 * @param atom atom to find command class for.
		 *
		 * @return the command class of <code>atom</code>, or
		 * 	<code>null</code> if no matching class exists.
		 */
		public static Class get(final String atom) {
			assert (atom != null): "Supplied atom is null";
			return classMap.get(atom);
		}

	}

	/**
	 * Executes this command.
	 *
	 * @throws CommandException if this command cannot be executed.
	 */
	public void execute() throws CommandException;

}
