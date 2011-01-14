/*
    JHilbert, a verifier for collaborative theorem proving
    Copyright © 2008, 2009 Alexander Klauer

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

    You may contact the author on this Wiki page:
    http://www.wikiproofs.de/w/index.php?title=User_talk:GrafZahl
*/

package jhilbert.data.impl;

import java.util.ArrayList;
import java.util.List;

import jhilbert.data.*;

import jhilbert.expressions.Expression;

import jhilbert.scanners.TokenFeed;

/**
 * {@link DataFactory} implementation.
 */
public final class DataFactory extends jhilbert.data.DataFactory {

	// default constructed.
	
	public @Override ModuleImpl createModule(final String name, final long revision) throws DataException {
		assert (name != null): "Supplied name is null";
		return new ModuleImpl(name, revision);
	}

	public @Override KindImpl createKind(final String name, final Kind oldkind, final int parameterIndex, final Namespace<? extends Kind> namespace)
	throws DataException {
		assert (name != null): "Supplied name is null";
		assert ((oldkind == null) || (oldkind instanceof KindImpl)): "Implementation type error";
		assert (parameterIndex >= -1): "Invalid parameter index";
		assert (namespace != null): "Supplied namespace is null";
		final KindImpl oldkindimpl = (KindImpl) oldkind;
		final KindImpl result = new KindImpl(name, oldkindimpl, parameterIndex);
		namespace.registerObject(result);
		return result;
	}

	public @Override VariableImpl createVariable(final String name, final Kind kind, final Namespace<? extends Symbol> namespace) throws DataException {
		assert (name != null): "Supplied name is null";
		assert (kind instanceof KindImpl): "Implementation type error";
		assert (namespace != null): "Supplied namespace is null";
		final VariableImpl result = new VariableImpl(name, (KindImpl) kind);
		namespace.registerObject(result);
		return result;
	}

	public @Override VariableImpl createUnnamedVariable(final Kind kind) {
		assert (kind instanceof KindImpl): "Implementation type error";
		return new UnnamedVariable((KindImpl) kind);
	}

	public @Override VariableImpl createDummyVariable(final Kind kind) {
		assert (kind instanceof KindImpl): "Implementation type error";
		return new DummyVariable((KindImpl) kind);
	}

	public @Override FunctorImpl createFunctor(final String name, final Functor orig, final int parameterIndex, final Kind kind, final List<Kind> inputKinds,
		final Namespace<? extends Functor> namespace)
	throws DataException {
		assert (name != null): "Supplied name is null";
		assert ((orig == null) || (orig instanceof FunctorImpl)): "Implementation type error";
		assert (parameterIndex >= -1): "Invalid parameter index";
		assert (kind instanceof KindImpl): "Implementation type error";
		assert (inputKinds != null): "Supplied input kind list is null";
		assert (namespace != null): "Supplied namespace is null";
		try {
			final List<KindImpl> kindImplList = new ArrayList(inputKinds.size());
			for (final Kind inputKind: inputKinds)
				kindImplList.add((KindImpl) inputKind);
			final FunctorImpl result = new FunctorImpl(name, (FunctorImpl) orig, parameterIndex, (KindImpl) kind, kindImplList);
			namespace.registerObject(result);
			return result;
		} catch (ClassCastException e) {
			throw new AssertionError("Implementation type error");
		}
	}

	public @Override DefinitionImpl createDefinition(final String name, final Definition orig,
			final int parameterIndex, final DVConstraints dvConstraints, final List<Variable> argList,
			final Expression definiens, final Namespace<? extends Functor> namespace)
	throws DataException {
		assert (name != null): "Supplied name is null";
		assert ((orig == null) || (orig instanceof DefinitionImpl)): "Implementation type error";
		assert ((dvConstraints == null) || (dvConstraints instanceof DVConstraintsImpl)):
			"Implementation type error";
		assert (argList != null): "Supplied argument list is null";
		assert (definiens != null): "Supplied definiens is null";
		assert (namespace != null): "Supplied namespace is null";
		final DefinitionImpl result = new DefinitionImpl(name, (DefinitionImpl) orig, parameterIndex,
				(DVConstraintsImpl) dvConstraints, argList, definiens);
		namespace.registerObject(result);
		return result;
	}

	public @Override DVConstraintsImpl createDVConstraints() {
		return new DVConstraintsImpl();
	}

	public @Override DVConstraintsImpl createDVConstraints(final Namespace<? extends Symbol> namespace, final TokenFeed tokenFeed)
	throws ConstraintException {
		assert (namespace != null): "Supplied namespace is null";
		assert (tokenFeed != null): "Supplied token feed is null";
		return new DVConstraintsImpl(namespace, tokenFeed);
	}

	public @Override StatementImpl createStatement(final String name, final Statement orig, final int parameterIndex,
			final DVConstraints dv,	final List<Expression> hypotheses, final Expression consequent,
			final Namespace<? extends Symbol> namespace)
	throws DataException {
		assert (name != null): "Supplied name is null";
		assert ((orig == null) || (orig instanceof StatementImpl)): "Implementation type error";
		assert (dv != null): "Supplied DV constraints are null";
		assert (hypotheses != null): "Supplied hypotheses are null";
		assert (consequent != null): "Supplied consequent is null";
		assert (namespace != null): "Supplied namespace is null";
		final StatementImpl result = new StatementImpl(name, (StatementImpl) orig, parameterIndex, dv, hypotheses,
				consequent);
		namespace.registerObject(result);
		return result;
	}

	public @Override ParameterImpl createParameter(final String name, final String locator, final List<Parameter> parameterList, final String prefix) {
		assert (name != null): "Supplied name is null";
		assert (locator != null): "Supplied locator is null";
		assert (parameterList != null): "Supplied parameter list is null";
		assert (prefix != null): "Supplied prefix is null";
		return new ParameterImpl(name, locator, parameterList, prefix);
	}

	public @Override ParameterImpl createParameter(final Module module, final TokenFeed tokenFeed)
	throws DataException {
		assert (module != null): "Supplied module is null";
		assert (tokenFeed != null): "Supplied token feed is null";
		return new ParameterImpl(module, tokenFeed);
	}

	public @Override ParameterLoaderImpl createParameterLoader(final Parameter parameter, final Module module)
	throws DataException {
		assert (parameter != null): "Supplied parameter is null";
		assert (module != null): "Supplied module is null";
		return new ParameterLoaderImpl(parameter, module);
	}

	public @Override ParameterLoaderImpl createParameterLoader(
			final Parameter parameter, final Module parameterModule, final Module module)
	throws DataException {
		assert (parameter != null): "Supplied parameter is null";
		assert (parameterModule != null): "Supplied parameterModule is null";
		assert (module != null): "Supplied module is null";
		return new ParameterLoaderImpl(parameter, parameterModule, module);
	}

}
