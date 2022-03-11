package eu.trade.repo.service;

import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.model.ObjectType;
import eu.trade.repo.model.ObjectTypeProperty;
import eu.trade.repo.model.Repository;
import eu.trade.repo.service.interfaces.IRepositoryService;
import eu.trade.repo.util.Constants;

public class RepositoryTradeTest extends BaseTestClass {

	@Autowired @Qualifier("repositoryService")
    private IRepositoryService repositoryService;
	
	
	@Test
	@Ignore
	public void testAddCustomRepositoryTypes() throws Exception {
		addTradeRepositoryTypes("dg_trade_test");
		addNestRepositoryTypes("nest_dev");
		addNestRepositoryTypes("nest_tst");
		addSherlockRepositoryTypes("tron_dev");
		addSherlockRepositoryTypes("tron_tst");
	}
	
	
	private void addTradeRepositoryTypes(String repoName) throws Exception {

		Repository repo = repositoryService.getRepositoryById(repoName);
		ObjectType cmisDoc = repositoryService.getObjectType(repoName, BaseTypeId.CMIS_DOCUMENT.value());
		ObjectType cmisFolder = repositoryService.getObjectType(repoName, BaseTypeId.CMIS_FOLDER.value());
		
		ObjectType tradeDoc = createTradeDocumentType(repo, cmisDoc);
		ObjectType tradeAttachment = createAttachmentDocumentType(repo, tradeDoc);

		ObjectType tradeFolder = createTradeFolderType(repo, cmisFolder);
		ObjectType tradeEmail = createEmailFolderType(repo, cmisFolder);
		
		utilService.persist(tradeDoc);
		utilService.persist(tradeAttachment);
		utilService.persist(tradeFolder);
		utilService.persist(tradeEmail);
		
	}
	
	private void addSherlockRepositoryTypes(String repoName) throws Exception {

		Repository repo = repositoryService.getRepositoryById(repoName);
		ObjectType cmisDoc = repositoryService.getObjectType(repoName, BaseTypeId.CMIS_DOCUMENT.value());
		ObjectType cmisFolder = repositoryService.getObjectType(repoName, BaseTypeId.CMIS_FOLDER.value());
		
		//ObjectType sherlockDoc = createSherlockDocumentType(repo, cmisDoc);
		ObjectType sherlockAttachment = createSherlockPublishedAttachmentType(repo, cmisDoc);
		ObjectType sherlockFolder = createSherlockFolderType(repo, cmisFolder);

		//utilService.persist(sherlockDoc);
		utilService.persist(sherlockAttachment);
		utilService.persist(sherlockFolder);
	}
	
	private void addNestRepositoryTypes(String repoName) throws Exception {

		Repository repo = repositoryService.getRepositoryById(repoName);
		ObjectType cmisDoc = repositoryService.getObjectType(repoName, BaseTypeId.CMIS_DOCUMENT.value());
		
		ObjectType nestDoc = createNestDocumentType(repo, cmisDoc);

		utilService.persist(nestDoc);
	}
	
	
	private ObjectType createTradeDocumentType(Repository repository, ObjectType doc0) {
		ObjectType doc = new ObjectType("trade:document");
		doc.setLocalName(doc.getCmisId());
		doc.setLocalNamespace(Constants.NS);
		doc.setQueryName(doc.getCmisId());
		doc.setDisplayName(doc.getCmisId());
		doc.setBase(doc0);
		doc.setParent(doc0);
		doc.setDescription("Trade Document");
		doc.setCreatable(true);
		doc.setFileable(true);
		doc.setQueryable(true);
		doc.setControllablePolicy(true);
		doc.setControllableAcl(true);
		doc.setFulltextIndexed(true);
		doc.setIncludedInSupertypeQuery(true);
		doc.setVersionable(true);
		doc.setContentStreamAllowed(ContentStreamAllowed.REQUIRED);
        doc.setRepository(repository);
		doc.addObjectTypeProperty(buildObjectTypeProperty("trade:author", 			PropertyType.STRING,	Cardinality.MULTI, Updatability.READWRITE, false, "Author"));
		return doc;
	}
	
	
	
