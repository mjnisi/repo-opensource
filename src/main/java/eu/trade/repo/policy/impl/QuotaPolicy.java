package eu.trade.repo.policy.impl;

import java.util.List;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.model.CMISObject;
import eu.trade.repo.policy.AbstractBasePolicy;
import eu.trade.repo.policy.PolicyContext;
import eu.trade.repo.policy.PolicyContext.PolicyState;
import eu.trade.repo.selectors.CMISObjectSelector;

public class QuotaPolicy extends AbstractBasePolicy {
	
	private static final Logger LOG = LoggerFactory.getLogger(QuotaPolicy.class);
	
	@Autowired
	private CMISObjectSelector objectSelector;

	@Override
	public String createDocument(String repositoryId, Properties properties, String folderId, ContentStream contentStream, VersioningState versioningState, List<String> policies, Acl addAces, Acl removeAces, ExtensionsData extension) {
		
		PolicyContext policyContext = getPolicyContext(extension);
		
		if (PolicyState.BEFORE == policyContext.getPolicyState()) {
			
			LOG.info("Quota policy triggered, {} objects applied", policyContext.getAppliedObjects().size());
			
			//for every applied object in the ancestors of the triggered object 
			for(CMISObject appliedObject: policyContext.getAppliedObjects()) {
				checkQuota(repositoryId, policyContext.getPolicyObject(), appliedObject);
			}
			
		}
		
		return null;
	}

	
	private void checkQuota(String repositoryId, CMISObject policyObject, CMISObject appliedObject) {
		Number quota = policyObject.getPropertyValue("trade:quota");
		
		//TODO, change the code to use the service instead the selector
		long t = System.currentTimeMillis();
		Number size = objectSelector.getFolderSize(repositoryId, appliedObject.getCmisObjectId());
		long delta = System.currentTimeMillis() - t;
		
		if(LOG.isDebugEnabled()) {
			LOG.debug("Policy: {} max size: {} ID: {}", policyObject.getPropertyValue("cmis:name"),
                    formatSize(quota),
                    policyObject.getCmisObjectId());
			LOG.debug("Object: {} size: {} ID: {}", appliedObject.getPropertyValue("cmis:name"),
                    formatSize(size),
                    appliedObject.getCmisObjectId());
			LOG.debug("getFolderSize() executed in {} ms", delta);
		}
		
		//relaxed quota check, only checks the existing size, it is not considering the current file size  
		if(size.longValue() > quota.longValue()) {
			LOG.info("Quota exceeded on {} ", appliedObject.getCmisObjectId());
			throw new CmisConstraintException("Quota exceeded.");
		}
	}
	
	/**
	 * Format the size
	 */
	private static final String formatSize(Number size) {
		final String[] suffix = {"Bytes", "KB", "MB", "GB", "TB"}; 
		double d = size.doubleValue();
		int suffixIndex = 0;
		while(d > 1024) {
			suffixIndex++;
			d = (d/1024);
		}
		
		return String.format("%.2f", d) + " " + suffix[suffixIndex];
	}
	
	/*
	  
select sum(numeric_value) from property 
where object_type_property_id = 10079
and object_id in (
  select 
    --oc.object_id,
    connect_by_root oc.child_object_id
  from object_child oc
  --where oc.object_id = 21789
  where oc.object_id = 21788
  connect by prior oc.object_id = oc.child_object_id);

	  
	 */
}
