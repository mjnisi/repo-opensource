/**
 * 
 */
package eu.trade.repo.delegates;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;

/**
 * {@link org.springframework.jdbc.support.lob.LobHandler} implementation based in {@link org.springframework.jdbc.support.lob.DefaultLobHandler}.
 * <p>
 * The difference in the implementation are:
 * <ul>
 * <li>
 * This force the use of JDBC 4.0 drivers {@link org.springframework.jdbc.support.lob.DefaultLobHandler#setStreamAsLob(boolean)}.
 * </li>
 * <li>
 * This implementation allows to save a Blob with an unknown size without the need to previously read it.
 * </li>
 * </ul>
 * <p>
 * Note: Currently, the {@link org.springframework.jdbc.support.lob.LobCreator} interface force the use of int to specify the maximum size, limiting it up to 2Gb.
 * @author porrjai
 * @see https://jira.springsource.org/browse/SPR-10881
 */
public final class LobHandlerImpl extends DefaultLobHandler {

	/**
	 * New instance.
	 * Sets streamsAsLob = true.
	 */
	public LobHandlerImpl() {
		super.setStreamAsLob(true);
	}

	/**
	 * @see org.springframework.jdbc.support.lob.DefaultLobHandler#setWrapAsLob(boolean)
	 */
	@Override
	public void setWrapAsLob(boolean wrapAsLob) {
		super.setWrapAsLob(false);
	}


	/**
	 * @see org.springframework.jdbc.support.lob.DefaultLobHandler#setStreamAsLob(boolean)
	 */
	@Override
	public void setStreamAsLob(boolean streamAsLob) {
		super.setStreamAsLob(true);
	}


	/**
	 * @see org.springframework.jdbc.support.lob.DefaultLobHandler#getLobCreator()
	 */
	@Override
	public LobCreator getLobCreator() {
		return new LobCreatorImpl();
	}

	protected class LobCreatorImpl extends DefaultLobCreator {

		/**
		 * @see org.springframework.jdbc.support.lob.DefaultLobHandler.DefaultLobCreator#setBlobAsBinaryStream(java.sql.PreparedStatement, int, java.io.InputStream, int)
		 */
		@Override
		public void setBlobAsBinaryStream(PreparedStatement ps, int paramIndex, InputStream binaryStream, int contentLength) throws SQLException {
			if (binaryStream != null) {
				if (contentLength <= 0) {
					ps.setBlob(paramIndex, binaryStream);
				}
				else {
					ps.setBlob(paramIndex, binaryStream, contentLength);
				}
			}
			else {
				ps.setBlob(paramIndex, (Blob) null);
			}
			if (logger.isDebugEnabled()) {
				logger.debug(binaryStream != null ? "Set binary stream for BLOB with length " + contentLength :
						"Set BLOB to null");
			}
		}
	}
}
