//package eu.trade.repo.index.extractor;
//
//import static junit.framework.Assert.assertTrue;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.io.Reader;
//
//import org.apache.commons.io.IOUtils;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.dbunit.operation.DatabaseOperation;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import eu.trade.repo.BaseTestClass;
//import eu.trade.repo.TestConstants;
//import eu.trade.repo.delegates.JDBCLobDelegate;
//import eu.trade.repo.index.pool.IndexThread;
//import eu.trade.repo.index.pool.IndexManager;
//import eu.trade.repo.model.CMISObject;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = "classpath:/testApplicationContext.xml")
//public class ContentExtractorManagerTest extends BaseTestClass {
//
//	private static Log log = LogFactory.getLog(ContentExtractorManagerTest.class);
//
//	@Autowired
//	private IndexManager contentExtractorManager;
//	@Autowired
//	private JDBCLobDelegate jdbcDelegate;
//	
//	private static final String PDF_FILE_PATH = "pdf/pdf_one.pdf";
//	private CMISObject cmisObject = null;
//
//	@Before
//	public void createParentCMISObjectDocument() throws Exception {
//		setScenario("scenario03.xml", DatabaseOperation.CLEAN_INSERT);
//        cmisObject = createTestObject(TestConstants.DEFAULT_REPOSITORY2, "cmis:document");
//    }
//
//	@Test
//	public void extractContentFromExistingSupportedDocumentTest(){
//		log.debug("manager: " + contentExtractorManager);
//		try{
//			
//			InputStream in = getPdfFileInputStream(PDF_FILE_PATH);
//			OutputStream out = getTxtResultOutputStream(PDF_FILE_PATH);
//			
//			IndexThread extractorTask = new IndexThread(cmisObject.getId(), in);
//			extractorTask.extractAndSave();
//			
//			jdbcDelegate.getStream(cmisObject.getId(), out);
////			Reader reader = jdbcDelegate.getStream(cmisObject.getId());
//			
////			FileWriter w = new FileWriter(PDF_FILE_PATH + ".txt");
////			IOUtils.copy(reader, w);
//			
//			File fout = new File(PDF_FILE_PATH + ".txt");
//			assertTrue( 0 < fout.length() );
//		
//		}catch(FileNotFoundException fe){
//			log.error(fe.getMessage(), fe);
//		}catch(IOException e){
//			log.error(e.getMessage(), e);
//		}
//	}
//	
//
//	private InputStream getPdfFileInputStream(String filePath) throws FileNotFoundException{
//		return ClassLoader.getSystemResourceAsStream(filePath);
//	}
//	
//	
//	private OutputStream getTxtResultOutputStream(String originFilePath) throws IOException{
//		File fout = new File(originFilePath + ".txt");
//		fout.createNewFile();
//		return new FileOutputStream(fout);
//	}
//
//}
