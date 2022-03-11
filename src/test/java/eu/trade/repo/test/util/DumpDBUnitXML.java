package eu.trade.repo.test.util;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;

/**
 * Class for dumping the DB content into DBUnit XML format. 
 * Used for define scenarios.
 * 
 *
 */
public class DumpDBUnitXML {
	
	public static void main(String[] args) throws Exception {

		Connection jdbcConnection = DriverManager
				.getConnection(
						"jdbc:oracle:thin:@(DESCRIPTION= (ADDRESS=(PROTOCOL=TCP)(HOST=server.trade.cec.eu.int)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=dev.ec.europa.eu)))",
						"username", "password");
		IDatabaseConnection connection = new DatabaseConnection(jdbcConnection);

		// partial database export
		QueryDataSet partialDataSet = new QueryDataSet(connection);
		partialDataSet.addTable("repository");
		partialDataSet.addTable("object_type");
		partialDataSet.addTable("object");
//		partialDataSet.addTable("index_transient");
//		partialDataSet.addTable("index_word");
//		partialDataSet.addTable("index_word_object");
//		partialDataSet.addTable("index_word_position");
		partialDataSet.addTable("property");
		partialDataSet.addTable("object_child");
		partialDataSet.addTable("object_type_property");
		FlatXmlDataSet.write(partialDataSet, new FileOutputStream(
				"src/test/resources/dbunit/scenario05.xml"));

	}
}
