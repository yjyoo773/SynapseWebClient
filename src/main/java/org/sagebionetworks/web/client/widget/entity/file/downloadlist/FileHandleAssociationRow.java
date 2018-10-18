package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import java.util.Date;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.asynch.EntityHeaderAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.FileHandleAsyncHandler;
import org.sagebionetworks.web.client.widget.asynch.UserProfileAsyncHandler;
import org.sagebionetworks.web.shared.exceptions.ForbiddenException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileHandleAssociationRow implements IsWidget, FileHandleAssociationRowView.Presenter {
	
	FileHandleAssociationRowView view;
	FileHandleAsyncHandler fhaAsyncHandler;
	UserProfileAsyncHandler userProfileAsyncHandler;
	SynapseJSNIUtils jsniUtils;
	EntityHeaderAsyncHandler entityHeaderAsyncHandler;
	FileHandleAssociation fha;
	EntityHeader entityHeader;
	CallbackP<FileHandleAssociation> onDeleteCallback;
	DateTimeUtils dateTimeUtils;
	GWTWrapper gwt;
	
	String fileName, createdBy;
	Date createdOn;
	Long fileSize = null;
	Boolean hasAccess = true;
	CallbackP<Double> addToPackageSizeCallback;
	@Inject
	public FileHandleAssociationRow(
			FileHandleAssociationRowView view,
			FileHandleAsyncHandler fhaAsyncHandler,
			UserProfileAsyncHandler userProfileAsyncHandler,
			SynapseJSNIUtils jsniUtils,
			EntityHeaderAsyncHandler entityHeaderAsyncHandler,
			DateTimeUtils dateTimeUtils,
			GWTWrapper gwt) {
		this.view = view;
		this.fhaAsyncHandler = fhaAsyncHandler;
		this.userProfileAsyncHandler = userProfileAsyncHandler;
		this.jsniUtils = jsniUtils;
		this.entityHeaderAsyncHandler = entityHeaderAsyncHandler;
		this.dateTimeUtils = dateTimeUtils;
		this.gwt = gwt;
		view.setPresenter(this);
	}
	
	public void configure(FileHandleAssociation fha, Callback accessRestrictionDetectedCallback, CallbackP<Double> addToPackageSizeCallback, CallbackP<FileHandleAssociation> onDeleteCallback) {
		this.fha = fha;
		this.onDeleteCallback = onDeleteCallback;
		this.addToPackageSizeCallback = addToPackageSizeCallback;
		entityHeaderAsyncHandler.getEntityHeader(fha.getAssociateObjectId(), new AsyncCallback<EntityHeader>() {
			@Override
			public void onFailure(Throwable caught) {
				jsniUtils.consoleError(caught.getMessage());
			}
			@Override
			public void onSuccess(EntityHeader result) {
				fileName = result.getName();
				view.setFileName(fileName, result.getId());
				//TODO: more information will be available from the entity header in the future.  the file handle bulk call may only be necessary for the access requirement state.
			}
		});
		
		fhaAsyncHandler.getFileResult(fha, new AsyncCallback<FileResult>() {
			@Override
			public void onFailure(Throwable caught) {
				setHasAccess(false);
				if (!(caught instanceof ForbiddenException)) {
					jsniUtils.consoleError(caught.getMessage());
				} else {
					accessRestrictionDetectedCallback.invoke();	
				}
			}
			public void onSuccess(FileResult result) {
				if (result.getFileHandle() == null) {
					setHasAccess(false);
				} else {
					FileHandle fileHandle = result.getFileHandle();
					setHasAccess(true);
					if (fileHandle.getCreatedOn() != null) {
						createdOn = fileHandle.getCreatedOn();
						view.setCreatedOn(dateTimeUtils.getDateTimeString(fileHandle.getCreatedOn()));	
					}					
					Long contentSize = fileHandle.getContentSize();
					if (contentSize != null) {
						fileSize = contentSize;
						view.setFileSize(gwt.getFriendlySize(contentSize.doubleValue(), true));
						updateTotalPackageSize();
					}
					updateCreatedBy(fileHandle.getCreatedBy());
				}
			};
		});
	}
	@Override
	public void onViewAttached() {
		updateTotalPackageSize();
	}
	
	public void updateTotalPackageSize() {
		if (view.isAttached() && fileSize != null) {
			addToPackageSizeCallback.invoke(fileSize.doubleValue());
		}
	}
	
	private void setHasAccess(boolean hasAccess) {
		this.hasAccess = hasAccess;
		view.setHasAccess(hasAccess);
	}
	
	public void updateCreatedBy(String userId) {
		userProfileAsyncHandler.getUserProfile(userId, new AsyncCallback<UserProfile>() {
			@Override
			public void onFailure(Throwable caught) {
				jsniUtils.consoleError(caught.getMessage());
			}
			public void onSuccess(UserProfile profile) {
				createdBy = DisplayUtils.getDisplayName(profile);
				view.setCreatedBy(createdBy);
			};
		});
	}
	
	@Override
	public void onRemove() {
		onDeleteCallback.invoke(fha);
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
	
	public Date getCreatedOn() {
		return createdOn;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public Long getFileSize() {
		return fileSize;
	}
	public String getFileName() {
		return fileName;
	}
	public Boolean getHasAccess() {
		return hasAccess;
	}
}
