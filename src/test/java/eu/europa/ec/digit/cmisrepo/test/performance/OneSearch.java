package eu.europa.ec.digit.cmisrepo.test.performance;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;

import eu.europa.ec.digit.cmisrepo.test.AbstractCmisTestClient;

public class OneSearch  extends AbstractCmisTestClient{

    public static void main(String[] args) {
        Session cSession = getSessionTest1();
        AtomicLong cTime0 = new AtomicLong(System.currentTimeMillis());
        getDelta(cTime0);

        ItemIterable<QueryResult> cResult2 = cSession.query("select * from cmis:document where in_tree('" + cSession.getRootFolder().getId() + "')", true);
        long nResult = cResult2.getTotalNumItems();
        System.out.println("Time (meta, cmis:document, in_tree, root): " + getDelta(cTime0) + ", result: " + nResult);
    }
}