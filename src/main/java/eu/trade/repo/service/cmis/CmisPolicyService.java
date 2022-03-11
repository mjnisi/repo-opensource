package eu.trade.repo.service.cmis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.spi.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.model.CMISObject;
import eu.trade.repo.service.cmis.data.out.ObjectDataImpl;
import eu.trade.repo.service.interfaces.IPolicyService;

/**
 * 
 * @author azaridi
 *
 */

@Transactional
public class CmisPolicyService implements PolicyService {
	@Autowired
	private IPolicyService policyService;

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.PolicyService#applyPolicy(java.lang.String, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public void applyPolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {
		policyService.applyPolicy(repositoryId, policyId, objectId, extension);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.PolicyService#removePolicy(java.lang.String, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public void removePolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {
		policyService.removePolicy(repositoryId, policyId, objectId, extension);
	}

	/**
	 * @see org.apache.chemistry.opencmis.commons.spi.PolicyService#getAppliedPolicies(java.lang.String, java.lang.String, java.lang.String, org.apache.chemistry.opencmis.commons.data.ExtensionsData)
	 */
	@Override
	public List<ObjectData> getAppliedPolicies(String repositoryId, String objectId, String filter, ExtensionsData extension) {
		Set<CMISObject> policies = policyService.getAppliedPolicies(repositoryId, objectId, filter, extension);
		List<ObjectData> ans = new ArrayList<ObjectData>();
		for (CMISObject cmisObject : policies) {
			ans.add(new ObjectDataImpl(cmisObject, null, null, filter)); 
		}
		return ans;
	}
}