package jhilbert;

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

}
