package eu.trade.repo.delegates;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionException;

import eu.trade.repo.index.model.TransientDTO;
import eu.trade.repo.model.Property;


/**
 * Class responsible for accessing and modifying the temporary index database table.
 */
public abstract class JDBCIndexTransientMetadataDelegate extends AbstractJDBCIndexTransientDelegate implements IndexTransientMetadataDelegate{

	private static final Logger LOG = LoggerFactory.getLogger(JDBCIndexTransientMetadataDelegate.class);


	//queries
	private static final String QUERY_METADATA_TRANSIENT_DELETE = "delete from index_transient_metadata where object_id = :objectId";

	private static final String QUERY_METADATA_TRANSIENT_WORDS_BY_OBJECT_ID = 
			WRAP_PAGINATED_INIT_TRANSIENT_WORDS_BY_OBJECT_ID + 
			"select distinct tmp.word word from index_transient_metadata tmp where tmp.object_id = :objectId order by word " +
			WRAP_PAGINATED_END_TRANSIENT_WORDS_BY_OBJECT_ID;

	private static final String QUERY_WORD_OBJECTS_BY_OBJECT_ID = 
			"select tmp.word, count(*), tmp.property_id, tmp.object_type_property_id from index_transient_metadata tmp where tmp.object_id = :objectId group by tmp.property_id, tmp.word order by tmp.object_id, tmp.property_id, tmp.word";

	private static final String QUERY_WORD_POSITIONS_BY_OBJECT_ID  = 
			WRAP_PAGINATED_INIT_WORD_POSITIONS_BY_OBJECT_ID_AND_WORDLIST + 
			"select tmp.word, tmp.position, tmp.step, tmp.object_id, tmp.property_id, tmp.object_type_property_id from index_transient_metadata tmp where tmp.object_id = :objectId and tmp.position > 0 order by tmp.object_id, tmp.property_id, tmp.word, tmp.position" +
			WRAP_PAGINATED_END_WORD_POSITIONS_BY_OBJECT_ID_AND_WORDLIST;


	private static final String QUERY_INSERT_METADATA_IN_TRANSIENT = "insert into index_transient_metadata(position, object_id, property_id, object_type_property_id, WORD, STEP) values (:position, :documentId, :propertyId, :objPropTypeId, :word, :step)";

	private static final String QUERY_OBJECT_TYPE_PROPERTY_ID_LIST_BY_OBJECT_ID = "select distinct tmp.object_type_property_id from index_transient_metadata tmp where tmp.object_id = :objectId";



	@Override
	protected String getSelectWordQuery() {
		return QUERY_METADATA_TRANSIENT_WORDS_BY_OBJECT_ID;
	}

	@Override
	protected String getSelectWordObjectQuery() {
		return QUERY_WORD_OBJECTS_BY_OBJECT_ID;
	}

	@Override
	protected String getSelectWordPositionQuery() {
		return QUERY_WORD_POSITIONS_BY_OBJECT_ID;
	}

	@Override
	protected String getDeleteTransientQuery() {
		return QUERY_METADATA_TRANSIENT_DELETE;
	}

	protected String getSelectObjectTypePropertyIdListQuery(){
		return QUERY_OBJECT_TYPE_PROPERTY_ID_LIST_BY_OBJECT_ID;
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
	public boolean processMetadataToTransientIndex( Integer repositoryId, Integer objectId, List<Property> propertyList ) throws IOException{

		LOG.info(">>> processToTransientIndex for document id: {}", objectId);
		try {

			List<TransientDTO> persistSegmentList = new ArrayList<TransientDTO>();
			TransientDTO token = null;
			Analyzer analyzer = createFullTextAnalyzer();

			for (Property property : propertyList) {
				TokenStream stream = analyzer.tokenStream("content", new StringReader(String.valueOf(property.getTypedValue())));
				CharTermAttribute charTermAtt = stream.addAttribute(CharTermAttribute.class);
				PositionIncrementAttribute positionIncrAtt = stream.addAttribute(PositionIncrementAttribute.class);

				stream.reset();

				int tsNum = 0;
				while (stream.incrementToken()) {
					tsNum++;

					if( LOG.isDebugEnabled() ){
						LOG.debug("token term: {}", charTermAtt.toString());
						LOG.debug("	 token Position increment: {}", positionIncrAtt.getPositionIncrement());
					}

					token = new TransientDTO(charTermAtt.toString(), tsNum, 0, positionIncrAtt.getPositionIncrement(), objectId, property.getId(), property.getObjectTypeProperty().getId());
					persistSegmentList.add(token);
				}
				stream.end();
			}
			writeTransientSegment(QUERY_INSERT_METADATA_IN_TRANSIENT, objectId, persistSegmentList);
			persistSegmentList.clear();

		}catch(IOException | TransactionException e ){
			LOG.error(e.getLocalizedMessage(), e);
			throw e;
		}
		return true;
	}

	
}
