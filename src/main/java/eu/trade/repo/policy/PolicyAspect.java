package eu.trade.repo.policy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ExtensionDataImpl;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import eu.trade.repo.model.ActionParameter;
import eu.trade.repo.model.Ancestor;
import eu.trade.repo.model.CMISObject;
import eu.trade.repo.policy.PolicyContext.PolicyState;
import eu.trade.repo.security.ApplyTo;
import eu.trade.repo.security.Security;
import eu.trade.repo.service.interfaces.IObjectService;
import eu.trade.repo.service.interfaces.IPolicyService;
import eu.trade.repo.service.interfaces.IRepositoryService;

@Aspect
public class PolicyAspect {
	
	private static final Logger LOG = LoggerFactory.getLogger(PolicyAspect.class);
	
	@Autowired
	private IObjectService objectService;
	
	@Autowired
	private IPolicyService policyService;
	
	@Autowired
	private IRepositoryService repositoryService;
	
	@Autowired
	private Security security;
	
	private Map<String, AbstractBasePolicy> policyImpls;
	public void setPolicyImpls(Map<String, AbstractBasePolicy> policyImpls) {
		this.policyImpls = policyImpls;
	}

	@Pointcut("within(eu.trade.repo.service.cmis.*)")
	private void anyPublicCmisInterfaceImpl() {
	}
	
