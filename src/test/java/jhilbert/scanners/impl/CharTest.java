package jhilbert.scanners.impl;

import junit.framework.TestCase;

public class CharTest extends TestCase {

	public void testHashmark() throws Exception {
		assertEquals(Char.Class.HASHMARK,
			new Char('#').getCharClass());
	}

	public void testAscii() throws Exception {
		assertEquals(Char.Class.ATOM,
			new Char('a').getCharClass());
	}

	public void testEndOfFile() throws Exception {
		assertEquals(Char.Class.EOF,
			new Char(-1).getCharClass());
	}

	public void testPerpendicular() throws Exception {
		assertEquals(Char.Class.ATOM,
			new Char('\u27c2').getCharClass());
	}

	public void
	testCharacterAddedByAFutureVersionOfUnicodeBeforeJavaIsUpdated()
	throws Exception {
		assertEquals(Char.Class.ATOM,
				new Char('\u0e76').getCharClass());
	}

}
