package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.ErrorPageProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DownViewImpl implements DownView {
	public static final String SYNAPSE_DOWN_MAINTENANCE_TITLE = "Sorry, Synapse is down for maintenance.";
	private Header headerWidget;
	private SynapseContextPropsProvider propsProvider;
	@UiField
	ReactComponentDiv srcDownContainer;
	String message;

	public static enum ErrorPageType {
		maintenance,
		noAccess,
		unavailable
	}
	public interface Binder extends UiBinder<Widget, DownViewImpl> {
	}

	Widget widget;

	@Inject
	public DownViewImpl(Binder uiBinder, Header headerWidget, final SynapseContextPropsProvider propsProvider
	) {
		widget = uiBinder.createAndBindUi(this);
		this.headerWidget = headerWidget;
		this.propsProvider = propsProvider;
		headerWidget.configure();
		widget.addAttachHandler(event -> {
			if (event.isAttached()) {
				renderMaintenancePage();
			}
		});
	}

	@Override
	public void init() {
		headerWidget.configure();
		com.google.gwt.user.client.Window.scrollTo(0, 0); // scroll user to top of page
	}

	@Override
	public Widget asWidget() {
		return widget;
	}
	
	@Override
	public void setMessage(String message) {
		this.message = message;
		if (widget.isAttached()) {
			renderMaintenancePage();
		}
	}

	@Override
	public boolean isAttached() {
		return widget.isAttached();
	}

	public void renderMaintenancePage() {
		ErrorPageProps props = ErrorPageProps.create(ErrorPageType.maintenance.name(), SYNAPSE_DOWN_MAINTENANCE_TITLE, message);
		ReactNode component = React.createElementWithSynapseContext(SRC.SynapseComponents.ErrorPage, props, propsProvider.getJsInteropContextProps());
		srcDownContainer.render(component);
	}
}
