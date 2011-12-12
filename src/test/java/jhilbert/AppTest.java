/*
    JHilbert, a verifier for collaborative theorem proving

    Copyright © 2008, 2009, 2011 The JHilbert Authors
      See the AUTHORS file for the list of JHilbert authors.
      See the commit logs ("git log") for a list of individual contributions.

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

package jhilbert;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

import jhilbert.commands.CommandException;
import jhilbert.commands.CommandFactory;
import jhilbert.data.DVConstraints;
import jhilbert.data.DataException;
import jhilbert.data.DataFactory;
import jhilbert.data.Definition;
import jhilbert.data.Functor;
import jhilbert.data.Kind;
import jhilbert.data.Module;
import jhilbert.data.Parameter;
import jhilbert.data.Symbol;
import jhilbert.data.Variable;
import jhilbert.expressions.Expression;
import jhilbert.expressions.ExpressionFactory;
import jhilbert.scanners.ScannerException;
import jhilbert.scanners.ScannerFactory;
import jhilbert.scanners.TokenFeed;
import jhilbert.scanners.WikiInputStreamTest;
import jhilbert.scanners.impl.CharTest;
import jhilbert.storage.wiki.StorageTest;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;

/**
 * Unit test.
 */
public class AppTest extends TestCase
{

	public static Test suite()
    {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(AppTest.class);
		suite.addTestSuite(MainTest.class);
		suite.addTestSuite(StorageTest.class);
		suite.addTestSuite(WikiInputStreamTest.class);
		suite.addTestSuite(CharTest.class);
		return suite;
    }

    private Module mainModule;
	private DataFactory dataFactory;

	protected void setUp() throws Exception {
		BasicConfigurator.configure(new NullAppender());
		dataFactory = DataFactory.getInstance();
		mainModule = dataFactory.createModule("");
	}

	private void process(String proofModule) throws ScannerException,
			UnsupportedEncodingException, CommandException {
		final TokenFeed tokenFeed = ScannerFactory
            .getInstance().createTokenFeed(new ByteArrayInputStream(proofModule.getBytes("UTF-8")));
        CommandFactory.getInstance().processCommands(mainModule, tokenFeed);
	}
    
	private void importInterface(String interfaceText) throws DataException,
	ScannerException, UnsupportedEncodingException, CommandException {
		Module parameterModule = dataFactory.createModule("test.jhi", -1);
		final TokenFeed tokenFeed = ScannerFactory
		    .getInstance().createTokenFeed(new ByteArrayInputStream(interfaceText .getBytes("UTF-8")));
		CommandFactory.getInstance().processCommands(parameterModule, tokenFeed);
		Parameter parameter = dataFactory.createParameter("TEST", "test.jhi", new ArrayList(), "");
		dataFactory.createParameterLoader(parameter, parameterModule, mainModule).importParameter();
	}

	public void testEmptyFile() throws Exception
    {
        process("");
    }
    
    public void testUnrecognizedKeyword() throws Exception {
    	try {
			process("foobar");
			fail("Expected exception but didn't get it");
    	}
    	catch (CommandException e) {
    		assertEquals(" foobar Command unknown: (cause unknown)", e.getMessage());
    	}
	}

    public void testDefineKind() throws Exception {
    	dataFactory.createKind("formula", mainModule.getKindNamespace());
    	Kind kind = mainModule.getKindNamespace().getObjectByString("formula");
    	assertEquals("formula", kind.getNameString());
	}
    
    public void testDefineTerm() throws Exception {
    	Kind formula = dataFactory.createKind("formula", mainModule.getKindNamespace());
    	dataFactory.createFunctor("!", formula, Arrays.asList(formula), mainModule.getFunctorNamespace());
		
		Functor not = mainModule.getFunctorNamespace().getObjectByString("!");
		assertEquals("!", not.getNameString());
	}

	public void testAbbreviation() throws Exception {
		Kind formula = dataFactory.createKind("formula", mainModule.getKindNamespace());
		dataFactory.createFunctor("!", formula, Arrays.asList(formula), mainModule.getFunctorNamespace());
		dataFactory.createFunctor("->", formula, Arrays.asList(formula, formula), mainModule.getFunctorNamespace());

		process("var (formula p q)");
		process("def ((| p q) ((! p) -> q))");
    	
		Definition or = (Definition) mainModule.getFunctorNamespace().getObjectByString("|");
		assertEquals("|", or.getNameString());

		// The definiens is something like "(-> (! (?1)) (?0))" but the
		// numbers do not seem to be completely consistent from run to run.
		String definiens = or.getDefiniens().toString();
		assertTrue("unexpected definiens " + definiens,
			definiens.matches(
				"\\(-> \\(! \\(\\?[0-9]+\\)\\) \\(\\?[0-9]+\\)\\)"));
	}
	
	public void testDefineStatement() throws Exception {
    	Kind formula = dataFactory.createKind("formula", mainModule.getKindNamespace());
		Functor implies = dataFactory.
		    createFunctor("->", formula, Arrays.asList(formula, formula), mainModule.getFunctorNamespace());
    	
    	Variable p = dataFactory.createVariable("p", formula, mainModule.getSymbolNamespace());
    	Variable q = dataFactory.createVariable("q", formula, mainModule.getSymbolNamespace());
    	Expression pExpression = ExpressionFactory.getInstance().createExpression(p);
    	Expression qExpression = ExpressionFactory.getInstance().createExpression(q);
    	Expression pImpliesQ = ExpressionFactory.getInstance().
    	    createExpression(implies, Arrays.asList(pExpression, qExpression));

    	DVConstraints emptyConstraints = dataFactory.createDVConstraints();
		dataFactory.createStatement("applyModusPonens", emptyConstraints, 
				Arrays.asList(pExpression, pImpliesQ), qExpression, mainModule.getSymbolNamespace());
		
		Symbol applyModusPonens = mainModule.getSymbolNamespace().getObjectByString("applyModusPonens");
		assertEquals("applyModusPonens", applyModusPonens.getNameString());
	}
	
	public void testImportInterface() throws Exception {
		importInterface("kind (formula)");
	}

}
