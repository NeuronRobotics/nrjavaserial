package gnu.io;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Enumeration;

import org.junit.jupiter.api.Test;

class CommPortIdentifierTest
{
	@Test
	void testGetCommPortIdentifiers()
	{
		@SuppressWarnings("rawtypes")
		Enumeration identifiers = CommPortIdentifier.getPortIdentifiers();
		assertNotNull(identifiers);

		/* Hard to assert on anything else when the test hardware may change
		 * dramatically.
		 * 
		 * TODO: Wire up SerialPortExtension without its test-disabling
		 * ExecutionCondition behaviour, and use the names of its configured
		 * ports to confirm that the identifiers enumeration at least contains
		 * something when we know what to expect in it. */
	}
}
