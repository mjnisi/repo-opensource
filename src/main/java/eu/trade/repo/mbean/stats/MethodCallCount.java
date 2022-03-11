package eu.trade.repo.mbean.stats;

import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

@Component
@ManagedResource(objectName = MethodCallCount.MBEAN_NAME, description = MethodCallCount.MBEAN_DESCRIPTION)
public class MethodCallCount extends AbstractMethodStat<Long>{

	static final String MBEAN_NAME = "trade.repo:type=reports,name=MethodCallCount";
	static final String MBEAN_DESCRIPTION = "Shows the number of calls for methods at the service layer";

	public MethodCallCount() {
		super(MethodCallCount.class.getName());
	}
	
	@Override
	public String getMBeanName() {
		return MethodCallCount.MBEAN_NAME;
	}

	@Override
	public String getMBeanDescription() {
		return MethodCallCount.MBEAN_DESCRIPTION;
	}

	@Override
	protected Long getDefaultValue() {
		return 0L;
	}

	@Override
	protected OpenType getValueType() {
		return SimpleType.LONG;
	}

	@Override
	public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
		return getMethodStatsCollector().getMethodStats().get(attribute).getCount();
	}
}
