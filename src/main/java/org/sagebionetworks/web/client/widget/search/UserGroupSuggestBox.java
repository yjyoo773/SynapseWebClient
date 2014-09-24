package org.sagebionetworks.web.client.widget.search;

import java.util.LinkedList;
import java.util.List;

import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SageImageBundle;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestBox.UserGroupSuggestOracle.UserGroupSuggestion;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UserGroupSuggestBox implements UserGroupSuggestBoxView.Presenter, SynapseWidgetPresenter {
	public static final int DELAY = 750;	// milliseconds
	public static final int PAGE_SIZE = 10;
	
	private UserGroupSuggestBoxView view;
	private UserGroupSuggestOracle oracle;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private SynapseClientAsync synapseClient;
	
	private static String baseFileHandleUrl;
	private static String baseProfileAttachmentUrl;
	
	private UserGroupSuggestion selectedSuggestion;
	private int offset;		// suggestion offset for paging
	
	@Inject
	public UserGroupSuggestBox(UserGroupSuggestBoxView view,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			SageImageBundle sageImageBundle) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		
		oracle = view.getUserGroupSuggestOracle();
		view.setPresenter(this);
	}
	
	public void configureURLs(String baseFileHandleUrl, String baseProfileAttachmentUrl) {
		this.baseFileHandleUrl = baseFileHandleUrl;
		this.baseProfileAttachmentUrl = baseProfileAttachmentUrl;
	}
	
	@Override
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();
	}
	
	@Override
	public void getPrevSuggestions() {
		offset -= PAGE_SIZE;
		getSuggestions(oracle.getRequest(), oracle.getCallback());
	}

	@Override
	public void getNextSuggestions() {
		offset += PAGE_SIZE;
		getSuggestions(oracle.getRequest(), oracle.getCallback());
	}
	
	public void getSuggestions(final SuggestOracle.Request request, final SuggestOracle.Callback callback) {
		view.showLoading();
		
		String prefix = request.getQuery();
		final List<Suggestion> suggestions = new LinkedList<Suggestion>();
		
		synapseClient.getUserGroupHeadersByPrefix(prefix, PAGE_SIZE, offset, new AsyncCallback<UserGroupHeaderResponsePage>() {
			@Override
			public void onSuccess(UserGroupHeaderResponsePage result) {
				// Update view fields.
				view.updateFieldStateForSuggestions(result, offset);
				
				// Load suggestions.
				for (UserGroupHeader header : result.getChildren()) {
					suggestions.add(oracle.new UserGroupSuggestion(header, view.getText()));
				}

				// Set up response
				SuggestOracle.Response response = new SuggestOracle.Response(suggestions);
				callback.onSuggestionsReady(request, response);
				
				view.hideLoading();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if (!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view)) {                    
					view.showErrorMessage(caught.getMessage());
				}
			}

		});
	}
	
	@Override
	public UserGroupSuggestion getSelectedSuggestion() {
		return selectedSuggestion;
	}

	@Override
	public void setSelectedSuggestion(UserGroupSuggestion selectedSuggestion) {
		this.selectedSuggestion = selectedSuggestion;
	}
	
	public String getText() {
		return view.getText();
	}
	
	public void clear() {
		view.clear();
	}
	
	
	/*
	 * SuggestOracle
	 */
	public static class UserGroupSuggestOracle extends SuggestOracle {
		private SuggestOracle.Request request;
		private SuggestOracle.Callback callback;
		
		private UserGroupSuggestBox suggestBox;
		
		public void configure(UserGroupSuggestBoxView view, UserGroupSuggestBox suggestBox) {
			this.suggestBox = suggestBox;
		}
		
		private Timer timer = new Timer() {

			@Override
			public void run() {
				
				// If you backspace quickly the contents of the field are emptied but a
				// query for a single character is still executed. Workaround for this
				// is to check for an empty string field here.
				if (!suggestBox.getText().trim().isEmpty()) {
					suggestBox.getSuggestions(request, callback);
				}
			}
			
		};
		
		@Override
		public boolean isDisplayStringHTML() {
			return true;
		}
		
		@Override
		public void requestSuggestions(SuggestOracle.Request request, SuggestOracle.Callback callback) {
			this.request = request;
			this.callback = callback;
			
			timer.cancel();
			timer.schedule(DELAY);
		}
		
		public SuggestOracle.Request getRequest()	{	return request;		}
		public SuggestOracle.Callback getCallback()	{	return callback;	}


		/*
		 * Suggestion
		 */
		public class UserGroupSuggestion implements IsSerializable, Suggestion {
			private UserGroupHeader header;
			private String prefix;
			
			public UserGroupSuggestion(UserGroupHeader header, String prefix) {
				this.header = header;
				this.prefix = prefix;
			}
			
			public UserGroupHeader getHeader()		{	return header;			}
			public String getPrefix() 				{	return prefix;			}
			public void setPrefix(String prefix)	{	this.prefix = prefix;	}
			
			@Override
			public String getDisplayString() {
				StringBuilder result = new StringBuilder();
				result.append("<div class=\"padding-left-5 userGroupSuggestion\" style=\"height:23px; width:375px;\">");
				result.append("<img class=\"margin-right-5 vertical-align-center tiny-thumbnail-image-container\" onerror=\"this.style.display=\'none\';\" src=\"");
				if (header.getIsIndividual()) {
					result.append(baseProfileAttachmentUrl);
					result.append("?userId=" + header.getOwnerId() + "&waitForUrl=true\" />");
				} else {
					result.append(baseFileHandleUrl);
					result.append("?teamId=" + header.getOwnerId() + "\" />");
				}
				result.append("<span class=\"search-item movedown-1 margin-right-5\">");
				if (header.getIsIndividual()) {
					result.append("<span class=\"font-italic\">" + header.getFirstName() + " " + header.getLastName() + "</span> ");
				}
				result.append("<span>" + header.getUserName() + "</span> ");
				result.append("</span>");
				if (!header.getIsIndividual()) {
					result.append("(Team)");
				}
				result.append("</div>");
				return result.toString();
			}

			@Override
			public String getReplacementString() {
				// Example output:
				// Pac Man  |  114085
				StringBuilder sb = new StringBuilder();
				if (!header.getIsIndividual())
					sb.append("(Team) ");
				
				String firstName = header.getFirstName();
				String lastName = header.getLastName();
				String username = header.getUserName();
				sb.append(DisplayUtils.getDisplayName(firstName, lastName, username));
				sb.append("  |  " + header.getOwnerId());
				return sb.toString();
			}
			
		} // end inner class UserGroupSuggestion	
	} // end inner class UserGroupSuggestOracle
}
