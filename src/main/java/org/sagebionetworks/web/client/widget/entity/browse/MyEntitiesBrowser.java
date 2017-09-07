package org.sagebionetworks.web.client.widget.entity.browse;

import static org.sagebionetworks.repo.model.EntityBundle.ENTITY_PATH;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.EntityPath;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.ProjectHeader;
import org.sagebionetworks.repo.model.ProjectListSortColumn;
import org.sagebionetworks.repo.model.ProjectListType;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.ProjectPagedResults;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class MyEntitiesBrowser implements MyEntitiesBrowserView.Presenter, SynapseWidgetPresenter {
	
	public static final int ZERO_OFFSET = 0;
	public static final int PROJECT_LIMIT = 20;
	private MyEntitiesBrowserView view;	
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private SynapseClientAsync synapseClient;
	private SynapseJavascriptClient jsClient;
	private SelectedHandler selectedHandler;
	private Place cachedPlace;
	private String cachedUserId;
	int userUpdatableOffset = ZERO_OFFSET;
	public interface SelectedHandler {
		void onSelection(String selectedEntityId);
	}
	
	@Inject
	public MyEntitiesBrowser(MyEntitiesBrowserView view,
			AuthenticationController authenticationController,
			final GlobalApplicationState globalApplicationState,
			SynapseClientAsync synapseClient,
			JSONObjectAdapter jsonObjectAdapter,
			SynapseJavascriptClient jsClient) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.jsClient = jsClient;
		// default selection behavior is to do nothing
		this.selectedHandler = new SelectedHandler() {			
			@Override
			public void onSelection(String selectedEntityId) {								
			}
		};
		
		view.setPresenter(this);
	}	

	public void clearState() {
		if (isSameContext()) {
			view.clearSelection();
		} else {
			view.clear();
		}
	}

	@Override
	public Widget asWidget() {
		view.setPresenter(this);
		refresh();
		return view.asWidget();
	}
	
	public void refresh() {
		//do not reload if the session is unchanged, and the context (project) is unchanged.
		if (!isSameContext()) {
			//reset user updatable entities
			view.getEntityTreeBrowser().clear();
			view.setIsMoreUpdatableEntities(true);
			userUpdatableOffset = ZERO_OFFSET;
			loadCurrentContext();
			//note: no need to load user updateable entities manually, since the LoadMoreWidgetContainer will invoke when the loading icon is in view
			loadFavorites();
			updateContext();
		}
	}

	public boolean isSameContext() {
		if (globalApplicationState.getCurrentPlace() == null || authenticationController.getCurrentUserPrincipalId() == null) {
			return false;
		}
		return globalApplicationState.getCurrentPlace().equals(cachedPlace) && authenticationController.getCurrentUserPrincipalId().equals(cachedUserId);
	}
	public void updateContext() {
		cachedPlace = globalApplicationState.getCurrentPlace();
		cachedUserId = authenticationController.getCurrentUserPrincipalId();
	}
	
	public void clearCurrentContent() {
		cachedPlace = null;
		cachedUserId = null;
	}
	
	public Place getCachedCurrentPlace() {
		return cachedPlace;
	}
	public String getCachedUserId() {
		return cachedUserId;
	}
	
	/**
	 * Define custom handling for when an entity is clicked
	 * @param handler
	 */
	public void setEntitySelectedHandler(SelectedHandler handler) {
		selectedHandler = handler;
	}

	@Override
	public void entitySelected(String selectedEntityId) {
		selectedHandler.onSelection(selectedEntityId);
	}

	public void loadCurrentContext() {
		view.getCurrentContextTreeBrowser().clear();
		//get the entity path, and ask for each entity to add to the tree
		Place currentPlace = globalApplicationState.getCurrentPlace();
		boolean isSynapsePlace = currentPlace instanceof Synapse;
		view.setCurrentContextTabVisible(isSynapsePlace);
		if (isSynapsePlace) {
			String entityId = ((Synapse) currentPlace).getEntityId();
			int mask = ENTITY_PATH;
			jsClient.getEntityBundle(entityId, mask, new AsyncCallback<EntityBundle>() {
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(caught.getMessage());
				}
				
				public void onSuccess(EntityBundle result) {
					EntityPath path = result.getPath();
					List<EntityHeader> pathHeaders = path.getPath();
					//remove the high level root, so that the first item in the list is the Project
					List<EntityHeader> projectHeader = new ArrayList<EntityHeader>();
					if (pathHeaders.size() > 1) {
						projectHeader.add(pathHeaders.get(1));		
					}
					//add to the current context tree, and show all children of this container (or siblings if leaf)
					view.getCurrentContextTreeBrowser().configure(projectHeader);
				};
			});
		}
	}
	@Override
	public void loadMoreUserUpdateable() {
		if (authenticationController.isLoggedIn()) {
			synapseClient.getMyProjects(ProjectListType.MY_CREATED_PROJECTS, PROJECT_LIMIT, userUpdatableOffset, ProjectListSortColumn.PROJECT_NAME, SortDirection.ASC, new AsyncCallback<ProjectPagedResults>() {
				@Override
				public void onSuccess(ProjectPagedResults projectHeaders) {
					List<EntityHeader> headers = new ArrayList<EntityHeader>();
					for (ProjectHeader result : projectHeaders.getResults()) {
						EntityHeader h = new EntityHeader();
						h.setType(Project.class.getName());
						h.setId(result.getId());
						h.setName(result.getName());
						headers.add(h);
					};
					view.addUpdatableEntities(headers);
					userUpdatableOffset += PROJECT_LIMIT;
					view.setIsMoreUpdatableEntities(userUpdatableOffset < projectHeaders.getTotalNumberOfResults());
				}
				@Override
				public void onFailure(Throwable caught) {
					view.showErrorMessage(caught.getMessage());
				}
			});
		}
	}
	
	public EntityTreeBrowser getEntityTreeBrowser() {
		return view.getEntityTreeBrowser();
	}
	
	public EntityTreeBrowser getFavoritesTreeBrowser() {
		return view.getFavoritesTreeBrowser();
	}

	@Override
	public void loadFavorites() {
		view.getFavoritesTreeBrowser().clear();
		EntityBrowserUtils.loadFavorites(jsClient, globalApplicationState, new AsyncCallback<List<EntityHeader>>() {
			@Override
			public void onSuccess(List<EntityHeader> result) {
				view.setFavoriteEntities(result);
			}
			@Override
			public void onFailure(Throwable caught) {
				if(!DisplayUtils.handleServiceException(caught, globalApplicationState, authenticationController.isLoggedIn(), view))
					view.showErrorMessage(DisplayConstants.ERROR_GENERIC_RELOAD);
			}
		});
	}
	
	public void setEntityFilter(EntityFilter filter) {
		getEntityTreeBrowser().setEntityFilter(filter);
		getFavoritesTreeBrowser().setEntityFilter(filter);
		view.getCurrentContextTreeBrowser().setEntityFilter(filter);
		clearCurrentContent();
		refresh();
	}
	
	public EntityFilter getEntityFilter() {
		return getEntityTreeBrowser().getEntityFilter();
	}

	public int getUserUpdatableOffset() {
		return userUpdatableOffset;
	}
	
	/*
	 * Private Methods
	 */
}