	@Around("anyPublicCmisInterfaceImpl()") // && @policy annotation...?
	public Object aroundPolicyControllableMethod(ProceedingJoinPoint point) throws Throwable {
		long t = System.currentTimeMillis();
	 
		//if the method is not under control of any policy code, just proceed
		Method method = ((MethodSignature) point.getSignature()).getMethod();
		if ("getRepositoryInfos".equals(method.getName()) || 
			"getRepositoryInfo".equals(method.getName()) || 
			!getMethodsToIntercept().contains(method.getName())) {
			return point.proceed();
		}
		
		
		List<String> objectIds = getObjectIds(point);
		//The key is the policy object, and the value the assigned objects
		//A policy could be assigned to several objects in the ancestors
		SetMultimap<CMISObject, CMISObject> policies = HashMultimap.create();
		
		if(!objectIds.isEmpty()) {
			policies = getPolicies(objectIds);
		}
		
		LOG.debug("{} Policy/ies found: {}", policies.size(), policies);
		LOG.debug("Policies Aspect overhead for {}: {} ms", point.getSignature().toShortString(), (System.currentTimeMillis() - t));

		// Pre processing work.. -----------------------------------------------
		for(CMISObject policy: policies.asMap().keySet()) {
			Set<CMISObject> assignedObjects = policies.get(policy);
			String policyType = policy.getPropertyValue(PropertyIds.OBJECT_TYPE_ID);

			//build the policy context
			PolicyContext policyContext = new PolicyContext();
			policyContext.setPolicyState(PolicyState.BEFORE);
			policyContext.setAppliedObjects(assignedObjects);
			policyContext.setPolicyObject(policy);
			triggerPolicy(point, policyType, policyContext);
		}// --------------------------------------------------------------------
		
		
		
		Object result = null;
	 
	    try {
			result = point.proceed();
		} catch (Throwable e) {
			LOG.debug("Exception found in the service execution", e);
			throw e;
		}
	    
	    // Post processing work.. ----------------------------------------------
	    for(CMISObject policy: policies.asMap().keySet()) {
			Set<CMISObject> assignedObjects = policies.get(policy);
			String policyType = policy.getPropertyValue(PropertyIds.OBJECT_TYPE_ID);

			//build the policy context
			PolicyContext policyContext = new PolicyContext();
			policyContext.setPolicyState(PolicyState.AFTER);
			policyContext.setAppliedObjects(assignedObjects);
			policyContext.setPolicyObject(policy);
			policyContext.setReturnValue(result);
			triggerPolicy(point, policyType, policyContext);
		}// --------------------------------------------------------------------

	    
	    return result;
	}
	
	
	/**
	 * Triggers the policy
	 * TBD
	 * 
	 * To be finished: add the context ***
	 *  
	 * @param point
	 * @param policyTypeId
	 * @param policyContext
	 * @throws CmisRuntimeException 
	 */
	private void triggerPolicy(ProceedingJoinPoint point, String policyTypeId, PolicyContext policyContext) {
		
		Object[] args = point.getArgs();
		Object[] argsLocalCopy = new Object[args.length];
		System.arraycopy(args, 0, argsLocalCopy, 0, args.length);
		
		Method method = ((MethodSignature) point.getSignature()).getMethod();

		
		//update the ExtensionData parameter
		int extensionsDataPos = getExtensionsDataPosition(method.getParameterTypes());
		ExtensionsData extensionsData = (ExtensionsData) argsLocalCopy[extensionsDataPos];

		if(extensionsData == null) {
			extensionsData = new ExtensionDataImpl();
			extensionsData.setExtensions(new ArrayList<CmisExtensionElement>());
			argsLocalCopy[extensionsDataPos] = extensionsData;
		}

		
		extensionsData.getExtensions().add(policyContext);
	    
		AbstractBasePolicy policyImpl = policyImpls.get(policyTypeId);
			
		if(policyImpl != null) {
			try {
				policyImpl.getClass().getMethod(
	 				method.getName(), method.getParameterTypes()).invoke(policyImpl, argsLocalCopy);
			} catch (Exception e) {
				throw new CmisConstraintException(
					"Unable to proceed with the action due to policy restrictions: " + e.getCause().getMessage(), e);
			}
		} else {
			LOG.error("Skiping execution of policy {}, missing server code associated.", policyTypeId);
		}
	}
	
	
	/**
	 * Extracts the position of the parameter corresponding to the ExtensionsData
	 * @param parameterTypes
	 * @return
	 */
	private int getExtensionsDataPosition(Class[] parameterTypes) {
		for(int i=0;i<parameterTypes.length;i++) {
			if(parameterTypes[i].equals(ExtensionsData.class)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Obtains the list of object IDs involved in the service execution.
	 * 
	 * It is using the annotation ApplyTo to discriminate.
	 * 
	 * @return
	 */
	private List<String> getObjectIds(ProceedingJoinPoint point) {
		long t = System.currentTimeMillis();
		List<String> objectIds = new ArrayList<String>();
	    Object [] args = point.getArgs();
	    Method method = ((MethodSignature) point.getSignature()).getMethod();
	    
	    Annotation[][] annotations = null;
		try {
			annotations = point.getTarget().getClass().getMethod(method.getName(), method.getParameterTypes()).getParameterAnnotations();
		} catch (NoSuchMethodException | SecurityException e1) {
			e1.printStackTrace();
		}
		
		for(int i=0; i<args.length; i++) {
			for(Annotation annotation : annotations[i]) {
	    		if(annotation instanceof ApplyTo) {
	    			
	    			ActionParameter targetType = ((ApplyTo) annotation).value();
	    			String targetId = getStringParameter(args[i]);
	    			if(targetId != null) {
		    			LOG.debug("TargetType: {} TargetID: {} ", targetType, targetId);
		    			objectIds.add(targetId);
	    			}
	    		}
			}
		}
		
		LOG.debug(" > getObjectIds() {} ms", (System.currentTimeMillis() -t));
		return objectIds;
	}
	
	/**
	 * Obtains the policies applied to the requested objectIds.
	 * 
	 * This method will return the policies associated to the list of objects 
	 * and all the ancestors.
	 * 
	 * ***To be done*** The list of policies will be filtered depending on the access of the
	 * policy object and the current user.
	 * 
	 * @param objectIds
	 * @return a Map with the policies and the assigned objects, the key is the
	 * policy object and the value the assigned object.
	 */
	private SetMultimap<CMISObject,CMISObject> getPolicies(List<String> objectIds) {
		//TODO+++ optimise: get all the objects in a single query
		//implement a selector passing the list of objects and retrieve the 
		//policies associated to all the ancestors and the objects
		//
		//filter in the DB to obtain only the enabled policies
		//(finally wrap the selector in the objectservice)
		long t = System.currentTimeMillis();
		
		String repositoryId = security.getCallContextHolder().getRepositoryId();
		Set<String> enabledPolicies = repositoryService.getEnabledPolicies(repositoryId);

		SetMultimap<CMISObject, CMISObject> policies = HashMultimap.create();

		//get the ancestors
		for(String objectId: objectIds) {
			//add policies to the current object
			t = System.currentTimeMillis();
			CMISObject object = objectService.getObject(repositoryId, objectId);
			for(CMISObject policy: policyService.getAppliedPolicies(repositoryId, object.getCmisObjectId(), null, null)) {
				if(enabledPolicies.contains(policy.getObjectType().getCmisId())) {
					policies.put(policy, object);
				}
			}
			LOG.debug(" > getPolicies() policy current obj {} ms", (System.currentTimeMillis() -t));
			
			t = System.currentTimeMillis();
			Set<Ancestor> ancestors = object.getAncestors();
			for(Ancestor ancestor: ancestors) {
				CMISObject ancestorObject = ancestor.getObjectAncestorId().getAncestor();
				String ancestorId = ancestorObject.getCmisObjectId();
				for(CMISObject policy: policyService.getAppliedPolicies(repositoryId, ancestorId, null, null)) {
					if(enabledPolicies.contains(policy.getObjectType().getCmisId())) {
						policies.put(policy, ancestorObject);
					}
				}
			}			
			LOG.debug(" > getPolicies() ancestors {} ms", (System.currentTimeMillis() -t));
			
		}
		
		return policies;
	}
	
	
	private String getStringParameter(Object param) {
		if (param == null) {
			return null;
		}
		if (param instanceof String) {
			return (String) param;
		}
		if (param instanceof Holder<?>) {
			return getStringParameter(((Holder<?>) param).getValue());
		}
		throw new IllegalArgumentException("Parameter type not supported (only String or Hoder<String>)");
	}
	
	
	/**
	 * Return all the method names overwritten in the policies code
	 * @return
	 */
	private Set<String> getMethodsToIntercept() {
		Set<String> methodNames = new HashSet<String>();
		String repositoryId = security.getCallContextHolder().getRepositoryId();
		Set<String> enabledPolicies = repositoryService.getEnabledPolicies(repositoryId);
		
		for(String key: policyImpls.keySet()) {
			//only the enabled policies for the current repository
			if(enabledPolicies.contains(key)) {
				AbstractBasePolicy policy = policyImpls.get(key);
				methodNames.addAll(policy.getMethodsUsedByPolicy());
			}
		}
		
		return methodNames;
	}
}
