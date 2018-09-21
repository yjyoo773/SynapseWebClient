package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.asynch.FileHandleAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.UserProfileAsyncHandler;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.EntityIdCellRenderer;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileHandleAssociationRow implements IsWidget, FileHandleAssociationRowView.Presenter {
	
	private FileHandleAssociationRowView view;
	FileHandleAsyncHandler fhaAsyncHandler;
	UserProfileAsyncHandler userProfileAsyncHandler;
	SynapseJSNIUtils jsniUtils;
	PortalGinInjector ginInjector;
	FileHandleAssociation fha;
	CallbackP<FileHandleAssociation> onDeleteCallback;
	DateTimeUtils dateTimeUtils;
	GWTWrapper gwt;
	@Inject
	public FileHandleAssociationRow(
			FileHandleAssociationRowView view,
			FileHandleAsyncHandler fhaAsyncHandler,
			UserProfileAsyncHandler userProfileAsyncHandler,
			SynapseJSNIUtils jsniUtils,
			PortalGinInjector ginInjector,
			DateTimeUtils dateTimeUtils,
			GWTWrapper gwt) {
		this.view = view;
		this.fhaAsyncHandler = fhaAsyncHandler;
		this.userProfileAsyncHandler = userProfileAsyncHandler;
		this.jsniUtils = jsniUtils;
		this.ginInjector = ginInjector;
		this.dateTimeUtils = dateTimeUtils;
		this.gwt = gwt;
		view.setPresenter(this);
	}
	
	public void configure(FileHandleAssociation fha, Callback accessRestrictionDetectedCallback, CallbackP<FileHandleAssociation> onDeleteCallback) {
		this.fha = fha;
		this.onDeleteCallback = onDeleteCallback;
		EntityIdCellRenderer entityBadge = ginInjector.createEntityIdCellRenderer();	
		entityBadge.setValue(fha.getAssociateObjectId());
		view.setFileNameWidget(entityBadge);
		
		fhaAsyncHandler.getFileResult(fha, new AsyncCallback<FileResult>() {
			@Override
			public void onFailure(Throwable caught) {
				view.setHasAccess(false);
				if (!(caught instanceof ForbiddenException)) {
					jsniUtils.consoleError(caught.getMessage());
				}
				accessRestrictionDetectedCallback.invoke();
			}
			public void onSuccess(FileResult result) {
				if (result.getFileHandle() == null) {
					view.setHasAccess(false);
				} else {
					FileHandle fileHandle = result.getFileHandle();
					view.setHasAccess(true);
					if (fileHandle.getCreatedOn() != null) {
						view.setCreatedOn(dateTimeUtils.getDateTimeString(fileHandle.getCreatedOn()));	
					}					
					Long contentSize = fileHandle.getContentSize();
					if (contentSize != null) {
						view.setFileSize(gwt.getFriendlySize(contentSize.doubleValue(), true));	
					}
					updateCreatedBy(fileHandle.getCreatedBy());
				}
			};
		});
	}
	
	public void updateCreatedBy(String userId) {
		userProfileAsyncHandler.getUserProfile(userId, new AsyncCallback<UserProfile>() {
			@Override
			public void onFailure(Throwable caught) {
				jsniUtils.consoleError(caught.getMessage());
			}
			public void onSuccess(UserProfile profile) {
				view.setCreatedBy(DisplayUtils.getDisplayName(profile));
			};
		});
	}
	
	@Override
	public void onDelete() {
		onDeleteCallback.invoke(fha);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}