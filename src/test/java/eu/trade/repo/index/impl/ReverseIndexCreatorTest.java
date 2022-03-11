package eu.trade.repo.index.impl;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;

import org.dbunit.operation.DatabaseOperation;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.util.StopWatch;

import eu.trade.repo.BaseTestClass;
import eu.trade.repo.delegates.JDBCLobDelegate;
import eu.trade.repo.index.Index;
import eu.trade.repo.index.IndexPartOperator;
import eu.trade.repo.index.IndexSynchronizer;
import eu.trade.repo.index.model.IndexOperation;

public class ReverseIndexCreatorTest extends BaseTestClass {

	private static final Logger LOG = LoggerFactory.getLogger(ReverseIndexCreatorTest.class);

	private static final String PDF_FILE_PATH = "pdf/pdf_one.pdf";


	@Autowired
	private IndexPartOperator contentIndexOperator;

	@Autowired 
	//	@Qualifier("indexForTest")
	private Index index;

	@Autowired
	private IndexSynchronizer indexSynchronizer;

	@Autowired
	private JDBCLobDelegate lobDelegate;


	@Test
	@Ignore
	public void fireReIndexThread(){
		index.executeOperation(1, 1, null, null, true, IndexOperation.CONTENT_INDEX);
	}

	//@Ignore
	@Test
	public void extractContentFromExistingSupportedDocumentTest(){
		LOG.info("extractContentFromExistingSupportedDocumentTest. BEGIN");
		try{

			setScenario("scenarioIndex_01.xml", DatabaseOperation.CLEAN_INSERT);

			StopWatch stopWatch = new StopWatch();
			stopWatch.start("saveStream");

			InputStream in = getPdfFileInputStream(PDF_FILE_PATH);
			lobDelegate.saveStream(1, in, in.available());

			stopWatch.stop();
			LOG.info("==++===============>> {} - ExecutionTime: {} s", "READ AND SAVE STREAM", stopWatch.getTotalTimeSeconds());



			StopWatch stopWatch2 = new StopWatch();
			stopWatch2.start("index");
			contentIndexOperator.createIndexPart(new IndexTaskImpl(1, 1, IndexOperation.CONTENT_INDEX, PDF_FILE_PATH.substring(4), BigInteger.valueOf(0), indexSynchronizer));
			stopWatch2.stop();
			LOG.info("==++===============>> {} - ExecutionTime: {} s", "INDEXING", stopWatch2.getTotalTimeSeconds());

		}catch(Exception e){
			LOG.error(e.getMessage(), e);
		}
	}




	//	@Ignore
	@Rollback(false)
	public void saveStream(){
		try{
			setScenario("scenarioIndex_01.xml", DatabaseOperation.CLEAN_INSERT);
			InputStream in = getPdfFileInputStream(PDF_FILE_PATH);
			lobDelegate.saveStream(1, in, in.available());
		}catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	private InputStream getPdfFileInputStream(String filePath) throws FileNotFoundException{
		if( LOG.isDebugEnabled() ){
			URL url = ClassLoader.getSystemResource(filePath);
			LOG.debug("FILE READ: {}", url.toString());
		}
		return ClassLoader.getSystemResourceAsStream(filePath);
	}


}
