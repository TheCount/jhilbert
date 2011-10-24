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
	public void testMultibyte() throws Exception {
		assertEquals("xx", Storage.fileName("Interface:Zermelo–Fra"));
	}
}