package eu.europa.ec.digit.cmisrepo.test.function;

import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import eu.europa.ec.digit.cmisrepo.test.AbstractCmisTestClient;

public class PerformPrivilegedOperation extends AbstractCmisTestClient {

	public static void main(String[] args) throws Exception {
		Logger.getRootLogger().setLevel(Level.WARN);
		
		Session cSessionTest1 = getSessionTest1();

		String cRootFolderId = cSessionTest1.getRepositoryInfo().getRootFolderId();
		System.out.println("Root id:   " + cRootFolderId);
		ObjectId cFolderId = createFolder(cSessionTest1, cRootFolderId, "topfolder");
		System.out.println("Folder id: " + cFolderId.getId());
	}
}