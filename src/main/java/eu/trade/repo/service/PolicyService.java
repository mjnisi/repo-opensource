package eu.trade.repo.service;

import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.index.triggers.annotation.TriggerIndex;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.selectors.CMISObjectSelector;
import eu.trade.repo.service.interfaces.IPolicyService;

/**
 * 
 * @author azaridi
 *
 */
public class PolicyService extends CMISBaseService  implements IPolicyService {
	@Autowired
	private CMISObjectSelector objSelector;

	@TriggerIndex
	@Override
	public void applyPolicy(String repositoryId, String policyId,
			String objectId, ExtensionsData extension) {

		CMISObject policy = objSelector.getCMISObject(repositoryId, policyId);
		CMISObject object = objSelector.getCMISObject(repositoryId, objectId);

		if (!policy.isPolicy()) {
			throw new CmisInvalidArgumentException(String.format("Not a policy (%s), cannot apply to object %s", policyId, objectId));
		}

		//TODO++ to be improved using the flag in the object type
		if (!object.isFolder() && !object.isDocument()) {
			throw new CmisInvalidArgumentException(String.format("Cannot apply a policy (%s), to object %s", policyId, objectId));
		}

		object.addPolicy(policy);
		merge(object);
	}

	@TriggerIndex
	@Override
	public void removePolicy(String repositoryId, String policyId,
			String objectId, ExtensionsData extension) {
		CMISObject policy = objSelector.getCMISObject(repositoryId, policyId);
		CMISObject object = objSelector.getCMISObject(repositoryId, objectId);

		object.removePolicy(policy);
		merge(object);
	}

	@Override
	public Set<CMISObject> getAppliedPolicies(String repositoryId,
			String objectId, String filter, ExtensionsData extension) {

		return objSelector.getCMISObjectWithPolicies(objectId).getPolicies();
	}
}
