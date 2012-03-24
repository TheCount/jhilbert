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

package jhilbert.storage.wiki;

import junit.framework.TestCase;

public class StorageTest extends TestCase
{
	public void testFileNameBasic() throws Exception {
		assertEquals("Interface/A/x/i/Axioms",
			Storage.fileName("Interface:Axioms"));
	}

	public void testSpaces() throws Exception {
		assertEquals("Interface/C/o/o/Cool axioms",
			Storage.fileName("Interface:Cool_axioms"));
		assertEquals("Interface/S/e/v/Several spaces to convert",
			Storage.fileName("Interface:Several_spaces_to_convert"));
	}

	public void testShortFirstWord() throws Exception {
		assertEquals("Interface/K/.20/m/K modal logic",
			Storage.fileName("Interface:K_modal_logic"));
	}

	public void testUserModule() throws Exception {
		assertEquals("User module/J/o/e/Joe\u001cSandbox",
			Storage.fileName("User_module:Joe/Sandbox"));
	}

	public void testMultibyteBoringCase() throws Exception {
		assertEquals("Interface/Z/e/r/Zermelo–Fra",
			Storage.fileName("Interface:Zermelo–Fra"));
	}

	public void testMultibyteInterestingCase() throws Exception {
		// Haven't verified this against what mediawiki/levitation actually do.
		assertEquals("Interface/ε/.20/c/ε conjecture",
			Storage.fileName("Interface:ε_conjecture"));
	}

//	public void testMultibyteSurrogatePairs() throws Exception {
//		// Haven't verified this against what mediawiki/levitation actually do.
//		assertEquals("Interface/𝔸/-/c/𝔸-completeness",
//			Storage.fileName("Interface:𝔸-completeness"));
//	}

	public void testNoColon() throws Exception {
		try {
			Storage.fileName("Interface/T/h/e/Theory One");
			fail();
		}
		catch (Exception error) {
			assertEquals(
				"Filename must contain exactly one colon: Interface/T/h/e/Theory One",
				error.getMessage());
		}
	}
}
