package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.principal.AccountCreationToken;
import org.sagebionetworks.repo.model.principal.AliasType;
import org.sagebionetworks.repo.model.principal.EmailValidationSignedToken;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.UserAccountServiceAsync;
import org.sagebionetworks.web.client.place.EmailInvitation;
import org.sagebionetworks.web.client.place.NewAccount;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.NewAccountView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class NewAccountPresenter extends AbstractActivity implements NewAccountView.Presenter, Presenter<NewAccount> {
	private NewAccountView view;
	private SynapseClientAsync synapseClient;
	private GlobalApplicationState globalAppState;
	private UserAccountServiceAsync userAccountService;
	private AuthenticationController authController;
	private AccountCreationToken accountCreationToken;
	private SynapseAlert synAlert;

	@Inject
	public NewAccountPresenter(NewAccountView view, SynapseClientAsync synapseClient, GlobalApplicationState globalAppState, UserAccountServiceAsync userAccountService, AuthenticationController authController, SynapseAlert synAlert) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.synAlert = synAlert;
		fixServiceEntryPoint(synapseClient);
		this.globalAppState = globalAppState;
		this.userAccountService = userAccountService;
		fixServiceEntryPoint(userAccountService);
		this.authController = authController;
		view.setSynAlert(synAlert);
		view.setPresenter(this);
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(NewAccount place) {
		if (authController.isLoggedIn()) {
			globalAppState.clearLastPlace();
			authController.logoutUser();
		}
		view.clear();
		this.view.setPresenter(this);
		synAlert.clear();
		synapseClient.hexDecodeAndDeserializeAccountCreationToken(place.toToken(), new AsyncCallback<AccountCreationToken>() {
			@Override
			public void onSuccess(AccountCreationToken result) {
				accountCreationToken = result;
				if (!accountCreationTokenIsValid()) {
					view.setLoading(false);
					synAlert.showError(DisplayConstants.ACCOUNT_CREATION_FAILURE + " Token is invalid.");					
					return;
				}
				String email = accountCreationToken.getEmailValidationSignedToken().getEmail();
				view.setEmail(email);
				checkEmailAvailable(email);
			}

			@Override
			public void onFailure(Throwable caught) {
				view.setLoading(false);
				synAlert.showError(DisplayConstants.ACCOUNT_CREATION_FAILURE + " Token is invalid: " + caught.getMessage());
			}
		});
	}

	private boolean accountCreationTokenIsValid() {
		return accountCreationToken.getEmailValidationSignedToken() != null;
	}

	@Override
	public void completeRegistration(String userName, String fName, String lName, String password) {
		view.setLoading(true);
		synAlert.clear();
		EmailValidationSignedToken emailValidationSignedToken = accountCreationToken.getEmailValidationSignedToken();
		userAccountService.createUserStep2(userName.trim(), fName.trim(), lName.trim(), password, emailValidationSignedToken, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String accessToken) {
				view.setLoading(false);
				// success, send to login place to continue login process (sign terms of use)
				if (accountCreationToken.getEncodedMembershipInvtnSignedToken() != null) {
					globalAppState.setLastPlace(new EmailInvitation(accountCreationToken.getEncodedMembershipInvtnSignedToken()));
				}
				// After creating the new user, update the access token
				authController.setNewAccessToken(accessToken, getSetNewAccessTokenCallback());
			}

			@Override
			public void onFailure(Throwable caught) {
				view.setLoading(false);
				synAlert.handleException(caught);
			}
		});
	}

	public AsyncCallback<UserProfile> getSetNewAccessTokenCallback() {
		return new AsyncCallback<UserProfile>() {
			@Override
			public void onSuccess(UserProfile result) {
				globalAppState.getPlaceChanger().goTo(new Profile(Profile.VIEW_PROFILE_TOKEN));
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.setLoading(false);
				synAlert.handleException(caught);
			}
		};
	}

	/**
	 * check that the email is available
	 * 
	 * @param email
	 */
	public void checkEmailAvailable(String email) {
		synapseClient.isAliasAvailable(email, AliasType.USER_EMAIL.toString(), new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean isAvailable) {
				if (!isAvailable) {
					view.showErrorMessage(DisplayConstants.ERROR_EMAIL_ALREADY_EXISTS);
					globalAppState.gotoLastPlace();
				}
			}

			@Override
			public void onFailure(Throwable e) {
				// do nothing. validation has failed, but updating the email will fail if it's already taken.
				e.printStackTrace();
			}
		});
	}

	/**
	 * Check that the username/alias is available
	 * 
	 * @param username
	 */
	public void checkUsernameAvailable(String username) {
		if (username.trim().length() > 3) {
			synapseClient.isAliasAvailable(username, AliasType.USER_NAME.toString(), new AsyncCallback<Boolean>() {
				@Override
				public void onSuccess(Boolean isAvailable) {
					if (!isAvailable)
						view.markUsernameUnavailable();
				}

				@Override
				public void onFailure(Throwable e) {
					// do nothing. validation has failed, but updating the username will fail if it's already taken.
					e.printStackTrace();
				}
			});
		}
	}

	@Override
	public String mayStop() {
		view.clear();
		return null;
	}
}
