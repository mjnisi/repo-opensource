package eu.trade.repo.delegates;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.core.support.AbstractLobStreamingResultSetExtractor;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.trade.repo.index.triggers.IndexParamName;
import eu.trade.repo.index.triggers.StreamChangeType;
import eu.trade.repo.index.triggers.annotation.RegisterStreamChange;
import eu.trade.repo.index.triggers.annotation.StreamChangeParam;
import eu.trade.repo.util.Utilities;
import eu.trade.repo.util.io.ExtPipedInputStream;
import eu.trade.repo.util.io.ExtPipedOutputStream;
import eu.trade.repo.util.io.IOUtils;


/**
 * Manages operations related to cmis object stream
 */
public class JDBCLobDelegate {

	private static final Logger LOG = LoggerFactory.getLogger(JDBCLobDelegate.class);

	private static final String SQL_INSERT_LOB = "INSERT INTO stream (id, data) VALUES (?, ?)";
	private static final String SQL_READ_LOB = "SELECT data FROM stream WHERE id = ?";
	private static final String SQL_DELETE_LOB = "DELETE FROM stream WHERE id = ?";
	private static final String SQL_COPY_LOB = "INSERT INTO stream (id, data) SELECT ?, st.data FROM stream st WHERE st.id = ?";

	@Autowired
	private LobHandler lobHandler;
	@Autowired
	private JdbcTemplate jdbcTemplate;


	@RegisterStreamChange(StreamChangeType.INSERT)
	@Transactional (propagation=Propagation.REQUIRED,
			rollbackFor=Exception.class)
	public void copyStream(Integer sourceId, @StreamChangeParam(IndexParamName.OBJECT_ID)Integer targetId){
		try{
			jdbcTemplate.update(SQL_COPY_LOB, targetId, sourceId);
		}catch(DataAccessException e){
			LOG.error(e.getMessage(), e);
			throw new CmisRuntimeException("Error copying stream", e);
		}
	}

	@RegisterStreamChange(StreamChangeType.INSERT)
	@Transactional (propagation=Propagation.REQUIRED,
			rollbackFor=Exception.class)
	public void saveStream(@StreamChangeParam(IndexParamName.OBJECT_ID)final Integer objectId, final InputStream in, @StreamChangeParam(IndexParamName.STREAM_SIZE)int inSize) {
		try{
			int result = jdbcTemplate.execute( SQL_INSERT_LOB, new LobInsertCallback(lobHandler, objectId, in, inSize));
			LOG.debug("Number of objects inserted: {}", result);

		}catch(DataIntegrityViolationException e){
			LOG.error(e.getMessage(), e);
			throw new CmisStorageException("Stream already exists or related object does not exist", e);
		}catch(DataAccessException e){
			LOG.error(e.getMessage(), e);
			throw new CmisStorageException("Stream could not be saved", e);
		}

	}

	/**
	 * The calling method is responsible for closing the stream
	 * @param objectId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Transactional (propagation=Propagation.REQUIRED)
	public InputStream getStream(final Integer objectId) {
		InputStream in = null;
		try {

			final ExtPipedOutputStream pos = new ExtPipedOutputStream();
			in = new ExtPipedInputStream(pos);

			new Thread() {
				@Override
				public void run() {
					try {
						jdbcTemplate.query( SQL_READ_LOB, new Object[] {objectId},	new LobResultSetExtractor(lobHandler, pos));

					} catch (Exception  e) {
						LOG.error("Error reading the document stream: " +  e.getLocalizedMessage(), e);						

					} finally {
						if( !pos.isPipedInputStreamClosed() ){
							Utilities.close(pos);
						}
					}
				}
			}.start();

			return in;

		} catch(IOException e) {
			Utilities.close(in);
			LOG.error(e.getLocalizedMessage(), e);
			throw new CmisRuntimeException("Error retrieving the stream for object " + objectId, e);
		} catch(EmptyResultDataAccessException de) {
			Utilities.close(in);
			LOG.error(de.getLocalizedMessage(), de);
			throw new CmisConstraintException("Stream not found for object "+ objectId, de);
		} catch(DataAccessException e) {
			Utilities.close(in);
			LOG.error(e.getLocalizedMessage(), e);
			throw new CmisStorageException("Error retrieving the stream for object " + objectId, e);
		}
	}

	@RegisterStreamChange(StreamChangeType.DELETE)
	public void deleteStream(@StreamChangeParam(IndexParamName.OBJECT_ID)Integer objectId){
		try{
			jdbcTemplate.update(SQL_DELETE_LOB, objectId);
		}catch(DataAccessException e){
			LOG.error(e.getMessage(), e);
			throw new CmisStorageException("Error deleting stream", e);
		}
	}



	private static class LobInsertCallback extends AbstractLobCreatingPreparedStatementCallback{
		private final Integer objectId;
		private final InputStream in;
		private final int inputSize;

		public LobInsertCallback(LobHandler lobHandler, Integer objectId,  InputStream in, int inputSize){
			super(lobHandler);
			this.objectId = objectId;
			this.in = in;
			this.inputSize = inputSize;
		}

		@Override
		protected void setValues(PreparedStatement ps, LobCreator lobCreator) throws SQLException {
			ps.setInt(1, objectId);
			lobCreator.setBlobAsBinaryStream(ps, 2, in, inputSize);
		}

	}

	private static class LobResultSetExtractor extends AbstractLobStreamingResultSetExtractor {
		private final LobHandler lobHandler;
		private final ExtPipedOutputStream writer;

		public LobResultSetExtractor(LobHandler lobHandler, ExtPipedOutputStream writer) {
			super();
			this.lobHandler = lobHandler;
			this.writer = writer;
		}

		@Override
		protected void streamData(ResultSet rs) throws SQLException, IOException {
			//TODO pipe closed exception by here??
			try (InputStream in = lobHandler.getBlobAsBinaryStream(rs, 1)) {
				if ( null != in ) {
					IOUtils.copyLarge(in, writer);
				}
			} catch(SQLException | IOException e ) {
				LOG.error("Error reading stream data: " + e.getLocalizedMessage(), e);
				throw e;
			}
			// Note, the writer should be always closed in the caller
		}

	}
}
