package eu.trade.repo.delegates;

import static junit.framework.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.BaseLobTestClass;

public class JDBCLobDelegateTest extends BaseLobTestClass {

	@Autowired
	JDBCLobDelegate lobDelegate;

	
	@Test
	public void testSave() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenario04_stream.xml");

		InputStream in = new ByteArrayInputStream(TXT_FILE_CONTENT_1.getBytes());
		lobDelegate.saveStream(200, in, in.available());

		compareLobTable(
				"stream", 
				"ID = 200", 
				"stream_create_1.xml");
	}

	@Test (expected=CmisStorageException.class)
	public void testSaveIfPreviousExist() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenario04_stream.xml");

		InputStream in = new ByteArrayInputStream(TXT_FILE_CONTENT_2.getBytes());
		lobDelegate.saveStream(202, in, in.available());
	}

	@Test(expected=CmisStorageException.class)
	public void testSaveIfObjectDoesNotExist() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenario04_stream.xml");

		InputStream in = new ByteArrayInputStream(TXT_FILE_CONTENT_2.getBytes());
		lobDelegate.saveStream(203, in, in.available());
	}
	
	@Test
	public void testGet() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenario04_stream.xml");
		InputStream in = lobDelegate.getStream(202);
		String result = getString(in);
		assertEquals(TXT_FILE_CONTENT_1, result);
	}
	@Ignore
	@Test(expected=CmisConstraintException.class)
	public void testGetIfStreamDoesNotExist() throws Exception {
		//this method no more throws an exception when the stream does not exists. This check is done at the service layer now.
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenario04_stream.xml");
		lobDelegate.getStream(201);
	}
	
	@Test
	public void testDelete() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenario04_stream.xml");
		lobDelegate.deleteStream(202);
	}
	
	@Test 
	public void testCopy() throws Exception {
		setLobScenario(DatabaseOperation.CLEAN_INSERT, "scenario04_stream.xml");

		lobDelegate.copyStream(202, 200);
		
		compareLobTable(
				"stream", 
				"ID = 200", 
				"stream_create_1.xml");
	}

}
