package eu.europa.ec.digit.cmisrepo.test.performance;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import eu.europa.ec.digit.cmisrepo.test.AbstractCmisTestClient;
import eu.europa.ec.digit.cmisrepo.test.util.RepositoryUtil;

public class CreateDocsForAclPerformance extends AbstractCmisTestClient {

	public static void main(String[] args) throws Exception {
		run(true);
		run(false);
	}

	static void run(final boolean a_bWithAcl) throws Exception {
		Logger.getRootLogger().setLevel(Level.WARN);
		SimpleDateFormat cFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String cDateTime = cFormat.format(System.currentTimeMillis());

		String cRepoId = "repo_NO_ACL";
		if (a_bWithAcl)
			cRepoId = "repo_WITH_ACL";

		String cRepoName = "repoName_" + cDateTime;
		String cRepoDescription = "repoDesc_" + cDateTime;
		RepositoryUtil.createRepositoryLocalhost(HOST, "cmisrepo", NAME_USER_SYSADMIN, PSWD_USER_SYSADMIN, cRepoId, cRepoName,
				cRepoDescription);

		final Session cSessionAdmin = getSession(NAME_USER_ADMIN, PSWD_USER_ADMIN, BindingType.WEBSERVICES, cRepoId);
		String cRootFolderId = cSessionAdmin.getRepositoryInfo().getRootFolderId();
		final Folder cFolderRoot = cSessionAdmin.getRootFolder();

		if (a_bWithAcl) {
			Ace cAceFolderRootAdmin = new AccessControlEntryImpl(new AccessControlPrincipalDataImpl(NAME_USER_ADMIN), Arrays.asList("cmis:all"));
			Ace cAceFolderRootAnyoneAdd = new AccessControlEntryImpl(new AccessControlPrincipalDataImpl("cmis:anyone"), Arrays.asList("cmis:read"));
			Ace[] acAcesAdd = new Ace[] {cAceFolderRootAdmin, cAceFolderRootAnyoneAdd};

			Ace cAceFolderRootAnyoneRemove = new AccessControlEntryImpl(new AccessControlPrincipalDataImpl("cmis:anyone"), Arrays.asList("cmis:all"));

			cSessionAdmin.applyAcl(new ObjectIdImpl(cRootFolderId), Arrays.asList(acAcesAdd), Arrays.asList(cAceFolderRootAnyoneRemove), AclPropagation.PROPAGATE);
		}

		List<Thread> cLstThr = new LinkedList<>();
		final AtomicInteger cCnt = new AtomicInteger();

		final int nCnt = 8;

		for (int i = 0; i < nCnt; i++) {
			final int nId = i;
			Thread cThr = new Thread() {
				public void run() {
					try {
					    Map<String, String> cPropsFolder = new HashMap<>();
					    cPropsFolder.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
					    cPropsFolder.put(PropertyIds.NAME, "top_" + nId);
						Folder cFolderTop = cFolderRoot.createFolder(cPropsFolder);

						if (a_bWithAcl) {
							Ace cAceFolder = cSessionAdmin.getObjectFactory().createAce(NAME_USER_ADMIN, Arrays.asList("cmis:all"));
							cFolderRoot.addAcl(Arrays.asList(cAceFolder), AclPropagation.PROPAGATE);
						}

						for (int j = 0; j < nCnt; j++) {
						    cPropsFolder.put(PropertyIds.NAME, "sub_" + nId + "_" + j);
							Folder cFolderSub = cFolderTop.createFolder(cPropsFolder);

							if (a_bWithAcl) {
								Ace cAceFolder = cSessionAdmin.getObjectFactory().createAce(NAME_USER_TEST1, Arrays.asList("cmis:all"));
								cFolderSub.addAcl(Arrays.asList(cAceFolder), AclPropagation.PROPAGATE);
							}

							for (int m = 0; m < nCnt; m++) {
							    cPropsFolder.put(PropertyIds.NAME, "subsub_" + nId + "_" + j + "_" + m);
								Folder cFolderSubSub = cFolderSub.createFolder(cPropsFolder);

								if (a_bWithAcl) {
									List<Ace> cLstAces = new LinkedList<>();
									cLstAces.add(cSessionAdmin.getObjectFactory().createAce(NAME_USER_TEST1, Arrays.asList("cmis:all")));

									if (m % 3 == 0)
										cLstAces.add(cSessionAdmin.getObjectFactory().createAce(NAME_USER_TEST2, Arrays.asList("cmis:all")));

									cFolderSubSub.addAcl(cLstAces, AclPropagation.PROPAGATE);
								}

								for (int n = 0; n < nCnt; n++) {
									ObjectId cDocId = createPdfDocument(cSessionAdmin, cFolderSubSub, "doc_" + nId + "_" + j + "_" + m + "_" + n, 3);
									Document cDoc = (Document) cSessionAdmin.getObject(cDocId);

									if (a_bWithAcl) {
										Ace cAceDocUser1 = cSessionAdmin.getObjectFactory().createAce(NAME_USER_TEST1, Arrays.asList("cmis:all"));
										cDoc.addAcl(Arrays.asList(cAceDocUser1), AclPropagation.OBJECTONLY);

										if (n % 3 == 0) {
											Ace cAceDocUser2 = cSessionAdmin.getObjectFactory().createAce(NAME_USER_TEST2, Arrays.asList("cmis:all"));
											cDoc.addAcl(Arrays.asList(cAceDocUser2), AclPropagation.OBJECTONLY);
										}
									}

									if (cCnt.incrementAndGet() % 20 == 0)
										System.out.println(cCnt.get());
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			cLstThr.add(cThr);
			cThr.start();
		}

		for (Thread cThr : cLstThr) {
			cThr.join();
		}

		System.out.println("Done " + (a_bWithAcl ? "WITH" : "WITHOUT"));
	}
}