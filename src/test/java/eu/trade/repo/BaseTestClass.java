package eu.trade.repo;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import net.sf.ehcache.CacheManager;

import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.apache.chemistry.opencmis.commons.spi.AclService;
import org.apache.chemistry.opencmis.commons.spi.DiscoveryService;
import org.apache.chemistry.opencmis.commons.spi.NavigationService;
import org.apache.chemistry.opencmis.commons.spi.ObjectService;
import org.apache.chemistry.opencmis.commons.spi.PolicyService;
import org.apache.chemistry.opencmis.commons.spi.RelationshipService;
import org.dbunit.Assertion;
import org.dbunit.DataSourceDatabaseTester;
import org.dbunit.assertion.DefaultFailureHandler;
import org.dbunit.assertion.FailureHandler;
import org.dbunit.database.AmbiguousTableNameException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.SortedTable;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.trade.repo.model.ObjectTypeProperty;
import eu.trade.repo.model.Property;
import eu.trade.repo.query.Query;
import eu.trade.repo.search.codecs.CMISPropertyTypeCodecUtil;
import eu.trade.repo.security.CallContextHolder;
import eu.trade.repo.selectors.AclTestSelector;
import eu.trade.repo.selectors.CMISObjectSelector;
import eu.trade.repo.selectors.ChangeEventSelector;
import eu.trade.repo.selectors.ObjectTypePropertySelector;
import eu.trade.repo.selectors.ObjectTypeRelationshipSelector;
import eu.trade.repo.selectors.ObjectTypeSelector;
import eu.trade.repo.selectors.PermissionMappingSelector;
import eu.trade.repo.selectors.PermissionSelector;
import eu.trade.repo.selectors.PropertySelector;
import eu.trade.repo.selectors.RenditionSelector;
import eu.trade.repo.selectors.RepositorySelector;
import eu.trade.repo.service.interfaces.IAclService;
import eu.trade.repo.service.interfaces.ICMISBaseService;
import eu.trade.repo.service.interfaces.IDiscoveryService;
import eu.trade.repo.service.interfaces.INavigationService;
import eu.trade.repo.service.interfaces.IObjectService;
import eu.trade.repo.service.interfaces.IRepositoryService;
import eu.trade.repo.service.interfaces.IVersioningService;
import eu.trade.repo.test.util.CallContextImpl;
import eu.trade.repo.util.Cleanable;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/testApplicationContext.xml")
public class BaseTestClass {
	private static final Logger LOG = LoggerFactory.getLogger(BaseTestClass.class);

	@Autowired @Qualifier("testConnectionConfig")
	private TestConnectionConfig dbunitProperties;

	@Autowired
	protected AclService cmisAclService;
	@Autowired
	protected ObjectService cmisObjectService;
	@Autowired
	protected DiscoveryService cmisDiscoveryService;
	@Autowired
	protected PolicyService cmisPolicyService;
	@Autowired
	protected RelationshipService cmisRelationshipService;
	@Autowired
	protected NavigationService cmisNavigationService;

	@Autowired
	protected PropertySelector propertySelector;
	@Autowired
	protected AclTestSelector aclSelector;
	@Autowired
	protected CMISObjectSelector cmisObjectSelector;
	@Autowired
	protected ObjectTypePropertySelector objTypePropSelector;
	@Autowired
	protected ObjectTypeRelationshipSelector objTypeRelSelector;
	@Autowired
	protected ObjectTypeSelector objTypeSelector;
	@Autowired
	protected PermissionMappingSelector permMappingSelector;
	@Autowired
	protected PermissionSelector permSelector;
	@Autowired
	protected RenditionSelector renditionSelector;
	@Autowired
	protected RepositorySelector repoSelector;

	@Autowired @Qualifier("mockRepositoryService")
	protected IRepositoryService repositoryService;

	@Autowired @Qualifier("mockObjectService")
	protected IObjectService objectService;
	@Autowired
	protected ICMISBaseService utilService;
	@Autowired
	protected IAclService aclService;
	@Autowired
	protected INavigationService navigationService;
	@Autowired
	protected IDiscoveryService discoveryService;
	@Autowired @Qualifier("mockVersioningService")
	protected IVersioningService versioningService;

	@Autowired
	protected Query query;

	@Autowired
	private DataSource dataSource;

	// Cleans application scope caches
	@Autowired
	private Cleanable testCleaner;

	@Autowired
	protected CallContextHolder callContextHolder;

	@Autowired
	protected ChangeEventSelector changeEventSelector;

	private final CallContextImpl callContext = new CallContextImpl();

	/**
	 * Inits the default test user before every test mehtod execution.
	 * <p>
	 * Needs to be call after the some repository has been created. By default after set scenario.
	 * 
	 * @see eu.trade.repo.BaseTestClass#setUser(String, String, String)
	 */
	@Before
	public void initUser() {
		setUser(TestConstants.TEST_USER, TestConstants.TEST_PWD, TestConstants.TEST_REPO_1);
	}

