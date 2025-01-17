package org.sagebionetworks.web.client.widget.entity.controller;

import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.Pre;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.extras.bootbox.client.callback.PromptCallback;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.entity.PromptForValuesModalView;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityActionControllerViewImpl implements EntityActionControllerView {

	public interface Binder extends UiBinder<Widget, EntityActionControllerViewImpl> {
	}

	Binder binder;
	@UiField
	Modal infoDialog;
	@UiField
	Pre infoDialogText;
	@UiField
	Div extraWidgetsContainer;
	@UiField
	Div uploadDialogWidgetContainer;
	@UiField
	Modal createVersionDialog;
	@UiField
	Div createVersionJobTrackingWidgetContainer;

	Span widget = new Span();
	Widget viewWidget = null;
	PromptForValuesModalView promptForValuesDialog;
	PortalGinInjector ginInjector;

	@Inject
	public EntityActionControllerViewImpl(Binder binder, PortalGinInjector ginInjector) {
		this.binder = binder;
		this.ginInjector = ginInjector;
	}

	private void lazyConstruct() {
		if (viewWidget == null) {
			viewWidget = binder.createAndBindUi(this);
			widget.add(viewWidget);
			promptForValuesDialog = ginInjector.getPromptForValuesModal();
			widget.add(promptForValuesDialog);
		}
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showConfirmDeleteDialog(String message, Callback callback) {
		DisplayUtils.confirmDelete(message, callback);
	}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void showSuccess(String message) {
		DisplayUtils.showSuccess(message);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void showPromptDialog(String title, String initialValue, PromptCallback callback, PromptForValuesModalView.InputType inputType) {
		lazyConstruct();
		promptForValuesDialog.configureAndShow(title, "", initialValue, inputType, value -> {
			promptForValuesDialog.hide();
			callback.callback(value);
		});
	}

	@Override
	public void showInfoDialog(String header, String message) {
		lazyConstruct();
		infoDialog.setTitle(header);
		infoDialogText.setText(message);
		infoDialog.show();
	}

	@Override
	public void addWidget(IsWidget w) {
		lazyConstruct();
		extraWidgetsContainer.add(w);
	}

	@Override
	public void setUploadDialogWidget(IsWidget w) {
		lazyConstruct();
		uploadDialogWidgetContainer.clear();
		uploadDialogWidgetContainer.add(w);
	}

	@Override
	public void showMultiplePromptDialog(PromptForValuesModalView.Configuration configuration) {
		lazyConstruct();
		promptForValuesDialog.configureAndShow(configuration);
	}

	@Override
	public void hideMultiplePromptDialog() {
		lazyConstruct();
		promptForValuesDialog.hide();
	}

	@Override
	public void setCreateVersionDialogJobTrackingWidget(IsWidget w) {
		lazyConstruct();
		createVersionJobTrackingWidgetContainer.add(w);
	}


	@Override
	public void hideCreateVersionDialog() {
		lazyConstruct();
		createVersionDialog.hide();
	}

	@Override
	public void showCreateVersionDialog() {
		lazyConstruct();
		createVersionDialog.show();
	}
}

