package eu.trade.repo.mbean.stats;

import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

@Component
@ManagedResource(objectName = MethodTotalTime.MBEAN_NAME, description = MethodTotalTime.MBEAN_DESCRIPTION)
public class MethodTotalTime extends AbstractMethodStat<Long> {

	static final String MBEAN_NAME = "trade.repo:type=reports,name=MethodTotalTime";
	static final String MBEAN_DESCRIPTION = "Shows the total time methods of the service layer are executing";

	public MethodTotalTime(){
		super(MethodTotalTime.class.getName());
	}

	@Override
	public String getMBeanName() {
		return MethodTotalTime.MBEAN_NAME;
	}
	@Override
	public String getMBeanDescription() {
		return MethodTotalTime.MBEAN_DESCRIPTION;
	}

	@Override
	protected Long getDefaultValue() {
		return 0L;
	}

	@Override
	protected OpenType getValueType() {
		return SimpleType.DOUBLE;
	}

	@Override
	public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
		return getMethodStatsCollector().getMethodStats().get(attribute).getTotalTime();
	}
}
