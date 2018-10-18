package org.sagebionetworks.web.client.widget.entity.file.downloadlist;

import com.google.gwt.user.client.ui.IsWidget;

public interface FileHandleAssociationRowView extends IsWidget {
	void setFileName(String fileName, String entityId);
	void setHasAccess(boolean hasAccess);
	void setCreatedBy(String createdBy);
	void setCreatedOn(String createdOn);
	void setFileSize(String fileSize);
	void setPresenter(Presenter p);
	boolean isAttached();
	public interface Presenter {
		void onRemove();
		void onViewAttached();
	}
}
