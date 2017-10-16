package org.sagebionetworks.web.client.widget.entity.tabs;

import static org.sagebionetworks.repo.model.EntityBundle.ANNOTATIONS;
import static org.sagebionetworks.repo.model.EntityBundle.BENEFACTOR_ACL;
import static org.sagebionetworks.repo.model.EntityBundle.DOI;
import static org.sagebionetworks.repo.model.EntityBundle.ENTITY;
import static org.sagebionetworks.repo.model.EntityBundle.ENTITY_PATH;
import static org.sagebionetworks.repo.model.EntityBundle.HAS_CHILDREN;
import static org.sagebionetworks.repo.model.EntityBundle.PERMISSIONS;
import static org.sagebionetworks.repo.model.EntityBundle.TABLE_DATA;

import java.util.Map;

import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.Project;
import org.sagebionetworks.repo.model.table.Query;
import org.sagebionetworks.repo.model.table.Table;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.events.EntityUpdatedHandler;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.EntityArea;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.breadcrumb.Breadcrumb;
import org.sagebionetworks.web.client.widget.entity.EntityMetadata;
import org.sagebionetworks.web.client.widget.entity.ModifiedCreatedByWidget;
import org.sagebionetworks.web.client.widget.entity.controller.StuAlert;
import org.sagebionetworks.web.client.widget.entity.file.BasicTitleBar;
import org.sagebionetworks.web.client.widget.entity.menu.v2.ActionMenuWidget;
import org.sagebionetworks.web.client.widget.provenance.ProvenanceWidget;
import org.sagebionetworks.web.client.widget.table.QueryChangeHandler;
import org.sagebionetworks.web.client.widget.table.TableListWidget;
import org.sagebionetworks.web.client.widget.table.v2.QueryTokenProvider;
import org.sagebionetworks.web.client.widget.table.v2.TableEntityWidget;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class TablesTab implements TablesTabView.Presenter, QueryChangeHandler{
	
	public static final String TABLE_QUERY_PREFIX = "query/";
	
	Tab tab;
	TablesTabView view;
	TableListWidget tableListWidget;
	BasicTitleBar tableTitleBar;
	Breadcrumb breadcrumb;
	EntityMetadata metadata;
	boolean annotationsShown;
	EntityUpdatedHandler handler;
	QueryTokenProvider queryTokenProvider;
	Entity entity;
	EntityBundle projectBundle;
	Throwable projectBundleLoadError;
	String projectEntityId;
	String areaToken;
	StuAlert synAlert;
	PortalGinInjector ginInjector;
	ModifiedCreatedByWidget modifiedCreatedBy;
	CallbackP<Boolean> showProjectInfoCallack;
	TableEntityWidget v2TableWidget;
	Map<String,String> configMap;
	SynapseJavascriptClient jsClient;
	ActionMenuWidget entityActionMenu;
	
	public static final String TABLES_HELP = "Build structured queryable data that can be described by a schema using the Tables.";
	public static final String TABLES_HELP_URL = WebConstants.DOCS_URL + "tables.html";
	
	@Inject
	public TablesTab(Tab tab,
			PortalGinInjector ginInjector
			) {
		this.tab = tab;
		this.ginInjector = ginInjector;
		tab.configure("Tables", TABLES_HELP, TABLES_HELP_URL);
	}
	
	public void lazyInject() {
		if (view == null) {
			this.view = ginInjector.getTablesTabView();
			this.tableListWidget = ginInjector.getTableListWidget();
			this.tableTitleBar = ginInjector.getBasicTitleBar();
			this.breadcrumb = ginInjector.getBreadcrumb();
			this.metadata = ginInjector.getEntityMetadata();
			this.queryTokenProvider = ginInjector.getQueryTokenProvider();
			this.synAlert = ginInjector.getStuAlert();
			this.jsClient = ginInjector.getSynapseJavascriptClient();
			this.modifiedCreatedBy = ginInjector.getModifiedCreatedByWidget();
			
			view.setBreadcrumb(breadcrumb.asWidget());
			view.setTableList(tableListWidget.asWidget());
			view.setTitlebar(tableTitleBar.asWidget());
			view.setEntityMetadata(metadata.asWidget());
			view.setSynapseAlert(synAlert.asWidget());
			view.setModifiedCreatedBy(modifiedCreatedBy);
			tab.setContent(view.asWidget());
			
			tableListWidget.setTableClickedCallback(new CallbackP<String>() {
				@Override
				public void invoke(String entityId) {
					areaToken = null;
					getTargetBundleAndDisplay(entityId);
				}
			});
			initBreadcrumbLinkClickedHandler();
			configMap = ProvenanceWidget.getDefaultWidgetDescriptor();
		}
	}
	
	@Override
	public void onPersistSuccess(EntityUpdatedEvent event) {
		if (handler != null) {
			handler.onPersistSuccess(event);
		}
	}
	
	public void initBreadcrumbLinkClickedHandler() {
		CallbackP<Place> breadcrumbClicked = new CallbackP<Place>() {
			public void invoke(Place place) {
				//if this is the project id, then just reconfigure from the project bundle
				Synapse synapse = (Synapse)place;
				String entityId = synapse.getEntityId();
				if (entityId.equals(projectEntityId)) {
				    showProjectLevelUI();
				} else {
				    getTargetBundleAndDisplay(entityId);
				}
			};
		};
		breadcrumb.setLinkClickedHandler(breadcrumbClicked);
	}
	
	public void setTabClickedCallback(CallbackP<Tab> onClickCallback) {
		tab.addTabClickedCallback(onClickCallback);
	}
	
	public void setProject(String projectEntityId, EntityBundle projectBundle, Throwable projectBundleLoadError) {
		this.projectEntityId = projectEntityId;
		this.projectBundle = projectBundle;
		this.projectBundleLoadError = projectBundleLoadError;
	}
	
	public void configure(Entity entity, EntityUpdatedHandler handler, String areaToken, ActionMenuWidget entityActionMenu) {
		lazyInject();
		this.entity = entity;
		this.areaToken = areaToken;
		this.handler = handler;
		this.entityActionMenu = entityActionMenu;
		metadata.setEntityUpdatedHandler(handler);
		synAlert.clear();
		boolean isTable = entity instanceof Table;
		
		if (!isTable) {
			//configure based on project
			showProjectLevelUI();
		} else {
			getTargetBundleAndDisplay(entity.getId());
		}
	}
	
	public void showProjectLevelUI() {
		String title = projectEntityId;
		if (projectBundle != null) {
			title = projectBundle.getEntity().getName();
			setTargetBundle(projectBundle);	
		} else {
			showError(projectBundleLoadError);
		}
		tab.setEntityNameAndPlace(title, new Synapse(projectEntityId, null, EntityArea.TABLES, null));
		tab.showTab(true);
	}
	
	public void resetView() {
		if (view != null) {
			synAlert.clear();
			view.setEntityMetadataVisible(false);
			view.setBreadcrumbVisible(false);
			view.setTableListVisible(false);
			view.setTitlebarVisible(false);
			showProjectInfoCallack.invoke(false);
			view.clearActionMenuContainer();
			view.clearTableEntityWidget();
			modifiedCreatedBy.setVisible(false);
			view.setProvenanceVisible(false);
		}
	}
	
	public void showError(Throwable error) {
		resetView();
		synAlert.handleException(error);
	}
	
	public void setTargetBundle(EntityBundle bundle) {
		this.entity = bundle.getEntity();
		boolean isTable = entity instanceof Table;
		boolean isProject = entity instanceof Project;
		view.setEntityMetadataVisible(isTable);
		view.setBreadcrumbVisible(isTable);
		view.setTableListVisible(isProject);
		view.setTitlebarVisible(isTable);
		showProjectInfoCallack.invoke(isProject);
		view.clearActionMenuContainer();
		view.clearTableEntityWidget();
		modifiedCreatedBy.setVisible(false);
		view.setProvenanceVisible(isTable);
		if (isTable) {
			breadcrumb.configure(bundle.getPath(), EntityArea.TABLES);
			metadata.setEntityBundle(bundle, null);
			tableTitleBar.configure(bundle);
			modifiedCreatedBy.configure(entity.getCreatedOn(), entity.getCreatedBy(), entity.getModifiedOn(), entity.getModifiedBy());
			v2TableWidget = ginInjector.createNewTableEntityWidget();
			view.setTableEntityWidget(v2TableWidget.asWidget());
			v2TableWidget.configure(bundle, bundle.getPermissions().getCanCertifiedUserEdit(), this, entityActionMenu);
			ProvenanceWidget provWidget = ginInjector.getProvenanceRenderer();
			configMap.put(WidgetConstants.PROV_WIDGET_DISPLAY_HEIGHT_KEY, Integer.toString(FilesTab.WIDGET_HEIGHT_PX-84));
			configMap.put(WidgetConstants.PROV_WIDGET_ENTITY_LIST_KEY, DisplayUtils.createEntityVersionString(entity.getId(), null));
			view.setProvenance(provWidget);
			provWidget.configure(configMap);
		} else if (isProject) {
			areaToken = null;
			tableListWidget.configure(bundle);
		}
	}
	
	public void getTargetBundleAndDisplay(final String entityId) {
		synAlert.clear();
		int mask = ENTITY | ANNOTATIONS | PERMISSIONS | ENTITY_PATH | HAS_CHILDREN | DOI | TABLE_DATA | BENEFACTOR_ACL;
		AsyncCallback<EntityBundle> callback = new AsyncCallback<EntityBundle>() {
			@Override
			public void onSuccess(EntityBundle bundle) {
				tab.setEntityNameAndPlace(bundle.getEntity().getName(), new Synapse(entityId, null, EntityArea.TABLES, null));
				setTargetBundle(bundle);
				// note: let TableEntityWidget query control browser history.  when the query is run, push url into the stack. 
				tab.showTab(false);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				tab.setEntityNameAndPlace(entityId, new Synapse(entityId, null, EntityArea.TABLES, null));
				showError(caught);
				tab.showTab(false);
			}			
		};
		
		jsClient.getEntityBundle(entityId, mask, callback);
	}
	
	public Tab asTab(){
		return tab;
	}
	
	public void onQueryChange(Query newQuery) {
		if(newQuery != null && tab.isTabPaneVisible()) {
			String token = queryTokenProvider.queryToToken(newQuery);
			if(token != null && !newQuery.equals(v2TableWidget.getDefaultQuery())){
				areaToken = TABLE_QUERY_PREFIX + token;
			} else {
				areaToken = "";
			}
			tab.setEntityNameAndPlace(entity.getName(), new Synapse(entity.getId(), null, EntityArea.TABLES, areaToken));
			tab.showTab(true);
		}
	}
	
	public void setShowProjectInfoCallback(CallbackP<Boolean> callback) {
		showProjectInfoCallack = callback;
		tab.addTabClickedCallback(new CallbackP<Tab>() {
			@Override
			public void invoke(Tab param) {
				boolean isProject = entity instanceof Project;
				showProjectInfoCallack.invoke(isProject);
			}
		});

	}
	
	public Query getQueryString() {
		if(areaToken != null && areaToken.startsWith(TABLE_QUERY_PREFIX)) {
			String token = areaToken.substring(TABLE_QUERY_PREFIX.length(), areaToken.length());
			if(token != null){
				return queryTokenProvider.tokenToQuery(token);
			}
		}
		return null;
	}
	
	public Entity getCurrentEntity() {
		return entity;
	}
}