	private ObjectType createAttachmentDocumentType(Repository repository, ObjectType parent) {
		ObjectType doc = new ObjectType("trade:attachment");
		doc.setLocalName(doc.getCmisId());
		doc.setLocalNamespace(Constants.NS);
		doc.setQueryName(doc.getCmisId());
		doc.setDisplayName(doc.getCmisId());
		doc.setBase(parent.getBase());
		doc.setParent(parent);
		doc.setDescription("Trade Document for email attackments");
		doc.setCreatable(true);
		doc.setFileable(true);
		doc.setQueryable(true);
		doc.setControllablePolicy(true);
		doc.setControllableAcl(true);
		doc.setFulltextIndexed(true);
		doc.setIncludedInSupertypeQuery(true);
		doc.setVersionable(true);
		doc.setContentStreamAllowed(ContentStreamAllowed.REQUIRED);
        doc.setRepository(repository);
		doc.addObjectTypeProperty(buildObjectTypeProperty("trade:emailId", 	PropertyType.ID,		Cardinality.MULTI, Updatability.READWRITE, true, "Email ID"));
		return doc;
	}
	
	
	private ObjectType createTradeFolderType(Repository repository, ObjectType doc0) {
		ObjectType folder = new ObjectType("trade:folder");
		
		folder.setLocalName(folder.getCmisId());
		folder.setLocalNamespace(Constants.NS);
		folder.setQueryName(folder.getCmisId());
		folder.setDisplayName(folder.getCmisId());
		folder.setBase(doc0);
		folder.setParent(doc0);
		folder.setDescription(folder.getCmisId());
		folder.setCreatable(true);
		folder.setFileable(true);
		folder.setQueryable(true);
		folder.setControllablePolicy(false);
		folder.setControllableAcl(true);
		folder.setFulltextIndexed(true);
		folder.setIncludedInSupertypeQuery(true);
		folder.setRepository(repository);

		folder.addObjectTypeProperty(buildObjectTypeProperty("trade:headingCode", 	PropertyType.ID,		Cardinality.SINGLE, Updatability.READWRITE, true, "Heading code"));
		folder.addObjectTypeProperty(buildObjectTypeProperty("trade:title",		 	PropertyType.STRING,	Cardinality.SINGLE, Updatability.READWRITE, true, "Title"));
		ObjectTypeProperty status = buildObjectTypeProperty("trade:status",			PropertyType.STRING,	Cardinality.SINGLE, Updatability.READWRITE, true, "Status");
		status.setOpenChoice(false);
		status.setChoices("ACTIVE" + Constants.CMIS_MULTIVALUE_SEP + "INACTIVE");
		folder.addObjectTypeProperty(status);
		folder.addObjectTypeProperty(buildObjectTypeProperty("trade:serviceOwner", 	PropertyType.STRING,	Cardinality.MULTI, Updatability.READWRITE, true, "Service owner"));

		return folder;
	}
	
	private ObjectType createSherlockFolderType(Repository repository, ObjectType doc0) {
		ObjectType folder = new ObjectType("slk:caseFolder");
		
		folder.setLocalName(folder.getCmisId());
		folder.setLocalNamespace(Constants.NS);
		folder.setQueryName(folder.getCmisId());
		folder.setDisplayName(folder.getCmisId());
		folder.setBase(doc0);
		folder.setParent(doc0);
		folder.setDescription(folder.getCmisId());
		folder.setCreatable(true);
		folder.setFileable(true);
		folder.setQueryable(true);
		folder.setControllablePolicy(false);
		folder.setControllableAcl(true);
		folder.setFulltextIndexed(true);
		folder.setIncludedInSupertypeQuery(true);
		folder.setRepository(repository);

		folder.addObjectTypeProperty(buildObjectTypeProperty("slk:nbOfDocuments", 	PropertyType.INTEGER,	Cardinality.SINGLE, Updatability.READWRITE, false, "Number of documents"));
		folder.addObjectTypeProperty(buildObjectTypeProperty("slk:caseName", 	PropertyType.STRING,	Cardinality.SINGLE, Updatability.ONCREATE, false, "Case Name"));

		return folder;
	}

	
	private ObjectType createEmailFolderType(Repository repository, ObjectType parent) {
		ObjectType folder = new ObjectType("trade:email");
		
		folder.setLocalName(folder.getCmisId());
		folder.setLocalNamespace(Constants.NS);
		folder.setQueryName(folder.getCmisId());
		folder.setDisplayName(folder.getCmisId());
		folder.setBase(parent.getBase());
		folder.setParent(parent);
		folder.setDescription(folder.getCmisId());
		folder.setCreatable(true);
		folder.setFileable(true);
		folder.setQueryable(true);
		folder.setControllablePolicy(false);
		folder.setControllableAcl(true);
		folder.setFulltextIndexed(true);
		folder.setIncludedInSupertypeQuery(true);
		folder.setRepository(repository);

		folder.addObjectTypeProperty(buildObjectTypeProperty("subject", 	PropertyType.STRING,	Cardinality.SINGLE, Updatability.READWRITE, true, "Subject"));
		folder.addObjectTypeProperty(buildObjectTypeProperty("from", 		PropertyType.STRING,	Cardinality.SINGLE, Updatability.READWRITE, true, "From"));
		folder.addObjectTypeProperty(buildObjectTypeProperty("to", 			PropertyType.STRING,	Cardinality.MULTI, Updatability.READWRITE, true, "To"));
		return folder;
	}

