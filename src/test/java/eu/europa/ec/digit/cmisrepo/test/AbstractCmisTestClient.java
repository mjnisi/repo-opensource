package eu.europa.ec.digit.cmisrepo.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Policy;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.tck.impl.TestParameters;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;

import eu.europa.ec.digit.cmisrepo.test.util.PdfDocUtil;

public class AbstractCmisTestClient {

    private static final Logger logger = Logger.getLogger(AbstractCmisTestClient.class);

//    public static final String HOST = "hermdevcs4.cc.cec.eu.int";
    public static final String HOST = "localhost";
    public static final String SERVER_NAME = "decide_cmis";

    public static final String NAME_ROOT_FOLDER = "root_folder";

    public static final String NAME_USER_SYSADMIN = "username";
    public static final String NAME_USER_ADMIN = "admin";
    public static final String NAME_USER_TEST1 = "test";
    public static final String NAME_USER_TEST2 = "test2";

    public static final String PSWD_USER_SYSADMIN = "******";
    public static final String PSWD_USER_ADMIN = "adminadmin";
    public static final String PSWD_USER_TEST1 = "******";
    public static final String PSWD_USER_TEST2 = "******";

    public static final String NAME_TEST_REPO = "testrepo";


    protected static Session getSessionAdmin() {
        return getSession(NAME_USER_ADMIN, PSWD_USER_ADMIN, BindingType.WEBSERVICES, NAME_TEST_REPO);
    }


    protected static Session getSessionTest1() {
        return getSession(NAME_USER_TEST1, PSWD_USER_TEST1, BindingType.WEBSERVICES, NAME_TEST_REPO);
    }


    protected static Session getSessionTest2() {
        return getSession(NAME_USER_TEST2, PSWD_USER_TEST2, BindingType.WEBSERVICES, NAME_TEST_REPO);
    }


    protected static Session getSession(String username, String password, BindingType bindingType, String repoId) {
        return getSession(SERVER_NAME, username, password, bindingType, repoId);
    }


    protected static Session getSession(String repoName, String username, String password, BindingType bindingType,
            String repoId) {
        // default factory implementation
        SessionFactory factory = SessionFactoryImpl.newInstance();
        Map<String, String> parameter = new HashMap<String, String>();

        parameter.put(SessionParameter.USER, username);
        parameter.put(SessionParameter.PASSWORD, password);

        // connection settings
        parameter.put(SessionParameter.WEBSERVICES_ACL_SERVICE,
                "http://" + HOST + ":8080/" + repoName + "/services11/cmis?wsdl");
        parameter.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE,
                "http://" + HOST + ":8080/" + repoName + "/services11/cmis?wsdl");
        parameter.put(SessionParameter.WEBSERVICES_ACL_SERVICE,
                "http://" + HOST + ":8080/" + repoName + "/services11/cmis?wsdl");
        parameter.put(SessionParameter.WEBSERVICES_DISCOVERY_SERVICE,
                "http://" + HOST + ":8080/" + repoName + "/services11/cmis?wsdl");
        parameter.put(SessionParameter.WEBSERVICES_MULTIFILING_SERVICE,
                "http://" + HOST + ":8080/" + repoName + "/services11/cmis?wsdl");
        parameter.put(SessionParameter.WEBSERVICES_NAVIGATION_SERVICE,
                "http://" + HOST + ":8080/" + repoName + "/services11/cmis?wsdl");
        parameter.put(SessionParameter.WEBSERVICES_OBJECT_SERVICE,
                "http://" + HOST + ":8080/" + repoName + "/services11/cmis?wsdl");
        parameter.put(SessionParameter.WEBSERVICES_POLICY_SERVICE,
                "http://" + HOST + ":8080/" + repoName + "/services11/cmis?wsdl");
        parameter.put(SessionParameter.WEBSERVICES_RELATIONSHIP_SERVICE,
                "http://" + HOST + ":8080/" + repoName + "/services11/cmis?wsdl");
        parameter.put(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE,
                "http://" + HOST + ":8080/" + repoName + "/services11/cmis?wsdl");
        parameter.put(SessionParameter.WEBSERVICES_VERSIONING_SERVICE,
                "http://" + HOST + ":8080/" + repoName + "/services11/cmis?wsdl");
        parameter.put(SessionParameter.ATOMPUB_URL, "http://" + HOST + ":8080/" + repoName + "/atom11");
        parameter.put(SessionParameter.BROWSER_URL, "http://" + HOST + ":8080/" + repoName + "/browser");

        parameter.put(SessionParameter.BINDING_TYPE, bindingType.value());
        parameter.put(SessionParameter.REPOSITORY_ID, repoId);

        // create session
        Session session = factory.createSession(parameter);
        if (logger.isInfoEnabled())
            logger.info("Session created: " + session);

