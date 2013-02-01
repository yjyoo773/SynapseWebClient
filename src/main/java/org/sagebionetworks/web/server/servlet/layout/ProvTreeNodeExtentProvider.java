package org.sagebionetworks.web.server.servlet.layout;

import org.abego.treelayout.NodeExtentProvider;
import org.sagebionetworks.web.client.widget.provenance.Dimension;
import org.sagebionetworks.web.shared.provenance.ActivityGraphNode;
import org.sagebionetworks.web.shared.provenance.ActivityType;
import org.sagebionetworks.web.shared.provenance.EntityGraphNode;
import org.sagebionetworks.web.shared.provenance.ExpandGraphNode;
import org.sagebionetworks.web.shared.provenance.ProvTreeNode;

public class ProvTreeNodeExtentProvider implements NodeExtentProvider<ProvTreeNode> {

	private static final Dimension ENTITY_DIMENSION = new Dimension(96, 50);
	private static final Dimension ACTIVITY_ENTITY_DIMENSION = new Dimension(110, 75);
	private static final Dimension ACTIVITY_MANUAL_DIMENSION = new Dimension(110, 24);
	private static final Dimension ACTIVITY_UNDEFINED_DIMENSION = new Dimension(110, 42);
	private static final Dimension EXPAND_DIMENSION = new Dimension(24, 10);
	
	@Override
	public double getHeight(ProvTreeNode node) {
		return getProvNodeDimenion(node).getHeight();
	}

	@Override
	public double getWidth(ProvTreeNode node) {
		return getProvNodeDimenion(node).getWidth();
	}
	
	private static Dimension getProvNodeDimenion(ProvTreeNode node) {
		if(node instanceof EntityGraphNode) {
			return ENTITY_DIMENSION;
		} else if(node instanceof ActivityGraphNode) {
			if(((ActivityGraphNode) node).getType() == ActivityType.CODE_EXECUTION)
				return ACTIVITY_ENTITY_DIMENSION;
			else if(((ActivityGraphNode) node).getType() == ActivityType.MANUAL)
				return ACTIVITY_MANUAL_DIMENSION;
			else if(((ActivityGraphNode) node).getType() == ActivityType.UNDEFINED)					
				return ACTIVITY_UNDEFINED_DIMENSION;
		} else if(node instanceof ExpandGraphNode) {
			return EXPAND_DIMENSION;
		}
		return null;
	}
}
