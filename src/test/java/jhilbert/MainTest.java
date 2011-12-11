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
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import jhilbert.data.DataFactory;
import jhilbert.data.Module;
import jhilbert.scanners.WikiInputStream;
import jhilbert.storage.MemoryStorage;
import jhilbert.storage.Storage;
import junit.framework.TestCase;
import static jhilbert.Main.isInterface;
import static jhilbert.Main.isProofModule;

public class MainTest extends TestCase {

	public void testIsInterface() throws Exception {
		assertTrue(isInterface("/foo/bar/Interface/Logic"));
		assertTrue(isInterface("Interface/L/o/g/Logic"));
		assertFalse(isInterface("Main/L/o/g/Logic"));
		assertTrue(isInterface("User interface/J/o/e/Joe\u001cSandbox"));
	}

	public void testIsModule() throws Exception {
		assertTrue(isProofModule("Main/L/o/g/Logic"));
		assertTrue(isProofModule("./Main/L/o/g/Logic"));
		assertFalse(isProofModule("Interface/L/o/g/Logic"));
		assertTrue(isProofModule("User module/J/o/e/Joe\u001cSandbox"));
	}

	private void process(String wikiText) throws IOException,
			UnsupportedEncodingException, JHilbertException {
		WikiInputStream wiki = WikiInputStream.create(new ByteArrayInputStream(
			wikiText.getBytes("UTF-8")));
		final Module interfaceModule = DataFactory.getInstance().
				createInterface("foo.jhi");
		Main.process(wiki, interfaceModule);
	}

	public void testExpectNoErrorsAndGetNone() throws Exception {
		process("<jh>\nkind (formula)\n</jh>\n");
	}

	public void testExpectNoErrorsAndGetOne() throws Exception {
		try {
			process("<jh>\nkind ness and peace\n</jh>\n");
			fail();
		}
		catch (JHilbertException e) {
			assertEquals(" kind ness Feed error: Expected beginning of LISP s-expression",
					e.getMessage());
		}
	}

	public void testExpectErrorAndGetIt() throws Exception {
		process("{{error expected|Kind not found}}\n" +
			"<jh>\nterm (formula (true))\n</jh>\n");
	}

	// Need to follow causes recursively, as long as they are JHilbertExceptions
	public void testExpectErrorNested() throws Exception {
		MemoryStorage storage = new MemoryStorage();
		storage.store("Interface:logic",
			"kind (formula)" +
			"var (formula p q)" +
			"term (formula (→ formula formula))" +
			"stmt (AntecedentIntroduction () () (p → (q → p)))"
		);
		Storage.setInstance(storage);

		// Hmm, do we want
		// {{error expected|Proof does not verify: Consequent does not match proof result}}
		// to work too?
		WikiInputStream wiki = WikiInputStream.create(new ByteArrayInputStream(
			("{{error expected|Consequent does not match proof result}}\n" +
			"<jh>\n" +
			"import (LOGIC Interface:logic () ())" +
			"var (formula r s)\n" +
			"thm (invalid () () ((r → s) → r) (\n" +
            "  r s AntecedentIntroduction\n" +
            "))\n" +
			"</jh>\n").getBytes("UTF-8")));
		final Module module = DataFactory.getInstance().createProofModule();
		Main.process(wiki, module);
	}

	/* Errors have two wordings. One goes in the exception. One is passed
	   to reject, which is what is shown to the user (in the case of --wiki,
	   both are currently shown but that might be overkill). */
	public void testWordingFromReject() throws Exception {
		MemoryStorage storage = new MemoryStorage();
		storage.store("Interface:logic",
			"kind (formula)" +
			"var (formula p q)" +
			"term (formula (→ formula formula))" +
			"stmt (AntecedentIntroduction () () (p → (q → p)))"
		);
		Storage.setInstance(storage);

		WikiInputStream wiki = WikiInputStream.create(new ByteArrayInputStream(
			("{{error expected|Consequent of theorem does not match proof result}}\n" +
			"<jh>\n" +
			"import (LOGIC Interface:logic () ())" +
			"var (formula r s)\n" +
			"thm (invalid () () ((r → s) → r) (\n" +
            "  r s AntecedentIntroduction\n" +
            "))\n" +
			"</jh>\n").getBytes("UTF-8")));
		final Module module = DataFactory.getInstance().createProofModule();
		Main.process(wiki, module);
	}

	public void testExpectErrorAndGetAnother() throws Exception {
		try {
			process("{{error expected|howzzzat}}\n" +
				"<jh>\nterm (formula (true))\n</jh>\n");
			fail();
		}
		catch (JHilbertException e) {
			assertEquals("expected error:\n" +
				"  howzzzat\n" +
				"but got:\n" +
				"   term ( formula Kind not found: (cause unknown)",
				e.getMessage());
		}
	}

	public void testExpectMultipleErrors() throws Exception {
		try {
			process("{{error expected|boring file}}\n" +
				"{{error expected|too much in one file}}\n");
			fail();
		}
		catch (JHilbertException e) {
			assertEquals("can only expect one error per file currently",
				e.getMessage());
		}
	}

}