	private ObjectType createSherlockDocumentType(Repository repository, ObjectType doc0) {
		ObjectType doc = new ObjectType("slk:document");
		doc.setLocalName(doc.getCmisId());
		doc.setLocalNamespace(Constants.NS);
		doc.setQueryName(doc.getCmisId());
		doc.setDisplayName(doc.getCmisId());
		doc.setBase(doc0);
		doc.setParent(doc0);
		doc.setDescription("Trade Document for Sherlock");
		doc.setCreatable(true);
		doc.setFileable(true);
		doc.setQueryable(true);
		doc.setControllablePolicy(true);
		doc.setControllableAcl(true);
		doc.setFulltextIndexed(true);
		doc.setIncludedInSupertypeQuery(true);
		doc.setVersionable(true);
		doc.setContentStreamAllowed(ContentStreamAllowed.REQUIRED);
        doc.setRepository(repository);
		doc.addObjectTypeProperty(buildObjectTypeProperty("slk:docId", 		PropertyType.STRING,	Cardinality.SINGLE, Updatability.READWRITE, true, "Document's id"));
		doc.addObjectTypeProperty(buildObjectTypeProperty("slk:comments", 	PropertyType.STRING,	Cardinality.SINGLE, Updatability.READWRITE, false, "Document's comments"));
		doc.addObjectTypeProperty(buildObjectTypeProperty("slk:title",  	PropertyType.STRING,	Cardinality.SINGLE, Updatability.READWRITE, false, "Document's title"));
		doc.addObjectTypeProperty(buildObjectTypeProperty("slk:caseNumber", PropertyType.STRING, 	Cardinality.MULTI, 	Updatability.READWRITE, false, "Document's case numbers"));
		return doc;
	}
	
