package eu.trade.repo.service.cmis;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.CmisExtensionElementImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectListImpl;
import org.apache.chemistry.opencmis.commons.spi.DiscoveryService;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.ChangeEvent;
import eu.trade.repo.query.QueryResult;
import eu.trade.repo.security.CustomSecured;
import eu.trade.repo.security.CustomSecured.CustomAction;
import eu.trade.repo.security.Security;
import eu.trade.repo.service.cmis.data.out.ChangeDataImpl;
import eu.trade.repo.service.cmis.data.out.ObjectDataImpl;
import eu.trade.repo.service.interfaces.IDiscoveryService;
import eu.trade.repo.service.interfaces.IRelationshipService;
import eu.trade.repo.util.Constants;

@Transactional
public class CmisDiscoveryService implements DiscoveryService {
	
	private static final int MAX_ITEMS = 60000;

	@Autowired
	private IDiscoveryService discoveryService;
	
	@Autowired
	private IRelationshipService relationshipService;

	@Autowired
	private Security security;

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.DiscoveryService#query(java.lang.String, java.lang.String, java.lang.Boolean, java.lang.Boolean, org.apache.chemistry.opencmis.commons.enums.IncludeRelationships, java.lang.String, java.math.BigInteger, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@CustomSecured(CustomAction.LOGIN)
	public ObjectList query(String repositoryId, String statement, Boolean searchAllVersions, Boolean includeAllowableActions,
			IncludeRelationships includeRelationships, String renditionFilter, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {

		Map<String, String> filter = discoveryService.getColumns(statement);
		boolean isNormalizedQuery = !Boolean.parseBoolean(getExtensionElementValue(extension, Constants.QUERY_CASE_SENSITIVE));
		QueryResult qr = discoveryService.query(statement, repositoryId, searchAllVersions, maxItems.intValue(), skipCount.intValue(), isNormalizedQuery);
		Set<CMISObject> cmisObjects = qr.getResult();
		List<ObjectData> objectDataList = new ArrayList<>(qr.getResult().size());
		
		Map<String, Set<CMISObject>> relationshipMappings = relationshipService.getRelationshipMappings(repositoryId, cmisObjects, includeRelationships);
		
		for (CMISObject cmisObject : cmisObjects) {
			AllowableActions allowableActions = null;
			
			if (includeAllowableActions) {
				allowableActions = security.getAllowableActions(cmisObject);
			}
						
			List<CMISObject> relationshipForObject = relationshipMappings.get(cmisObject.getCmisObjectId()) == null ? new ArrayList() : new ArrayList(relationshipMappings.get(cmisObject.getCmisObjectId()));						
			objectDataList.add(new ObjectDataImpl(cmisObject, allowableActions, relationshipForObject, filter));
		}
		
		ObjectListImpl objectList = new ObjectListImpl();
		objectList.setObjects(objectDataList);

		List<CmisExtensionElement> l = new ArrayList<>();
		CmisExtensionElement query = new CmisExtensionElementImpl(Constants.NS, "query", null, qr.getQuery());
		l.add(query);
		objectList.setExtensions(l);

		/*
		 * The count is not needed to be executed every time.
		 * 
		 * - If the user is asking for the first page and the number of results is less than skip count; count = page result Count
		 * - If the user is asking for the second page and the number of results is less than page size (max Items); count = skip + page result count  
		 * 
		 * Hence the count is only needed in the perfect page edges, for example:
		 * 
		 *  - if skip:0 and maxItems:10; if the result page count is 
		 *  	-- 0; as the skip is 0, in the first page this means 0 matches
		 *  	-- between 1 and 9: as the skip is 0, the total count is the result page count
		 *  	-- or 10: we do not know if there are 10 results in total or more, a count is needed
		 *  
		 *  - if skip:10 and maxItems:10, if the result page count is
		 *  	-- 0; as the skip is 10, we could decide the approach:
		 *  		* accuracy: we launch the count because we do not know if the previous pas was full
		 *  		* performance: we assume the previous page was full: so the total count is: the skip
		 *  	-- between 1 and 9: as the skip is 10, the total count is the result page count + skip
		 *  	-- or 10: we do not know if there are 20 results in total or more, a count is needed
		 *  
		 *  IMPORTANT: Also both queries, page and count, could be executed in parallel.
		 */
		int totalNumberOfObjects = discoveryService.queryCount(statement, repositoryId, searchAllVersions, isNormalizedQuery);
		objectList.setHasMoreItems(skipCount.intValue() + qr.getResult().size() < totalNumberOfObjects);
		objectList.setNumItems(BigInteger.valueOf(totalNumberOfObjects));
		return objectList;
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.DiscoveryService#getContentChanges(java.lang.String, org.apache.chemistry.opencmis.commons.spi.Holder, java.lang.Boolean, java.lang.String, java.lang.Boolean, java.lang.Boolean, java.math.BigInteger, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	@CustomSecured(CustomAction.LOGIN)
	public ObjectList getContentChanges(String repositoryId, Holder<String> changeLogToken, Boolean includeProperties,
			String filter, Boolean includePolicyIds, Boolean includeAcl, BigInteger maxItems, ExtensionsData extension) {

		List<ChangeEvent> changes = discoveryService.getContentChanges(repositoryId, changeLogToken, (maxItems == null || maxItems.intValue() > MAX_ITEMS ? BigInteger.valueOf(CmisDiscoveryService.MAX_ITEMS): maxItems));

		List<ObjectData> objectDataList = new ArrayList<>();
		for (ChangeEvent event : changes) {
			objectDataList.add(new ChangeDataImpl(event));
			changeLogToken.setValue(event.getChangeLogToken());
		}
		ObjectListImpl objectList = new ObjectListImpl();
		objectList.setObjects(objectDataList);
		return objectList;
	}

	private String getExtensionElementValue(ExtensionsData extension, String name){
		String result = null;
		if( null != extension ){
			List<CmisExtensionElement> elemList = extension.getExtensions();
			for (CmisExtensionElement elem : elemList) {
				if( elem.getName().equals(name) ){
					result = elem.getValue();
					break;
				}
			}
		}
		return result;
	}


}
