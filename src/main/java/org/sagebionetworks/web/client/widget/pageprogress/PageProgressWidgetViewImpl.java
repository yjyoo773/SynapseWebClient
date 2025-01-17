package org.sagebionetworks.web.client.widget.pageprogress;

import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.context.SynapseContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.PageProgressProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsni.SynapseContextProviderPropsJSNIObject;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.ReactComponentDiv;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PageProgressWidgetViewImpl implements PageProgressWidgetView, IsWidget {
	public interface PageProgressWidgetViewImplUiBinder extends UiBinder<Widget, PageProgressWidgetViewImpl> {
	}

	@UiField
	ReactComponentDiv srcContainer;
	Widget widget;
	SynapseJSNIUtils jsniUtils;
	SynapseContextPropsProvider propsProvider;
	boolean isConfigured = false;

	@Inject
	public PageProgressWidgetViewImpl(PageProgressWidgetViewImplUiBinder binder, SynapseJSNIUtils jsniUtils, SynapseProperties synapseProperties, SynapseContextPropsProvider propsProvider) {
		widget = binder.createAndBindUi(this);
		this.jsniUtils = jsniUtils;
		this.propsProvider = propsProvider;
	}

	@Override
	public void configure(String barColor, int barPercent, String backBtnLabel, Callback backBtnCallback, String forwardBtnLabel, Callback forwardBtnCallback, boolean isForwardActive) {
		PageProgressProps props = PageProgressProps.create(barColor, barPercent, backBtnLabel, () -> backBtnCallback.invoke(), forwardBtnLabel, () -> forwardBtnCallback.invoke(), isForwardActive);
		ReactNode component = React.createElementWithSynapseContext(SRC.SynapseComponents.PageProgress, props, propsProvider.getJsInteropContextProps());
		srcContainer.render(component);
		isConfigured = true;
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void clear() {}

	@Override
	public void setVisible(boolean visible) {
		widget.setVisible(visible);
	}
}
