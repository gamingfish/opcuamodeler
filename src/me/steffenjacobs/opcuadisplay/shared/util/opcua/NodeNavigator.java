package me.steffenjacobs.opcuadisplay.shared.util.opcua;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.IdType;

import me.steffenjacobs.opcuadisplay.shared.domain.CachedBaseNode;

public class NodeNavigator {

	private static NodeNavigator instance;

	private CachedBaseNode root;

	// does not work for multiple clients simultaniously!
	private AtomicInteger highestNodeId = new AtomicInteger(-1);

	private NodeNavigator() {
		// singleton
	}

	public static NodeNavigator getInstance() {
		if (instance == null) {
			instance = new NodeNavigator();
		}
		return instance;
	}

	/** @return the root node */
	public CachedBaseNode getRoot() {
		return root;
	}

	/**
	 * Example: navigateByName("Objects/Types/ObjectTypes/BaseObjectType")
	 * 
	 * @return the node navigated to or null, if the node does not exist
	 */
	public CachedBaseNode navigateByName(String url) {
		String[] path = url.split("/");

		CachedBaseNode node = getRoot();
		for (String p : path) {
			final CachedBaseNode orig = node;
			for (CachedBaseNode cbn : node.getChildren()) {
				if (cbn.getBrowseName().getName().equals(p)) {
					node = cbn;
					break;
				}
			}
			if (orig == node) {
				return null;
			}
		}
		return node;
	}

	public void setRoot(CachedBaseNode newRoot) {
		this.root = newRoot;
	}

	// does not work for multiple clients simultaniously!
	public void increaseHighestNodeIdIfNecessarySafe(CachedBaseNode cn) {

		if (cn == null || cn.getNodeId() == null || cn.getNodeId().getIdentifier() == null
				|| cn.getNodeId().getType() != IdType.Numeric) {
			return;
		}
		final int nodeId;
		if (cn.getNodeId().getIdentifier() instanceof UInteger) {
			nodeId = ((UInteger) cn.getNodeId().getIdentifier()).intValue();
		} else if (cn.getNodeId().getIdentifier() instanceof Integer) {
			nodeId = (int) cn.getNodeId().getIdentifier();
		} else {
			return;
		}

		highestNodeId.getAndUpdate(x -> x = x > nodeId ? x : nodeId);
	}

	// does not work for multiple clients simultaniously!
	public int generateNewNodeId() {
		return highestNodeId.incrementAndGet();
	}

	public void resetHighestNodeId() {
		highestNodeId.set(-1);
	}

	public int getHighestNodeId() {
		return highestNodeId.get();
	}


	/** @return true, if <i>node</i> is in the Types folder. */
	public boolean isInTypesFolder(CachedBaseNode node) {

		while ((node = node.getParent()) != null) {
			if (Identifiers.TypesFolder.equals(node.getNodeId())) {
				return true;
			}
		}

		return false;
	}
}
