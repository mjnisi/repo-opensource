package eu.trade.repo.service.interfaces;

import java.util.Set;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import eu.trade.repo.model.CMISObject;

public interface IPolicyService {
	void applyPolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) ;
	void removePolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) ;
	Set<CMISObject> getAppliedPolicies(String repositoryId, String objectId, String filter, ExtensionsData extension) ;
}
