package eu.trade.it;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;
import org.junit.Assert;

public class RemoveInherited extends AbstractSessionTest {

	/**
	 * @see org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest#run(org.apache.chemistry.opencmis.client.api.Session)
	 */
	@Override
	public void run(Session session) throws Exception {
		// connect to the repository as admin
		Map<String, Object> folderProperties = new HashMap<String, Object>();
		folderProperties.put(PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_FOLDER.value());
		folderProperties.put(PropertyIds.NAME, "it-test" + System.currentTimeMillis() + session.getRepositoryInfo().hashCode());

		// create folder under root folder
		Folder folder = session.getRootFolder().createFolder(folderProperties);

		// add direct permission
		// remove inherited permission
		Acl acl = session.getAcl(folder, false);
		List<Ace> inheritedAces = new ArrayList<>();
		for (Ace ace : acl.getAces()) {
			if (!ace.isDirect()) {
				inheritedAces.add(ace);
			}
		}
		AccessControlEntryImpl addAce = new AccessControlEntryImpl(new AccessControlPrincipalDataImpl("cmis:user"), Arrays.asList("cmis:all"));
		addAce.setDirect(true);

		//folder.applyAcl(Collections.singletonList((Ace) addAce), inheritedAces, AclPropagation.OBJECTONLY);

		acl = session.getBinding().getAclService().applyAcl(session.getRepositoryInfo().getId(), folder.getId(),
				new AccessControlListImpl(Collections.singletonList((Ace) addAce)),
				new AccessControlListImpl(inheritedAces),
				AclPropagation.OBJECTONLY, null);


		acl = session.getAcl(folder, false);
		
		
		int countDirect = 0;
		int countNonDirect = 0;
		for (Ace ace : acl.getAces()) {
			if (!ace.isDirect()) {
				countNonDirect++;
			}
			else {
				countDirect++;
			}
		}
		
		folder.delete();//removes the folder before finishing

		Assert.assertEquals("Some inherited permissions are still present", 0, countNonDirect);
		Assert.assertEquals("More than one direct permission: " + acl, 1, countDirect);
	}

}
