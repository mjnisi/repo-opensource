package eu.trade.repo.policyimpl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.MutableProperties;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.model.CMISObject;
import eu.trade.repo.policy.AbstractBasePolicy;
import eu.trade.repo.policy.PolicyContext;
import eu.trade.repo.policy.PolicyContext.PolicyState;

public class APolicy extends AbstractBasePolicy {
	
	@Autowired
	private PolicyMonitor policyMonitor;
	
	@Override
	public String createFolder(String repositoryId, Properties properties,
			String folderId, List<String> policies, Acl addAces,
			Acl removeAces, ExtensionsData extension) {

		PolicyContext context = getPolicyContext(extension);
		if(context.getPolicyState() == PolicyState.AFTER) {
			policyMonitor.incrementCounter();
			policyMonitor.addMessage("A AFTER (createFolder)");
		} else if(context.getPolicyState() == PolicyState.BEFORE) {
			policyMonitor.incrementCounter();
			policyMonitor.addMessage("A BEFORE (createFolder)");
		}
		
		
		return null;
	}

	@Override
	public String createItem(String repositoryId, Properties properties,
			String folderId, List<String> policies, Acl addAces,
			Acl removeAces, ExtensionsData extension) {

		PolicyContext context = getPolicyContext(extension);
		List<String> ids = new ArrayList<String>();
		for(CMISObject object: context.getAppliedObjects()) {
			ids.add(object.getCmisObjectId());
		}
		
		if(context.getPolicyState() == PolicyState.AFTER) {
			policyMonitor.incrementCounter();
			policyMonitor.addMessage("A AFTER (createItem) " + ids.toString());
		} else if(context.getPolicyState() == PolicyState.BEFORE) {
			policyMonitor.incrementCounter();
			policyMonitor.addMessage("A BEFORE (createItem) " + ids.toString());
		}
		
		return null;
	}

	@Override
	public String createDocument(String repositoryId, Properties properties,
			String folderId, ContentStream contentStream,
			VersioningState versioningState, List<String> policies,
			Acl addAces, Acl removeAces, ExtensionsData extension) {
		
		policyMonitor.incrementCounter();
		
		PolicyContext context = getPolicyContext(extension);
		if(context.getPolicyState() == PolicyState.BEFORE) {
			policyMonitor.addMessage("A BEFORE (createDocument)");
			
			String name = properties.getProperties().get("cmis:name").getFirstValue().toString();
			
			if(name.contains(" ")) {
				((MutableProperties)properties).replaceProperty(
						new PropertyStringImpl("cmis:name", name.replaceAll("\\s", "")));
			}
		}
		return null;
	}

	@Override
	public ObjectInFolderList getChildren(String repositoryId, String folderId,
			String filter, String orderBy, Boolean includeAllowableActions,
			IncludeRelationships includeRelationships, String renditionFilter,
			Boolean includePathSegment, BigInteger maxItems,
			BigInteger skipCount, ExtensionsData extension) {
			
		PolicyContext context = getPolicyContext(extension);
		
		if(context.getPolicyState() == PolicyState.AFTER) {
			ObjectInFolderList objectInFolderList = (ObjectInFolderList)context.getReturnValue();
			
			policyMonitor.addMessage("A AFTER (getChildren) " + objectInFolderList.getNumItems());
		} else if(context.getPolicyState() == PolicyState.BEFORE) {
			policyMonitor.addMessage("A BEFORE (getChildren)");
		}
		
		return null;
	}


	
	
	

}
