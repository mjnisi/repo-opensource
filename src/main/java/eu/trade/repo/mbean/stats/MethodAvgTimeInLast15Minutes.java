package eu.trade.repo.mbean.stats;

import eu.trade.repo.util.Constants;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

@Component
@ManagedResource(objectName = MethodAvgTimeInLast15Minutes.MBEAN_NAME, description = MethodAvgTimeInLast15Minutes.MBEAN_DESCRIPTION)
public class MethodAvgTimeInLast15Minutes extends AbstractMethodStat<Long>{

	static final String MBEAN_NAME = "trade.repo:type=reports,name=MethodAvgTimeInLast15Minutes";
	static final String MBEAN_DESCRIPTION = "Shows the average time of execution for methods at the service layer in last 15 minutes";

	public MethodAvgTimeInLast15Minutes() {
		super(MethodAvgTimeInLast15Minutes.class.getName());
	}

	@Override
	public String getMBeanName() {
		return MethodAvgTimeInLast15Minutes.MBEAN_NAME;
	}

	@Override
	public String getMBeanDescription() {
		return MethodAvgTimeInLast15Minutes.MBEAN_DESCRIPTION;
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
		return getMethodStatsCollector().getMethodStats().get(attribute).getAvgInvocationTimeInSpecifiedPeriod(Constants.FIFTEEN_MINUTES_IN_MILLIS, System.currentTimeMillis());
	}
}
