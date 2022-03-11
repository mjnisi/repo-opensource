package eu.trade.repo.web.admin;

import static eu.trade.repo.util.Constants.VIEW_REPO_SUMMARY;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableInt;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import eu.trade.repo.model.IndexingState;
import eu.trade.repo.model.Repository;
import eu.trade.repo.web.admin.bean.IndexStatusSummary;

@Controller
@Secured(VIEW_REPO_SUMMARY)
public class StatusPageController extends AdminController {

	public static final int INDEX_STATE = 0;
	public static final int DOCUMENT_COUNT = 1;

	@RequestMapping(value = "/summary/{repoId}")
	public String displayDocuments(Model model, @PathVariable String repoId) {
		setObjectStatus(model, repoId);
		setIndexingStatusContent(model, repoId);
		setIndexingStatusMetadata(model, repoId);
		return "statusPage";
	}

	private void setObjectStatus(Model model, String repoId) {
		List<Repository> allRepositories = repositoryService.getAllRepositories();
		setAdminPageAttributes(model, allRepositories, generateRepositoryTitle(getSelectedRepository(repoId, allRepositories)), repoId, null);
		List objectsCountForRepository = objectService.getObjectsCountForRepository(repoId);
		model.addAttribute("objectCount", objectsCountForRepository);
	}

	private void setIndexingStatusContent(Model model, String repoId) {
		LinkedHashMap<String, IndexStatusSummary> result = new LinkedHashMap<String, IndexStatusSummary>();
		MutableInt totalNumberOfDocuments = new MutableInt(0);
		
		//objectsWaitingForIndexing -> it is special case where several conditions must be met
		long numberOfObjectsWaitingForIndexing = objectService.getNumberOfObjectsWaitingForContentIndexing(repoId);
		totalNumberOfDocuments.add(BigInteger.valueOf(numberOfObjectsWaitingForIndexing));
		result.put(IndexingState.NONE.name(), new IndexStatusSummary(IndexingState.NONE.getState(), IndexingState.NONE.name(), IndexingState.NONE.getDescription(), numberOfObjectsWaitingForIndexing));
		
		List<Object[]> objectsCountWithIndexStatus = (List<Object[]>) objectService.getObjectsCountWithContentIndexStatus(repoId);
		result.putAll(obtainResultMap(objectsCountWithIndexStatus, totalNumberOfDocuments));
		
		model.addAttribute("indexStatus", result);
		model.addAttribute("totalNumberOfDocuments", totalNumberOfDocuments.longValue());
	
	}
	
	
	private void setIndexingStatusMetadata(Model model, String repoId) {
		MutableInt totalNumberOfObjects = new MutableInt(0);
		List<Object[]> objectsCountWithIndexStatus = (List<Object[]>) objectService.getObjectsCountWithMetadataIndexStatus(repoId);
		LinkedHashMap<String, IndexStatusSummary> indexingStatus = obtainResultMap(objectsCountWithIndexStatus, totalNumberOfObjects);
		
		model.addAttribute("indexStatusMetadata", indexingStatus);
		model.addAttribute("totalNumberOfObjects", totalNumberOfObjects.longValue());
	}

	
	private LinkedHashMap<String, IndexStatusSummary> obtainResultMap(List<Object[]> objectsCountWithIndexStatus, MutableInt totalNumberOfObjects){
		Collections.sort(objectsCountWithIndexStatus, new IndexStatusComparator());
		LinkedHashMap<String, IndexStatusSummary> result = new LinkedHashMap<String, IndexStatusSummary>();
		IndexingState indexingState = null;
		long objectCount = 0;
		for (Object[] indexStatus : objectsCountWithIndexStatus) {
			indexingState = IndexingState.get((Integer) indexStatus[INDEX_STATE]);
			if ( null != indexingState ) {
				objectCount = (Long) indexStatus[DOCUMENT_COUNT];
				totalNumberOfObjects.add(BigInteger.valueOf(objectCount));
				result.put(indexingState.name(), new IndexStatusSummary(indexingState.getState(), indexingState.name(), indexingState.getDescription(), objectCount));
			}
		}
		return result;
	}
	
	private class IndexStatusComparator implements Comparator<Object[]>{

		@Override
		public int compare(Object[] o1, Object[] o2) {
			return ((Integer)o1[INDEX_STATE]).compareTo((Integer)o2[INDEX_STATE]);
		}
	}
}
