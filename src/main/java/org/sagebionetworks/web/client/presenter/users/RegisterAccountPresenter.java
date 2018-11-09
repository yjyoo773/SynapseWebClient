package org.sagebionetworks.web.client.presenter.users;

import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.presenter.Presenter;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.users.RegisterAccountView;
import org.sagebionetworks.web.client.view.users.RegisterWidget;
import org.sagebionetworks.web.client.widget.header.Header;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class RegisterAccountPresenter extends AbstractActivity implements RegisterAccountView.Presenter, Presenter<RegisterAccount> {
	private RegisterAccount place;
	private RegisterAccountView view;
	
	private RegisterWidget registerWidget;
	private Header headerWidget;
	private AuthenticationController authController;
	private GlobalApplicationState globalAppState;
	
	@Inject
	public RegisterAccountPresenter(RegisterAccountView view,
			RegisterWidget registerWidget,
			Header headerWidget, 
			AuthenticationController authController,
			GlobalApplicationState globalAppState) {
		this.view = view;
		this.headerWidget = headerWidget;
		this.registerWidget = registerWidget;
		this.authController = authController;
		this.globalAppState = globalAppState;
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}
	
	public void init() {
		if (!authController.isLoggedIn()) {
			view.setRegisterWidget(registerWidget.asWidget());
			headerWidget.configure();
			headerWidget.refresh();
		} else {
			// SWC-4363
			globalAppState.getPlaceChanger().goTo(new Profile(Profile.VIEW_PROFILE_TOKEN));
		}
	}

	@Override
	public void setPlace(RegisterAccount place) {
		this.place = place;
		String token = place.toToken();
		String email = "";
		if(token != null && !ClientProperties.DEFAULT_PLACE_TOKEN.equals(token)){
			email = token.trim();
		}
		registerWidget.setEmail(email);
		init();
	}
}
