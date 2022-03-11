package eu.trade.repo.migration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.model.ObjectType;
import eu.trade.repo.model.ObjectTypeProperty;
import eu.trade.repo.model.Repository;
import eu.trade.repo.service.CMISBaseService;
import eu.trade.repo.service.interfaces.IRepositoryService;

@Transactional
public class CMIS11Migrate {
	
	private static final Logger LOG = LoggerFactory.getLogger(CMIS11Migrate.class);
	

	@Autowired
	private IRepositoryService repositoryService; 
	
	@PersistenceContext
	private EntityManager entityManager;
	
	public int migration() throws Exception {
		
		final Map<String, ObjectType> tplTypes = new HashMap<String, ObjectType>();
		final Repository repo = new Repository();
		repo.setCmisId("x");
		repo.setName("x");
		repo.setDescription("x");

		/*
		 * the methods that creates the types are protected,
		 * this little hack executes the code and saves the returned ObjectType
		 */
		new CMISBaseService() {
			{
				tplTypes.put("cmis:folder", createFolderType(repo));
				tplTypes.put("cmis:document", createDocumentType(repo));
				tplTypes.put("cmis:policy", createPolicyType(repo));
				tplTypes.put("cmis:relationship", createRelationshipType(repo));
				tplTypes.put("cmis:item", createItemType(repo));
				tplTypes.put("cmis:secondary", createSecondaryType(repo));
			}
		};
		
		List<Repository> repositories = repositoryService.getAllRepositories();
		for(Repository repository: repositories) {
			migrationCheck(repository, tplTypes, false);
		}
		
		
		LOG.info("=================================================");
		//second loop to check
		int totalErrors = 0;
		for(Repository repository: repositories) {
			totalErrors += migrationCheck(repository, tplTypes, true);
		}

		return totalErrors;
	}

	/**
	 * Checks the types defined in the repository and compares with CMIS 1.1 types
	 * 
	 * @param repository
	 */
	private int migrationCheck(Repository repository, Map<String, ObjectType> tplTypes, boolean dryRun) {
		int errors = 0;
		
		LOG.info(repository.getCmisId());
		LOG.info("-------------------------------------------------");
		Map<String, ObjectType> dbTypes = new HashMap<String, ObjectType>();
		for(ObjectType ot: repository.getObjectTypes()) {
			dbTypes.put(ot.getCmisId(), ot);
		}
		
		Set<String> missingTypes = new HashSet<String>(tplTypes.keySet());
		Set<String> existingTypes = new HashSet<String>(tplTypes.keySet());
		missingTypes.removeAll(dbTypes.keySet());
		existingTypes.retainAll(dbTypes.keySet());
		
		for(String cmisId: missingTypes) {
			LOG.info("> " + cmisId + " [missing]");
			errors++;
			
			if(!dryRun) {
				LOG.info(">>> adding " + cmisId );
				repository.addObjectType(tplTypes.get(cmisId));
			}
		}
		
		for(String cmisId: existingTypes) {
			LOG.info("> " + cmisId);

			Map<String, ObjectTypeProperty> tplProperties = tplTypes.get(cmisId).getObjectTypePropertiesMap();
			Map<String, ObjectTypeProperty> dbProperties = dbTypes.get(cmisId).getObjectTypePropertiesMap();

			//compare the type attributes
			ObjectType tpl = tplTypes.get(cmisId);
			ObjectType db = dbTypes.get(cmisId);
			int diffs = 0;
			diffs += (tpl.isCreatable().equals(db.isCreatable()) ? 0:1);
			diffs += (tpl.isFileable().equals(db.isFileable()) ? 0:1);
			diffs += (tpl.isQueryable().equals(db.isQueryable()) ? 0:1);
			if(diffs > 0) {
				LOG.info("+ " + cmisId + " has different attributes");
				errors++;
			}
			
			
			Set<String> missingProps = new HashSet<String>(tplProperties.keySet());
			missingProps.removeAll(dbProperties.keySet());
			if(!missingProps.isEmpty()) {
				LOG.info("  .. missing properties " + missingProps);
				errors++;
				
				if(!dryRun) {
					for(String propertyCmisId: missingProps) {
						LOG.info(">>> adding property " + propertyCmisId);
						dbTypes.get(cmisId).addObjectTypeProperty(tplProperties.get(propertyCmisId));
					}
					
				}
			}
			
			Set<String> existingProps = new HashSet<String>(tplProperties.keySet());
			existingProps.retainAll(dbProperties.keySet());
			
			for(String propertyCmisId: existingProps) {
				
				int diff = ObjectTypeProperty.FULL_COMPARATOR.compare(tplProperties.get(propertyCmisId), dbProperties.get(propertyCmisId));
				//id and objecttype are always going to be different
				if(diff > 2) {
					LOG.info("  .. " + propertyCmisId + " different");
					errors++;
					
					if(!dryRun) {
						LOG.info(">>> updating property " + propertyCmisId);
							ObjectTypeProperty tplp = tplProperties.get(propertyCmisId);
							ObjectTypeProperty dbp = dbProperties.get(propertyCmisId);
							
							dbp.setUpdatability(tplp.getUpdatability());
							dbp.setRequired(tplp.getRequired());
							dbp.setQueryable(tplp.getQueryable());
							dbp.setOrderable(tplp.getOrderable());
						
					}
				} 
				
			}
		}
		LOG.info("");
		
		
		if(!dryRun) {
			entityManager.merge(repository);
		}
		
		return errors;
	}

}
