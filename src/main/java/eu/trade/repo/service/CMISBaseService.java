package eu.trade.repo.service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolationException;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.model.DBEntity;
import eu.trade.repo.model.ObjectType;
import eu.trade.repo.model.ObjectTypeProperty;
import eu.trade.repo.model.Repository;
import eu.trade.repo.service.interfaces.ICMISBaseService;
import eu.trade.repo.util.Constants;
import eu.trade.repo.util.EntityManagerProxyBuilder;
import eu.trade.repo.util.TxKeySynchronizer;

@Transactional
public class CMISBaseService implements ICMISBaseService {
	//MODEL
	private static final Logger LOG = LoggerFactory.getLogger(CMISBaseService.class);

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private EntityManagerProxyBuilder entityManagerProxyBuilder;

	@Autowired
	private TxKeySynchronizer txKeySynchronizer;

	protected CMISBaseService() {}

	//GETTERS/SETTERS
	@Override
	public EntityManager getEntityManager() {	return entityManagerProxyBuilder.getInstance(entityManager); }

	@Override
	public void setEntityManager(EntityManager entityManager) {	this.entityManager = entityManager;	}

	//INTERFACE
	@Override
	public <T extends DBEntity> T find(Class<T> clazz, Integer id)  {
		return entityManager.find(clazz, id);
	}

	@Override
	public <T extends DBEntity> T merge(T entity)  {
		return entityManager.merge(entity);
	}

	@Override
	public <T extends DBEntity> void persist(T entity)  {
		try {
			entityManager.persist(entity);
		} catch (ConstraintViolationException e) {
			LOG.error(e.getLocalizedMessage(), e);
			throw e;
		}
	}

	@Override
	public void flush()  {
		entityManager.flush();
	}

	@Override
	public <T extends DBEntity> void removeDetached(T dbe) {
		entityManager.remove(find(dbe.getClass(), dbe.getId()));
	}

	@Override
	public <T extends DBEntity> void remove(T dbe) {
		entityManager.remove(dbe);
	}

