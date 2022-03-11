package eu.europa.ec.digit.cmisrepo.test.performance;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import eu.europa.ec.digit.cmisrepo.test.AbstractCmisTestClient;

public class TestAclPerformance extends AbstractCmisTestClient {

	/*-
	- - - - - - - - - - - - - - - -
	NO ACLs
	- - - - - - - - - - - - - - - -
	Time, user1: 11185, result: 4096
	Time, user2: 9146, result: 4096
	--
	Time, user1: 8682, result: 4096
	Time, user2: 9078, result: 4096
	--
	Time, user1: 9145, result: 4096
	Time, user2: 9017, result: 4096
	--
	- - - - - - - - - - - - - - - -
	WITH ACLs
	- - - - - - - - - - - - - - - -
	Time, user1: 18042, result: 4096
	Time, user2: 12359, result: 4096
	--
	Time, user1: 16905, result: 4096
	Time, user2: 12363, result: 4096
	--
	Time, user1: 17891, result: 4096
	Time, user2: 12451, result: 4096
	--
	Done!
	*/

	public static void main(String[] args) {
		System.out.println("- - - - - - - - - - - - - - - -");
		System.out.println("    NO ACLs");
		System.out.println("- - - - - - - - - - - - - - - -");
		run("repo_NO_ACL");

		System.out.println("- - - - - - - - - - - - - - - -");
		System.out.println("    WITH ACLs");
		System.out.println("- - - - - - - - - - - - - - - -");
		run("repo_WITH_ACL");

		System.out.println("Done!");
	}

	static void run(String a_cRepoId) {
		Logger.getRootLogger().setLevel(Level.WARN);

		// String cRepoId = "repo_NO_ACL";

		Session cSessionAdmin = getSession(NAME_USER_ADMIN, PSWD_USER_ADMIN, BindingType.WEBSERVICES, a_cRepoId);
		Session cSessionUser1 = getSession(NAME_USER_TEST1, PSWD_USER_TEST1, BindingType.WEBSERVICES, a_cRepoId);
		Session cSessionUser2 = getSession(NAME_USER_TEST2, PSWD_USER_TEST2, BindingType.WEBSERVICES, a_cRepoId);

		String cRootFolderId = cSessionAdmin.getRepositoryInfo().getRootFolderId();

		AtomicLong cTime0 = new AtomicLong(System.currentTimeMillis());
		getDelta(cTime0);

		for (int i = 0; i < 3; i++) {
			ItemIterable<QueryResult> cResultFolders = cSessionUser1
					.query("select * from cmis:document where in_tree('" + cRootFolderId + "')", true);
			long nResult = cResultFolders.getTotalNumItems();
			System.out.println("Time, user1: " + getDelta(cTime0) + ", result: " + nResult);
			// for (QueryResult queryResult : cResultFolders) {
			// PropertyData<?> cProps = queryResult.getPropertyById("cmis:objectId");
			// String cIdFolder = cProps.getFirstValue().toString();
			// Folder cFolder = (Folder) cSessionAdmin.getObject(cIdFolder);
			// printAces("Folder: " + cFolder.getName(), cSessionAdmin, new
			// ObjectIdImpl(cIdFolder));
			// System.out.println("-");
			// }
			// System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");

			// ItemIterable<QueryResult> cResultUser1 = cSessionUser1.query("select * from
			// cmis:document where in_tree('" + cRootFolderId + "')", true);
			// long nResult = cResultUser1.getTotalNumItems();
			// System.out.println("Time, user1: " + getDelta(cTime0) + ", result: " +
			// nResult);

			ItemIterable<QueryResult> cResultUser2 = cSessionUser2
					.query("select * from cmis:document where in_tree('" + cRootFolderId + "')", true);
			nResult = cResultUser2.getTotalNumItems();
			System.out.println("Time, user2: " + getDelta(cTime0) + ", result: " + nResult);
			// for (QueryResult queryResult : cResultUser2) {
			// PropertyData<?> cProps = queryResult.getPropertyById("cmis:objectId");
			// String cIdDoc = cProps.getFirstValue().toString();
			// Document cDocument = (Document) cSessionUser1.getObject(cIdDoc);
			// printAces("Document: " + cDocument.getName(), cSessionUser1, new
			// ObjectIdImpl(cIdDoc));
			// FileableCmisObject cFolder = cDocument.getParents().get(0);
			// while((cFolder = printAcesForParent(cSessionUser1, cFolder)) != null) {
			//
			// }
			// System.out.println("-");
			// }

			System.out.println("--");
		}
	}

	static FileableCmisObject printAcesForParent(Session a_cSession, FileableCmisObject a_cObj) {
		printAces("Obj: " + a_cObj.getName() + ", id: " + a_cObj.getId(), a_cSession, a_cObj);
		return a_cObj.getParents() != null && a_cObj.getParents().size() > 0 ? a_cObj.getParents().get(0) : null;
	}
}
