package eu.trade.repo.service.util;

import java.util.ArrayList;
import java.util.List;

import eu.trade.repo.model.CMISObject;

/**
 * The node of a {@link Tree}
 * 
 * @author porrjai
 */
public class Node {

	private final Integer key;
	private CMISObject cmisObject;
	private final List<Node> children;
	private boolean deleted;

	public Node(Integer key) {
		this.key = key;
		this.children = new ArrayList<>();
	}

	/**
	 * @return the key
	 */
	public Integer getKey() {
		return key;
	}

	/**
	 * @return the cmisObject
	 */
	public CMISObject getCmisObject() {
		return cmisObject;
	}

	/**
	 * @param cmisObject the cmisObject to set
	 */
	void setCmisObject(CMISObject cmisObject) {
		this.cmisObject = cmisObject;
	}

	/**
	 * @return the children
	 */
	public List<Node> getChildren() {
		return children;
	}

	/**
	 * @return the deleted
	 */
	public boolean isDeleted() {
		return deleted;
	}

	/**
	 * @param deleted the deleted to set
	 */
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
}