	//PROTECTED
	protected ObjectType createRelationshipType(Repository repository) {
		ObjectType relationship = new ObjectType(BaseTypeId.CMIS_RELATIONSHIP.value());
		relationship.setLocalName(relationship.getCmisId());
		relationship.setLocalNamespace(Constants.NS);
		relationship.setQueryName(relationship.getCmisId());
		relationship.setDisplayName(relationship.getCmisId());
		relationship.setBase(relationship);
		relationship.setDescription(relationship.getCmisId());
		relationship.setCreatable(true);
		relationship.setFileable(false);
		relationship.setQueryable(true);
		relationship.setControllablePolicy(true);
		relationship.setControllableAcl(true);
		relationship.setFulltextIndexed(true);
		relationship.setIncludedInSupertypeQuery(true);
		relationship.setRepository(repository);
		relationship.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.NAME,							PropertyType.STRING,	Cardinality.SINGLE,	Updatability.READWRITE, true, true, true, "Name of the object"));
		relationship.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.DESCRIPTION,						PropertyType.STRING,	Cardinality.SINGLE,	Updatability.READWRITE, false, true, true, "Description of the object"));
		relationship.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.OBJECT_ID,						PropertyType.ID,		Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "Id of the object"));
		relationship.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.BASE_TYPE_ID,					PropertyType.ID,		Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "Id of the base object-type for the object"));
		relationship.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.OBJECT_TYPE_ID,					PropertyType.ID,		Cardinality.SINGLE,	Updatability.ONCREATE, false, true, true, "Id of the object's type"));
		relationship.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.SECONDARY_OBJECT_TYPE_IDS,		PropertyType.ID,		Cardinality.MULTI,	Updatability.READWRITE, false, true, false, "Ids of the object's secondary types"));
		relationship.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.CREATED_BY,						PropertyType.STRING,	Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "User who created the object"));
		relationship.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.CREATION_DATE,					PropertyType.DATETIME,	Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "Date Time when the object was created"));
		relationship.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.LAST_MODIFIED_BY,				PropertyType.STRING,	Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "User who last modified the object"));
		relationship.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.LAST_MODIFICATION_DATE,			PropertyType.DATETIME,	Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "Date Time when the object was last modified"));
		relationship.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.CHANGE_TOKEN,					PropertyType.STRING,	Cardinality.SINGLE,	Updatability.READONLY, false, false, false, "Token used for optimistic locking and concurrency checking"));
		relationship.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.SOURCE_ID,						PropertyType.ID,		Cardinality.SINGLE,	Updatability.ONCREATE, true, true, true, "ID of the source object of the relationship"));
		relationship.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.TARGET_ID,						PropertyType.ID,		Cardinality.SINGLE,	Updatability.ONCREATE, true, true, true, "ID of the target object of the relationship"));
		return relationship;
	}

	protected ObjectType createPolicyType(Repository repository) {
		ObjectType policy = new ObjectType(BaseTypeId.CMIS_POLICY.value());
		policy.setLocalName(policy.getCmisId());
		policy.setLocalNamespace(Constants.NS);
		policy.setQueryName(policy.getCmisId());
		policy.setDisplayName(policy.getCmisId());
		policy.setBase(policy);
		policy.setDescription(policy.getCmisId());
		policy.setCreatable(true);
		policy.setFileable(true);
		policy.setQueryable(true);
		policy.setControllablePolicy(true);
		policy.setControllableAcl(true);
		policy.setFulltextIndexed(true);
		policy.setIncludedInSupertypeQuery(true);
		policy.setRepository(repository);
		policy.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.NAME,							PropertyType.STRING,	Cardinality.SINGLE,	Updatability.READWRITE, true, true, true, "Name of the object"));
		policy.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.DESCRIPTION,					PropertyType.STRING,	Cardinality.SINGLE,	Updatability.READWRITE, false, true, true, "Description of the object"));
		policy.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.OBJECT_ID,						PropertyType.ID,		Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "Id of the object"));
		policy.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.BASE_TYPE_ID,					PropertyType.ID,		Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "Id of the base object-type for the object"));
		policy.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.OBJECT_TYPE_ID,				PropertyType.ID,		Cardinality.SINGLE,	Updatability.ONCREATE, false, true, true, "Id of the object's type"));
		policy.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.SECONDARY_OBJECT_TYPE_IDS,		PropertyType.ID,		Cardinality.MULTI,	Updatability.READWRITE, false, true, false, "Ids of the object's secondary types"));
		policy.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.CREATED_BY,					PropertyType.STRING,	Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "User who created the object"));
		policy.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.CREATION_DATE,					PropertyType.DATETIME,	Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "Date Time when the object was created"));
		policy.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.LAST_MODIFIED_BY,				PropertyType.STRING,	Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "User who last modified the object"));
		policy.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.LAST_MODIFICATION_DATE,		PropertyType.DATETIME,	Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "Date Time when the object was last modified"));
		policy.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.CHANGE_TOKEN,					PropertyType.STRING,	Cardinality.SINGLE,	Updatability.READONLY, false, false, false, "Token used for optimistic locking and concurrency checking"));
		policy.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.POLICY_TEXT,					PropertyType.STRING,	Cardinality.SINGLE,	Updatability.READWRITE, false, true, true, "User-friendly description of the policy"));
		return policy;
	}

	protected ObjectType createItemType(Repository repository) {
		ObjectType item = new ObjectType(BaseTypeId.CMIS_ITEM.value());
		item.setLocalName(item.getCmisId());
		item.setLocalNamespace(Constants.NS);
		item.setQueryName(item.getCmisId());
		item.setDisplayName(item.getCmisId());
		item.setBase(item);
		item.setDescription(item.getCmisId());
		item.setCreatable(true);
		item.setFileable(true);
		item.setQueryable(true);
		item.setControllablePolicy(true);
		item.setControllableAcl(true);
		item.setFulltextIndexed(true);
		item.setIncludedInSupertypeQuery(true);
		item.setRepository(repository);
		item.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.NAME,						PropertyType.STRING,	Cardinality.SINGLE,	Updatability.READWRITE, true, true, true, "Name of the object"));
		item.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.DESCRIPTION,					PropertyType.STRING,	Cardinality.SINGLE,	Updatability.READWRITE, false, true, true, "Description of the object"));
		item.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.OBJECT_ID,					PropertyType.ID,		Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "Id of the object"));
		item.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.BASE_TYPE_ID,				PropertyType.ID,		Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "Id of the base object-type for the object"));
		item.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.OBJECT_TYPE_ID,				PropertyType.ID,		Cardinality.SINGLE,	Updatability.ONCREATE, false, true, true, "Id of the object's type"));
		item.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.SECONDARY_OBJECT_TYPE_IDS,	PropertyType.ID,		Cardinality.MULTI,	Updatability.READWRITE, false, true, false, "Ids of the object's secondary types"));
		item.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.CREATED_BY,					PropertyType.STRING,	Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "User who created the object"));
		item.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.CREATION_DATE,				PropertyType.DATETIME,	Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "Date Time when the object was created"));
		item.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.LAST_MODIFIED_BY,			PropertyType.STRING,	Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "User who last modified the object"));
		item.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.LAST_MODIFICATION_DATE,		PropertyType.DATETIME,	Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "Date Time when the object was last modified"));
		item.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.CHANGE_TOKEN,				PropertyType.STRING,	Cardinality.SINGLE,	Updatability.READONLY, false, false, false, "Token used for optimistic locking and concurrency checking"));
		return item;
	}
	
	
	protected ObjectType createSecondaryType(Repository repository) {
		ObjectType secondary = new ObjectType(BaseTypeId.CMIS_SECONDARY.value());
		secondary.setLocalName(secondary.getCmisId());
		secondary.setLocalNamespace(Constants.NS);
		secondary.setQueryName(secondary.getCmisId());
		secondary.setDisplayName(secondary.getCmisId());
		secondary.setBase(secondary);
		secondary.setDescription(secondary.getCmisId());
		secondary.setCreatable(false);
		secondary.setFileable(false);
		secondary.setQueryable(true);
		secondary.setControllablePolicy(false);
		secondary.setControllableAcl(false);
		secondary.setFulltextIndexed(true);
		secondary.setIncludedInSupertypeQuery(true);
		secondary.setRepository(repository);
		return secondary;
	}
	
	protected ObjectType createFolderType(Repository repository) {
		ObjectType folder = new ObjectType(BaseTypeId.CMIS_FOLDER.value());
		folder.setLocalName(folder.getCmisId());
		folder.setLocalNamespace(Constants.NS);
		folder.setQueryName(folder.getCmisId());
		folder.setDisplayName(folder.getCmisId());
		folder.setBase(folder);
		folder.setDescription(folder.getCmisId());
		folder.setCreatable(true);
		folder.setFileable(true);
		folder.setQueryable(true);
		folder.setControllablePolicy(true);
		folder.setControllableAcl(true);
		folder.setFulltextIndexed(true);
		folder.setIncludedInSupertypeQuery(true);
		folder.setRepository(repository);
		folder.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.NAME,							PropertyType.STRING,	Cardinality.SINGLE,	Updatability.READWRITE, true, true, true, "Name of the object"));
		folder.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.DESCRIPTION,					PropertyType.STRING,	Cardinality.SINGLE,	Updatability.READWRITE, false, true, true, "Description of the object"));
		folder.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.OBJECT_ID,						PropertyType.ID,		Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "Id of the object"));
		folder.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.BASE_TYPE_ID,					PropertyType.ID,		Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "Id of the base object-type for the object"));
		folder.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.OBJECT_TYPE_ID,				PropertyType.ID,		Cardinality.SINGLE,	Updatability.ONCREATE, false, true, true, "Id of the object's type"));
		folder.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.SECONDARY_OBJECT_TYPE_IDS,		PropertyType.ID,		Cardinality.MULTI,	Updatability.READWRITE, false, true, false, "Ids of the object's secondary types"));
		folder.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.CREATED_BY,					PropertyType.STRING,	Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "User who created the object"));
		folder.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.CREATION_DATE,					PropertyType.DATETIME,	Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "Date Time when the object was created"));
		folder.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.LAST_MODIFIED_BY,				PropertyType.STRING,	Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "User who last modified the object"));
		folder.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.LAST_MODIFICATION_DATE,		PropertyType.DATETIME,	Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "Date Time when the object was last modified"));
		folder.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.CHANGE_TOKEN,					PropertyType.STRING,	Cardinality.SINGLE,	Updatability.READONLY, false, false, false, "Token used for optimistic locking and concurrency checking"));
		folder.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.PARENT_ID,						PropertyType.ID,		Cardinality.SINGLE,	Updatability.READONLY, false, true, false, "PropertyType.ID.value() of the parent folder of the folder"));
		folder.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.PATH,							PropertyType.STRING,	Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "The fully qualified path to this folder"));
		folder.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS,	PropertyType.ID,		Cardinality.MULTI,	Updatability.READONLY, false, false, false, "Id's of the set of Object-types that can be created, moved or filed into this folder"));
		return folder;
	}

	/**
	 * Creates document type
	 * @param repository repository is needed because the collection of object type properties is using Repository.hashCode()
	 * @return
	 */
	protected ObjectType createDocumentType(Repository repository) {
		ObjectType doc = new ObjectType(BaseTypeId.CMIS_DOCUMENT.value());
		doc.setLocalName(doc.getCmisId());
		doc.setLocalNamespace(Constants.NS);
		doc.setQueryName(doc.getCmisId());
		doc.setDisplayName(doc.getCmisId());
		doc.setBase(doc);
		doc.setDescription(doc.getCmisId());
		doc.setCreatable(true);
		doc.setFileable(true);
		doc.setQueryable(true);
		doc.setControllablePolicy(true);
		doc.setControllableAcl(true);
		doc.setFulltextIndexed(true);
		doc.setIncludedInSupertypeQuery(true);
		doc.setVersionable(true);
		doc.setContentStreamAllowed(ContentStreamAllowed.ALLOWED);
		doc.setRepository(repository);
		doc.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.NAME, 							PropertyType.STRING,		Cardinality.SINGLE, Updatability.READWRITE, true, true, true, "Name of the Object"));
		doc.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.DESCRIPTION, 						PropertyType.STRING,		Cardinality.SINGLE, Updatability.READWRITE, false, true, true, "Description of the Object"));
		doc.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.OBJECT_ID, 						PropertyType.ID, 			Cardinality.SINGLE, Updatability.READONLY, false, true, true, "Id of the object"));
		doc.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.BASE_TYPE_ID,  					PropertyType.ID,			Cardinality.SINGLE, Updatability.READONLY, false, true, true, "Id of the base object-type for the object"));
		doc.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.OBJECT_TYPE_ID, 					PropertyType.ID, 			Cardinality.SINGLE, Updatability.ONCREATE, false, true, true, "Id of the object's type"));
		doc.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, 		PropertyType.ID, 			Cardinality.MULTI, 	Updatability.READWRITE, false, true, false, "Ids of the object's secondary types"));
		doc.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.CREATED_BY, 						PropertyType.STRING, 		Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "User who created the object"));
		doc.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.CREATION_DATE, 					PropertyType.DATETIME, 		Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "DateTime when the object was created"));
		doc.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.LAST_MODIFIED_BY,					PropertyType.STRING, 		Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "User who last modified the object"));
		doc.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.LAST_MODIFICATION_DATE,			PropertyType.DATETIME,		Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "DateTime when the object was last modified"));
		doc.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.CHANGE_TOKEN,						PropertyType.STRING, 		Cardinality.SINGLE,	Updatability.READONLY, false, false, false, "Opaque token used for optimistic locking and concurrency checking"));
		doc.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.IS_IMMUTABLE,						PropertyType.BOOLEAN,		Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "TRUE if the repository MUST throw and error at any attempt to update or delete the object"));
		doc.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.IS_LATEST_VERSION,				PropertyType.BOOLEAN,		Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "If the Document object is the Latest Version in its Version Series"));
		doc.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.IS_MAJOR_VERSION,					PropertyType.BOOLEAN,		Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "If the Document object is a Major Version in its Version Series"));
		doc.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.IS_LATEST_MAJOR_VERSION,			PropertyType.BOOLEAN,		Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "If the Document object is the Latest Major Version in its Version Series"));
		doc.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.IS_PRIVATE_WORKING_COPY,			PropertyType.BOOLEAN,		Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "If the Document object is a Private Working Copy"));
		doc.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.VERSION_LABEL,					PropertyType.STRING,		Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "Textual description the position of an individual object with respect to the version series"));
		doc.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.VERSION_SERIES_ID,				PropertyType.ID,			Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "ID of the Version Series for this Object"));
		doc.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT,	PropertyType.BOOLEAN,		Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "If there currenly exists a Private Working Copy for this Version Series"));
		doc.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY,	PropertyType.STRING,		Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "If IsVersionSeriesCheckedOut then an identifier for the user who created the Private Working Copy"));
		doc.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID,	PropertyType.ID,			Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "If IsVersionSeriesCheckedOut the Identifier for the Private Working Copy"));
		doc.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.CHECKIN_COMMENT,					PropertyType.STRING,		Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "Textual comment associated with the given version"));
		doc.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.CONTENT_STREAM_LENGTH,			PropertyType.INTEGER,		Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "Length of the content stream (in bytes)"));
		doc.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.CONTENT_STREAM_MIME_TYPE,			PropertyType.STRING,		Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "MIME type of the Content Stream"));
		doc.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.CONTENT_STREAM_FILE_NAME,			PropertyType.STRING,		Cardinality.SINGLE,	Updatability.READONLY, false,true, true, "File name of the Content Stream"));
		doc.addObjectTypeProperty(buildObjectTypeProperty(PropertyIds.CONTENT_STREAM_ID,				PropertyType.ID,			Cardinality.SINGLE,	Updatability.READONLY, false, true, true, "Id of the stream"));
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
	 * @param queryable
	 * @param orderable
	 * @param description
	 * @return
	 */
	protected ObjectTypeProperty buildObjectTypeProperty (String id, PropertyType type, Cardinality cardinality, 
			Updatability updatability, boolean required, boolean queryable, boolean orderable, String description) {

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

		p.setQueryable(queryable);
		p.setOrderable(orderable);

		return p;
	}

	/**
	 * Locks an {@link Object} key in order to synchronise a block of code within a transaction.
	 * 
	 * @param key {@link Object} The key to be locked.
	 * @see TxKeySynchronizer
	 */
	protected void synch(Object key) {
		txKeySynchronizer.synch(key);
	}
}