	/**
	 * Cleans the threadLocal caches after the test method execution. It also can be invoke manually.
	 */
	@After
	public void clean() {
		LOG.info("TEST cleaning.");
		testCleaner.clean();
	}

	/**
	 * Sets the current thread user in the context.
	 * <p>
	 * This will fail if the user is not authenticated.
	 * 
	 * @param username
	 * @param password
	 * @param repositoryId
	 */
	protected void setUser(String username, String password, String repositoryId) {
		callContext.setUsername(username);
		callContext.setPassword(password);
		callContext.setRepositoryId(repositoryId);
		callContextHolder.initContext(callContext);
	}

	/**
	 * Cleans the cached values (only thread/request scope) and inits the default user.
	 * <p>
	 * Used in case your test is calling more than one service method supposed to be called in different requests.
	 */
	protected void restart() {
		LOG.info("TEST restarting.");
		initUser();
	}
	/**
	 * Returns the root folder id of the specified repository
	 * 
	 * @param repositoryId The repository id.
	 * @return {@link String} The root folder id of the specified repository
	 */
	protected String getRootFolderId(String repositoryId) {
		return repositoryService.getRootFolderId(repositoryId);
	}


	/**
	 * Compares the database and the expected result set
	 * 
	 * IMPORTANT: The comparation is only taking care the columns defined in the
	 * result file.
	 * 
	 * @param tableName name of the table to compare
	 * @param resultFile expected results (file under /dbunit/results/)
	 * @throws Exception
	 */
	protected void compareTable(String tableName, String resultFile) throws Exception {
		compareTable(tableName, null, resultFile);
	}