	/**
	 * Helper method to create object type property definitions.
	 * All properties all queriables and orderables. 
	 * 
	 * @param id
	 * @param type
	 * @param cardinality
	 * @param updatability
	 * @param required
	 * @param description
	 * @return
	 */
	private ObjectTypeProperty buildObjectTypeProperty (
			String id, PropertyType type, 
			Cardinality cardinality, Updatability updatability, boolean required, String description) {
		
		ObjectTypeProperty p = new ObjectTypeProperty(id);
		p.setLocalName(id);
		p.setLocalNamespace(Constants.NS);
		p.setQueryName(id);
		p.setDisplayName(id);
		p.setDescription(description);
		p.setPropertyType(type);
		p.setCardinality(cardinality);
		p.setUpdatability(updatability);
		p.setRequired(required);
		
		p.setQueryable(true);
		p.setOrderable(true);
		
		return p;
	}
	
	
	private ObjectType createNestDocumentType(Repository repository, ObjectType doc0) {
		ObjectType doc = new ObjectType("nest:document");
		doc.setLocalName(doc.getCmisId());
		doc.setLocalNamespace(Constants.NS);
		doc.setQueryName(doc.getCmisId());
		doc.setDisplayName(doc.getCmisId());
		doc.setBase(doc0);
		doc.setParent(doc0);
		doc.setDescription("Trade Document for Nest");
		doc.setCreatable(true);
		doc.setFileable(true);
		doc.setQueryable(true);
		doc.setControllablePolicy(true);
		doc.setControllableAcl(true);
		doc.setFulltextIndexed(true);
		doc.setIncludedInSupertypeQuery(true);
		doc.setVersionable(true);
		doc.setContentStreamAllowed(ContentStreamAllowed.REQUIRED);
        doc.setRepository(repository);
		doc.addObjectTypeProperty(buildObjectTypeProperty("nest:documentClass", 	PropertyType.STRING,	Cardinality.SINGLE, Updatability.READWRITE, false, "Document class property"));
		doc.addObjectTypeProperty(buildObjectTypeProperty("nest:documentState", 	PropertyType.STRING,	Cardinality.SINGLE, Updatability.READWRITE, false, "Document state property"));
		doc.addObjectTypeProperty(buildObjectTypeProperty("nest:documentType",  	PropertyType.STRING,	Cardinality.SINGLE, Updatability.READWRITE, false, "Document type property"));
		doc.addObjectTypeProperty(buildObjectTypeProperty("nest:description", 		PropertyType.STRING, 	Cardinality.SINGLE, Updatability.READWRITE, false, "Document's description"));
		return doc;
	}
	
	
	private ObjectType createSherlockPublishedAttachmentType(Repository repository, ObjectType doc0) {
		ObjectType doc = new ObjectType("slk:publishedAttachment");
		doc.setLocalName(doc.getCmisId());
		doc.setLocalNamespace(Constants.NS);
		doc.setQueryName(doc.getCmisId());
		doc.setDisplayName(doc.getCmisId());
		doc.setBase(doc0);
		doc.setParent(doc0);
		doc.setDescription("Published Item");
		doc.setCreatable(true);
		doc.setFileable(true);
		doc.setQueryable(true);
		doc.setControllablePolicy(true);
		doc.setControllableAcl(true);
		doc.setFulltextIndexed(true);
		doc.setIncludedInSupertypeQuery(true);
		doc.setVersionable(true);
		doc.setContentStreamAllowed(ContentStreamAllowed.REQUIRED);
        doc.setRepository(repository);
        
		doc.addObjectTypeProperty(buildObjectTypeProperty("slk:docId",				PropertyType.STRING,	Cardinality.SINGLE,	Updatability.ONCREATE, true, "Original Document's id"));
		doc.addObjectTypeProperty(buildObjectTypeProperty("slk:attachId",			PropertyType.STRING,	Cardinality.SINGLE,	Updatability.ONCREATE, true, "Original item's id"));
		doc.addObjectTypeProperty(buildObjectTypeProperty("slk:caseNumber",			PropertyType.STRING,	Cardinality.MULTI,	Updatability.ONCREATE, true, "Document's case numbers"));
		doc.addObjectTypeProperty(buildObjectTypeProperty("slk:caseNumberSector",	PropertyType.STRING,	Cardinality.MULTI,	Updatability.ONCREATE, true, "Document's case numbers+sectors"));
        doc.addObjectTypeProperty(buildObjectTypeProperty("slk:comments",			PropertyType.STRING,	Cardinality.SINGLE,	Updatability.ONCREATE, false, "Document's case numbers"));
        doc.addObjectTypeProperty(buildObjectTypeProperty("slk:docTitle",			PropertyType.STRING,	Cardinality.SINGLE,	Updatability.ONCREATE, true, "Document's title"));
        doc.addObjectTypeProperty(buildObjectTypeProperty("slk:saveNb",				PropertyType.STRING,	Cardinality.SINGLE,	Updatability.ONCREATE, true, "Item's save number"));        
        doc.addObjectTypeProperty(buildObjectTypeProperty("slk:docRegNb",			PropertyType.STRING,	Cardinality.SINGLE,	Updatability.ONCREATE, false, "Document's registration number"));
        doc.addObjectTypeProperty(buildObjectTypeProperty("slk:docMirror",			PropertyType.STRING,	Cardinality.MULTI,	Updatability.ONCREATE, false, "Document's ref of mirror"));
        doc.addObjectTypeProperty(buildObjectTypeProperty("slk:docDate",			PropertyType.DATETIME,	Cardinality.SINGLE,	Updatability.ONCREATE, true, "Document's date"));
        doc.addObjectTypeProperty(buildObjectTypeProperty("slk:attachDate",			PropertyType.DATETIME,	Cardinality.SINGLE,	Updatability.ONCREATE, true, "Document's date"));
        doc.addObjectTypeProperty(buildObjectTypeProperty("slk:type",				PropertyType.STRING,	Cardinality.SINGLE,	Updatability.ONCREATE, true, "Item's docType"));
        doc.addObjectTypeProperty(buildObjectTypeProperty("slk:docFlow",			PropertyType.STRING,	Cardinality.SINGLE,	Updatability.ONCREATE, true, "Item's docType"));
        doc.addObjectTypeProperty(buildObjectTypeProperty("slk:publicationDate",	PropertyType.DATETIME,	Cardinality.SINGLE,	Updatability.ONCREATE, true, "Item's publication date"));
        doc.addObjectTypeProperty(buildObjectTypeProperty("slk:access",				PropertyType.STRING,	Cardinality.SINGLE,	Updatability.ONCREATE, true, "Item's accessibility"));
        doc.addObjectTypeProperty(buildObjectTypeProperty("slk:party",				PropertyType.STRING,	Cardinality.MULTI,	Updatability.ONCREATE, false, "Parties"));
        doc.addObjectTypeProperty(buildObjectTypeProperty("slk:fromTo",				PropertyType.STRING,	Cardinality.SINGLE,	Updatability.ONCREATE, true, "Parties"));
        doc.addObjectTypeProperty(buildObjectTypeProperty("slk:numberInCase",       PropertyType.STRING,	Cardinality.MULTI,	Updatability.ONCREATE, true,  "Document's number in the case"));
        return doc;
	}
	
}
