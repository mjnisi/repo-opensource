package eu.europa.ec.digit.cmisrepo.test.performance;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;

import eu.europa.ec.digit.cmisrepo.test.AbstractCmisTestClient;


public class FullTextSearch extends AbstractCmisTestClient{
	

    public static void main(String[] args) throws Exception {
        Session cSession = getSessionAdmin();
        
        AtomicLong cTime0 = new AtomicLong(System.currentTimeMillis());
        getDelta(cTime0);

        ItemIterable<QueryResult> cResult1 = cSession.query("SELECT * FROM cmis:document WHERE CONTAINS('cwcubiynkwhff')", true);
        long nResult = cResult1.getTotalNumItems();
        System.out.println("Unique word: " + getDelta(cTime0) + ", result: " + nResult);

//        ItemIterable<QueryResult> cResult2 = cSession.query("SELECT * FROM cmis:document WHERE CONTAINS('yours when someone nine system')", true);
//        nResult = cResult2.getTotalNumItems();
//        System.out.println("Sentence, 5 common words: " + getDelta(cTime0) + ", result: " + nResult);

        ItemIterable<QueryResult> cResult5 = cSession.query("SELECT * FROM cmis:document WHERE CONTAINS('fifteen thus what its cdniggtyhy')", true);
        nResult = cResult5.getTotalNumItems();
        System.out.println("Sentence, one unique word: " + getDelta(cTime0) + ", result: " + nResult);

        ItemIterable<QueryResult> cResult3 = cSession.query("SELECT * FROM cmis:document WHERE CONTAINS('anything')", true);
        nResult = cResult3.getTotalNumItems();
        System.out.println("Common word: " + getDelta(cTime0) + ", result: " + nResult);

        ItemIterable<QueryResult> cResult4 = cSession.query("SELECT * FROM cmis:document WHERE CONTAINS('per their')", true);
        nResult = cResult4.getTotalNumItems();
        System.out.println("Common, 2-word combo: " + getDelta(cTime0) + ", result: " + nResult);
    }

}
