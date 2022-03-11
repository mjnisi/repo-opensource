package eu.trade.repo.mbean.stats;

import javax.management.*;

public abstract class NoAttributeHolder {

	public static final String MESSAGE = "No attribute can be set in this MBean";

	public void setAttribute(Attribute attribute)
			throws AttributeNotFoundException, InvalidAttributeValueException,
			MBeanException, ReflectionException {

		throw new AttributeNotFoundException(MESSAGE);

	}

	protected void validateAttributes(String[] attributes) {
		if (attributes == null) {
			throw new RuntimeOperationsException(
					new IllegalArgumentException("attributeNames[] cannot be null"),
					"Cannot call getAttributes with null attribute names");
		}
	}
}
