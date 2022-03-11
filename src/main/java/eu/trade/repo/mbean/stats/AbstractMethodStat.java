package eu.trade.repo.mbean.stats;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.DynamicMBean;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenMBeanConstructorInfoSupport;
import javax.management.openmbean.OpenMBeanInfoSupport;
import javax.management.openmbean.OpenMBeanOperationInfoSupport;
import javax.management.openmbean.OpenType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import eu.trade.repo.stats.MethodStatsInfo;
import eu.trade.repo.stats.RepoStatsCollector;


public abstract class AbstractMethodStat<T> extends NoAttributeHolder implements DynamicMBean {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractMethodStat.class);

	private final String mBeanClassName;
	private Map<String, OpenMBeanAttributeInfoSupport> attributeMap;

	@Autowired
	private MBeanServer mBeanServer;
	private RepoStatsCollector methodStatsCollector;


	public AbstractMethodStat(String mBeanClassName){
		this.mBeanClassName = mBeanClassName;
		this.attributeMap = new HashMap<String, OpenMBeanAttributeInfoSupport>();
	}

	public abstract String getMBeanName();
	public abstract String getMBeanDescription();
	//FOR ATTRIBUTE BUILDING
	protected abstract T getDefaultValue();
	protected abstract OpenType getValueType();

	public Map<String, OpenMBeanAttributeInfoSupport> getAttributeMap() {
		return attributeMap;
	}

	public void setAttributeMap(
			Map<String, OpenMBeanAttributeInfoSupport> attributeMap) {
		this.attributeMap = attributeMap;
	}

	public RepoStatsCollector getMethodStatsCollector() {
		return methodStatsCollector;
	}

	public void setMethodStatsCollector(RepoStatsCollector methodStatsCollector) {
		this.methodStatsCollector = methodStatsCollector;
	}

	@Autowired
	protected void setMethodStatsRecorder(RepoStatsCollector methodStatsRecorder) {
		this.methodStatsCollector = methodStatsRecorder;
	}

	protected Map<String, T> getAttributeValueMap() {
		Map<String, T> valueMap = new HashMap<>();
		Collection<OpenMBeanAttributeInfoSupport> attributeInfoSupportCollection = attributeMap.values();
		for (OpenMBeanAttributeInfoSupport attributeInfoSupport : attributeInfoSupportCollection) {
			try {
				valueMap.put(attributeInfoSupport.getName(), (T) getAttribute(attributeInfoSupport.getName()));
			} catch (Exception e) {
				throw new RuntimeMBeanException(new RuntimeException(e));
			}
		}
		return valueMap;
	}

	private OpenMBeanInfoSupport buildMBeanInfo() throws MalformedObjectNameException, InstanceNotFoundException, ReflectionException {
		return new OpenMBeanInfoSupport(
				mBeanClassName,
				getMBeanDescription(), 
				getAttributeDefinitions(),
				new OpenMBeanConstructorInfoSupport[0],
				new OpenMBeanOperationInfoSupport[0],  
				new MBeanNotificationInfo[0]);
	}

	protected OpenMBeanAttributeInfoSupport[] getAttributeDefinitions() throws MalformedObjectNameException, InstanceNotFoundException, ReflectionException {
		if (methodStatsCollector == null) {
			return new OpenMBeanAttributeInfoSupport[0];
		}
		Collection<MethodStatsInfo> values = methodStatsCollector.getMethodStats().values();
		for (MethodStatsInfo info : values) {
			if (null == attributeMap.get(info.getMethodName())) {

				OpenMBeanAttributeInfoSupport attr = new OpenMBeanAttributeInfoSupport(
						info.getMethodName(),
						info.getMethodName() + getMBeanDescription(),
						getValueType(),
						true, false, false);
				attributeMap.put(info.getMethodName(), attr);
			}
		}
		if (!attributeMap.isEmpty()) {
			ObjectName objName = new ObjectName(getMBeanName());
			mBeanServer.setAttributes(objName, getAttributes(attributeMap.keySet().toArray(new String[attributeMap.size()])));
		}
		Collection<OpenMBeanAttributeInfoSupport> attributeInfoSupports = attributeMap.values();
		return attributeInfoSupports.toArray(new OpenMBeanAttributeInfoSupport[attributeInfoSupports.size()]);
	}

	@Override
	public AttributeList getAttributes(String[] attributes) {
		validateAttributes(attributes);
		AttributeList resultList = new AttributeList();

		for (String attribute : attributes) {
			try {
				Object value = getAttribute(attribute);
				resultList.add(new Attribute(attribute, value));
			} catch (Exception e) {
				LOG.error(e.getLocalizedMessage(), e);
			}
		}
		return (resultList);
	}

	@Override
	public AttributeList setAttributes(AttributeList attributes) {
		return new AttributeList();
	}

	@Override
	public Object invoke(String actionName, Object[] params, String[] signature)
			throws MBeanException, ReflectionException {

		throw new RuntimeOperationsException(new IllegalArgumentException("No actions defined for this bean"), 
				"No actions defined for this bean");
	}

	@Override
	public MBeanInfo getMBeanInfo() {
		try {
			return buildMBeanInfo();
		} catch (MalformedObjectNameException | ReflectionException |InstanceNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
