package eu.europa.ec.digit.cmisrepo.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResetDatabase {

    private static final Logger logger = LoggerFactory.getLogger(ResetDatabase.class);


    public static void main(String[] args) throws Exception {
        if (logger.isInfoEnabled())
            logger.info("Starting...");

        resetDatabase();
    }


    public static void resetDatabase() throws Exception {
        Class.forName("oracle.jdbc.OracleDriver");
        Connection cConn = DriverManager.getConnection(
                "jdbc:oracle:thin:@olrdev8.cc.cec.eu.int:1597/HER2COMD_TAF.cc.cec.eu.int", "CMIS_Repo", "0ra.pass_");

        // deleteEverything(cConn);
        // createTestData(cConn);
         dropEverything(cConn);
         createTablesAndViews(cConn);

        cConn.close();
    }


    static void deleteEverything(Connection cConn) throws Exception {
        List<String> cLstTableNames = new LinkedList<>();
        cLstTableNames.add("ACL");
        cLstTableNames.add("CHANGE_EVENT");
        cLstTableNames.add("INDEX_WORD_POSITION");
        cLstTableNames.add("OBJECT_CHILD");
        cLstTableNames.add("OBJECT_POLICY");
        cLstTableNames.add("OBJECT_SECONDARY_TYPE");
        cLstTableNames.add("OBJECT_TYPE_RELATIONSHIP");
        cLstTableNames.add("RENDITION");
        cLstTableNames.add("REPOSITORY_POLICY");
        cLstTableNames.add("SECURITY_HANDLER");
        cLstTableNames.add("PERMISSION_MAPPING");
        cLstTableNames.add("PROPERTY");
        cLstTableNames.add("STREAM");
        cLstTableNames.add("REPOSITORY");
        cLstTableNames.add("INDEX_WORD_OBJECT");
        cLstTableNames.add("INDEX_WORD");
        cLstTableNames.add("OBJECT_TYPE_PROPERTY");
        cLstTableNames.add("OBJECT");
        cLstTableNames.add("PERMISSION");
        cLstTableNames.add("OBJECT_TYPE");

        Statement cStmt = cConn.createStatement();
        cConn.setAutoCommit(true);

        // Delete data
        for (String cTableName : cLstTableNames) {
            if (logger.isInfoEnabled())
                logger.info("Deleting ALL from: " + cTableName);

            try {
                cStmt.executeQuery("TRUNCATE TABLE " + cTableName + " CASCADE DROP STORAGE");
            } catch (Exception e) {
                if (!e.getMessage().contains("table or view does not exist"))
                    throw e;
            }
        }

        cStmt.close();
    }


    static void dropEverything(Connection cConn) throws Exception {
        List<String> cLstTableNames = new LinkedList<>();
        cLstTableNames.add("ACL");
        cLstTableNames.add("CHANGE_EVENT");
        cLstTableNames.add("INDEX_WORD_POSITION");
        cLstTableNames.add("OBJECT_CHILD");
        cLstTableNames.add("OBJECT_POLICY");
        cLstTableNames.add("OBJECT_SECONDARY_TYPE");
        cLstTableNames.add("OBJECT_TYPE_RELATIONSHIP");
        cLstTableNames.add("RENDITION");
        cLstTableNames.add("REPOSITORY_POLICY");
        cLstTableNames.add("SECURITY_HANDLER");
        cLstTableNames.add("INDEX_WORD_OBJECT");
        cLstTableNames.add("INDEX_WORD");
        cLstTableNames.add("PERMISSION_MAPPING");
        cLstTableNames.add("PROPERTY");
        cLstTableNames.add("STREAM");
        cLstTableNames.add("OBJECT_TYPE_PROPERTY");
        cLstTableNames.add("OBJECT");
        cLstTableNames.add("PERMISSION");
        cLstTableNames.add("OBJECT_TYPE");
        cLstTableNames.add("REPOSITORY");

        Statement cStmt = cConn.createStatement();
        cConn.setAutoCommit(true);

        // Delete data
        for (String cTableName : cLstTableNames) {
            if (logger.isInfoEnabled())
                logger.info("Dropping table: " + cTableName);

            try {
                cStmt.executeQuery("DROP TABLE " + cTableName);
            } catch (Exception e) {
                if (!e.getMessage().contains("table or view does not exist"))
                    throw e;
            }
        }

        List<String> cLstViewNames = new LinkedList<>();
        cLstViewNames.add("ANCESTORS_VIEW");
        cLstViewNames.add("CHILDREN_COUNT_VIEW");
        cLstViewNames.add("DESCENDANTS_VIEW");
        cLstViewNames.add("OBJECT_PATH_VIEW");
        cLstViewNames.add("OBJECT_VERSION_VIEW");
        cLstViewNames.add("PARENTS_COUNT_VIEW");
        cLstViewNames.add("SCORE_VIEW");

        for (String cViewName : cLstViewNames) {
            if (logger.isInfoEnabled())
                logger.info("Dropping view: " + cViewName);

            try {
                cStmt.executeQuery("DROP VIEW " + cViewName);
            } catch (Exception e) {
                if (!e.getMessage().contains("table or view does not exist"))
                    throw e;
            }
        }

        List<String> cLstSequenceNames = new LinkedList<>();
        cLstSequenceNames.add("SQ_ACL");
        cLstSequenceNames.add("SQ_CHANGE_EVENT");
        cLstSequenceNames.add("SQ_INDEX_WORD");
        cLstSequenceNames.add("SQ_INDEX_WORD_OBJECT");
        cLstSequenceNames.add("SQ_OBJECT");
        cLstSequenceNames.add("SQ_OBJECT_TYPE");
        cLstSequenceNames.add("SQ_OBJECT_TYPE_PROPERTY");
        cLstSequenceNames.add("SQ_OBJECT_TYPE_RELATIONSHIP");
        cLstSequenceNames.add("SQ_PERMISSION");
        cLstSequenceNames.add("SQ_PERMISSION_MAPPING");
        cLstSequenceNames.add("SQ_PROPERTY");
        cLstSequenceNames.add("SQ_RENDITION");
        cLstSequenceNames.add("SQ_REPOSITORY");
        cLstSequenceNames.add("SQ_SECURITY_HANDLER");

        for (String cSequenceName : cLstSequenceNames) {
            if (logger.isInfoEnabled())
                logger.info("Dropping sequence: " + cSequenceName);

            try {
                cStmt.executeQuery("DROP SEQUENCE " + cSequenceName);
            } catch (Exception e) {
                if (!e.getMessage().contains("sequence does not exist"))
                    throw e;
            }
        }

        cStmt.executeQuery("PURGE RECYCLEBIN");

        cStmt.close();
    }


    public static void createTablesAndViews(Connection a_cConn) throws Exception {
        Statement stmt = a_cConn.createStatement();

        List<String> lst = getAllStatements("01_repo_structure.sql");
        for (String statement : lst) {
            System.out.println("Executing: " + statement);

            stmt.execute(statement);
        }

        lst = getAllStatements("02_repo_scoreView_oracle.sql");
        for (String statement : lst) {
            System.out.println("Executing: " + statement);
            stmt.execute(statement);
        }

        lst = getAllStatements("03_repo_updateAncestorsView_oracle.sql");
        for (String statement : lst) {
            System.out.println("Executing: " + statement);
            stmt.execute(statement);
        }

//        stmt.execute("SHUTDOWN");
        a_cConn.close();
    }


    public static List<String> getAllStatements(String filename) throws Exception {
        File file = new File("./releases/1.3.0/db/consolidated/" + filename);
        BufferedReader reader = null;
        List<String> lst = new LinkedList<>();
        try {
            reader = new BufferedReader(new FileReader(file));
            String statement = null;
            while ((statement = getNextStatement(reader)) != null) {
                lst.add(statement);
            }
        } finally {
            if (reader != null)
                reader.close();
        }

        return lst;
    }


    private static String getNextStatement(BufferedReader reader) throws IOException {
        String line = null;
        List<String> lst = new LinkedList<>();
        while ((line = reader.readLine()) != null) {
            if (line.trim().indexOf("--") == 0)
                continue;

            if (line.indexOf(';') > 0) {
                line = line.replace(";", "");
                lst.add(line);
                break;
            }

            lst.add(line);
        }

        if (lst.size() == 0)
            return null;

        StringBuilder buf = new StringBuilder();
        for (String line2 : lst) {
            buf.append(line2).append('\n');
        }

        return buf.toString();
    }


    static void createTestData(Connection cConn) throws SQLException {
        /*
         * Insert base data
         */
        Statement cStmt = cConn.createStatement();

        if (logger.isInfoEnabled())
            logger.info("Creating base data...");

        // REPOSITORY
        cStmt.execute(
                "Insert into REPOSITORY (ID,CMIS_ID,NAME,DESCRIPTION,C_GET_DESCENDANTS,C_GET_FOLDER_TREE,C_CONTENT_STREAM_UPDATABILITY,C_CHANGES,C_RENDITIONS,C_MULTIFILING,C_UNFILING,C_VERSION_SPECIFIC_FILING,C_PWC_UPDATABLE,C_PWC_SEARCHABLE,C_ALL_VERSIONS_SEARCHABLE,C_QUERY,C_JOIN,C_ACL,C_ACL_PROPAGATION,SECURITY_TYPE,AUTHENTICATION_HANDLER,AUTHORISATION_HANDLER) values (10000,'testrepo','Test Repository','ForTestRepo','T','T','anytime','objectidsonly','read','T','T','T','T','T','T','bothcombined','innerandouter','manage','propagate','SIMPLE','mock','builtin')");

        // CHANGE_EVENT
        cStmt.execute(
                "Insert into CHANGE_EVENT (ID,REPOSITORY_ID,CMIS_OBJECT_ID,CHANGE_TYPE,CHANGE_LOG_TOKEN,CHANGE_TIME,USERNAME) values (10000,10000,'f7071dd4fb42ee762729b122676d8fc99bc0a45','created','4f5baffaa0fd8f1487532d3ba5882bf115137294',to_timestamp('06-JUL-18 08.58.33.738000000','DD-MON-RR HH24.MI.SSXFF'),'internal:system')");

        // INDEX_WORD
        cStmt.execute(
                "Insert into INDEX_WORD (ID,REPOSITORY_ID,WORD) values (10000,10000,'369819149707fa0b46a6c161bd77a2bc62dfa8dc')");
        cStmt.execute("Insert into INDEX_WORD (ID,REPOSITORY_ID,WORD) values (10001,10000,'admin')");
        cStmt.execute("Insert into INDEX_WORD (ID,REPOSITORY_ID,WORD) values (10002,10000,'rootfolder')");

        // INDEX_WORD_OBJECT
        cStmt.execute(
                "Insert into INDEX_WORD_OBJECT (ID,WORD_ID,OBJECT_ID,PROPERTY_ID,OBJECT_TYPE_PROPERTY_ID,FREQUENCY,SQRT_FREQUENCY) values (10000,10002,10000,10000,10026,1,1)");
        cStmt.execute(
                "Insert into INDEX_WORD_OBJECT (ID,WORD_ID,OBJECT_ID,PROPERTY_ID,OBJECT_TYPE_PROPERTY_ID,FREQUENCY,SQRT_FREQUENCY) values (10001,10001,10000,10004,10032,1,1)");
        cStmt.execute(
                "Insert into INDEX_WORD_OBJECT (ID,WORD_ID,OBJECT_ID,PROPERTY_ID,OBJECT_TYPE_PROPERTY_ID,FREQUENCY,SQRT_FREQUENCY) values (10002,10001,10000,10006,10034,1,1)");
        cStmt.execute(
                "Insert into INDEX_WORD_OBJECT (ID,WORD_ID,OBJECT_ID,PROPERTY_ID,OBJECT_TYPE_PROPERTY_ID,FREQUENCY,SQRT_FREQUENCY) values (10003,10000,10000,10008,10036,1,1)");

        // INDEX_WORD_POSITION
        cStmt.execute("Insert into INDEX_WORD_POSITION (WORD_OBJECT_ID,POSITION,STEP) values (10000,1,1)");
        cStmt.execute("Insert into INDEX_WORD_POSITION (WORD_OBJECT_ID,POSITION,STEP) values (10001,1,1)");
        cStmt.execute("Insert into INDEX_WORD_POSITION (WORD_OBJECT_ID,POSITION,STEP) values (10002,1,1)");
        cStmt.execute("Insert into INDEX_WORD_POSITION (WORD_OBJECT_ID,POSITION,STEP) values (10003,1,1)");

        // PERMISSION
        cStmt.execute(
                "Insert into PERMISSION (ID,REPOSITORY_ID,NAME,DESCRIPTION,PARENT_ID) values (10000,10000,'cmis:all','All',null)");
        cStmt.execute(
                "Insert into PERMISSION (ID,REPOSITORY_ID,NAME,DESCRIPTION,PARENT_ID) values (10001,10000,'cmis:write','Write',10000)");
        cStmt.execute(
                "Insert into PERMISSION (ID,REPOSITORY_ID,NAME,DESCRIPTION,PARENT_ID) values (10002,10000,'cmis:read','Read',10001)");

        // OBJECT_TYPE
        cStmt.execute(
                "Insert into OBJECT_TYPE (ID,REPOSITORY_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,BASE_ID,PARENT_ID,DESCRIPTION,CREATABLE,FILEABLE,QUERYABLE,CONTROLLABLE_POLICY,CONTROLLABLE_ACL,FULLTEXT_INDEXED,INCLUDED_IN_SUPERTYPE_QUERY,VERSIONABLE,CONTENT_STREAM_ALLOWED) values (10000,10000,'cmis:document','cmis:document','http://ec.europa.eu/trade/repo','cmis:document','cmis:document',10000,null,'cmis:document','T','T','T','T','T','T','T','T','allowed')");
        cStmt.execute(
                "Insert into OBJECT_TYPE (ID,REPOSITORY_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,BASE_ID,PARENT_ID,DESCRIPTION,CREATABLE,FILEABLE,QUERYABLE,CONTROLLABLE_POLICY,CONTROLLABLE_ACL,FULLTEXT_INDEXED,INCLUDED_IN_SUPERTYPE_QUERY,VERSIONABLE,CONTENT_STREAM_ALLOWED) values (10001,10000,'cmis:folder','cmis:folder','http://ec.europa.eu/trade/repo','cmis:folder','cmis:folder',10001,null,'cmis:folder','T','T','T','T','T','T','T',null,'notallowed')");
        cStmt.execute(
                "Insert into OBJECT_TYPE (ID,REPOSITORY_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,BASE_ID,PARENT_ID,DESCRIPTION,CREATABLE,FILEABLE,QUERYABLE,CONTROLLABLE_POLICY,CONTROLLABLE_ACL,FULLTEXT_INDEXED,INCLUDED_IN_SUPERTYPE_QUERY,VERSIONABLE,CONTENT_STREAM_ALLOWED) values (10002,10000,'cmis:relationship','cmis:relationship','http://ec.europa.eu/trade/repo','cmis:relationship','cmis:relationship',10002,null,'cmis:relationship','T','F','T','T','T','T','T',null,'notallowed')");
        cStmt.execute(
                "Insert into OBJECT_TYPE (ID,REPOSITORY_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,BASE_ID,PARENT_ID,DESCRIPTION,CREATABLE,FILEABLE,QUERYABLE,CONTROLLABLE_POLICY,CONTROLLABLE_ACL,FULLTEXT_INDEXED,INCLUDED_IN_SUPERTYPE_QUERY,VERSIONABLE,CONTENT_STREAM_ALLOWED) values (10003,10000,'cmis:policy','cmis:policy','http://ec.europa.eu/trade/repo','cmis:policy','cmis:policy',10003,null,'cmis:policy','T','T','T','T','T','T','T',null,'notallowed')");
        cStmt.execute(
                "Insert into OBJECT_TYPE (ID,REPOSITORY_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,BASE_ID,PARENT_ID,DESCRIPTION,CREATABLE,FILEABLE,QUERYABLE,CONTROLLABLE_POLICY,CONTROLLABLE_ACL,FULLTEXT_INDEXED,INCLUDED_IN_SUPERTYPE_QUERY,VERSIONABLE,CONTENT_STREAM_ALLOWED) values (10004,10000,'cmis:item','cmis:item','http://ec.europa.eu/trade/repo','cmis:item','cmis:item',10004,null,'cmis:item','T','T','T','T','T','T','T',null,'notallowed')");
        cStmt.execute(
                "Insert into OBJECT_TYPE (ID,REPOSITORY_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,BASE_ID,PARENT_ID,DESCRIPTION,CREATABLE,FILEABLE,QUERYABLE,CONTROLLABLE_POLICY,CONTROLLABLE_ACL,FULLTEXT_INDEXED,INCLUDED_IN_SUPERTYPE_QUERY,VERSIONABLE,CONTENT_STREAM_ALLOWED) values (10005,10000,'cmis:secondary','cmis:secondary','http://ec.europa.eu/trade/repo','cmis:secondary','cmis:secondary',10005,null,'cmis:secondary','F','F','T','F','F','T','T',null,'notallowed')");

        // OBJECT
        cStmt.execute(
                "Insert into OBJECT (ID,OBJECT_TYPE_ID,CMIS_OBJECT_ID,INDEX_STATE_CONTENT,INDEX_TRIES_CONTENT,INDEX_STATE_METADATA,INDEX_TRIES_METADATA) values (10000,10001,'f7071dd4fb42ee762729b122676d8fc99bc0a45',0,0,1,0)");

        // ACL
        cStmt.execute(
                "Insert into ACL (ID,OBJECT_ID,PRINCIPAL_ID,PERMISSION_ID,IS_DIRECT) values (10000,10000,'cmis:anyone',10000,'T')");

        // PERMISSION_MAPPING
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10000,10000,'canGetDescendents.Folder',10002)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10001,10000,'canGetChildren.Folder',10002)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10002,10000,'canGetFolderParent.Object',10002)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10003,10000,'canGetParents.Folder',10002)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10004,10000,'canCreateDocument.Folder',10002)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10005,10000,'canCreateFolder.Folder',10002)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10006,10000,'canCreateRelationship.Source',10002)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10007,10000,'canCreateRelationship.Target',10002)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10008,10000,'canGetProperties.Object',10002)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10009,10000,'canUpdateProperties.Object',10001)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10010,10000,'canMove.Object',10001)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10011,10000,'canMove.Target',10002)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10012,10000,'canMove.Source',10002)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10013,10000,'canDelete.Object',10001)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10014,10000,'canViewContent.Object',10001)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10015,10000,'canSetContent.Document',10001)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10016,10000,'canDeleteContent.Document',10001)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10017,10000,'canDeleteTree.Folder',10001)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10018,10000,'canAddToFolder.Object',10002)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10019,10000,'canAddToFolder.Folder',10002)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10020,10000,'canRemoveFromFolder.Object',10002)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10021,10000,'canRemoveFromFolder.Folder',10002)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10022,10000,'canCheckout.Document',10001)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10023,10000,'canCancelCheckout.Document',10001)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10024,10000,'canCheckin.Document',10001)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10025,10000,'canGetAllVersions.VersionSeries',10002)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10026,10000,'canGetObjectRelationships.Object',10002)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10027,10000,'canAddPolicy.Object',10002)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10028,10000,'canAddPolicy.Policy',10002)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10029,10000,'canRemovePolicy.Object',10002)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10030,10000,'canRemovePolicy.Policy',10002)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10031,10000,'canGetAppliedPolicies.Object',10002)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10032,10000,'canGetACL.Object',10002)");
        cStmt.execute(
                "Insert into PERMISSION_MAPPING (ID,REPOSITORY_ID,KEY,PERMISSION_ID) values (10033,10000,'canApplyACL.Object',10001)");

        // OBJECT_TYPE_PROPERTY
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10075,10004,'cmis:changeToken','cmis:changeToken','http://ec.europa.eu/trade/repo','cmis:changeToken','cmis:changeToken','Token used for optimistic locking and concurrency checking','string','single','readonly','F','F','F',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10000,10000,'cmis:name','cmis:name','http://ec.europa.eu/trade/repo','cmis:name','cmis:name','Name of the Object','string','single','readwrite','T','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10001,10000,'cmis:description','cmis:description','http://ec.europa.eu/trade/repo','cmis:description','cmis:description','Description of the Object','string','single','readwrite','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10002,10000,'cmis:objectId','cmis:objectId','http://ec.europa.eu/trade/repo','cmis:objectId','cmis:objectId','Id of the object','id','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10003,10000,'cmis:baseTypeId','cmis:baseTypeId','http://ec.europa.eu/trade/repo','cmis:baseTypeId','cmis:baseTypeId','Id of the base object-type for the object','id','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10004,10000,'cmis:objectTypeId','cmis:objectTypeId','http://ec.europa.eu/trade/repo','cmis:objectTypeId','cmis:objectTypeId','Id of the object''s type','id','single','oncreate','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10005,10000,'cmis:secondaryObjectTypeIds','cmis:secondaryObjectTypeIds','http://ec.europa.eu/trade/repo','cmis:secondaryObjectTypeIds','cmis:secondaryObjectTypeIds','Ids of the object''s secondary types','id','multi','readwrite','F','T','F',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10006,10000,'cmis:createdBy','cmis:createdBy','http://ec.europa.eu/trade/repo','cmis:createdBy','cmis:createdBy','User who created the object','string','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10007,10000,'cmis:creationDate','cmis:creationDate','http://ec.europa.eu/trade/repo','cmis:creationDate','cmis:creationDate','DateTime when the object was created','datetime','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10008,10000,'cmis:lastModifiedBy','cmis:lastModifiedBy','http://ec.europa.eu/trade/repo','cmis:lastModifiedBy','cmis:lastModifiedBy','User who last modified the object','string','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10009,10000,'cmis:lastModificationDate','cmis:lastModificationDate','http://ec.europa.eu/trade/repo','cmis:lastModificationDate','cmis:lastModificationDate','DateTime when the object was last modified','datetime','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10010,10000,'cmis:changeToken','cmis:changeToken','http://ec.europa.eu/trade/repo','cmis:changeToken','cmis:changeToken','Opaque token used for optimistic locking and concurrency checking','string','single','readonly','F','F','F',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10011,10000,'cmis:isImmutable','cmis:isImmutable','http://ec.europa.eu/trade/repo','cmis:isImmutable','cmis:isImmutable','TRUE if the repository MUST throw and error at any attempt to update or delete the object','boolean','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10012,10000,'cmis:isLatestVersion','cmis:isLatestVersion','http://ec.europa.eu/trade/repo','cmis:isLatestVersion','cmis:isLatestVersion','If the Document object is the Latest Version in its Version Series','boolean','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10013,10000,'cmis:isMajorVersion','cmis:isMajorVersion','http://ec.europa.eu/trade/repo','cmis:isMajorVersion','cmis:isMajorVersion','If the Document object is a Major Version in its Version Series','boolean','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10014,10000,'cmis:isLatestMajorVersion','cmis:isLatestMajorVersion','http://ec.europa.eu/trade/repo','cmis:isLatestMajorVersion','cmis:isLatestMajorVersion','If the Document object is the Latest Major Version in its Version Series','boolean','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10015,10000,'cmis:isPrivateWorkingCopy','cmis:isPrivateWorkingCopy','http://ec.europa.eu/trade/repo','cmis:isPrivateWorkingCopy','cmis:isPrivateWorkingCopy','If the Document object is a Private Working Copy','boolean','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10016,10000,'cmis:versionLabel','cmis:versionLabel','http://ec.europa.eu/trade/repo','cmis:versionLabel','cmis:versionLabel','Textual description the position of an individual object with respect to the version series','string','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10017,10000,'cmis:versionSeriesId','cmis:versionSeriesId','http://ec.europa.eu/trade/repo','cmis:versionSeriesId','cmis:versionSeriesId','ID of the Version Series for this Object','id','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10018,10000,'cmis:isVersionSeriesCheckedOut','cmis:isVersionSeriesCheckedOut','http://ec.europa.eu/trade/repo','cmis:isVersionSeriesCheckedOut','cmis:isVersionSeriesCheckedOut','If there currenly exists a Private Working Copy for this Version Series','boolean','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10019,10000,'cmis:versionSeriesCheckedOutBy','cmis:versionSeriesCheckedOutBy','http://ec.europa.eu/trade/repo','cmis:versionSeriesCheckedOutBy','cmis:versionSeriesCheckedOutBy','If IsVersionSeriesCheckedOut then an identifier for the user who created the Private Working Copy','string','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10020,10000,'cmis:versionSeriesCheckedOutId','cmis:versionSeriesCheckedOutId','http://ec.europa.eu/trade/repo','cmis:versionSeriesCheckedOutId','cmis:versionSeriesCheckedOutId','If IsVersionSeriesCheckedOut the Identifier for the Private Working Copy','id','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10021,10000,'cmis:checkinComment','cmis:checkinComment','http://ec.europa.eu/trade/repo','cmis:checkinComment','cmis:checkinComment','Textual comment associated with the given version','string','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10022,10000,'cmis:contentStreamLength','cmis:contentStreamLength','http://ec.europa.eu/trade/repo','cmis:contentStreamLength','cmis:contentStreamLength','Length of the content stream (in bytes)','integer','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10023,10000,'cmis:contentStreamMimeType','cmis:contentStreamMimeType','http://ec.europa.eu/trade/repo','cmis:contentStreamMimeType','cmis:contentStreamMimeType','MIME type of the Content Stream','string','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10024,10000,'cmis:contentStreamFileName','cmis:contentStreamFileName','http://ec.europa.eu/trade/repo','cmis:contentStreamFileName','cmis:contentStreamFileName','File name of the Content Stream','string','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10025,10000,'cmis:contentStreamId','cmis:contentStreamId','http://ec.europa.eu/trade/repo','cmis:contentStreamId','cmis:contentStreamId','Id of the stream','id','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10026,10001,'cmis:name','cmis:name','http://ec.europa.eu/trade/repo','cmis:name','cmis:name','Name of the object','string','single','readwrite','T','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10027,10001,'cmis:description','cmis:description','http://ec.europa.eu/trade/repo','cmis:description','cmis:description','Description of the object','string','single','readwrite','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10028,10001,'cmis:objectId','cmis:objectId','http://ec.europa.eu/trade/repo','cmis:objectId','cmis:objectId','Id of the object','id','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10029,10001,'cmis:baseTypeId','cmis:baseTypeId','http://ec.europa.eu/trade/repo','cmis:baseTypeId','cmis:baseTypeId','Id of the base object-type for the object','id','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10030,10001,'cmis:objectTypeId','cmis:objectTypeId','http://ec.europa.eu/trade/repo','cmis:objectTypeId','cmis:objectTypeId','Id of the object''s type','id','single','oncreate','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10031,10001,'cmis:secondaryObjectTypeIds','cmis:secondaryObjectTypeIds','http://ec.europa.eu/trade/repo','cmis:secondaryObjectTypeIds','cmis:secondaryObjectTypeIds','Ids of the object''s secondary types','id','multi','readwrite','F','T','F',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10032,10001,'cmis:createdBy','cmis:createdBy','http://ec.europa.eu/trade/repo','cmis:createdBy','cmis:createdBy','User who created the object','string','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10033,10001,'cmis:creationDate','cmis:creationDate','http://ec.europa.eu/trade/repo','cmis:creationDate','cmis:creationDate','Date Time when the object was created','datetime','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10034,10001,'cmis:lastModifiedBy','cmis:lastModifiedBy','http://ec.europa.eu/trade/repo','cmis:lastModifiedBy','cmis:lastModifiedBy','User who last modified the object','string','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10035,10001,'cmis:lastModificationDate','cmis:lastModificationDate','http://ec.europa.eu/trade/repo','cmis:lastModificationDate','cmis:lastModificationDate','Date Time when the object was last modified','datetime','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10036,10001,'cmis:changeToken','cmis:changeToken','http://ec.europa.eu/trade/repo','cmis:changeToken','cmis:changeToken','Token used for optimistic locking and concurrency checking','string','single','readonly','F','F','F',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10037,10001,'cmis:parentId','cmis:parentId','http://ec.europa.eu/trade/repo','cmis:parentId','cmis:parentId','PropertyType.ID.value() of the parent folder of the folder','id','single','readonly','F','T','F',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10038,10001,'cmis:path','cmis:path','http://ec.europa.eu/trade/repo','cmis:path','cmis:path','The fully qualified path to this folder','string','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10039,10001,'cmis:allowedChildObjectTypeIds','cmis:allowedChildObjectTypeIds','http://ec.europa.eu/trade/repo','cmis:allowedChildObjectTypeIds','cmis:allowedChildObjectTypeIds','Id''s of the set of Object-types that can be created, moved or filed into this folder','id','multi','readonly','F','F','F',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10040,10002,'cmis:name','cmis:name','http://ec.europa.eu/trade/repo','cmis:name','cmis:name','Name of the object','string','single','readwrite','T','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10041,10002,'cmis:description','cmis:description','http://ec.europa.eu/trade/repo','cmis:description','cmis:description','Description of the object','string','single','readwrite','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10042,10002,'cmis:objectId','cmis:objectId','http://ec.europa.eu/trade/repo','cmis:objectId','cmis:objectId','Id of the object','id','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10043,10002,'cmis:baseTypeId','cmis:baseTypeId','http://ec.europa.eu/trade/repo','cmis:baseTypeId','cmis:baseTypeId','Id of the base object-type for the object','id','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10044,10002,'cmis:objectTypeId','cmis:objectTypeId','http://ec.europa.eu/trade/repo','cmis:objectTypeId','cmis:objectTypeId','Id of the object''s type','id','single','oncreate','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10045,10002,'cmis:secondaryObjectTypeIds','cmis:secondaryObjectTypeIds','http://ec.europa.eu/trade/repo','cmis:secondaryObjectTypeIds','cmis:secondaryObjectTypeIds','Ids of the object''s secondary types','id','multi','readwrite','F','T','F',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10046,10002,'cmis:createdBy','cmis:createdBy','http://ec.europa.eu/trade/repo','cmis:createdBy','cmis:createdBy','User who created the object','string','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10047,10002,'cmis:creationDate','cmis:creationDate','http://ec.europa.eu/trade/repo','cmis:creationDate','cmis:creationDate','Date Time when the object was created','datetime','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10048,10002,'cmis:lastModifiedBy','cmis:lastModifiedBy','http://ec.europa.eu/trade/repo','cmis:lastModifiedBy','cmis:lastModifiedBy','User who last modified the object','string','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10049,10002,'cmis:lastModificationDate','cmis:lastModificationDate','http://ec.europa.eu/trade/repo','cmis:lastModificationDate','cmis:lastModificationDate','Date Time when the object was last modified','datetime','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10050,10002,'cmis:changeToken','cmis:changeToken','http://ec.europa.eu/trade/repo','cmis:changeToken','cmis:changeToken','Token used for optimistic locking and concurrency checking','string','single','readonly','F','F','F',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10051,10002,'cmis:sourceId','cmis:sourceId','http://ec.europa.eu/trade/repo','cmis:sourceId','cmis:sourceId','ID of the source object of the relationship','id','single','oncreate','T','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10052,10002,'cmis:targetId','cmis:targetId','http://ec.europa.eu/trade/repo','cmis:targetId','cmis:targetId','ID of the target object of the relationship','id','single','oncreate','T','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10053,10003,'cmis:name','cmis:name','http://ec.europa.eu/trade/repo','cmis:name','cmis:name','Name of the object','string','single','readwrite','T','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10054,10003,'cmis:description','cmis:description','http://ec.europa.eu/trade/repo','cmis:description','cmis:description','Description of the object','string','single','readwrite','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10055,10003,'cmis:objectId','cmis:objectId','http://ec.europa.eu/trade/repo','cmis:objectId','cmis:objectId','Id of the object','id','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10056,10003,'cmis:baseTypeId','cmis:baseTypeId','http://ec.europa.eu/trade/repo','cmis:baseTypeId','cmis:baseTypeId','Id of the base object-type for the object','id','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10057,10003,'cmis:objectTypeId','cmis:objectTypeId','http://ec.europa.eu/trade/repo','cmis:objectTypeId','cmis:objectTypeId','Id of the object''s type','id','single','oncreate','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10058,10003,'cmis:secondaryObjectTypeIds','cmis:secondaryObjectTypeIds','http://ec.europa.eu/trade/repo','cmis:secondaryObjectTypeIds','cmis:secondaryObjectTypeIds','Ids of the object''s secondary types','id','multi','readwrite','F','T','F',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10059,10003,'cmis:createdBy','cmis:createdBy','http://ec.europa.eu/trade/repo','cmis:createdBy','cmis:createdBy','User who created the object','string','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10060,10003,'cmis:creationDate','cmis:creationDate','http://ec.europa.eu/trade/repo','cmis:creationDate','cmis:creationDate','Date Time when the object was created','datetime','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10061,10003,'cmis:lastModifiedBy','cmis:lastModifiedBy','http://ec.europa.eu/trade/repo','cmis:lastModifiedBy','cmis:lastModifiedBy','User who last modified the object','string','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10062,10003,'cmis:lastModificationDate','cmis:lastModificationDate','http://ec.europa.eu/trade/repo','cmis:lastModificationDate','cmis:lastModificationDate','Date Time when the object was last modified','datetime','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10063,10003,'cmis:changeToken','cmis:changeToken','http://ec.europa.eu/trade/repo','cmis:changeToken','cmis:changeToken','Token used for optimistic locking and concurrency checking','string','single','readonly','F','F','F',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10064,10003,'cmis:policyText','cmis:policyText','http://ec.europa.eu/trade/repo','cmis:policyText','cmis:policyText','User-friendly description of the policy','string','single','readwrite','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10065,10004,'cmis:name','cmis:name','http://ec.europa.eu/trade/repo','cmis:name','cmis:name','Name of the object','string','single','readwrite','T','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10066,10004,'cmis:description','cmis:description','http://ec.europa.eu/trade/repo','cmis:description','cmis:description','Description of the object','string','single','readwrite','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10067,10004,'cmis:objectId','cmis:objectId','http://ec.europa.eu/trade/repo','cmis:objectId','cmis:objectId','Id of the object','id','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10068,10004,'cmis:baseTypeId','cmis:baseTypeId','http://ec.europa.eu/trade/repo','cmis:baseTypeId','cmis:baseTypeId','Id of the base object-type for the object','id','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10069,10004,'cmis:objectTypeId','cmis:objectTypeId','http://ec.europa.eu/trade/repo','cmis:objectTypeId','cmis:objectTypeId','Id of the object''s type','id','single','oncreate','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10070,10004,'cmis:secondaryObjectTypeIds','cmis:secondaryObjectTypeIds','http://ec.europa.eu/trade/repo','cmis:secondaryObjectTypeIds','cmis:secondaryObjectTypeIds','Ids of the object''s secondary types','id','multi','readwrite','F','T','F',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10071,10004,'cmis:createdBy','cmis:createdBy','http://ec.europa.eu/trade/repo','cmis:createdBy','cmis:createdBy','User who created the object','string','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10072,10004,'cmis:creationDate','cmis:creationDate','http://ec.europa.eu/trade/repo','cmis:creationDate','cmis:creationDate','Date Time when the object was created','datetime','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10073,10004,'cmis:lastModifiedBy','cmis:lastModifiedBy','http://ec.europa.eu/trade/repo','cmis:lastModifiedBy','cmis:lastModifiedBy','User who last modified the object','string','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");
        cStmt.execute(
                "Insert into OBJECT_TYPE_PROPERTY (ID,OBJECT_TYPE_ID,CMIS_ID,LOCAL_NAME,LOCAL_NAMESPACE,QUERY_NAME,DISPLAY_NAME,DESCRIPTION,PROPERTY_TYPE,CARDINALITY,UPDATABILITY,REQUIRED,QUERYABLE,ORDERABLE,CHOICES,OPEN_CHOICE,DEFAULT_VALUE,MIN_VALUE,MAX_VALUE,RESOLUTION,PRECISION,MAX_LENGTH) values (10074,10004,'cmis:lastModificationDate','cmis:lastModificationDate','http://ec.europa.eu/trade/repo','cmis:lastModificationDate','cmis:lastModificationDate','Date Time when the object was last modified','datetime','single','readonly','F','T','T',null,null,null,null,null,null,null,null)");

        // PROPERTY
        cStmt.execute(
                "Insert into PROPERTY (ID,OBJECT_ID,OBJECT_TYPE_PROPERTY_ID,VALUE,NUMERIC_VALUE,NORMALIZED_VALUE) values (10000,10000,10026,'rootFolder',0,'rootfolder')");
        cStmt.execute(
                "Insert into PROPERTY (ID,OBJECT_ID,OBJECT_TYPE_PROPERTY_ID,VALUE,NUMERIC_VALUE,NORMALIZED_VALUE) values (10001,10000,10028,'f7071dd4fb42ee762729b122676d8fc99bc0a45',0,null)");
        cStmt.execute(
                "Insert into PROPERTY (ID,OBJECT_ID,OBJECT_TYPE_PROPERTY_ID,VALUE,NUMERIC_VALUE,NORMALIZED_VALUE) values (10002,10000,10029,'cmis:folder',0,null)");
        cStmt.execute(
                "Insert into PROPERTY (ID,OBJECT_ID,OBJECT_TYPE_PROPERTY_ID,VALUE,NUMERIC_VALUE,NORMALIZED_VALUE) values (10003,10000,10030,'cmis:folder',0,null)");
        cStmt.execute(
                "Insert into PROPERTY (ID,OBJECT_ID,OBJECT_TYPE_PROPERTY_ID,VALUE,NUMERIC_VALUE,NORMALIZED_VALUE) values (10004,10000,10032,'admin',0,'admin')");
        cStmt.execute(
                "Insert into PROPERTY (ID,OBJECT_ID,OBJECT_TYPE_PROPERTY_ID,VALUE,NUMERIC_VALUE,NORMALIZED_VALUE) values (10005,10000,10033,'2018-07-06T06:58:33.248Z',0,null)");
        cStmt.execute(
                "Insert into PROPERTY (ID,OBJECT_ID,OBJECT_TYPE_PROPERTY_ID,VALUE,NUMERIC_VALUE,NORMALIZED_VALUE) values (10006,10000,10034,'admin',0,'admin')");
        cStmt.execute(
                "Insert into PROPERTY (ID,OBJECT_ID,OBJECT_TYPE_PROPERTY_ID,VALUE,NUMERIC_VALUE,NORMALIZED_VALUE) values (10007,10000,10035,'2018-07-06T06:58:33.248Z',0,null)");
        cStmt.execute(
                "Insert into PROPERTY (ID,OBJECT_ID,OBJECT_TYPE_PROPERTY_ID,VALUE,NUMERIC_VALUE,NORMALIZED_VALUE) values (10008,10000,10036,'369819149707fa0b46a6c161bd77a2bc62dfa8dc',0,'369819149707fa0b46a6c161bd77a2bc62dfa8dc')");
        cStmt.execute(
                "Insert into PROPERTY (ID,OBJECT_ID,OBJECT_TYPE_PROPERTY_ID,VALUE,NUMERIC_VALUE,NORMALIZED_VALUE) values (10009,10000,10038,'/',0,'/')");

        cStmt.close();

        if (logger.isInfoEnabled())
            logger.info("Created base data");
    }
}
