package org.sagebionetworks.web.client.widget.table.v2.results.cell;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.html.Text;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FileCellRendererViewImpl implements FileCellRendererView {
	
	public interface Binder extends UiBinder<Widget, FileCellRendererViewImpl> {}
	
	@UiField
	Image loadingImage;
	@UiField
	Text errorText;
	@UiField
	Anchor anchor;
	
	Widget widget;

	@Inject
	public FileCellRendererViewImpl(Binder binder){
		widget = binder.createAndBindUi(this);
	}
	
	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setLoadingVisible(boolean visible) {
		loadingImage.setVisible(visible);
	}

	@Override
	public void setErrorText(String fileName) {
		this.errorText.setVisible(true);
		this.errorText.setText(fileName);
	}

	@Override
	public boolean isAttached() {
		return widget.isAttached();
	}

}
