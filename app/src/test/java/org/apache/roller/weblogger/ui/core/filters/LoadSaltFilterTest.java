package org.apache.roller.weblogger.ui.core.filters;

import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.ui.core.RollerSession;
import org.apache.roller.weblogger.ui.rendering.util.cache.SaltCache;
import org.apache.roller.weblogger.ui.struts2.util.UIBeanFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;

public class LoadSaltFilterTest {

    private LoadSaltFilter filter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Mock
    private RollerSession rollerSession;

    @Mock
    private SaltCache saltCache;

    @BeforeEach
    public void setUp() throws ServletException {
        MockitoAnnotations.openMocks(this);

        try (MockedStatic<UIBeanFactory> mockedFactory = mockStatic(UIBeanFactory.class)) {
            mockedFactory.when(() -> UIBeanFactory.getBean(RollerSession.class))
                .thenReturn(rollerSession);

            filter = new LoadSaltFilter();
            filter.init(mock(FilterConfig.class));
        }
    }

    @Test
    public void testDoFilterGeneratesSalt() throws Exception {
        try (MockedStatic<SaltCache> mockedSaltCache = mockStatic(SaltCache.class)) {
            mockedSaltCache.when(SaltCache::getInstance).thenReturn(saltCache);

            filter.doFilter(request, response, chain);
            verify(request).setAttribute(eq("salt"), anyString());
            verify(chain).doFilter(request, response);
        }
    }

    @Test
    public void testDoFilterWithNullRollerSession() throws Exception {
        try (MockedStatic<UIBeanFactory> mockedUIBeanFactory = mockStatic(UIBeanFactory.class)) {
            mockedUIBeanFactory.when(() -> UIBeanFactory.getBean(RollerSession.class))
                    .thenReturn(null);

            filter.init(mock(FilterConfig.class));
            filter.doFilter(request, response, chain);

            verify(request, never()).setAttribute(eq("salt"), anyString());
            verify(chain).doFilter(request, response);
        }
    }

    private static class TestUser extends User {
        private final String id;

        TestUser(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }
}
