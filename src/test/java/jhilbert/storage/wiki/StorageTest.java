package jhilbert.storage.wiki;

import junit.framework.TestCase;

public class StorageTest extends TestCase
{
	public void testFileNameBasic() throws Exception {
		assertEquals("Interface/A/x/i/Axioms",
			Storage.fileName("Interface:Axioms"));
	}
}