package org.apache.roller.weblogger.ui.core.filters;

import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.ui.core.RollerSession;
import org.apache.roller.weblogger.ui.rendering.util.cache.SaltCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import javax.servlet.FilterChain;
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
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        filter = new ValidateSaltFilter();
    }

    @Test
    public void testDoFilterWithGetMethod() throws Exception {
        when(request.getMethod()).thenReturn("GET");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    public void testDoFilterWithPostMethodAndValidSalt() throws Exception {
        try (MockedStatic<RollerSession> mockedRollerSession = mockStatic(RollerSession.class);
             MockedStatic<SaltCache> mockedSaltCache = mockStatic(SaltCache.class)) {

            mockedRollerSession.when(() -> RollerSession.getRollerSession(request)).thenReturn(rollerSession);
            mockedSaltCache.when(SaltCache::getInstance).thenReturn(saltCache);

            when(request.getMethod()).thenReturn("POST");
            when(request.getParameter("salt")).thenReturn("validSalt");
            when(saltCache.get("validSalt")).thenReturn("userId");
            when(rollerSession.getAuthenticatedUser()).thenReturn(new TestUser("userId"));
            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
            verify(saltCache).remove("validSalt");
        }
    }

    @Test
    public void testDoFilterWithPostMethodAndInvalidSalt() throws Exception {
        try (MockedStatic<RollerSession> mockedRollerSession = mockStatic(RollerSession.class);
             MockedStatic<SaltCache> mockedSaltCache = mockStatic(SaltCache.class)) {

            mockedRollerSession.when(() -> RollerSession.getRollerSession(request)).thenReturn(rollerSession);
            mockedSaltCache.when(SaltCache::getInstance).thenReturn(saltCache);

            when(request.getMethod()).thenReturn("POST");
            when(request.getParameter("salt")).thenReturn("invalidSalt");
            when(saltCache.get("invalidSalt")).thenReturn(null);
            assertThrows(ServletException.class, () -> {
                filter.doFilter(request, response, chain);
            });
        }
    }

    @Test
    public void testDoFilterWithPostMethodAndMismatchedUserId() throws Exception {
        try (MockedStatic<RollerSession> mockedRollerSession = mockStatic(RollerSession.class);
             MockedStatic<SaltCache> mockedSaltCache = mockStatic(SaltCache.class)) {

            mockedRollerSession.when(() -> RollerSession.getRollerSession(request)).thenReturn(rollerSession);
            mockedSaltCache.when(SaltCache::getInstance).thenReturn(saltCache);

            when(request.getMethod()).thenReturn("POST");
            when(request.getParameter("salt")).thenReturn("validSalt");
            when(saltCache.get("validSalt")).thenReturn("differentUserId");
            when(rollerSession.getAuthenticatedUser()).thenReturn(new TestUser("userId"));
            assertThrows(ServletException.class, () -> {
                filter.doFilter(request, response, chain);
            });
        }
    }

    @Test
    public void testDoFilterWithPostMethodAndNullRollerSession() throws Exception {
        try (MockedStatic<RollerSession> mockedRollerSession = mockStatic(RollerSession.class);
             MockedStatic<SaltCache> mockedSaltCache = mockStatic(SaltCache.class)) {

            mockedRollerSession.when(() -> RollerSession.getRollerSession(request)).thenReturn(null);
            mockedSaltCache.when(SaltCache::getInstance).thenReturn(saltCache);

            when(request.getMethod()).thenReturn("POST");
            when(request.getParameter("salt")).thenReturn("validSalt");
            when(saltCache.get("validSalt")).thenReturn("");
            filter.doFilter(request, response, chain);

            verify(saltCache, never()).remove("validSalt");
        }
    }

    @Test
    public void testPostWithoutParameterRejectsRequestAttributeSalt() throws Exception {
        try (MockedStatic<RollerSession> mockedRollerSession = mockStatic(RollerSession.class);
             MockedStatic<SaltCache> mockedSaltCache = mockStatic(SaltCache.class)) {

            mockedRollerSession.when(() -> RollerSession.getRollerSession(request)).thenReturn(rollerSession);
            mockedSaltCache.when(SaltCache::getInstance).thenReturn(saltCache);

            when(request.getMethod()).thenReturn("POST");
            when(request.getAttribute("salt")).thenReturn("responseSalt");
            when(request.getParameter("salt")).thenReturn(null);
            when(rollerSession.getAuthenticatedUser()).thenReturn(new TestUser("userId"));
            when(saltCache.get("responseSalt")).thenReturn("userId");

            assertThrows(ServletException.class,
                    () -> filter.doFilter(request, response, chain));

            verify(chain, never()).doFilter(request, response);
            verify(saltCache, never()).get(anyString());
            verify(saltCache, never()).remove(anyString());
        }
    }

    @Test
    public void testSubmittedSaltCanOnlyBeUsedOnce() throws Exception {
        try (MockedStatic<RollerSession> mockedRollerSession = mockStatic(RollerSession.class);
             MockedStatic<SaltCache> mockedSaltCache = mockStatic(SaltCache.class)) {

            mockedRollerSession.when(() -> RollerSession.getRollerSession(request)).thenReturn(rollerSession);
            mockedSaltCache.when(SaltCache::getInstance).thenReturn(saltCache);

            when(request.getMethod()).thenReturn("POST");
            when(request.getParameter("salt")).thenReturn("validSalt");
            when(rollerSession.getAuthenticatedUser()).thenReturn(new TestUser("userId"));
            when(saltCache.get("validSalt")).thenReturn("userId", (String) null);

            filter.doFilter(request, response, chain);
            assertThrows(ServletException.class,
                    () -> filter.doFilter(request, response, chain));

            verify(chain, times(1)).doFilter(request, response);
            verify(saltCache, times(1)).remove("validSalt");
        }
    }

    @Test
    public void testMultipartStrutsPostIsDeferred() throws Exception {
        when(request.getMethod()).thenReturn("POST");
        when(request.getContentType()).thenReturn("multipart/form-data; boundary=abc123");
        when(request.getServletPath()).thenReturn("/roller-ui/mediaFileAdd!save.rol");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(request, never()).getParameter("salt");
    }

    @Test
    public void testMultipartNonStrutsPostIsNotDeferred() throws Exception {
        try (MockedStatic<RollerSession> mockedRollerSession = mockStatic(RollerSession.class)) {
            mockedRollerSession.when(() -> RollerSession.getRollerSession(request)).thenReturn(rollerSession);

            when(request.getMethod()).thenReturn("POST");
            when(request.getContentType()).thenReturn("multipart/form-data; boundary=abc123");
            when(request.getServletPath()).thenReturn("/roller-ui/upload");
            when(request.getParameter("salt")).thenReturn(null);

            assertThrows(ServletException.class,
                    () -> filter.doFilter(request, response, chain));

            verify(chain, never()).doFilter(request, response);
        }
    }

    @Test
    public void testValidationRunsBeforeResponseSaltGeneration() throws Exception {
        try (MockedStatic<RollerSession> mockedRollerSession = mockStatic(RollerSession.class);
             MockedStatic<SaltCache> mockedSaltCache = mockStatic(SaltCache.class)) {

            mockedRollerSession.when(() -> RollerSession.getRollerSession(request)).thenReturn(rollerSession);
            mockedSaltCache.when(SaltCache::getInstance).thenReturn(saltCache);

            when(request.getMethod()).thenReturn("POST");
            when(request.getParameter("salt")).thenReturn("submittedSalt");
            when(rollerSession.getAuthenticatedUser()).thenReturn(new TestUser("userId"));
            when(saltCache.get("submittedSalt")).thenReturn("userId");

            LoadSaltFilter loadSaltFilter = new LoadSaltFilter();
            FilterChain terminalChain = mock(FilterChain.class);
            FilterChain loadSaltChain = (servletRequest, servletResponse) ->
                    loadSaltFilter.doFilter(servletRequest, servletResponse, terminalChain);

            filter.doFilter(request, response, loadSaltChain);

            InOrder order = inOrder(saltCache, request, terminalChain);
            order.verify(saltCache).get("submittedSalt");
            order.verify(saltCache).remove("submittedSalt");
            order.verify(saltCache).put(anyString(), eq("userId"));
            order.verify(request).setAttribute(eq("salt"), anyString());
            order.verify(terminalChain).doFilter(request, response);
        }
    }

    private static class TestUser extends User {
        private static final long serialVersionUID = 1L;
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
