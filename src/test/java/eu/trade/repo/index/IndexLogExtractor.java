package eu.trade.repo.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class IndexLogExtractor {

	private static final Log log = LogFactory.getLog(IndexLogExtractor.class);

	private static final String LINE_PREFIX_BEGIN_OPERATION_LOG = "BEGIN. eu.trade.repo.index.impl.IndexHelper.indexFromScratch";
	private static final String LINE_PREFIX_WRITING_PAGE_LOG = ">>>> Writing index page ";
	private static final String LINE_INDEX_DONE_LOG = "Index DONE for document ";
	private static final String LINE_PREFIX_END_OPERATION_LOG = ">> END. ExecutionTime: ";

	// All lines
	private static final String PARAM_ALL_EXECUTION_POOL = "[";
	private static final String PARAM_ALL_DATE_LIMITTOKEN = " [";
	private static final String PARAM_ALL_EXECUTION_POOL_LIMITTOKEN = "]";

	//Begin line
	private static final String PARAM_BEGIN_END_OPERATION = "operation: ";
	//	private static final String PARAM_BEGIN_END_REPO_ID = "repoId: ";
	private static final String PARAM_BEGIN_END_DOCUMENT_ID = "objId: ";
	private static final String PARAM_BEGIN_END_FILE_NAME = "fileName: ";
	private static final String PARAM_BEGIN_END_FILE_SIZE = "fileSize: ";
	private static final String PARAM_BEGIN_END_OWNER = "owner: ";

	private static final String PARAM_BEGIN_END_LIMITTOKEN_MEDIUM = ";";
	private static final String PARAM_BEGIN_END_LIMITTOKEN_END = ")";

	//Writing document page line
	private static final String PARAM_WRITING_PAGE_MAX_INDEX_PAGE = LINE_PREFIX_WRITING_PAGE_LOG;
	private static final String PARAM_WRITING_PAGE_DOCUMENT_ID = "for documentId: ";
	private static final String PARAM_WRITING_PAGE_LIMITTOKEN_MEDIUM = " ";

	//Index done line
	private static final String PARAM_INDEX_DONE_DOCUMENT_ID = LINE_INDEX_DONE_LOG;
	private static final String PARAM_INDEX_DONE_RESULT_STATE = "Result state: ";
	private static final String PARAM_INDEX_DONE_LIMITTOKEN = ".";

	//End line
	private static final String PARAM_END_EXECUTION_TIME = LINE_PREFIX_END_OPERATION_LOG;
	private static final String PARAM_END_EXECUTION_TIME_LIMITTOKEN = " s.";


	public static void main(String[] args){
		log.info("BEGIN");
		IndexLogExtractor extractor = new IndexLogExtractor();

		//documentId - LogInfo
		Map<String, LogInfo> indexMap = new HashMap<String, LogInfo>();

		//String rootPath = ClassLoader.getSystemResource("indexlogs").getPath();
		String rootPath = "C:/Dev/git/repo/src/test/resources/indexlogs";
		log.info("Root path: " + rootPath);

		File logDir = new File(rootPath, "logs");
		log.info("-- logs: " + logDir.getAbsolutePath());

		File resultFile = new File(rootPath, "indexSummary.csv");

		String[] logFileArray = logDir.list();
		File f = null;
		for (String logFile : logFileArray) {
			log.info("Log file: " + logFile);

			f = new File(logDir, logFile);
			if( f.isFile() ){
				extractor.readFile(new File(logDir, logFile), resultFile, indexMap);
			}
		}
		log.info("END");
	}


	public void readFile(File file, File outFile, Map<String, LogInfo> indexMap){

		FileReader fr = null;
		BufferedReader breader = null;

		FileWriter fwout = null;
		PrintWriter pout = null;
		try{
			fwout = new FileWriter(outFile, true);
			pout = new PrintWriter(fwout);

			fr = new FileReader (file);
			breader = new BufferedReader(fr);

			String line = null;
			String docId = null;

			while((line = breader.readLine()) != null){

				if( isBeginOperationLog(line)){
					processBeginLineToIndexMap(line, indexMap, getDefaultDateForOldFiles(file.getName()));

				}else if( isEndOperationLog(line)){
					docId = processEndLineToIndexMap(line, indexMap);

					pout.println(getLogInfo(docId, indexMap).toString());

				}else if ( isWritingIndexPageLog(line) ){
					processWritingLineToIndexMap(line, indexMap);

				}else if( isIndexDoneLog(line) ){
					processIndexDoneLineToIndexMap(line, indexMap);
				}
			}

		}catch (Exception ex){
			log.error(ex.getMessage(), ex);

		}finally{
			try{
				if (null != breader){
					breader.close();
				}
				if( null != pout ){
					pout.close();
				}
			}catch (Exception ex1){
				log.error(ex1.getMessage(), ex1);
			}
		}
	}

	private String getDefaultDateForOldFiles(String logFileName){
		String defaultDate = "";
		if( logFileName.startsWith("old")){
			Pattern p = Pattern.compile("\\d+");
			Matcher m = p.matcher(logFileName);
			if( m.find() ){
				defaultDate = "2013/11/" + m.group();
			}
		}
		return defaultDate;
	}
	
	private String getPoolName(String threadName){
		String poolName = "";
		Pattern p = Pattern.compile("-\\d+");
		Matcher m = p.matcher(threadName);
		if( m.find() ){
			int idx = m.start(m.groupCount());
			if( -1 < idx ){
				poolName = threadName.substring(0, idx);
			}
		}
		return poolName;
	}

	//>> END. ExecutionTime: 5.372 s. eu.trade.repo.index.impl.IndexHelper.indexFromScratch (operation: INDEX; repoId: 10020; objId: 953173; fileName: SHERPA - TDI dynamic pages 2 0 - screen specifications 0.1.9.doc)
	private boolean isEndOperationLog(String line){
		return null != line && line.contains(LINE_PREFIX_END_OPERATION_LOG);
	}

	//>>>> Writing index page 0 for documentId: 953176 
	private boolean isWritingIndexPageLog(String line){
		return null != line && line.contains(LINE_PREFIX_WRITING_PAGE_LOG);
	}

	//Index DONE for document 979957. Result state: Not indexable
	private boolean isIndexDoneLog(String line){
		return null != line && line.contains(LINE_INDEX_DONE_LOG);
	}

	//16:05:17,611 [indexTaskExecutor-2] INFO [index.impl.ReverseIndexLogger]  - BEGIN. eu.trade.repo.index.impl.IndexHelper.indexFromScratch (operation: INDEX; repoId: 10043; objId: 10067; fileName: CMIS-v1.1-cs01.pdf)
	private boolean isBeginOperationLog(String line){
		return null != line && line.contains(LINE_PREFIX_BEGIN_OPERATION_LOG);
	}

	private void processBeginLineToIndexMap(String line, Map<String, LogInfo> indexMap, String defaultDate){
		log.debug("storeToIndexTimeMap - line: ." + line + ".");

		String[] dateTime	= processDateTimeParam(line, 0, line.indexOf(PARAM_ALL_DATE_LIMITTOKEN), defaultDate);
		String execPool		= getPoolName(processParam(line, PARAM_ALL_EXECUTION_POOL, PARAM_ALL_EXECUTION_POOL_LIMITTOKEN));
		String operation 	= processParam(line, PARAM_BEGIN_END_OPERATION, PARAM_BEGIN_END_LIMITTOKEN_MEDIUM);
		String docId 		= processParam(line, PARAM_BEGIN_END_DOCUMENT_ID, PARAM_BEGIN_END_LIMITTOKEN_MEDIUM);
		String fileName 	= processParam(line, PARAM_BEGIN_END_FILE_NAME, PARAM_BEGIN_END_LIMITTOKEN_MEDIUM);
		String fileSize 	= processParam(line, PARAM_BEGIN_END_FILE_SIZE, PARAM_BEGIN_END_LIMITTOKEN_END);
		String owner 		= processParam(line, PARAM_BEGIN_END_OWNER, PARAM_BEGIN_END_LIMITTOKEN_MEDIUM);

		log.debug(">>>>>>>> docId = ." + docId + ".");

		LogInfo logInfo = getLogInfo(docId, indexMap);
		logInfo.setExecutionPool(execPool);
		logInfo.setOwner(owner);
		logInfo.setOperation(operation);
		logInfo.setFileName(fileName);
		logInfo.setFileSize(fileSize);
		logInfo.setDate(dateTime[0]);
		logInfo.setStartTime(dateTime[1]);

	}

	private void processWritingLineToIndexMap(String line, Map<String, LogInfo> indexMap){
		log.debug("storeToIndexPageMap - line: ." + line + ".");

		String maxIndexPage	= processParam(line, PARAM_WRITING_PAGE_MAX_INDEX_PAGE, PARAM_WRITING_PAGE_LIMITTOKEN_MEDIUM);
		String docId		= processParam(line, PARAM_WRITING_PAGE_DOCUMENT_ID, null);

		getLogInfo(docId, indexMap).setMaxIndexPage(maxIndexPage);
	}

	private void processIndexDoneLineToIndexMap(String line, Map<String, LogInfo> indexMap){
		log.debug("storeToIndexStateMap - line: ." + line + ".");

		String docId	= processParam(line, PARAM_INDEX_DONE_DOCUMENT_ID, PARAM_INDEX_DONE_LIMITTOKEN);
		String resultState	= processParam(line, PARAM_INDEX_DONE_RESULT_STATE, null);

		getLogInfo(docId, indexMap).setResultState(resultState);
	}

	private String processEndLineToIndexMap(String line, Map<String, LogInfo> indexMap){
		log.debug("processEndLineToIndexMap - line: ." + line + ".");

		String executionTime	= processParam(line, PARAM_END_EXECUTION_TIME, PARAM_END_EXECUTION_TIME_LIMITTOKEN);
		String docId			= processParam(line, PARAM_BEGIN_END_DOCUMENT_ID, PARAM_BEGIN_END_LIMITTOKEN_MEDIUM);

		getLogInfo(docId, indexMap).setExecutionTime(executionTime);
		return docId;
	}


	private String processParam(String line, String paramName, String limitToken){
		if( 0 <= line.indexOf(paramName)){
			String substr = line.substring(line.indexOf(paramName) + paramName.length());
			return ( null != limitToken && 0 <= substr.indexOf(limitToken))?
					substr.substring(0, substr.indexOf(limitToken)).trim()
					: substr.trim();
		}
		return "";
	}

	private String[] processDateTimeParam(String line, int startIdx, int endIdx, String defaultDate){
		String date = null;
		String time = null;

		String lineSubstr = line.substring(startIdx, endIdx);
		if( lineSubstr.contains(" ") ){
			String[] dateTime = lineSubstr.split(" ");
			date = dateTime[0];
			time = dateTime[1];
		}else{
			date = defaultDate;
			time = lineSubstr;
		}
		if( time.contains(",") ){
			time = time.substring(0, time.indexOf(","));
		}
		return new String[]{date, time};
	}

	private LogInfo getLogInfo(String docId, Map<String, LogInfo> indexMap){
		if( null == indexMap.get(docId)){
			indexMap.put(docId, new LogInfo(docId));
		}
		return indexMap.get(docId);
	}


	private static class LogInfo{
		private String documentId;
		private String fileName;
		private String fileSize;
		private String operation;
		private String date;
		private String startTime;
		private String executionTime;
		private String resultState;
		private String maxIndexPage;
		private int numberOfPages;
		private String owner;


		private String executionPool;

		public LogInfo(String docId){
			this.documentId = docId;
		}

		public String getDocumentId() {
			return (null != documentId)? documentId : "";
		}

		public void setDocumentId(String documentId) {
			this.documentId = documentId;
		}

		public String getExecutionTime() {
			return (null != executionTime)? executionTime : "";
		}

		public void setExecutionTime(String executionTime) {
			this.executionTime = executionTime;
		}

		public String getResultState() {
			return (null == resultState)? "Exception" : resultState;
		}

		public void setResultState(String resultState) {
			this.resultState = resultState;
		}

		public String getMaxIndexPage() {
			return (null != maxIndexPage)? maxIndexPage : "-1";
		}

		public void setMaxIndexPage(String maxIndexPage) {
			this.maxIndexPage = maxIndexPage;

			int pageNumber = 0;
			try{
				//first page is page 0 => page number = page index +1
				pageNumber = Integer.parseInt(maxIndexPage) + 1;
			}catch(Exception e){}
			setNumberOfPages(pageNumber);
		}

		public int getNumberOfPages() {
			return numberOfPages;
		}

		public void setNumberOfPages(int numberOfPages) {
			this.numberOfPages = numberOfPages;
		}

		public String getFileSize() {
			return (null != fileSize)? fileSize : "0";
		}

		public void setFileSize(String fileSize) {
			this.fileSize = fileSize;
		}

		public String getStartTime() {
			return (null != startTime)? startTime : "";
		}

		public void setStartTime(String startTime) {
			this.startTime = startTime;
		}


		public String getDate() {
			return (null != date)? date : "";
		}

		public void setDate(String date) {
			this.date = date;
		}


		public String getExecutionPool() {
			return (null != executionPool)? executionPool : "";
		}

		public void setExecutionPool(String executionPool) {
			this.executionPool = executionPool;
		}


		public String getFileName() {
			return (null != fileName)? fileName.replaceAll(",", "-") : "";
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}


		public String getOperation() {
			return (null != operation)? operation : "";
		}

		public void setOperation(String operation) {
			this.operation = operation;
		}

		
		public String getOwner() {
			return (null != owner)? owner : "";
		}

		public void setOwner(String owner) {
			this.owner = owner;
		}

		@Override
		public String toString(){
			StringBuilder stb = new StringBuilder();
			stb.append(getOwner());
			stb.append(",").append(getOperation());
			stb.append(",").append(getDocumentId());
			stb.append(",").append(getFileName());
			stb.append(",").append(getFileSize());
			stb.append(",").append(getDate());
			stb.append(",").append(getStartTime());
			stb.append(",").append(getExecutionPool());
			stb.append(",").append(getExecutionTime());
			stb.append(",").append(getResultState());
			stb.append(",").append(getNumberOfPages());
			return stb.toString();
		}

	}

}
