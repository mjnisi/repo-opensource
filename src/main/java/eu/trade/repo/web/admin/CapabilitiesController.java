package eu.trade.repo.web.admin;


import static eu.trade.repo.util.Constants.*;

import java.util.List;

import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.CapabilityChanges;
import org.apache.chemistry.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.enums.CapabilityRenditions;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.trade.repo.model.Repository;

@Controller
@Secured(CHANGE_REPO_CAPABILITIES)
public class CapabilitiesController extends AdminController{

	private static final Logger LOGGER = LoggerFactory.getLogger(CapabilitiesController.class);
	public static final String UPDATE_OK = "ok";
	public static final String UPDATE_ERROR = "error";

	@RequestMapping(value = "/capabilities/{repoId}")
	public String capabilities(Model model,
			@PathVariable String repoId,
			@RequestParam(value = "status", required = false) String updateStatus) {
		List<Repository> allRepositories = repositoryService.getAllRepositories();

		Repository repository = getSelectedRepository(repoId, allRepositories);
		setAdminPageAttributes(model, allRepositories, generateRepositoryTitle(repository), repoId, null);
		model.addAttribute("status", updateStatus);
		model.addAttribute("repository", repository);
		model.addAttribute("contentStreamUpdatesCapabilities", CapabilityContentStreamUpdates.values());
		model.addAttribute("changesCapabilities", CapabilityChanges.values());
		model.addAttribute("renditionsCapabilities", CapabilityRenditions.values());
		model.addAttribute("queryCapabilities", CapabilityQuery.values());
		model.addAttribute("joinCapabilities", CapabilityJoin.values());
		model.addAttribute("aclCapabilities", CapabilityAcl.values());
		model.addAttribute("aclPropagationCapabilities", AclPropagation.values());
		
		//if the version specific filing is disabled, show the disaggregated documents
		if(!repository.getVersionSpecificFiling()) {
			List<String> versionSeriesDisaggregated = repositoryService.getDisaggregatedVersionSeries(repoId);
			model.addAttribute("versionSeriesDisaggregated", versionSeriesDisaggregated);
		}
		
		return "capabilities";
	}

	@RequestMapping(value = "/updateCapabilities", method = RequestMethod.POST)
	public String updateCapabilities(Model model, @RequestParam(value = "repoId") String repoId,
			@RequestParam(value = "getDescendants", required = false) Object getDescendants,
			@RequestParam(value = "getFolderTree", required = false) Object getFolderTree,
			@RequestParam(value = "contentStreamUpdatability") String contentStreamUpdatability,
			@RequestParam(value = "changes") String changes,
			@RequestParam(value = "renditions") String renditions,
			@RequestParam(value = "multifiling", required = false) Object multifiling,
			@RequestParam(value = "unfiling", required = false) Object unfiling,
			@RequestParam(value = "versionSpecificFiling", required = false) Object versionSpecificFiling,
			@RequestParam(value = "pwcUpdatable", required = false) Object pwcUpdatable,
			@RequestParam(value = "pwcSearchable", required = false) Object pwcSearchable,
			@RequestParam(value = "allVersionsSearchable", required = false) Object allVersionsSearchable,
			@RequestParam(value = "query") String query,
			@RequestParam(value = "join") String join,
			@RequestParam(value = "acl") String acl,
			@RequestParam(value = "aclPropagation") String aclPropagation
			) {
		String updateStatus = UPDATE_OK;
		try {
			Repository repository = repositoryService.getRepositoryById(repoId);

			repository.setGetDescendants(getDescendants != null);
			repository.setGetFolderTree(getFolderTree != null);
			repository.setContentStreamUpdatability(CapabilityContentStreamUpdates.fromValue(contentStreamUpdatability));
			repository.setChanges(CapabilityChanges.fromValue(changes));
			repository.setRenditions(CapabilityRenditions.fromValue(renditions));
			repository.setMultifiling(multifiling != null);
			repository.setUnfiling(unfiling != null);
			repository.setVersionSpecificFiling(versionSpecificFiling != null);
			repository.setPwcUpdatable(pwcUpdatable != null);
			repository.setPwcSearchable(pwcSearchable != null);
			repository.setAllVersionsSearchable(allVersionsSearchable != null);
			repository.setQuery(CapabilityQuery.fromValue(query));
			repository.setJoin(CapabilityJoin.fromValue(join));
			repository.setAcl(CapabilityAcl.fromValue(acl));
			if (CapabilityAcl.NONE.equals(CapabilityAcl.fromValue(acl))) {
				repository.setAclPropagation(null);
			}else{
				repository.setAclPropagation(AclPropagation.fromValue(aclPropagation));
			}

			repositoryService.update(repository);
		} catch (CmisBaseException | DataAccessException e) {
			LOGGER.error("Error occurred when updating repository", e);
			updateStatus = UPDATE_ERROR;
		}

		return "redirect:/admin/capabilities/" + repoId + "?status=" + updateStatus;
	}
}
