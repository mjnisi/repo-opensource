package eu.europa.ec.digit.cmisrepo.test.function;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.enums.BindingType;

import eu.europa.ec.digit.cmisrepo.test.AbstractCmisTestClient;

public class TestAtomPubBinding extends AbstractCmisTestClient{

	public static void main(String[] args) {
		Session cSession = getSession(NAME_USER_ADMIN, PSWD_USER_ADMIN, BindingType.ATOMPUB, NAME_TEST_REPO);
		Folder cRootFolder = cSession.getRootFolder();
        ItemIterable<QueryResult> cResult = cSession.query("select * from cmis:document where in_tree('" + cRootFolder.getId() + "')", true);
        System.out.println(cResult.getTotalNumItems());
        for (QueryResult cQueryResult : cResult) {
        	System.out.println(cQueryResult.getPropertyById("cmis:objectId").getFirstValue());
		}
	}
}
