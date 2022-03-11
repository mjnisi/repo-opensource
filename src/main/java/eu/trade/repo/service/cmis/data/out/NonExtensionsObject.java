package eu.trade.repo.service.cmis.data.out;

import java.util.List;

import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;

public abstract class NonExtensionsObject implements ExtensionsData {

	/**
	 * No extensions defined. 
	 * 
	 * @see org.apache.chemistry.opencmis.commons.data.ExtensionsData#getExtensions()
	 */
	@Override
	public final List<CmisExtensionElement> getExtensions() {
		return null;
	}

	/**
	 * No extensions defined. 
	 * 
	 * @see org.apache.chemistry.opencmis.commons.data.ExtensionsData#setExtensions(java.util.List)
	 */
	@Override
	public final void setExtensions(List<CmisExtensionElement> extensions) {
	}
}
