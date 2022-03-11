package eu.trade.repo.mbean.stats;

import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

@Component
@ManagedResource(objectName = MethodMaxTime.MBEAN_NAME, description = MethodMaxTime.MBEAN_DESCRIPTION)
public class MethodMaxTime extends AbstractMethodStat<Long>{

	static final String MBEAN_NAME = "trade.repo:type=reports,name=MethodMaxTime";
	static final String MBEAN_DESCRIPTION = "Shows maximum time of execution for methods at the service layer";
	
	public MethodMaxTime() {
		super(MethodMaxTime.class.getName());
	}
	
	@Override
	public String getMBeanName() {
		return MethodMaxTime.MBEAN_NAME;
	}

	@Override
	public String getMBeanDescription() {
		return MethodMaxTime.MBEAN_DESCRIPTION;
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
		return getMethodStatsCollector().getMethodStats().get(attribute).getMaxTime();
	}

}
