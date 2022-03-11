package eu.trade.repo.test.util;

import java.io.File;
import java.math.BigInteger;

import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.server.CallContext;

public class CallContextImpl implements CallContext {

	private String repositoryId;
	private String username;
	private String password;
	
	/**
	 * @param repositoryId the repositoryId to set
	 */
	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String getBinding() {
		return null;
	}

	@Override
	public boolean isObjectInfoRequired() {
		return false;
	}

	@Override
	public Object get(String key) {
		return null;
	}

	@Override
	public String getRepositoryId() {
		return repositoryId;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getLocale() {
		return null;
	}

	@Override
	public BigInteger getOffset() {
		return null;
	}

	@Override
	public BigInteger getLength() {
		return null;
	}

	@Override
	public File getTempDirectory() {
		return null;
	}

	@Override
	public int getMemoryThreshold() {
		return 0;
	}

	@Override
	public long getMaxContentSize() {
		return 0;
	}

	@Override
	public CmisVersion getCmisVersion() {
		return CmisVersion.CMIS_1_0;
	}

	@Override
	public boolean encryptTempFiles() {
		return false;
	}
}
