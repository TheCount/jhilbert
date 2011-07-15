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
}