	/**
	 * Compares the database and the expected result set.
	 * 
	 * IMPORTANT: The comparation is only taking care the columns defined in the
	 * result file.
	 * 
	 * @param tableName name of the table to compare
	 * @param dbFilter filter to apply to the database
	 * @param resultFile expected results (file under /dbunit/results/)
	 * @throws Exception
	 */
	protected void compareTable(String tableName, String dbFilter, String resultFile)
			throws Exception {
		compareTable(tableName, dbFilter, resultFile, null);
	}
	protected void compareTable(String tableName, String dbFilter, String resultFile, String joinPart)
			throws Exception {
		IDatabaseConnection con = null;

		try {
			con = getConnection();
			// Fetch database data after executing your code
			ITable actualTable = null;

			if( null != joinPart ){
				PreparedStatement st = con.getConnection().prepareStatement("select * from " + joinPart +
						(dbFilter != null ? " where " + dbFilter : ""));
				actualTable = con.createTable(tableName, st);
			}else{
				actualTable = con.createQueryTable(tableName,
						"select * from " + tableName +
						(dbFilter != null ? " where " + dbFilter : ""));
			}
			// Load expected data from an XML dataset
			IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(this
					.getClass()
					.getResourceAsStream("/dbunit/results/" + resultFile));
			ITable expectedTable = expectedDataSet.getTable(tableName);

			// filter to use the results columns, not using IDs
			ITable filteredTable = DefaultColumnFilter.includedColumnsTable(
					actualTable, expectedTable.getTableMetaData().getColumns());

			//sort the results before comparing
			final ITable sortedExpected = new SortedTable(expectedTable, expectedTable.getTableMetaData().getColumns());
			final ITable sortedFiltered = new SortedTable(filteredTable, filteredTable.getTableMetaData().getColumns());
			
			FailureHandler fh = new DefaultFailureHandler() {

				public Error createFailure(String message, String expected, String actual) {
					//in case of difference print the both tables to compare easier. 
					try {
						System.out.println("DB---------------------------------------------");
						FlatXmlDataSet.write(new DefaultDataSet(sortedFiltered), System.out);

						System.out.println("File-------------------------------------------");
						FlatXmlDataSet.write(new DefaultDataSet(sortedExpected), System.out);
					} catch (AmbiguousTableNameException e) {
						e.printStackTrace();
					} catch (DataSetException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					return super.createFailure(message, expected, actual);
				}

			};
			
			// Assert actual database table match expected table
			Assertion.assertEquals(sortedExpected, sortedFiltered, fh);
			
		} finally {
			close(con);
		}
	}


	protected void setScenario(DatabaseOperation op, String ... scenarios) throws Exception {

		IDataSet dataSet = prepareDataSet(scenarios);

		dataSet = replaceDataSet(dataSet);

		IDatabaseConnection con = getConnection();

		executeDbOperation(con, op, dataSet);
	}

	/**
	 * By default replace "[current_timestamp]" by the current timestamp. Override when needed to add or modify the datasets replacements.
	 * 
	 * @param dataSet
	 * @return
	 */
	protected IDataSet replaceDataSet(IDataSet dataSet)  {
		ReplacementDataSet rDataSet = new ReplacementDataSet(dataSet);
		rDataSet.addReplacementObject("[current_timestamp]", new Timestamp(System.currentTimeMillis()));
		rDataSet.addReplacementObject("[current_cmisdate]", CMISPropertyTypeCodecUtil.codecFor(PropertyType.DATETIME).encode(new Date()));
		return rDataSet;
	}

	protected IDataSet prepareDataSet(String ... scenarios) throws DataSetException {
		//invalidating all the cache
		for(CacheManager manager: CacheManager.ALL_CACHE_MANAGERS) {
			manager.clearAll();
		}

		FlatXmlDataSetBuilder flatXmlDataSetBuilder = new FlatXmlDataSetBuilder();
		flatXmlDataSetBuilder.setColumnSensing(true);
		flatXmlDataSetBuilder.setCaseSensitiveTableNames(false);

		List<IDataSet> dataSets = new ArrayList<IDataSet>();
		for(String scenario : scenarios) {
			dataSets.add(flatXmlDataSetBuilder.build(this.getClass().getResourceAsStream("/dbunit/" + scenario)));
		}
		return new CompositeDataSet(dataSets.toArray(new IDataSet[dataSets.size()]));
	}

	protected void executeDbOperation(IDatabaseConnection con, DatabaseOperation op, IDataSet dataSet) throws Exception{
		try {
			op.execute(con, dataSet);
			con.getConnection().commit();
		} finally {
			close(con);
		}
	}


	/**
	 * Set up the scenario
	 * 
	 * @param scenario dataset file (under /dbunit folder)
	 * @param op dbunit operation type
	 * @throws Exception
	 */
	protected void setScenario(String scenario, DatabaseOperation op) throws Exception {
		setScenario(op, scenario);
	}

	protected void close(IDatabaseConnection con) {
		if (con != null) {
			try {
				con.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Get a DBUnit connection.
	 * 
	 * @return
	 * @throws Exception
	 */
	protected IDatabaseConnection getConnection() throws Exception {
		return getConnection(dbunitProperties, dataSource);
	}

	protected IDatabaseConnection getConnection(TestConnectionConfig testConfig, DataSource dataSource) throws Exception {
		String schema = getDbProperty(testConfig, TestConnectionConfig.DBUNIT_DATABASE_SCHEMA);
		String dataTypeFactory = getDbProperty(testConfig, TestConnectionConfig.DBUNIT_DATATYPE_FACTORY);

		IDatabaseConnection connection = null != schema? new DataSourceDatabaseTester(dataSource, schema).getConnection()
				: new DataSourceDatabaseTester(dataSource).getConnection();

		DatabaseConfig config = connection.getConfig();
		if( null != dataTypeFactory ){
			Class<?> factory_class = Class.forName( "org.dbunit.ext.oracle.Oracle10DataTypeFactory" );
			config.setProperty( DatabaseConfig.PROPERTY_DATATYPE_FACTORY, factory_class.newInstance() );
		}
		return connection;
	}

	protected String getDbProperty(TestConnectionConfig config, String propName){
		String prop = config.getProperties().getProperty(propName);
		return null != prop && !"".equals(prop.trim())? prop : null;
	}

	protected void resetSequence(String sequenceName) throws Exception{
		resetSequence(sequenceName, 10000);
	}

	protected void resetSequence(String sequenceName, int initVal) throws Exception{
		IDatabaseConnection con = null;
		try {
			con = getConnection();

			Statement st = con.getConnection().createStatement();
			st.execute("drop sequence " + sequenceName);
			st.execute("create sequence " + sequenceName + " start with "+ initVal);
			con.getConnection().commit();

		} finally {
			close(con);
		}
	}
	
	protected Property getTestProperty(String value, String repoid, String objectType, String propcmisid) {
		ObjectTypeProperty type = objTypePropSelector.getObjTypeProperty(objectType, propcmisid, repoid);
		Property p = new Property(type, value);
		return p;
	}

	/**
	 * Applies cmis:all by default if no permissions varargs specified
	 * 
	 * @param principalIds
	 * @param permissions
	 * @return
	 */
	protected Acl createAcl(boolean isDirect, List<String> principalIds, String ... permissions) {
		AccessControlListImpl acl = new AccessControlListImpl();
		List<Ace> aces = new ArrayList<>();
		acl.setAces(aces);
		acl.setExact(true);
		
		if(permissions.length == 0) {
			permissions = new String[]{"cmis:all"};
		}
		
		for(String principalId : principalIds) {
			AccessControlEntryImpl ace = new AccessControlEntryImpl();
			ace.setPrincipal(new AccessControlPrincipalDataImpl(principalId));
			ace.setDirect(isDirect);
			List<String> acePermissions = new ArrayList<>();
			ace.setPermissions(acePermissions);
			for(String permission : permissions) {
				acePermissions.add(permission);
			}
			aces.add(ace);
		}
		
		return acl;
	}

}
