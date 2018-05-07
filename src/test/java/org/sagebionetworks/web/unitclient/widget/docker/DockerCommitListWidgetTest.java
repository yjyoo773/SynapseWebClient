package org.sagebionetworks.web.unitclient.widget.docker;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.docker.DockerCommit;
import org.sagebionetworks.repo.model.docker.DockerCommitSortBy;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.RadioWidget;
import org.sagebionetworks.web.client.widget.docker.DockerCommitListWidget;
import org.sagebionetworks.web.client.widget.docker.DockerCommitListWidgetView;
import org.sagebionetworks.web.client.widget.docker.DockerCommitRowWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

public class DockerCommitListWidgetTest {
	@Mock
	private DockerCommitListWidgetView mockView;
	@Mock
	private SynapseJavascriptClient mockJsClient;
	@Mock
	private SynapseAlert mockSynAlert;
	@Mock
	private LoadMoreWidgetContainer mockCommitsContainer;
	@Mock
	private PortalGinInjector mockGinInjector;
	@Mock
	private DockerCommitRowWidget mockCommitRow;
	@Mock
	private RadioWidget mockRadioWidget;
	@Mock
	private GWTWrapper mockGwtWrapper;
	@Mock
	private Callback mockCallback;
	private DockerCommitListWidget dockerCommitListWidget;
	private String entityId;
	private List<DockerCommit> dockerCommitList;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		dockerCommitListWidget = new DockerCommitListWidget(mockView, mockJsClient, mockSynAlert, mockCommitsContainer, mockGinInjector, mockGwtWrapper);

		entityId = "syn123";
		dockerCommitList = new ArrayList<DockerCommit>();
		AsyncMockStubber.callSuccessWith(dockerCommitList)
			.when(mockJsClient).getDockerCommits(anyString(), anyLong(), anyLong(), any(DockerCommitSortBy.class), anyBoolean(), any(AsyncCallback.class));
	}

	@Test
	public void testConstructor() {
		verify(mockView).setPresenter(dockerCommitListWidget);
		verify(mockView).setCommitsContainer(mockCommitsContainer);
		verify(mockView).setSynAlert(any(Widget.class));
		ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
		verify(mockCommitsContainer).configure(captor.capture());
		Callback callback = captor.getValue();
		callback.invoke();
		verify(mockSynAlert).clear();
		verify(mockJsClient).getDockerCommits(anyString(), anyLong(), anyLong(), any(DockerCommitSortBy.class), anyBoolean(), any(AsyncCallback.class));
	}

	@Test
	public void testConfigure() {
		boolean withRadio = false;
		dockerCommitListWidget.configure(entityId, withRadio);
		verify(mockCommitsContainer).clear();
		verify(mockSynAlert).clear();
		verify(mockJsClient).getDockerCommits(eq(entityId), anyLong(), anyLong(), any(DockerCommitSortBy.class), anyBoolean(), any(AsyncCallback.class));
	}

	@Test
	public void testLoadMoreZeroResult() {
		boolean withRadio = false;
		dockerCommitListWidget.setEmptyListCallback(mockCallback);
		dockerCommitListWidget.configure(entityId, withRadio);
		verify(mockCommitsContainer).clear();
		verify(mockSynAlert).clear();
		verify(mockJsClient).getDockerCommits(eq(entityId),
				anyLong(), anyLong(), any(DockerCommitSortBy.class),
				anyBoolean(), any(AsyncCallback.class));
		verify(mockCommitsContainer, never()).add(any(Widget.class));
		verify(mockCommitsContainer).setIsMore(false);
		verify(mockCallback).invoke();
	}

	@Test
	public void testLoadMoreSuccess() {
		boolean withRadio = false;
		when(mockGinInjector.createNewDockerCommitRowWidget()).thenReturn(mockCommitRow);
		DockerCommit commit = new DockerCommit();
		dockerCommitList.add(commit);
		dockerCommitListWidget.configure(entityId, withRadio);
		verify(mockCommitRow).configure(commit);
		verify(mockCommitsContainer).clear();
		verify(mockSynAlert).clear();
		verify(mockJsClient).getDockerCommits(eq(entityId),
				anyLong(), anyLong(), any(DockerCommitSortBy.class),
				anyBoolean(), any(AsyncCallback.class));
		verify(mockCommitsContainer).add(any(Widget.class));
		verify(mockCommitsContainer).setIsMore(false);
	}

	@Test
	public void testLoadMoreSuccessWithRadio() {
		boolean withRadio = true;
		String id = "uniqueId";
		when(mockGwtWrapper.getUniqueElementId()).thenReturn(id);
		when(mockGinInjector.createNewDockerCommitRowWidget()).thenReturn(mockCommitRow);
		when(mockGinInjector.createNewRadioWidget()).thenReturn(mockRadioWidget);
		DockerCommit commit = new DockerCommit();
		dockerCommitList.add(commit);
		dockerCommitListWidget.configure(entityId, withRadio);
		verify(mockCommitRow).configure(commit);
		verify(mockRadioWidget).add(any(Widget.class));
		verify(mockRadioWidget).setGroupName(id);
		verify(mockRadioWidget).addClickHandler(any(ClickHandler.class));
		verify(mockCommitsContainer).clear();
		verify(mockSynAlert).clear();
		verify(mockJsClient).getDockerCommits(eq(entityId),
				anyLong(), anyLong(), any(DockerCommitSortBy.class),
				anyBoolean(), any(AsyncCallback.class));
		verify(mockCommitsContainer).add(any(Widget.class));
		verify(mockCommitsContainer).setIsMore(false);
	}

	@Test
	public void testLoadMoreFailure() {
		boolean withRadio = false;
		Throwable exception = new Throwable();
		AsyncMockStubber.callFailureWith(exception)
				.when(mockJsClient).getDockerCommits(anyString(),
						anyLong(), anyLong(), any(DockerCommitSortBy.class),
						anyBoolean(), any(AsyncCallback.class));
		dockerCommitListWidget.configure(entityId, withRadio);
		verify(mockCommitsContainer).clear();
		verify(mockSynAlert).clear();
		verify(mockJsClient).getDockerCommits(eq(entityId),
				anyLong(), anyLong(), any(DockerCommitSortBy.class),
				anyBoolean(), any(AsyncCallback.class));
		verify(mockCommitsContainer, never()).add(any(Widget.class));
		verify(mockSynAlert).handleException(exception);
		verify(mockCommitsContainer).setIsMore(false);
	}
}
