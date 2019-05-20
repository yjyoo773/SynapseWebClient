package org.sagebionetworks.web.unitclient.widget.profile;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserBundle;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.UserProfileClientAsync;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.client.widget.profile.ProfileCertifiedValidatedView;
import org.sagebionetworks.web.client.widget.profile.ProfileCertifiedValidatedWidget;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class ProfileCertifiedValidatedWidgetTest {
	@Mock
	ProfileCertifiedValidatedView mockView;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	UserBundle mockUserBundle;
	
	ProfileCertifiedValidatedWidget widget;
	
	@Before
	public void setUp(){
		MockitoAnnotations.initMocks(this);
		widget = new ProfileCertifiedValidatedWidget(mockView, mockSynapseJavascriptClient);
		AsyncMockStubber.callSuccessWith(mockUserBundle).when(mockSynapseJavascriptClient).getUserBundle(anyLong(), anyInt(), any(AsyncCallback.class));
	}
	
	@Test
	public void testLoadData() {
		Long userId = 87663L;
		boolean isCertified = false;
		boolean isVerified = true;
		when(mockUserBundle.getIsCertified()).thenReturn(isCertified);
		when(mockUserBundle.getIsVerified()).thenReturn(isVerified);
		
		widget.configure(userId);
		
		verify(mockSynapseJavascriptClient).getUserBundle(eq(userId), anyInt(), any(AsyncCallback.class));
		verify(mockView).setCertifiedVisible(isCertified);
		verify(mockView).setVerifiedVisible(isVerified);
	}
	@Test
	public void testLoadDataError() {
		String errorMessage = "there was an error";
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockSynapseJavascriptClient).getUserBundle(anyLong(), anyInt(), any(AsyncCallback.class));
		widget.loadData();
		verify(mockView).setError(errorMessage);
	}
}
