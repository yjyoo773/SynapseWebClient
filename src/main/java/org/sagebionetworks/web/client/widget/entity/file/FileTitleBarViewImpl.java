package org.sagebionetworks.web.client.widget.entity.file;

import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.EntityTypeUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.EntityTypeIcon;
import org.sagebionetworks.web.client.widget.HelpWidget;
import org.sagebionetworks.web.client.widget.entity.FavoriteWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileTitleBarViewImpl extends Composite implements FileTitleBarView {
	Presenter presenter;
	private Md5Link md5Link;
	private FavoriteWidget favoriteWidget;
	AuthenticationController authController;
	@UiField
	HTMLPanel panel;
	@UiField
	HTMLPanel fileFoundContainer;
	@UiField
	HTMLPanel fileNameContainer;

	@UiField
	SimplePanel md5LinkContainer;
	@UiField
	EntityTypeIcon entityIcon;
	@UiField
	SpanElement fileName;
	@UiField
	SpanElement fileSize;
	@UiField
	SpanElement fileLocation;
	@UiField
	SimplePanel favoritePanel;
	@UiField
	DivElement externalUrlUI;
	@UiField
	SpanElement externalUrl;
	@UiField
	Heading entityName;
	@UiField
	DropDownMenu dropdownMenu;
	@UiField
	AnchorListItem addToDownloadListLink;
	@UiField
	AnchorListItem programmaticOptionsLink;
	@UiField
	Div externalObjectStoreUI;
	@UiField
	Span endpoint;
	@UiField
	Span bucket;
	@UiField
	Span fileKey;
	@UiField
	Span version;
	@UiField
	Anchor showVersionHistoryLink;
	@UiField
	Span versionUiCurrent;
	@UiField
	Button downloadOptionsButton;
	@UiField
	Div actionMenuContainer;
	@UiField
	Tooltip downloadTooltip;

	interface FileTitleBarViewImplUiBinder extends UiBinder<Widget, FileTitleBarViewImpl> {
	}

	private String currentEntityId;
	private static FileTitleBarViewImplUiBinder uiBinder = GWT.create(FileTitleBarViewImplUiBinder.class);

	@Inject
	public FileTitleBarViewImpl(FavoriteWidget favoriteWidget, Md5Link md5Link, GlobalApplicationState globalAppState, AuthenticationController authController) {
		this.favoriteWidget = favoriteWidget;
		this.md5Link = md5Link;
		this.authController = authController;

		initWidget(uiBinder.createAndBindUi(this));
		md5LinkContainer.addStyleName("inline-block margin-left-5");

		favoritePanel.addStyleName("inline-block");
		favoritePanel.setWidget(favoriteWidget.asWidget());
		addToDownloadListLink.addClickHandler(event -> {
			presenter.onAddToDownloadList();
		});
		programmaticOptionsLink.addClickHandler(event -> {
			presenter.onProgrammaticDownloadOptions();
		});
		showVersionHistoryLink.addClickHandler(event -> {
			presenter.toggleShowVersionHistory();
		});
	}

	@Override
	public void setVersionHistoryLinkText(String text) {
		showVersionHistoryLink.setText(text);
	}

	@Override
	public void setPresenter(Presenter p) {
		this.presenter = p;
	}

	@Override
	public void createTitlebar(Entity entity) {
		currentEntityId = entity.getId();
		favoriteWidget.configure(currentEntityId);
		md5Link.clear();
		md5LinkContainer.setWidget(md5Link);
		entityIcon.setType(EntityTypeUtils.getEntityType(entity));
	}

	@Override
	public void setExternalUrlUIVisible(boolean visible) {
		UIObject.setVisible(externalUrlUI, visible);
	}

	@Override
	public void setFilenameContainerVisible(boolean visible) {
		fileNameContainer.setVisible(visible);
	}

	@Override
	public void setFilename(String fileNameString) {
		fileName.setInnerText(fileNameString);
	}

	@Override
	public void setFileSize(String fileSizeString) {
		fileSize.setInnerText(fileSizeString);
	}

	@Override
	public void setMd5(String md5) {
		md5Link.configure(md5);
	}

	@Override
	public void setEntityName(String name) {
		entityName.setText(name);
	}

	@Override
	public void setExternalUrl(String url) {
		externalUrl.setInnerText(url);
	}

	@Override
	public Widget asWidget() {
		return this;
	}

	@Override
	public void showErrorMessage(String message) {
		DisplayUtils.showErrorMessage(message);
	}

	@Override
	public void showLoading() {}

	@Override
	public void showInfo(String message) {
		DisplayUtils.showInfo(message);
	}

	@Override
	public void clear() {}

	@Override
	public void setFileLocation(String location) {
		fileLocation.setInnerText(location);
	}

	@Override
	public void setFileDownloadMenuItem(Widget w) {
		dropdownMenu.insert(w, 0);
	}

	@Override
	public void setExternalObjectStoreUIVisible(boolean visible) {
		externalObjectStoreUI.setVisible(visible);
	}

	@Override
	public void setExternalObjectStoreInfo(String endpointValue, String bucketValue, String fileKeyValue) {
		endpoint.setText(endpointValue);
		bucket.setText(bucketValue);
		fileKey.setText(fileKeyValue);
	}

	@Override
	public void setVersionUICurrentVisible(boolean visible) {
		versionUiCurrent.setVisible(visible);
	}

	@Override
	public void setVersion(Long versionNumber) {
		version.setText(versionNumber.toString());
	}

	@Override
	public void setCanDownload(boolean canDownload) {
		downloadOptionsButton.setEnabled(canDownload);
		if (canDownload) {
			downloadTooltip.setTitle("Direct and programmatic download options");
		} else{
			String viewOnlyHelpText = authController.isLoggedIn() ? "You don't have download permission. Request access from an administrator, shown under File Tools ➔ File Sharing Settings" : "You need to log in to download this file.";
			downloadTooltip.setTitle(viewOnlyHelpText);
		}
	}
	
	@Override
	public void setActionMenu(IsWidget w) {
		w.asWidget().removeFromParent();
		actionMenuContainer.clear();
		actionMenuContainer.add(w);
	}

}
