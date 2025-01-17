package org.sagebionetworks.web.client.widget.provenance.v2;

import java.util.List;

import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.ProvenanceGraphProps;
import org.sagebionetworks.web.client.jsinterop.ProvenanceGraphProps.OnUpdateJavaScriptObject;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.inject.Inject;

public class ProvenanceWidgetViewImpl extends ReactComponentDiv implements ProvenanceWidgetView {

	List<Reference> refs;
	String containerHeight;
	// this view stores the nodes and edges from this instance.  So you can reinitialize the previous state by calling rerender on this instance!
	JavaScriptObject initialNodes;
	JavaScriptObject initialEdges;
	OnUpdateJavaScriptObject nodesListener;
	OnUpdateJavaScriptObject edgesListener;
	
	SynapseContextPropsProvider contextPropsProvider;
	
	@Inject
	public ProvenanceWidgetViewImpl(SynapseContextPropsProvider contextPropsProvider) {
		addStyleName("overflowHidden");
		this.contextPropsProvider = contextPropsProvider;
		this.nodesListener = jsObject -> {
			initialNodes = jsObject;
		};
		this.edgesListener = jsObject -> {
			initialEdges = jsObject;
		};
	}
	
	public void configure(List<Reference> refs, String containerHeight) {
		this.refs = refs;
		this.containerHeight = containerHeight;
		renderComponent();
	}

	public void renderComponent() {
		ProvenanceGraphProps props = ProvenanceGraphProps.create(refs, containerHeight, initialNodes, initialEdges, nodesListener, edgesListener);
		ReactNode component = React.createElementWithSynapseContext(
				SRC.SynapseComponents.ProvenanceGraph,
				props,
				contextPropsProvider.getJsInteropContextProps()
		);
		this.render(component);
	}
}
