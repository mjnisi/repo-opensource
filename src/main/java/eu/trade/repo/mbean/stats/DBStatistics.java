package eu.trade.repo.mbean.stats;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.ReflectionException;
import javax.management.RuntimeMBeanException;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenMBeanConstructorInfoSupport;
import javax.management.openmbean.OpenMBeanInfoSupport;
import javax.management.openmbean.OpenMBeanOperationInfoSupport;
import javax.management.openmbean.SimpleType;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.access.InvalidInvocationException;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@Component
@ManagedResource(objectName = DBStatistics.MBEAN_NAME, description = DBStatistics.MBEAN_DESCRIPTION)
public class DBStatistics implements DynamicMBean {

	public static final String MBEAN_NAME = "trade.repo:type=reports,name=DBStatistics";
	public static final String MBEAN_DESCRIPTION = "Shows the DB statistics";
	private static final String ACTIVE_DB_CONNECTIONS = "activeDBConnections";
	private static final String ACTIVE_DB_CONNECTIONS_INDEXTRANSIENT_READ_WRITE_DB = "activeDBConnectionsRWForIndexing";
	private static final String IDLE_DB_CONNECTIONS = "idleDBConnections";
	private static final String IDLE_DB_CONNECTIONS_INDEXTRANSIENT_READ_WRITE_DB = "idleDBConnectionsForRWIndexing";

	@Qualifier("dataSource")
	@Autowired
	private DataSource datasource;


	@Qualifier("dataSourceTransient")
	@Autowired
	private DataSource dataSourceTransient;

	private final OpenMBeanAttributeInfoSupport[] attributeDefinitions;

	public DBStatistics() {

		attributeDefinitions = new OpenMBeanAttributeInfoSupport[]{
				getMBeanInfoSupport(ACTIVE_DB_CONNECTIONS, "Number of activeDbConnections"),
				getMBeanInfoSupport(IDLE_DB_CONNECTIONS, "Number of idleDbConnections"),
				getMBeanInfoSupport(ACTIVE_DB_CONNECTIONS_INDEXTRANSIENT_READ_WRITE_DB, "Number of activeDbConnections for read/write index transient data source"),
				getMBeanInfoSupport(IDLE_DB_CONNECTIONS_INDEXTRANSIENT_READ_WRITE_DB, "Number of idleDbConnections for read/write index transient data source")
		};
	}

	private OpenMBeanAttributeInfoSupport getMBeanInfoSupport(String activeDbConnections, String description) {
		return new OpenMBeanAttributeInfoSupport(
				activeDbConnections,
				description,
				SimpleType.INTEGER,
				true, false, false);
	}

	@Override
	public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
		switch (attribute) {
		case ACTIVE_DB_CONNECTIONS:
			return ((BasicDataSource) datasource).getNumActive();
		case IDLE_DB_CONNECTIONS:
			return ((BasicDataSource) datasource).getNumIdle();
		case ACTIVE_DB_CONNECTIONS_INDEXTRANSIENT_READ_WRITE_DB:
			return ((BasicDataSource) dataSourceTransient).getNumActive();
		case IDLE_DB_CONNECTIONS_INDEXTRANSIENT_READ_WRITE_DB:
			return ((BasicDataSource) dataSourceTransient).getNumIdle();
		default:
			throw new InvalidInvocationException("Unknown attribute");
		}
	}

	@Override
	public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
		throw new UnsupportedOperationException("No attribute can be set in this MBean");
	}

	@Override
	public AttributeList getAttributes(String[] attributes) {
		AttributeList resultList = new AttributeList();
		for (String attribute : attributes) {
			try {
				resultList.add(new Attribute(attribute, getAttribute(attribute)));
			} catch (Exception e) {
				throw new RuntimeMBeanException(new RuntimeException(e));
			}
		}

		return resultList;
	}

	@Override
	public AttributeList setAttributes(AttributeList attributes) {
		throw new UnsupportedOperationException("No attribute can be set in this MBean");
	}

	@Override
	public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
		return new UnsupportedOperationException("No attribute can be set in this MBean");
	}

	@Override
	public MBeanInfo getMBeanInfo() {
		return new OpenMBeanInfoSupport(
				DBStatistics.class.getName(),
				MBEAN_DESCRIPTION,
				attributeDefinitions,
				new OpenMBeanConstructorInfoSupport[0],
				new OpenMBeanOperationInfoSupport[0],
				new MBeanNotificationInfo[0]);
	}
}
