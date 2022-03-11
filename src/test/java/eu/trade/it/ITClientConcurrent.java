package eu.trade.it;

import org.junit.Test;

import eu.trade.tck.ITRunTck;
import eu.trade.tck.JUnitHelper;

public class ITClientConcurrent {

	
	@Test
	public void doConcurrentVersioningTest() {
		
		JUnitHelper.run(new String[] {
				ITRunTck.COMMON_PROP_FILE_PATH,
				ITRunTck.ADMIN_PROP_FILE_PATH,
				ITRunTck.WS_PROP_FILE_PATH
		}, new ConcurrentVersioning());
	}
	

	/**
	 * Two threads trying to update the same object.
	 * One Exception expected.
	 */
	@Test
	public void doConcurrentPropertiesTest() {
		
		JUnitHelper.run(new String[] {
				ITRunTck.COMMON_PROP_FILE_PATH,
				ITRunTck.ADMIN_PROP_FILE_PATH,
				ITRunTck.WS_PROP_FILE_PATH
		}, new ConcurrentProperties());
	}

	
	/**
	 * Update properties of several documents at the same time.
	 * Control check. 
	 */
	@Test
	public void doConcurrentPropertiesControlCheckTest() {
		
		JUnitHelper.run(new String[] {
				ITRunTck.COMMON_PROP_FILE_PATH,
				ITRunTck.ADMIN_PROP_FILE_PATH,
				ITRunTck.WS_PROP_FILE_PATH
		}, new ConcurrentPropertiesControlCheck());
	}

	
	@Test
	public void doConcurrentDocumentCreationTest() {

		JUnitHelper.run(new String[] {
				ITRunTck.COMMON_PROP_FILE_PATH,
				ITRunTck.ADMIN_PROP_FILE_PATH,
				ITRunTck.WS_PROP_FILE_PATH
		}, new ConcurrentDocumentCreation());
	}
	
	@Test
	public void doConcurrentMoveTest() {

		JUnitHelper.run(new String[] {
				ITRunTck.COMMON_PROP_FILE_PATH,
				ITRunTck.ADMIN_PROP_FILE_PATH,
				ITRunTck.WS_PROP_FILE_PATH
		}, new ConcurrentMove());
	}

	@Test
	public void doConcurrentACLCreationTest() {

		JUnitHelper.run(new String[] {
				ITRunTck.COMMON_PROP_FILE_PATH,
				ITRunTck.ADMIN_PROP_FILE_PATH,
				ITRunTck.WS_PROP_FILE_PATH
		}, new ConcurrentACLCreation());

	}
}
