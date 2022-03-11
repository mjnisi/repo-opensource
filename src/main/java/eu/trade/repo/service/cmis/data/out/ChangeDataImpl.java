package eu.trade.repo.service.cmis.data.out;

import java.util.GregorianCalendar;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ChangeEventInfo;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.enums.ChangeType;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ChangeEventInfoDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;

import eu.trade.repo.model.ChangeEvent;

/**
 * {@link ObjectData} implementation.
 * 
 * @author azaridi
 */
public class ChangeDataImpl extends ObjectDataImpl {
	
	private ChangeEvent event = null;
	private ChangeEventInfo changeEventInfo = null;
	private final PropertiesImpl properties;
	
	public ChangeDataImpl(final ChangeEvent event) {
		this.event = event;
		final GregorianCalendar date = new GregorianCalendar();
		date.setTime(event.getChangeTime());

		changeEventInfo = new ChangeEventInfoDataImpl() {
			public ChangeType getChangeType() {	return event.getChangeType();	}
			public GregorianCalendar getChangeTime() {	return date;	}
		};
		
		properties = new PropertiesImpl();
		properties.addProperty(new PropertyIdImpl(PropertyIds.OBJECT_ID, event.getObjectId()));
	}
	
	@Override
	public ChangeEventInfo getChangeEventInfo() {
		return changeEventInfo;
	}

	@Override
	public String getId() {
		return event.getObjectId();
	}

	@Override
	public Properties getProperties() {
		return properties;
	}
}
