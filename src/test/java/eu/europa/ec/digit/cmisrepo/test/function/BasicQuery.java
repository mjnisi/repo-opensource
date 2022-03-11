package eu.europa.ec.digit.cmisrepo.test.function;

import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.PropertyData;

import eu.europa.ec.digit.cmisrepo.test.AbstractCmisTestClient;

public class BasicQuery extends AbstractCmisTestClient {

	public static void main(String[] args) throws Exception {
        Session cSession = getSessionAdmin();
        System.out.println("ROOT: " + cSession.getRootFolder());
        ItemIterable<QueryResult> cResult = cSession.query("select * from cmis:folder where in_tree('" + cSession.getRootFolder().getId() + "')", true);
        System.out.println("RESULTS: " + cResult.getTotalNumItems());
        for (QueryResult cQueryResult : cResult) {
			for (PropertyData<?> cData : cQueryResult.getProperties()) {
				System.out.println(cData);
			}
		}
    }
}
