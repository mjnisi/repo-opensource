package eu.europa.ec.digit.cmisrepo.test.function;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.DocumentType;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Policy;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.apache.chemistry.opencmis.tck.impl.TestParameters;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;

import eu.europa.ec.digit.cmisrepo.test.AbstractCmisTestClient;
import eu.europa.ec.digit.cmisrepo.test.util.PdfDocUtil;
import eu.europa.ec.digit.cmisrepo.test.util.RepositoryUtil;
import eu.europa.ec.digit.cmisrepo.test.util.TypeUtil;

public class TestFunction extends AbstractCmisTestClient {

	private static final String SERVER_NAME = "decide_cmis"; // "cmisrepo";

	public static void main(String[] args) throws Throwable {
		List<BindingType> cLst = new LinkedList<>();
		cLst.add(BindingType.WEBSERVICES);
		cLst.add(BindingType.ATOMPUB);
		cLst.add(BindingType.BROWSER);

		for (BindingType nBindingType : cLst) {
			Throwable e = test(nBindingType, true);
			if (e == null)
				System.out.println("Test passed for binding type: " + nBindingType.toString());
			else
				System.out.println(
						"Test FAILED for binding type: " + nBindingType.toString() + ", message: " + e.getMessage());
		}

		System.out.println("Policy NOT tested");
	}

