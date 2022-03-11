package eu.trade.it;

import org.junit.Test;

import eu.trade.tck.ITRunTck;
import eu.trade.tck.JUnitHelper;

public class ITClientACL {


	@Test
	public void doRemoveInheritedTest() {
		
		JUnitHelper.run(new String[] {
				ITRunTck.COMMON_PROP_FILE_PATH,
				ITRunTck.TESTUSER_PROP_FILE_PATH,
				ITRunTck.WS_PROP_FILE_PATH
		}, new RemoveInherited());
	}
	
}
