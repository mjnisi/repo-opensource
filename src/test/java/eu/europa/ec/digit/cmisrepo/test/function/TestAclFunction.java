package eu.europa.ec.digit.cmisrepo.test.function;


import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import eu.europa.ec.digit.cmisrepo.test.AbstractCmisTestClient;

public class TestAclFunction extends AbstractCmisTestClient {

	public static void main(String[] args) {
		Logger.getRootLogger().setLevel(Level.WARN);

		String cRepoId = "repo_WITH_ACL";

		Session cSessionAdmin = getSession(NAME_USER_ADMIN, PSWD_USER_ADMIN, BindingType.WEBSERVICES, cRepoId);
		Session cSessionUser1 = getSession(NAME_USER_TEST1, PSWD_USER_TEST1, BindingType.WEBSERVICES, cRepoId);
//		Session cSessionUser2 = getSession(NAME_USER_TEST2, PSWD_USER_TEST2, BindingType.WEBSERVICES, cRepoId);

		String cRootFolderId = cSessionAdmin.getRepositoryInfo().getRootFolderId();

		AtomicLong cTime0 = new AtomicLong(System.currentTimeMillis());
		getDelta(cTime0);

		ItemIterable<QueryResult> cResultAdmin = cSessionAdmin.query("select * from cmis:document where in_tree('" + cRootFolderId + "')", true);
		long nResult = cResultAdmin.getTotalNumItems();
		System.out.println("Result, admin: " + nResult);
		for (QueryResult queryResult : cResultAdmin) {
			PropertyData<?> cProps = queryResult.getPropertyById("cmis:objectId");
			String cIdDoc = cProps.getFirstValue().toString();
			Document cDocument = (Document) cSessionAdmin.getObject(cIdDoc);
			Acl cAcl = cSessionAdmin.getAcl(new ObjectIdImpl(cIdDoc), false);
			List<Ace> cLstAces = cAcl.getAces();
			for (Ace cAce : cLstAces) {
				if(cAce.getPrincipalId().equals("test")) {
					printAces("Document: " + cDocument.getName(), cSessionAdmin, new ObjectIdImpl(cIdDoc));
					FileableCmisObject cFolder = cDocument.getParents().get(0);
					while ((cFolder = printAcesForParent(cSessionAdmin, cFolder)) != null) {
					}
					
					// Get the document as user1
					Document cDocUser1 = (Document) cSessionUser1.getObject(cIdDoc);
					System.out.println("User1 got the document: " + cDocUser1.getName());
					
					Folder cFolderSubSub = cDocUser1.getParents().get(0);
					ItemIterable<QueryResult> cResultUser1 = cSessionUser1.query("select * from cmis:document where in_folder('" + cFolderSubSub.getId() + "')", true);
					nResult = cResultUser1.getTotalNumItems();
					System.out.println("Search result, folder, sub sub: " + nResult);
					
					cResultUser1 = cSessionUser1.query("select * from cmis:document where in_tree('" + cFolderSubSub.getId() + "')", true);
					nResult = cResultUser1.getTotalNumItems();
					System.out.println("Search result, tree, sub sub: " + nResult);
					
					Folder cFolderSub = cFolderSubSub.getParents().get(0);
					cResultUser1 = cSessionUser1.query("select * from cmis:document where in_folder('" + cFolderSub.getId() + "')", true);
					nResult = cResultUser1.getTotalNumItems();
					System.out.println("Search result, folder, sub: " + nResult);
					
					cResultUser1 = cSessionUser1.query("select * from cmis:document where in_tree('" + cFolderSub.getId() + "')", true);
					nResult = cResultUser1.getTotalNumItems();
					System.out.println("Search result, tree, sub: " + nResult);
					
					Folder cFolderTop = cFolderSub.getParents().get(0);
					cResultUser1 = cSessionUser1.query("select * from cmis:document where in_folder('" + cFolderTop.getId() + "')", true);
					nResult = cResultUser1.getTotalNumItems();
					System.out.println("Search result, folder, top: " + nResult);
					
					cResultUser1 = cSessionUser1.query("select * from cmis:document where in_tree('" + cFolderTop.getId() + "')", true);
					nResult = cResultUser1.getTotalNumItems();
					System.out.println("Search result, tree, top: " + nResult);
					
					Folder cFolderRoot = cFolderTop.getParents().get(0);
					cResultUser1 = cSessionUser1.query("select * from cmis:document where in_folder('" + cFolderRoot.getId() + "')", true);
					nResult = cResultUser1.getTotalNumItems();
					System.out.println("Search result, folder, root: " + nResult);
					
					cResultUser1 = cSessionUser1.query("select * from cmis:document where in_tree('" + cFolderRoot.getId() + "')", true);
					nResult = cResultUser1.getTotalNumItems();
					System.out.println("Search result, tree, root: " + nResult);
					
					break;
				}
			}
			System.out.println("-");
		}

	}

	static FileableCmisObject printAcesForParent(Session a_cSession, FileableCmisObject a_cObj) {
		try {
			printAces("Obj: " + a_cObj.getName() + ", id: " + a_cObj.getId(), a_cSession, a_cObj);
			return a_cObj.getParents() != null && a_cObj.getParents().size() > 0 ? a_cObj.getParents().get(0) : null;
		} catch (Exception e) {
			System.out.println("No access");
			return null;
		}
	}
}
