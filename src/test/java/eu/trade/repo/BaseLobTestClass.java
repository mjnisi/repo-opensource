package eu.trade.repo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.sql.DataSource;

import org.dbunit.Assertion;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.SortedTable;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;



public class BaseLobTestClass extends BaseTestClass{
	private static final Logger LOG = LoggerFactory.getLogger(BaseLobTestClass.class);

	public static final String TXT_FILE_MARK_1 = "TXT_FILE_MARK_1";
	public static final String TXT_FILE_CONTENT_1 = "This is a test document. Its goal is to verify the retrieval and indexing of documents works as expected."
			+ " That means that, for example, the word \"document\" has to appear twice,"
			+ " there are 43 positions, 35 document terms and 35 words in dictionary";

	public static final String TXT_FILE_MARK_2 = "TXT_FILE_MARK_2";
	public static final String TXT_FILE_CONTENT_2 = "Something like an email ssf@gmail.com, some other symbols @,%.;";

	public static final String TXT_FILE_MARK_3 = "TXT_FILE_MARK_3";
	public static final String TXT_FILE_CONTENT_3 = "";

	public static final String TXT_FILE_MARK_4 = "TXT_FILE_MARK_4";
	public static final String TXT_FILE_CONTENT_4 = "Convinience text to test the indexing process";
	
	public static final String TXT_FILE_MARK_5 = "TXT_FILE_MARK_5";
	public static final String TXT_FILE_CONTENT_5 = "one two three four one";


	public static final String SYSDATE = "SYSDATE";

	enum DataBase {
		EXTERNAL , EMBEDDED
	}

	@Autowired @Qualifier("dbEmbeddedProperties")
	private TestConnectionConfig dbEmbeddedProperties;
	@Autowired
	private DataSource dataSourceTransient;


	
	/**
	 * Set up the scenario
	 * 
	 * @param scenario dataset file (under /dbunit folder)
	 * @param op dbunit operation type
	 * @throws Exception
	 */
	//general database
	protected void setLobScenario(DatabaseOperation op, String... scenarios) throws Exception {
		setLobScenarioInternal(DataBase.EXTERNAL, op, scenarios);
	}
	//transient database
	protected void setLobTransientScenario(DatabaseOperation op, String... scenarios) throws Exception {
		setLobScenarioInternal(DataBase.EMBEDDED, op, scenarios);
	}
	
	protected void setLobScenarioInternal(DataBase dataBase, DatabaseOperation op, String... scenarios) throws Exception {
		IDataSet dataSet = prepareDataSet(scenarios);
		
		dataSet = applyLobReplacements(dataSet);
		
		IDatabaseConnection con = getConnection(dataBase);
		
		executeDbOperation(con, op, dataSet);
	}
	

	protected IDatabaseConnection getConnection(DataBase database) throws Exception {
		IDatabaseConnection connection = null;
		switch (database) {
		case EMBEDDED:
			connection = getConnection(dbEmbeddedProperties, dataSourceTransient);
			break;
		default:
			connection = getConnection();
			break;
		}
		return connection;
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
	//general database
	protected void compareLobTable(String tableName, String dbFilter, String resultFile) throws Exception {
		compareLobTableInternal(DataBase.EXTERNAL, tableName, dbFilter, null, resultFile);
	}
	protected void compareLobTable(String tableName, String dbFilter, String orderBy, String resultFile) throws Exception {
		compareLobTableInternal(DataBase.EXTERNAL, tableName, dbFilter, orderBy, resultFile);
	}
	
	//transient database
	protected void compareLobTransientTable(String tableName, String dbFilter, String resultFile) throws Exception {
		compareLobTableInternal(DataBase.EMBEDDED, tableName, dbFilter, null, resultFile);
	}
	protected void compareLobTransientTable(String tableName, String dbFilter, String orderBy, String resultFile) throws Exception {
		compareLobTableInternal(DataBase.EMBEDDED, tableName, dbFilter, orderBy, resultFile);
	}

	protected void compareLobTableInternal(DataBase database, String tableName, String dbFilter, String orderBy, String resultFile)
			throws Exception {
		IDatabaseConnection con = null;

		try {
			con = getConnection(database);
			// Fetch database data after executing your code
			ITable actualTable = con.createQueryTable(tableName,
					"select * from " + tableName +
					(dbFilter != null ? " where " + dbFilter : "") +
					(orderBy != null ? " order by " + orderBy : ""));

			// Load expected data from an XML dataset

			IDataSet expectedDataSet = new FlatXmlDataSetBuilder().build(this
					.getClass()
					.getResourceAsStream("/dbunit/results/" + resultFile));
			expectedDataSet = applyLobReplacements(expectedDataSet);

			final ITable expectedTable = expectedDataSet.getTable(tableName);

			// filter to use the results columns, not using IDs
			ITable filteredTable = DefaultColumnFilter.includedColumnsTable(
					actualTable, expectedTable.getTableMetaData().getColumns());

			//sort the results before comparing
			ITable sortedExpected = new SortedTable(expectedTable, expectedTable.getTableMetaData().getColumns());
			ITable sortedFiltered = new SortedTable(filteredTable, filteredTable.getTableMetaData().getColumns());
						
			// Assert actual database table match expected table
			Assertion.assertEquals(sortedExpected, sortedFiltered);
		} finally {
			close(con);
		}
	}

	private IDataSet applyLobReplacements(IDataSet dataSet) throws Exception{
		ReplacementDataSet tmpExpReplacementDataSet = new ReplacementDataSet(dataSet);
		tmpExpReplacementDataSet.addReplacementObject(TXT_FILE_MARK_1, TXT_FILE_CONTENT_1.getBytes("UTF-8")); // this row also needs to be in the getDataSet method
		tmpExpReplacementDataSet.addReplacementObject(TXT_FILE_MARK_2, TXT_FILE_CONTENT_2.getBytes("UTF-8"));
		tmpExpReplacementDataSet.addReplacementObject(TXT_FILE_MARK_3, TXT_FILE_CONTENT_3.getBytes("UTF-8"));
		tmpExpReplacementDataSet.addReplacementObject(TXT_FILE_MARK_4, TXT_FILE_CONTENT_4.getBytes("UTF-8"));
		tmpExpReplacementDataSet.addReplacementObject(TXT_FILE_MARK_5, TXT_FILE_CONTENT_5.getBytes("UTF-8"));
		return tmpExpReplacementDataSet;
	}


	protected String getString(InputStream in){
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		if( null != in ){
			String line;
			try {

				br = new BufferedReader(new InputStreamReader(in));
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return sb.toString();
	}


	/**
	 * Get the InputStream from a file
	 * 
	 * @return
	 * @throws Exception
	 */
	protected InputStream getPdfFileInputStream(String filePath) throws FileNotFoundException{
		if( LOG.isDebugEnabled() ){
			URL url = ClassLoader.getSystemResource(filePath);
			LOG.debug("FILE READ: {}", url.toString());
		}
		return ClassLoader.getSystemResourceAsStream(filePath);
	}
}
