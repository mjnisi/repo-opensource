package eu.trade.repo.delegates;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionException;

import eu.trade.repo.index.IndexNotSupportedException;
import eu.trade.repo.index.model.TransientDTO;


/**
 * Class responsible for accessing and modifying the temporary index database table.
 */
public abstract class JDBCIndexTransientContentDelegate extends AbstractJDBCIndexTransientDelegate implements IndexTransientContentDelegate{

	private static final Logger LOG = LoggerFactory.getLogger(JDBCIndexTransientContentDelegate.class);


	//queries
	private static final String QUERY_CONTENT_TRANSIENT_DELETE = "delete from index_transient where object_id = :objectId and position > :start and position <= :end";


	private static final String QUERY_CONTENT_TRANSIENT_WORDS_BY_DOCUMENT_ID = 
			WRAP_PAGINATED_INIT_TRANSIENT_WORDS_BY_OBJECT_ID + 
			"select distinct tmp.word word from index_transient tmp where tmp.object_id = :objectId order by word" + 
			WRAP_PAGINATED_END_TRANSIENT_WORDS_BY_OBJECT_ID;

	private static final String QUERY_CONTENT_WORD_OBJECTS_BY_DOCUMENT_ID_AND_WORDLIST = 
			"select tmp.word, count(*), -1 propId, -1 objPropTypeId from index_transient tmp where tmp.object_id = :objectId and tmp.word in(:wordList) group by tmp.word order by tmp.object_id, tmp.word";

	private static final String QUERY_CONTENT_WORD_POSITIONS_BY_DOCUMENT_ID_AND_WORDLIST  = 
			WRAP_PAGINATED_INIT_WORD_POSITIONS_BY_OBJECT_ID_AND_WORDLIST + 
			"select tmp.word, tmp.position, tmp.step, tmp.object_id, -1 propId, -1 objPropTypeId from index_transient tmp where tmp.object_id = :objectId and tmp.word in(:wordList) and tmp.position > 0 order by tmp.object_id, tmp.word, tmp.position" +
			WRAP_PAGINATED_END_WORD_POSITIONS_BY_OBJECT_ID_AND_WORDLIST;

	private static final String QUERY_CONTENT_INSERT_IN_TRANSIENT = "insert into index_transient(position, object_id, WORD, STEP) values (:position, :documentId, :word, :step)";



	@Override
	protected String getSelectWordQuery() {
		return QUERY_CONTENT_TRANSIENT_WORDS_BY_DOCUMENT_ID;
	}

	@Override
	protected String getSelectWordObjectQuery() {
		return QUERY_CONTENT_WORD_OBJECTS_BY_DOCUMENT_ID_AND_WORDLIST;
	}

	@Override
	protected String getSelectWordPositionQuery() {
		return QUERY_CONTENT_WORD_POSITIONS_BY_DOCUMENT_ID_AND_WORDLIST;
	}


	@Override
	protected String getDeleteTransientQuery() {
		return QUERY_CONTENT_TRANSIENT_DELETE;
	}


	/**
	 * Reads tokens from document content and stores them in the temporary index.
	 * Uses the Lucene's StandardAnalizer to tokenize the content of the document.
	 * 
	 * @param repositoryId The repository that contains the document
	 * @param objectId	The id of the document
	 * @param docReader	A reader for the document content
	 * @throws IOException
	 */
	public boolean processContentToTransientIndex( Integer repositoryId, Integer objectId, Reader docReader, int segmentSize, int indexSizeLimit ) throws IOException{
		boolean fullProcessed = true;
		try{
			LOG.info(">>> processContentToTempIndex for document id: {}", objectId);
			try (TokenStream ts = obtainTokenStreamFromReader(docReader)) {
				CharTermAttribute charTermAtt = ts.addAttribute(CharTermAttribute.class);
				PositionIncrementAttribute positionIncrAtt = ts.addAttribute(PositionIncrementAttribute.class);

				// Resets this stream to the beginning. (Required)
				ts.reset();

				List<TransientDTO> persistSegmentList = new ArrayList<TransientDTO>(segmentSize);
				TransientDTO token = null;

				int tsNum = 0;
				while (ts.incrementToken()) {
					tsNum++;

					if( LOG.isDebugEnabled() ){
						LOG.debug("token term: {}", charTermAtt.toString());
						LOG.debug("	 token Position increment: {}", positionIncrAtt.getPositionIncrement());
					}

					token = new TransientDTO(charTermAtt.toString(), tsNum, 0, positionIncrAtt.getPositionIncrement(), objectId);
					persistSegmentList.add(token);

					if( tsNum % segmentSize == 0){
						writeTransientSegment(QUERY_CONTENT_INSERT_IN_TRANSIENT, objectId, persistSegmentList);
						persistSegmentList.clear();
					}

					// limitReached
					if( tsNum == indexSizeLimit ){
						fullProcessed = false;
						break;
					}

				}
				writeTransientSegment(QUERY_CONTENT_INSERT_IN_TRANSIENT, objectId, persistSegmentList);
				persistSegmentList.clear();
				ts.end();
			}

		}catch(IndexNotSupportedException e ){
			LOG.info(e.getLocalizedMessage(), e);
			throw e;
		}catch(IOException | TransactionException e ){
			LOG.error(e.getLocalizedMessage(), e);
			throw e;
		}
		return fullProcessed;
	}


	//PRIVATES
	private TokenStream obtainTokenStreamFromReader(Reader docReader) throws IOException{
		return createFullTextAnalyzer().tokenStream("content", docReader);
	}

}
