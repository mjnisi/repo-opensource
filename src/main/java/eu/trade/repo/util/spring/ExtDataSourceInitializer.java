package eu.trade.repo.util.spring;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;

public class ExtDataSourceInitializer extends DataSourceInitializer {
	private static final Logger logger = LoggerFactory.getLogger(ExtDataSourceInitializer.class);

	private static final String PROP_URL = "url";
	private static final String H2_FILE_PREFIX = "jdbc:h2:file:";
	private static final String H2_FILE_PREFIX_END = ";";

	private static final String H2_FILE_SUFFIX_H2DB = ".h2.db";
	private static final String H2_FILE_SUFFIX_LOCKDB = ".lock.db";
	private static final String H2_FILE_SUFFIX_TRACEDB = ".trace.db";

	private Properties properties;

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	@Override
	public void afterPropertiesSet() {


		String url = (null != properties)? properties.getProperty(PROP_URL) : null;
		int filePrefixIndex = (null != url)? url.indexOf(H2_FILE_PREFIX) : -1;

		if( null == properties ){
			logger.info("No properties provided");

		}else if ( null == url ){
			logger.info("H2 URL not found in properties");

		}else if ( -1 == filePrefixIndex ){
			logger.info("H2 is not using files");

		}else{
			logger.info("H2 is using files. Preparing to delete old files");

			String dbFileName = url.substring(filePrefixIndex + H2_FILE_PREFIX.length());
			dbFileName = dbFileName.substring(0, dbFileName.indexOf(H2_FILE_PREFIX_END));

			logger.info("ExtDataSourceInitializer - FILES to REMOVE: {}, {}, {}", dbFileName + H2_FILE_SUFFIX_H2DB, dbFileName + H2_FILE_SUFFIX_LOCKDB, dbFileName + H2_FILE_SUFFIX_TRACEDB);

			removeFile(dbFileName + H2_FILE_SUFFIX_H2DB);
			removeFile(dbFileName + H2_FILE_SUFFIX_LOCKDB);
			removeFile(dbFileName + H2_FILE_SUFFIX_TRACEDB);

		}
		super.afterPropertiesSet();
	}


	private boolean removeFile(String filePath){
		boolean deleted = false;
		Path path = null;
		try{
			path = FileSystems.getDefault().getPath(filePath).toAbsolutePath();
			logger.info("The file {} is writable: {}", path, Files.isWritable(path));

			deleted = Files.deleteIfExists(path);
			if( deleted ){
				logger.info("The {} H2 database file was DELETED", path);
			}else{
				logger.info("The {} H2 database file could NOT be DELETED", path);
			}
		}catch(Exception e){
			String dpath = (null == path)? filePath : path.toString();
			logger.warn("The {} H2 database file could not be deleted", dpath);
		}
		return deleted;
	}

}
