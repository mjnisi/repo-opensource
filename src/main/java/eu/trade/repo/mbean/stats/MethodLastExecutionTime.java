package eu.trade.repo.mbean.stats;

import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

@Component
@ManagedResource(objectName = MethodLastExecutionTime.MBEAN_NAME, description = MethodLastExecutionTime.MBEAN_DESCRIPTION)
public class MethodLastExecutionTime extends AbstractMethodStat<Long>{

	static final String MBEAN_NAME = "trade.repo:type=reports,name=MethodLastExecutionTime";
	static final String MBEAN_DESCRIPTION = "Shows the time of last execution for methods at the service layer";

	public MethodLastExecutionTime() {
		super(MethodLastExecutionTime.class.getName());
	}

	@Override
	public String getMBeanName() {
		return MethodLastExecutionTime.MBEAN_NAME;
	}

	@Override
	public String getMBeanDescription() {
		return MethodLastExecutionTime.MBEAN_DESCRIPTION;
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
		return getMethodStatsCollector().getMethodStats().get(attribute).getLastExecutionTime();
	}
}
