package eu.trade.repo.policyimpl;

import java.util.List;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.policy.AbstractBasePolicy;
import eu.trade.repo.policy.PolicyContext;
import eu.trade.repo.policy.PolicyContext.PolicyState;

public class BPolicy extends AbstractBasePolicy {

	@Autowired
	private PolicyMonitor policyMonitor;
	
	@Override
	public String createFolder(String repositoryId, Properties properties,
			String folderId, List<String> policies, Acl addAces,
			Acl removeAces, ExtensionsData extension) {

		PolicyContext context = getPolicyContext(extension);
		if(context.getPolicyState() == PolicyState.BEFORE) {
			policyMonitor.incrementCounter();
			policyMonitor.addMessage("B BEFORE");
		} else if(context.getPolicyState() == PolicyState.AFTER) {
			policyMonitor.incrementCounter();
			policyMonitor.addMessage("B AFTER");
		}
		
		return null;
	}

	@Override
	public void updateProperties(String repositoryId, Holder<String> objectId,
			Holder<String> changeToken, Properties properties,
			ExtensionsData extension) {
		
		PolicyContext context = getPolicyContext(extension);
		if(context.getPolicyState() == PolicyState.BEFORE) {
			policyMonitor.incrementCounter();
			policyMonitor.addMessage("B BEFORE (updateProperties)");
		} else if(context.getPolicyState() == PolicyState.AFTER) {
			policyMonitor.incrementCounter();
			policyMonitor.addMessage("B AFTER (updateProperties)");
		}
	}

}
