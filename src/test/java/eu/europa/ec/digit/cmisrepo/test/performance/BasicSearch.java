package eu.europa.ec.digit.cmisrepo.test.performance;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;

import eu.europa.ec.digit.cmisrepo.test.AbstractCmisTestClient;


public class BasicSearch extends AbstractCmisTestClient{
	

    public static void main(String[] args) throws Exception {
        Session cSession = getSessionTest2();
        
        System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        System.out.println("Search performance");
        System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        
        /*
         * Search folders metadata
         */
        AtomicLong cTime0 = new AtomicLong(System.currentTimeMillis());
        getDelta(cTime0);

        ItemIterable<QueryResult> cResult3 = cSession.query("select * from cmis:folder where in_folder('" + cSession.getRootFolder().getId() + "')", true);
        long nResult = cResult3.getTotalNumItems();
        System.out.println("Time (meta, cmis:folder, in_folder, root):         " + getDelta(cTime0) + ", result: " + nResult);
        String cTopFolderId = cResult3.iterator().next().getPropertyById("cmis:objectId").getFirstValue().toString();

        ItemIterable<QueryResult> cResult8 = cSession.query("select * from cmis:folder where in_folder('" + cTopFolderId + "')", true);
        nResult = cResult8.getTotalNumItems();
        System.out.println("Time (meta, cmis:folder, in_folder, top):          " + getDelta(cTime0) + ", result: " + nResult);
        String cSubFolderId = cResult8.iterator().next().getPropertyById("cmis:objectId").getFirstValue().toString();

        ItemIterable<QueryResult> cResult1 = cSession.query("select * from cmis:folder where in_tree('" + cSession.getRootFolder().getId() + "')", true);
        nResult = cResult1.getTotalNumItems();
        System.out.println("Time (meta, cmis:folder, in_tree, root):           " + getDelta(cTime0) + ", result: " + nResult);

        ItemIterable<QueryResult> cResult4 = cSession.query("select * from cmis:document where in_folder('" + cSubFolderId + "')", true);
        nResult = cResult4.getTotalNumItems();
        System.out.println("Time (meta, cmis:document, in_folder, sub):        " + getDelta(cTime0) + ", result: " + nResult);

        ItemIterable<QueryResult> cResult2 = cSession.query("select * from cmis:document where in_tree('" + cSession.getRootFolder().getId() + "')", true);
        nResult = cResult2.getTotalNumItems();
        System.out.println("Time (meta, cmis:document, in_tree, root):         " + getDelta(cTime0) + ", result: " + nResult);

        
        
        OperationContext cOperationContext = cSession.createOperationContext();
        ItemIterable<CmisObject> cResult5 = cSession.queryObjects("cmis:document", "in_folder('" + cSubFolderId + "')", false, cOperationContext);
        nResult = cResult5.getTotalNumItems();
        System.out.println("Time (objects, cmis:document, in_folder, sub):     " + getDelta(cTime0) + ", result: " + nResult);

        ItemIterable<CmisObject> cResult6 = cSession.queryObjects("cmis:document", "in_tree('" + cSession.getRootFolder().getId() + "')", false, cOperationContext);
        nResult = cResult6.getTotalNumItems();
        System.out.println("Time (objects, cmis:document, in_tree, root):      " + getDelta(cTime0) + ", result: " + nResult);

        ItemIterable<CmisObject> cResult7 = cSession.queryObjects("cmis:folder", "in_folder('" + cTopFolderId + "')", false, cOperationContext);
        nResult = cResult7.getTotalNumItems();
        System.out.println("Time (objects, cmis:folder, in_folder, top):       " + getDelta(cTime0) + ", result: " + nResult);

        ItemIterable<CmisObject> cResult9 = cSession.queryObjects("cmis:folder", "in_tree('" + cSession.getRootFolder().getId() + "')", false, cOperationContext);
        nResult = cResult9.getTotalNumItems();
        System.out.println("Time (objects, cmis:folder, in_tree, root):        " + getDelta(cTime0) + ", result: " + nResult);
    }


}
