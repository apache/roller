package org.apache.roller.weblogger.ui.core.filters;

import org.apache.roller.weblogger.config.WebloggerConfig;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class ValidateSaltFilterTest {

    private ValidateSaltFilter filter;

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
        filter = new ValidateSaltFilter();
        try (MockedStatic<UIBeanFactory> mockedUIBeanFactory = mockStatic(UIBeanFactory.class)) {
            mockedUIBeanFactory.when(() -> UIBeanFactory.getBean(RollerSession.class)).thenReturn(rollerSession);
            filter.init(mock(FilterConfig.class));
        }
    }

    @Test
    public void testDoFilterWithGetMethod() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        StringBuffer requestURL = new StringBuffer("https://example.com/app/ignoredurl");
        when(request.getRequestURL()).thenReturn(requestURL);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    public void testDoFilterWithPostMethodAndValidSalt() throws Exception {
        try (MockedStatic<SaltCache> mockedSaltCache = mockStatic(SaltCache.class)) {
            mockedSaltCache.when(SaltCache::getInstance).thenReturn(saltCache);

            when(request.getMethod()).thenReturn("POST");
            when(request.getParameter("salt")).thenReturn("validSalt");
            when(saltCache.get("validSalt")).thenReturn("userId");
            when(rollerSession.getAuthenticatedUser()).thenReturn(new TestUser("userId"));
            StringBuffer requestURL = new StringBuffer("https://example.com/app/ignoredurl");
            when(request.getRequestURL()).thenReturn(requestURL);

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
            verify(saltCache).remove("validSalt");
        }
    }

    @Test
    public void testDoFilterWithPostMethodAndInvalidSalt() throws Exception {
        try (MockedStatic<SaltCache> mockedSaltCache = mockStatic(SaltCache.class)) {
            mockedSaltCache.when(SaltCache::getInstance).thenReturn(saltCache);

            when(request.getMethod()).thenReturn("POST");
            when(request.getParameter("salt")).thenReturn("invalidSalt");
            when(saltCache.get("invalidSalt")).thenReturn(null);
            StringBuffer requestURL = new StringBuffer("https://example.com/app/ignoredurl");
            when(request.getRequestURL()).thenReturn(requestURL);

            assertThrows(ServletException.class, () -> {
                filter.doFilter(request, response, chain);
            });
        }
    }

    @Test
    public void testDoFilterWithPostMethodAndMismatchedUserId() throws Exception {
        try (MockedStatic<SaltCache> mockedSaltCache = mockStatic(SaltCache.class)) {
            mockedSaltCache.when(SaltCache::getInstance).thenReturn(saltCache);

            when(request.getMethod()).thenReturn("POST");
            when(request.getParameter("salt")).thenReturn("validSalt");
            when(saltCache.get("validSalt")).thenReturn("differentUserId");
            when(rollerSession.getAuthenticatedUser()).thenReturn(new TestUser("userId"));
            StringBuffer requestURL = new StringBuffer("https://example.com/app/ignoredurl");
            when(request.getRequestURL()).thenReturn(requestURL);

            assertThrows(ServletException.class, () -> {
                filter.doFilter(request, response, chain);
            });
        }
    }

    @Test
    public void testDoFilterWithPostMethodAndNullRollerSession() throws Exception {
        try (MockedStatic<SaltCache> mockedSaltCache = mockStatic(SaltCache.class);
              MockedStatic<UIBeanFactory> mockedUIBeanFactory = mockStatic(UIBeanFactory.class)) {

            mockedSaltCache.when(SaltCache::getInstance).thenReturn(saltCache);
            mockedUIBeanFactory.when(() -> UIBeanFactory.getBean(RollerSession.class)).thenReturn(null);

            when(request.getMethod()).thenReturn("POST");
            when(request.getParameter("salt")).thenReturn("validSalt");
            when(saltCache.get("validSalt")).thenReturn("");
            StringBuffer requestURL = new StringBuffer("https://example.com/app/ignoredurl");
            when(request.getRequestURL()).thenReturn(requestURL);

            filter.init(mock(FilterConfig.class));
            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
            verify(saltCache, never()).remove(anyString());
        }
    }

    @Test
    public void testDoFilterWithIgnoredURL() throws Exception {
        try (MockedStatic<WebloggerConfig> mockedWebloggerConfig = mockStatic(WebloggerConfig.class);
             MockedStatic<UIBeanFactory> mockedUIBeanFactory = mockStatic(UIBeanFactory.class)) {

            mockedWebloggerConfig.when(() -> WebloggerConfig.getProperty("salt.ignored.urls"))
                    .thenReturn("https://example.com/app/ignoredurl?param1=value1&m2=value2");
            mockedUIBeanFactory.when(() -> UIBeanFactory.getBean(RollerSession.class)).thenReturn(rollerSession);

            when(request.getMethod()).thenReturn("POST");
            StringBuffer requestURL = new StringBuffer("https://example.com/app/ignoredurl");
            when(request.getRequestURL()).thenReturn(requestURL);
            when(request.getQueryString()).thenReturn("param1=value1&m2=value2");
            when(request.getParameter("salt")).thenReturn(null);

            filter.init(mock(FilterConfig.class));
            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
            verify(saltCache, never()).get(anyString());
            verify(saltCache, never()).remove(anyString());
        }
    }

    private static class TestUser extends User {
        private final String id;

        TestUser(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }
    }
}