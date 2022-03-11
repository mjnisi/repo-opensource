package eu.trade.repo.service.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import eu.trade.repo.model.CMISObject;
import eu.trade.repo.model.view.ObjectParent;
import eu.trade.repo.model.view.ObjectParentId;

/**
 * Utility class to build a tree of {@link CMISObject} based in an object parents query result.
 * 
 * @author porrjai
 * @see eu.trade.repo.selectors.ObjectParentSelector
 */
public class Tree {

	private Map<Integer, Node> nodeMap;
	private final Integer rootKey;
	private final Node rootNode;

	/**
	 * New instance
	 * 
	 * @param rootFolder
	 * @param objectParents
	 */
	public Tree(CMISObject rootFolder, Set<ObjectParent> objectParents) {
		nodeMap = new HashMap<>();
		rootKey = rootFolder.getId();
		rootNode = new Node(rootKey);
		rootNode.setCmisObject(rootFolder);
		addNodes(objectParents);
	}

	/**
	 * Get the collection {@link Node} of descendants.
	 * 
	 * @return
	 */
	public Collection<Node> getDescendants() {
		return nodeMap.values();
	}

	/**
	 * Gets the root {@link Node}
	 * 
	 * @return
	 */
	public Node getRoot() {
		return rootNode;
	}

	/**
	 * Gets the node for the given key.
	 * 
	 * @param nodeKey
	 * @return {@link Node}
	 */
	public Node getNode(Integer nodeKey) {
		return nodeMap.get(nodeKey);
	}

	private void addNodes(Set<ObjectParent> objectParents) {
		for (ObjectParent objectParent : objectParents) {
			ObjectParentId objectParentId = objectParent.getObjectParentId();
			Node node = getNewNode(objectParentId.getObjectId());
			node.setCmisObject(objectParent.getCmisObject());
			Node parent = getNewNode(objectParentId.getParentId());
			parent.getChildren().add(node);
		}
		// Keep only reachable nodes
		Map<Integer, Node> reachedNodes = new HashMap<>();
		addReachable(rootNode, reachedNodes);
		nodeMap = reachedNodes;
	}

	private void addReachable(Node node, Map<Integer, Node> reachedNodes) {
		for (Node child : node.getChildren()) {
			reachedNodes.put(child.getKey(), child);
			addReachable(child, reachedNodes);
		}
	}

	private Node getNewNode(Integer nodeKey) {
		if (nodeKey.equals(rootKey)) {
			return rootNode;
		}
		Node node = nodeMap.get(nodeKey);
		if (node == null) {
			node = new Node(nodeKey);
			nodeMap.put(nodeKey, node);
		}
		return node;
	}
}