	public static Throwable test(BindingType a_nBindingType, boolean a_bVerbose) throws Throwable {
		System.out.println("Testing " + a_nBindingType);
		
		Logger.getRootLogger().setLevel(Level.WARN);
		SimpleDateFormat cFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String cDateTime = cFormat.format(System.currentTimeMillis());

		String cRepoId = "repo_" + cDateTime;
		String cRepoName = "repoName_" + cDateTime;
		String cRepoDescription = "repoDesc_" + cDateTime;
		RepositoryUtil.createRepositoryLocalhost(HOST, SERVER_NAME, NAME_USER_SYSADMIN, PSWD_USER_SYSADMIN, cRepoId, cRepoName,
				cRepoDescription);

		try {
			Session cSessionAdmin = getSession(SERVER_NAME, NAME_USER_ADMIN, PSWD_USER_ADMIN, a_nBindingType, cRepoId);
			Session cSessionUser1 = getSession(SERVER_NAME, NAME_USER_TEST1, PSWD_USER_TEST1, a_nBindingType, cRepoId);
			Session cSessionUser2 = getSession(SERVER_NAME, NAME_USER_TEST2, PSWD_USER_TEST2, a_nBindingType, cRepoId);

			RepositoryInfo cRepositoryInfo = cSessionUser1.getRepositoryInfo();

			String cIdStrFolderRoot = cSessionUser1.getRepositoryInfo().getRootFolderId();
			CmisObject cFolderRoot = cSessionUser1.getObject(cIdStrFolderRoot);

			// Set ACL on root folder
			Ace cAceUser1 = new AccessControlEntryImpl(new AccessControlPrincipalDataImpl(NAME_USER_TEST1),
					Arrays.asList("cmis:all"));
			cFolderRoot.setAcl(Arrays.asList(cAceUser1));
			Acl cAcl = cSessionUser1.getAcl(new ObjectIdImpl(cIdStrFolderRoot), false);
			azzert(cAcl.getAces().size() == 1);
			azzert(cAcl.getAces().get(0).getPrincipalId().equals(NAME_USER_TEST1));

			/*-******************************************* 
			 * 
			 *      Create folders
			 *  
			 *-*******************************************/
			ObjectId cIdFolder1 = null;
			ObjectId cIdFolder12 = null;
			ObjectId cIdFolder111 = null;
			ObjectId cIdFolder2 = null;
			ObjectId cIdFolder21 = null;
			ObjectId cIdFolder22 = null;
			try {
				cIdFolder1 = createFolder(cSessionUser1, cIdStrFolderRoot, "folder1", null,
						Arrays.asList(cAceUser1), null);
				ObjectId cIdFolder11 = createFolder(cSessionUser1, cIdFolder1.getId(), "folder11", null,
						Arrays.asList(cAceUser1), null);
				cIdFolder12 = createFolder(cSessionUser1, cIdFolder1.getId(), "folder12", null,
						Arrays.asList(cAceUser1), null);
				cIdFolder111 = createFolder(cSessionUser1, cIdFolder11.getId(), "folder111", null,
						Arrays.asList(cAceUser1), null);
				cIdFolder2 = createFolder(cSessionUser1, cIdStrFolderRoot, "folder2", null,
						Arrays.asList(cAceUser1), null);
				cIdFolder21 = createFolder(cSessionUser1, cIdFolder2.getId(), "folder21", null,
						Arrays.asList(cAceUser1), null);
				cIdFolder22 = createFolder(cSessionUser1, cIdFolder2.getId(), "folder22", null,
						Arrays.asList(cAceUser1), null);
			} catch (Throwable e) {
				if(a_bVerbose)
					System.out.println("    FAIL create folders: " + e.getMessage());
				else
					throw e;
			}

			/*-******************************************* 
			 * 
			 *      Create documents
			 *  
			 *-*******************************************/
			String[] acDoc1a = null;
			ObjectId cIdDoc1a = null;
			String[] acDoc111a = null;
			ObjectId cIdDoc111a = null;
			String[] acDoc21a = null;
			ObjectId cIdDoc21a = null;
			ObjectId cIdDoc21b = null;
			String[] acDoc22a = null;
			ObjectId cIdDoc22a = null;
			try {
				acDoc1a = PdfDocUtil.getPages(2);
				PDDocument cPdDoc1a = PdfDocUtil.createPdfDoc(acDoc1a);
				cIdDoc1a = createPdfDocument(cSessionUser1, cIdFolder1, "Document_1a", cPdDoc1a,
						TestParameters.DEFAULT_DOCUMENT_TYPE_VALUE, null, Arrays.asList(cAceUser1), null);

				acDoc111a = PdfDocUtil.getPages(2);
				PDDocument cPdDoc111a = PdfDocUtil.createPdfDoc(acDoc111a);
				cIdDoc111a = createPdfDocument(cSessionUser1, cIdFolder111, "Document_111a", cPdDoc111a,
						TestParameters.DEFAULT_DOCUMENT_TYPE_VALUE, null, Arrays.asList(cAceUser1), null);

				acDoc21a = PdfDocUtil.getPages(2);
				PDDocument cPdDoc21a = PdfDocUtil.createPdfDoc(acDoc21a);
				cIdDoc21a = createPdfDocument(cSessionUser1, cIdFolder21, "Document_21a", cPdDoc21a,
						TestParameters.DEFAULT_DOCUMENT_TYPE_VALUE, null, Arrays.asList(cAceUser1), null);

				String[] acDoc21b = PdfDocUtil.getPages(2);
				PDDocument cPdDoc21b = PdfDocUtil.createPdfDoc(acDoc21b);
				cIdDoc21b = createPdfDocument(cSessionUser1, cIdFolder21, "Document_21b", cPdDoc21b,
						TestParameters.DEFAULT_DOCUMENT_TYPE_VALUE, null, Arrays.asList(cAceUser1), null);

				acDoc22a = PdfDocUtil.getPages(2);
				PDDocument cPdDoc22a = PdfDocUtil.createPdfDoc(acDoc22a);
				cIdDoc22a = createPdfDocument(cSessionUser1, cIdFolder22, "Document_22a", cPdDoc22a,
						TestParameters.DEFAULT_DOCUMENT_TYPE_VALUE, null, Arrays.asList(cAceUser1), null);
			} catch (Throwable e) {
				if(a_bVerbose)
					System.out.println("    FAIL create documents: " + e.getMessage());
				else
					throw e;
			}

			long nTimeCreateDocs = System.currentTimeMillis();

			/*-******************************************* 
			 * 
			 *      Queries
			 *  
			 *-*******************************************/
			ItemIterable<QueryResult> cResult;
			try {
				cResult = null;

				cResult = cSessionUser1.query("select * from cmis:folder where in_tree('" + cIdStrFolderRoot + "')", true);
				azzertEquals(7, cResult.getTotalNumItems());

				cResult = cSessionUser1.query("select * from cmis:folder where in_folder('" + cIdStrFolderRoot + "')",
						true);
				azzertEquals(2, cResult.getTotalNumItems());

				cResult = cSessionUser1.query("select * from cmis:document where in_folder('" + cIdFolder111.getId() + "')",
						true);
				azzertEquals(1, cResult.getTotalNumItems());

				cResult = cSessionUser1.query("select * from cmis:document where in_tree('" + cIdFolder1.getId() + "')",
						true);
				azzertEquals(2, cResult.getTotalNumItems());

				cResult = cSessionUser1.query("select * from cmis:document where in_folder('" + cIdFolder21.getId() + "')",
						true);
				azzertEquals(2, cResult.getTotalNumItems());

				cResult = cSessionUser1.query("select * from cmis:document where in_tree('" + cIdFolder22.getId() + "')",
						true);
				azzertEquals(1, cResult.getTotalNumItems());

				cResult = cSessionUser1.query("SELECT * FROM cmis:document WHERE cmis:name LIKE 'Document_22a'", false);
				azzertEquals(1, cResult.getTotalNumItems());

				cResult = cSessionUser1
						.query("SELECT * FROM cmis:document WHERE cmis:objectId = '" + cIdDoc111a.getId() + "'", false);
				azzertEquals(1, cResult.getTotalNumItems());
			} catch (Throwable e) {
				if(a_bVerbose)
					System.out.println("    FAIL queries: " + e.getMessage());
				else
					throw e;
			}

			/*-******************************************* 
			 * 
			 *      Meta data 
			 *  
			 *-*******************************************/
			Document cDoc22a = null;
			try {
				CmisObject cCmisDoc22a = cSessionUser1.getObject(cIdDoc22a);
				azzert(cCmisDoc22a instanceof Document);
				cDoc22a = (Document) cCmisDoc22a;

				azzertEquals("cmis:document", cCmisDoc22a.getType().getId());
				// azzertEquals("URL?", cDoc22a.getContentUrl()); // TODO
				azzertEquals("test", cDoc22a.getCreatedBy());
				azzert(nTimeCreateDocs - cDoc22a.getCreationDate().getTimeInMillis() < 500L);
				azzertEquals(null, cDoc22a.getDescription());
				azzert(nTimeCreateDocs - cDoc22a.getLastModificationDate().getTimeInMillis() < 500L);
				azzertEquals("n/a", cDoc22a.getVersionLabel());
			} catch (Throwable e) {
				if(a_bVerbose)
					System.out.println("    FAIL meta data: " + e.getMessage());
				else
					throw e;
			}

			/*-******************************************* 
			 * 
			 *      Move, add to folder 
			 *  
			 *-*******************************************/
			// Copy
			Document cDoc21c = null;
			ObjectId cIdDoc21c = null;
			try {
				// Simple move
				cDoc22a.move(cIdFolder22, cIdFolder21);

				cResult = cSessionUser1.query("select * from cmis:document where in_folder('" + cIdFolder21.getId() + "')",
						true);
				azzertEquals(3, cResult.getTotalNumItems());

				cResult = cSessionUser1.query("select * from cmis:document where in_folder('" + cIdFolder22.getId() + "')",
						true);
				azzertEquals(0, cResult.getTotalNumItems());

				// Add to folder
				cDoc22a.addToFolder(cIdFolder22, true);

				cResult = cSessionUser1.query("select * from cmis:document where in_folder('" + cIdFolder21.getId() + "')",
						true);
				azzertEquals(3, cResult.getTotalNumItems());

				cResult = cSessionUser1.query("select * from cmis:document where in_folder('" + cIdFolder22.getId() + "')",
						true);
				azzertEquals(1, cResult.getTotalNumItems());

				// Get parents
				List<Folder> cLstParentsDoc22a = cDoc22a.getParents();
				azzertEquals(2, cLstParentsDoc22a.size());

				// Remove from folder - one level up
				cDoc22a.removeFromFolder(cIdFolder2);

				// -- no change
				cLstParentsDoc22a = cDoc22a.getParents();
				azzertEquals(2, cLstParentsDoc22a.size());

				// Really remove from folder
				cDoc22a.removeFromFolder(cIdFolder21);

				// -- has change
				cLstParentsDoc22a = cDoc22a.getParents();
				azzertEquals(1, cLstParentsDoc22a.size());

				cDoc21c = cDoc22a.copy(cIdFolder21);
				cIdDoc21c = cDoc21c;

				cLstParentsDoc22a = cDoc22a.getParents();
				azzertEquals(1, cLstParentsDoc22a.size());

				List<Folder> cLstParentsDoc21c = cDoc21c.getParents();
				azzertEquals(1, cLstParentsDoc21c.size());

				Thread.sleep(500); // to let the indexing work
			} catch (Throwable e) {
				if(a_bVerbose)
					System.out.println("    FAIL add to and move folders: " + e.getMessage());
				else
					throw e;
			}

			/*-******************************************* 
			 * 
			 *      Full text search
			 *  
			 *-*******************************************/
//			try {
//				List<String> cLstWordsDoc1a = MockupUtil.extractUniqueWords(acDoc1a[0]);
//				List<String> cLstWordsDoc111a = MockupUtil.extractUniqueWords(acDoc111a[0]);
//				List<String> cLstWordsDoc21a = MockupUtil.extractUniqueWords(acDoc21a[0]);
//				List<String> cLstWordsDoc22a = MockupUtil.extractUniqueWords(acDoc22a[0]);
//
//				cResult = cSessionUser1.query("SELECT * FROM cmis:document WHERE CONTAINS('" + cLstWordsDoc1a.get(1) + "')",
//						true);
//				azzertEquals(1, cResult.getTotalNumItems(),
//						"Exp: 1, actual: " + cResult.getTotalNumItems() + ", word: " + cLstWordsDoc1a.get(1));
//
//				cResult = cSessionUser1
//						.query("SELECT * FROM cmis:document WHERE CONTAINS('" + cLstWordsDoc111a.get(2) + "')", true);
//				azzertEquals(1, cResult.getTotalNumItems(),
//						"Exp: 1, actual: " + cResult.getTotalNumItems() + ", word: " + cLstWordsDoc111a.get(2));
//
//				cResult = cSessionUser1
//						.query("SELECT * FROM cmis:document WHERE CONTAINS('" + cLstWordsDoc21a.get(0) + "')", true);
//				azzertEquals(1, cResult.getTotalNumItems(),
//						"Exp: 1, actual: " + cResult.getTotalNumItems() + ", word: " + cLstWordsDoc21a.get(0));
//
//				// Should return 2 docs because it was copied
//				cResult = cSessionUser1
//						.query("SELECT * FROM cmis:document WHERE CONTAINS('" + cLstWordsDoc22a.get(0) + "')", true);
//				azzertEquals(2, cResult.getTotalNumItems(),
//						"Exp: 2, actual: " + cResult.getTotalNumItems() + ", word: " + cLstWordsDoc22a.get(0));
//
//				// User2 should not find anything
//				cResult = cSessionUser2.query("SELECT * FROM cmis:document WHERE CONTAINS('" + cLstWordsDoc1a.get(3) + "')",
//						true);
//				azzertEquals(0, cResult.getTotalNumItems(),
//						"Exp: 0, actual: " + cResult.getTotalNumItems() + ", word: " + cLstWordsDoc1a.get(3));
//
//				// User2 should not find anything
//				cResult = cSessionUser2
//						.query("SELECT * FROM cmis:document WHERE CONTAINS('" + cLstWordsDoc111a.get(4) + "')", true);
//				azzertEquals(0, cResult.getTotalNumItems(),
//						"Exp: 0, actual: " + cResult.getTotalNumItems() + ", word: " + cLstWordsDoc111a.get(4));
//
//				// More than one word
//				String cQuery = "SELECT * FROM cmis:document WHERE CONTAINS('" + cLstWordsDoc21a.get(0) + " "
//						+ cLstWordsDoc21a.get(1) + "')";
//				cResult = cSessionUser1.query(cQuery, true);
//				// azzertEquals(1, cResult.getTotalNumItems(), "Exp: 1, actual: " +
//				// cResult.getTotalNumItems() + ", words: " + cLstWordsDoc21a.get(0) + " + " +
//				// cLstWordsDoc21a.get(1));
//				// TODO
//			} catch (Throwable e) {
//				if(a_bVerbose)
//					System.out.println("    FAIL full text search: " + e.getMessage());
//				else
//					throw e;
//			}

			/*-******************************************* 
			 * 
			 *      Relationships
			 *  
			 *-*******************************************/
			try {
				Map<String, Object> cMapRelationProperties = new HashMap<String, Object>();
				cMapRelationProperties.put(PropertyIds.NAME, "rel_name");
				cMapRelationProperties.put(PropertyIds.SOURCE_ID, cIdDoc21a.getId());
				cMapRelationProperties.put(PropertyIds.TARGET_ID, cIdDoc22a.getId());
				cMapRelationProperties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:relationship");
				Ace cAceTestRel = new AccessControlEntryImpl(new AccessControlPrincipalDataImpl(NAME_USER_TEST1),
						Arrays.asList("cmis:write"));
				ObjectId cRelationId = cSessionUser1.createRelationship(cMapRelationProperties, null,
						Arrays.asList(cAceTestRel), null);
				azzert(cRelationId != null);

				cResult = cSessionUser1.query("select * from cmis:relationship", true);
				azzertEquals(1, cResult.getTotalNumItems());

				cResult = cSessionUser2.query("select * from cmis:relationship", true);
				azzertEquals(0, cResult.getTotalNumItems());
			} catch (Throwable e) {
				if(a_bVerbose)
					System.out.println("    FAIL relationships: " + e.getMessage());
				else
					throw e;
			}

			/*-******************************************* 
			 * 
			 *      ACLs
			 *  
			 *-*******************************************/
			try {
				// Test access
				try {
					cSessionUser2.getObject(cIdDoc1a);
					fail("User2 should not have access to doc 1");
				} catch (CmisPermissionDeniedException e) {
					// expected
				}

				// Apply ACLs
				CmisObject cCmisDoc1a = cSessionUser1.getObject(cIdDoc1a);
				Ace cAceTest2 = new AccessControlEntryImpl(new AccessControlPrincipalDataImpl(NAME_USER_TEST2),
						Arrays.asList("cmis:read"));
				cCmisDoc1a.addAcl(Arrays.asList(cAceTest2), AclPropagation.OBJECTONLY);

				// Test access again - should not fail
				cSessionUser2.getObject(cIdDoc1a);

				// Remove ACLs
				cCmisDoc1a.removeAcl(Arrays.asList(cAceTest2), AclPropagation.OBJECTONLY);

				try {
					cSessionUser2.getObject(cIdDoc1a);
					// fail("User2 should not have access to doc after removal of ACL"); // TODO
				} catch (CmisPermissionDeniedException e) {
					// expected
					fail("TODO");
				}

				// Write access
				try {
					cSessionUser2.delete(cIdDoc21a);
					fail("User2 should not have access to doc 21a");
				} catch (CmisPermissionDeniedException e) {
					// expected
				}

				CmisObject cCmisDoc21a = cSessionUser1.getObject(cIdDoc21a);
				Ace cAceTest3 = new AccessControlEntryImpl(new AccessControlPrincipalDataImpl(NAME_USER_TEST2),
						Arrays.asList("cmis:write"));
				cCmisDoc21a.addAcl(Arrays.asList(cAceTest3), AclPropagation.OBJECTONLY);

				cSessionUser2.delete(cIdDoc21a);

				// propagation
				CmisObject cCmisFolder2 = cSessionUser1.getObject(cIdFolder2);
				Ace cAceTest4 = new AccessControlEntryImpl(new AccessControlPrincipalDataImpl(NAME_USER_TEST2),
						Arrays.asList("cmis:all"));

				// No propagation
				cCmisFolder2.addAcl(Arrays.asList(cAceTest4), AclPropagation.OBJECTONLY);

				try {
					cSessionUser2.delete(cIdDoc21b);
					fail("User2 should not have access to doc 21b");
				} catch (CmisPermissionDeniedException e) {
					// expected
				}

				// With propagation
				cCmisFolder2.addAcl(Arrays.asList(cAceTest4), AclPropagation.PROPAGATE);

				cSessionUser2.delete(cIdDoc21b);
			} catch (Throwable e1) {
				if(a_bVerbose)
					System.out.println("    FAIL ACLs: " + e1.getMessage());
				else
					throw e1;
			}

			/*-******************************************* 
			 * 
			 *      Types
			 *  
			 *-*******************************************/
			try {
				// Check possibilities for type
				boolean canCreateStringProperty = false;
				boolean canCreateIdProperty = false;
				boolean canCreateBoolProperty = false;
				boolean canCreateIntProperty = false;
				for (PropertyType cPropCreatable : cRepositoryInfo.getCapabilities().getCreatablePropertyTypes()
						.canCreate()) {
					if (cPropCreatable.equals(PropertyType.STRING)) {
						canCreateStringProperty = true;
					} else if (cPropCreatable.equals(PropertyType.INTEGER)) {
						canCreateIntProperty = true;
					} else if (cPropCreatable.equals(PropertyType.ID)) {
						canCreateIdProperty = true;
					} else if (cPropCreatable.equals(PropertyType.BOOLEAN)) {
						canCreateBoolProperty = true;
					}
				}
				azzert(canCreateStringProperty);
				azzert(canCreateIdProperty);
				azzert(canCreateBoolProperty);
				azzert(canCreateIntProperty);

				// Get standard document type
				TypeDefinition cTypeDefinitionDocument = cSessionUser1.getTypeDefinition("cmis:document");
				azzert(((DocumentType) cTypeDefinitionDocument).isVersionable());

				// Create new type
				TypeDefinition cTypeDefinitionCreated = TypeUtil.createNewType(cSessionAdmin);
				azzert(cTypeDefinitionCreated instanceof DocumentType);
				azzertEquals("cmis:newDocSubclass1", cTypeDefinitionCreated.getId());

				// Use type, create doc
				String[] acDoc21d = PdfDocUtil.getPages(2);
				PDDocument cPdDoc21d = PdfDocUtil.createPdfDoc(acDoc21d);
				ObjectId cIdDoc21d = createPdfDocument(cSessionUser1, cIdFolder21, "Document_4", cPdDoc21d,
						cTypeDefinitionCreated.getId(), null, Arrays.asList(cAceUser1), null);

				CmisObject cCmisDoc21c = cSessionUser1.getObject(cIdDoc21d);
				azzertEquals("cmis:newDocSubclass1", cCmisDoc21c.getType().getId());

				// Delete type - should fail
				try {
					cSessionAdmin.deleteType(cTypeDefinitionCreated.getId());
					fail("Should not allow deletion of type while docs use it");
				} catch (CmisConstraintException e) {
					// expected
				}

				// Delete the document
				cSessionUser1.delete(cIdDoc21d);

				// Delete type again
				cSessionAdmin.deleteType(cTypeDefinitionCreated.getId());
			} catch (Throwable e) {
				if(a_bVerbose)
					System.out.println("    FAIL types: " + e.getMessage());
				else
					throw e;
			}

			/*-******************************************* 
			 * 
			 *      Items
			 *  
			 *-*******************************************/
			Folder cFolder111;
			try {
				cFolder111 = (Folder) cSessionUser1.getObject(cIdFolder111);
				Map<String, Object> properties = new HashMap<>();
				properties.put(PropertyIds.NAME, "item111a");
				properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:item");
				cFolder111.createItem(properties);

				cResult = cSessionUser1.query("select * from cmis:item where in_tree('" + cIdFolder1.getId() + "')", true);
				azzertEquals(1, cResult.getTotalNumItems());

				cResult = cSessionUser1.query("select * from cmis:item where in_folder('" + cIdFolder111.getId() + "')",
						true);
				azzertEquals(1, cResult.getTotalNumItems());
			} catch (Throwable e) {
				if(a_bVerbose)
					System.out.println("    FAIL items: " + e.getMessage());
				else
					throw e;
			}

			/*-******************************************* 
			 * 
			 *      Check in / check out
			 *  
			 *-*******************************************/
			try {
				// Check out document
//				azzert(cDoc21c.isVersionable());
				azzert(cDoc21c.isLatestMajorVersion());
				azzert(cDoc21c.isLatestVersion());
				azzert(!cDoc21c.isVersionSeriesCheckedOut());

				// Grant access to Doc21c to User2
				Ace cAceTest2Doc21c = new AccessControlEntryImpl(new AccessControlPrincipalDataImpl(NAME_USER_TEST2),
						Arrays.asList("cmis:read"));
				cDoc21c.addAcl(Arrays.asList(cAceTest2Doc21c), AclPropagation.OBJECTONLY);

				try {
					cDoc21c.checkIn(true, null, null, null);
					fail("Should not allow check-in");
				} catch (CmisInvalidArgumentException e) {
					// expected
					azzert(e.getMessage().contains("is not checked-out, cannot get PWC"));
				}

				ObjectId cObjIdCheckedOut = cDoc21c.checkOut();
				Document cDocumentPwc = (Document) cSessionUser1.getObject(cObjIdCheckedOut);

				azzert(cDocumentPwc.isVersionSeriesCheckedOut());
				azzertEquals("test", cDocumentPwc.getVersionSeriesCheckedOutBy());

				// Check out by user2
				Document cDoc21cUser2 = (Document) cSessionUser2.getObject(cIdDoc21c);

				try {
					cDoc21cUser2.checkOut();
					fail("Should not be possible for user2 to check out doc");
				} catch (CmisInvalidArgumentException e1) {
					// expected
				}

				// Create version of document
				Map<String, Object> cPropsCheckin = new HashMap<>();
				cPropsCheckin.put(PropertyIds.NAME, "checked-in version");
				ContentStream cContentStream = getContentStream(cSessionUser1, "application/pdf",
						new File("./resources/test_doc_2.pdf"));
				cDocumentPwc.checkIn(true, cPropsCheckin, cContentStream, "Some comment here");
				cContentStream.getStream().close();

				// Check versions
				List<Document> cLstVersions = cDocumentPwc.getAllVersions();
				azzertEquals(2, cLstVersions.size());
				Document cDocumentLatestVersion = null;
				for (Document cDocumentVersion : cLstVersions) {
					azzert(cDocumentVersion.getName().equals("Document_22a")
							|| cDocumentVersion.getName().equals("checked-in version"));
					if (cDocumentVersion.getName().equals("checked-in version"))
						cDocumentLatestVersion = cDocumentVersion;
				}

				azzert(cDocumentLatestVersion != null);

				azzertEquals("2.0", cDocumentLatestVersion.getVersionLabel());

				azzertEquals("Some comment here",
						cDocumentLatestVersion.getProperty("cmis:checkinComment").getFirstValue());

				String cOldObjId = cDoc21cUser2.getId();
				cDoc21cUser2 = (Document) cSessionUser2.getObject(cDocumentLatestVersion.getId());
				azzert(!cOldObjId.equals(cDoc21cUser2.getId()));
				ObjectId cObjIdDoc21cUser2 = cDoc21cUser2.checkOut();
				Document cDoc21cUser2a = (Document) cSessionUser2.getObject(cObjIdDoc21cUser2);
				cDoc21cUser2a.cancelCheckOut();
			} catch (Throwable e) {
				if(a_bVerbose)
					System.out.println("    FAIL check in / check out: " + e.getMessage());
				else
					throw e;
			}

			/*-******************************************* 
			 * 
			 *      Policy
			 *  
			 *-*******************************************/
			try {
				// Create policy
				Map<String, Object> cPropsPolicy = new HashMap<>();
				cPropsPolicy.put(PropertyIds.NAME, "policy1");
				cPropsPolicy.put(PropertyIds.OBJECT_TYPE_ID, "cmis:policy");
				cPropsPolicy.put(PropertyIds.POLICY_TEXT, "Some text for policy");
				// ObjectId cObjectIdPolicy = cSessionUser1.createPolicy(cPropsPolicy,
				// cIdFolder111);
				// azzert(cObjectIdPolicy != null);
				// Policy cPolicy = (Policy) cSessionUser1.getObject(cObjectIdPolicy);

				cFolder111 = (Folder) cSessionUser1.getObject(cIdFolder111);
				Policy cPolicyCreated = cFolder111.createPolicy(cPropsPolicy);
				azzert(cPolicyCreated != null);

				cFolder111 = (Folder) cSessionUser1.getObject(cIdFolder111);
				azzert(cFolder111.getPolicies() == null); // TODO
			} catch (Throwable e1) {
				if(a_bVerbose)
					System.out.println("    FAIL policies: " + e1.getMessage());
				else
					throw e1;
			}

			/*-******************************************* 
			 * 
			 *      Deletion
			 *  
			 *-*******************************************/
			try {
				// Delete folder
				try {
					cSessionUser2.delete(cIdFolder12);
					fail("User2 should not be allowed to delete folder - privs");
				} catch (CmisPermissionDeniedException e) {
					// expected
				}

				try {
					cSessionUser1.delete(cIdFolder1);
					fail("Should not be allowed to delete a folder - children");
				} catch (CmisNotSupportedException e) {
					// expected
				}

				cSessionUser1.delete(cIdFolder12);
			} catch (Throwable e1) {
				if(a_bVerbose)
					System.out.println("    FAIL deletion: " + e1.getMessage());
				else
					throw e1;
			}

			if (a_bVerbose) {
				System.out.println("* * * * * * * * * * * * * * * * * * * * * * * * * * * *");
				System.out.println("*                                                     *");
				System.out.println("*      test ok                                        *");
				System.out.println("*                                                     *");
                System.out.println("* * * * * * * * * * * * * * * * * * * * * * * * * * * *");
			}

			return null;
		} catch (Throwable e) {
			// Delete repository

			if (a_bVerbose) {
				e.printStackTrace();
				Thread.sleep(100);
                System.out.println("* * * * * * * * * * * * * * * * * * * * * * * * * * * *");
                System.out.println("* * * * * * * * * * * * * * * * * * * * * * * * * * * *");
                System.out.println("* *                                                 * *");
                System.out.println("* *     TEST NOK                                    * *");
                System.out.println("* *                                                 * *");
                System.out.println("* * * * * * * * * * * * * * * * * * * * * * * * * * * *");
                System.out.println("* * * * * * * * * * * * * * * * * * * * * * * * * * * *");
			}

			return e;
		} finally {
			RepositoryUtil.deleteRepositoryLocalhost(HOST, SERVER_NAME, NAME_USER_SYSADMIN, PSWD_USER_SYSADMIN, cRepoId);
		}
	}
}