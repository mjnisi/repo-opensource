package eu.europa.ec.digit.cmisrepo.test.function;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Session;

import eu.europa.ec.digit.cmisrepo.test.AbstractCmisTestClient;

public class GetDocuments extends AbstractCmisTestClient{
	
	public static void main(String[] args) throws Exception {
        Session cSession = getSessionAdmin();

        OperationContext cOperationContext = cSession.createOperationContext();
        ItemIterable<CmisObject> cResult = cSession.queryObjects("cmis:document", "in_tree('" + cSession.getRootFolder().getId() + "')", false, cOperationContext);
        for (CmisObject cCmisObject : cResult) {
			Document cDocument = (Document)cCmisObject;
			InputStream cIn = cDocument.getContentStream().getStream();
			FileOutputStream cOut = new FileOutputStream(new File("./resources/test_docs/for_full_text_search/" + cDocument.getName() + ".pdf"));
			byte[] anBytes = new byte[1024];
			int nBytesRead = 0;
			while((nBytesRead = cIn.read(anBytes)) >= 0) {
				cOut.write(anBytes, 0, nBytesRead);
			}
			
			cOut.flush();
			cOut.close();
		}

	}

}