        return session;
    }


    protected static String getDocumentTestTypeId() {
        return TestParameters.DEFAULT_DOCUMENT_TYPE_VALUE;
    }


    protected static ContentStream getStreamForPdf(Session session, String docName, File localFile) throws Exception {
        InputStream cIn = new FileInputStream(localFile);
        return session.getObjectFactory().createContentStream(docName, localFile.length(), "application/pdf", cIn);
    }


    protected static ContentStream getContentStreamForPdf(Session session, String docName, InputStream inputStream,
            long length) throws Exception {
        return session.getObjectFactory().createContentStream(docName, length, "application/pdf", inputStream);
    }


    protected static ObjectId createFolder(Session session, String parentFolderId, String name) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(PropertyIds.NAME, name);
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
        return session.createFolder(properties, new ObjectIdImpl(parentFolderId));
    }


    protected static ObjectId createFolder(Session session, String parentFolderId, String name, List<Policy> policies,
            List<Ace> addAces, List<Ace> removeAces) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(PropertyIds.NAME, name);
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
        return session.createFolder(properties, new ObjectIdImpl(parentFolderId), policies, addAces, removeAces);
    }


    protected static ObjectId createPdfDocument(Session session, ObjectId folderId, String name, PDDocument doc)
            throws Exception {
        return createPdfDocument(session, folderId, name, doc, TestParameters.DEFAULT_DOCUMENT_TYPE_VALUE, null, null,
                null);
    }


    protected static void printAces(Session session, ObjectId objectId) {
        printAces(null, session, objectId);
    }


    protected static void printAces(String title, Session session, ObjectId objectId) {
        Acl cAcl = session.getAcl(objectId, false);
        if (title != null)
            System.out.println(title);
        for (Ace cAce : cAcl.getAces()) {
            System.out.println(cAce);
        }

    }


    protected static ObjectId createPdfDocument(Session session, ObjectId folderId, String name, PDDocument doc,
            String objectTypeId, List<Policy> policies, List<Ace> addAces, List<Ace> removeAces) throws Exception {
        Map<String, Object> cProperties = new HashMap<String, Object>();
        cProperties.put(PropertyIds.NAME, name);
        cProperties.put(PropertyIds.OBJECT_TYPE_ID, objectTypeId);
        InputStream cIn = null;
        ByteArrayOutputStream cOut = null;

        try {
            cOut = new ByteArrayOutputStream();
            doc.save(cOut);
            doc.close();
            cOut.flush();
            cIn = new ByteArrayInputStream(cOut.toByteArray());
            ContentStream cContent = getContentStreamForPdf(session, name, cIn, 0);
            return session.createDocument(cProperties, folderId, cContent, VersioningState.NONE, policies, addAces,
                    removeAces);
        } finally {
            if (cIn != null)
                cIn.close();

            if (cOut != null)
                cOut.close();
        }
    }


    protected static ContentStream getContentStream(Session session, String mimeType, File file)
            throws FileNotFoundException {
        return session.getObjectFactory().createContentStream(file.getName(), file.length(), mimeType,
                new FileInputStream(file));
    }


    protected static ObjectId createPdfDocument(Session session, ObjectId folderId, String name, int pages)
            throws Exception {
        return createPdfDocument(session, folderId, name, pages, null);
    }


    protected static ObjectId createPdfDocument(Session session, ObjectId folderId, String name, int pages,
            ObjectId objectId) throws Exception {
        InputStream cIn = null;

        try {
            String cObjectTypeId = getDocumentTestTypeId();
            Map<String, Object> cProperties = new HashMap<String, Object>();
            cProperties.put(PropertyIds.NAME, name);
            cProperties.put(PropertyIds.OBJECT_TYPE_ID, cObjectTypeId);
            if (objectId != null)
                cProperties.put(PropertyIds.OBJECT_ID, objectId);

            ContentStream cContent = null;
            if (pages > 0) {
                PDDocument cDoc = PdfDocUtil.getPdfDoc(pages);
                ByteArrayOutputStream cOut = new ByteArrayOutputStream();
                cDoc.save(cOut);
                cDoc.close();
                cIn = new ByteArrayInputStream(cOut.toByteArray());
                cContent = getContentStreamForPdf(session, name, cIn, 0);
            }

            if (objectId == null)
                return session.createDocument(cProperties, folderId, cContent, VersioningState.NONE);
            else
                return session.createDocument(cProperties, folderId, cContent, VersioningState.MINOR);
        } finally {
            if (cIn != null)
                cIn.close();
        }
    }


    public static ObjectType createNewType() {
        return null;
    }


    protected static void azzertEquals(Object expected, Object actual) {
        String cMessage = "Expected: " + expected + ", actual: " + actual;
        azzertEquals(expected, actual, cMessage);
    }


    protected static void azzertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) {
            return;
        } else
            if ((expected == null && actual != null) || (expected != null && actual == null)) {
                fail(message);
            } else
                if (expected instanceof Number) {
                    if (((Number) expected).doubleValue() != ((Number) actual).doubleValue())
                        fail(message);
                } else {
                    if (!expected.equals(actual))
                        fail(message);
                }
    }


    protected static void azzert(boolean condition) {
        azzertTrue(condition, null);
    }


    protected static void azzertTrue(boolean condition, String message) {
        if (!condition)
            fail(message);
    }


    protected static void fail(String message) {
        if (message != null && message.length() > 0)
            throw new RuntimeException(message);

        throw new RuntimeException();
    }


    protected static long getDelta(AtomicLong time0) {
        long nTime = System.currentTimeMillis();
        return nTime - time0.getAndSet(nTime);
    }

}
