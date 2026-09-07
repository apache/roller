package org.apache.roller.weblogger.ui.core.filters;

import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.ui.core.RollerSession;
import org.apache.roller.weblogger.ui.rendering.util.cache.SaltCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import javax.servlet.FilterChain;
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
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        filter = new LoadSaltFilter();
    }

    @Test
    public void testDoFilterGeneratesSalt() throws Exception {
        try (MockedStatic<RollerSession> mockedRollerSession = mockStatic(RollerSession.class);
             MockedStatic<SaltCache> mockedSaltCache = mockStatic(SaltCache.class)) {

            mockedRollerSession.when(() -> RollerSession.getRollerSession(request)).thenReturn(rollerSession);
            mockedSaltCache.when(SaltCache::getInstance).thenReturn(saltCache);

            when(rollerSession.getAuthenticatedUser()).thenReturn(new TestUser("userId"));

            filter.doFilter(request, response, chain);

            verify(request).setAttribute(eq("salt"), anyString());
            verify(saltCache).put(anyString(), eq("userId"));
            verify(chain).doFilter(request, response);
        }
    }

    @Test
    public void testDoFilterWithNullRollerSession() throws Exception {
        try (MockedStatic<RollerSession> mockedRollerSession = mockStatic(RollerSession.class);
             MockedStatic<SaltCache> mockedSaltCache = mockStatic(SaltCache.class)) {

            mockedRollerSession.when(() -> RollerSession.getRollerSession(request)).thenReturn(null);
            mockedSaltCache.when(SaltCache::getInstance).thenReturn(saltCache);

            filter.doFilter(request, response, chain);

            verify(request, never()).setAttribute(eq("salt"), anyString());
            verify(saltCache, never()).put(anyString(), anyString());
            verify(chain).doFilter(request, response);
        }
    }

    @Test
    void firstFormHasAUsableSalt() throws Exception {
        javax.servlet.http.HttpSession session = mock(javax.servlet.http.HttpSession.class);
        java.util.Map<String, Object> attributes = new java.util.HashMap<>();
        java.util.Map<String, Object> sessionAttributes = new java.util.HashMap<>();
        when(request.getSession(true)).thenAnswer(invocation -> {
            when(request.getSession(false)).thenReturn(session);
            return session;
        });
        when(session.getAttribute(anyString())).thenAnswer(i -> sessionAttributes.get(i.getArgument(0)));
        doAnswer(i -> { sessionAttributes.put(i.getArgument(0), i.getArgument(1)); return null; })
                .when(session).setAttribute(anyString(), any());
        doAnswer(i -> { attributes.put(i.getArgument(0), i.getArgument(1)); return null; })
                .when(request).setAttribute(anyString(), any());
        java.util.Map<String, String> salts = new java.util.HashMap<>();
        try (MockedStatic<SaltCache> cache = mockStatic(SaltCache.class)) {
            cache.when(SaltCache::getInstance).thenReturn(saltCache);
            doAnswer(i -> { salts.put(i.getArgument(0), i.getArgument(1)); return null; })
                    .when(saltCache).put(anyString(), anyString());
            when(saltCache.get(anyString())).thenAnswer(i -> salts.get(i.getArgument(0)));
            doAnswer(i -> { salts.remove(i.getArgument(0)); return null; })
                    .when(saltCache).remove(anyString());
            filter.doFilter(request, response, chain);
            String salt = (String) attributes.get("salt");
            org.junit.jupiter.api.Assertions.assertNotNull(salt);
            when(request.getParameter("salt")).thenReturn(null);
            org.junit.jupiter.api.Assertions.assertFalse(SaltValidator.consumeSubmittedSalt(request));
            when(request.getParameter("salt")).thenReturn("unknown");
            org.junit.jupiter.api.Assertions.assertFalse(SaltValidator.consumeSubmittedSalt(request));
            when(request.getParameter("salt")).thenReturn(salt);
            org.junit.jupiter.api.Assertions.assertTrue(SaltValidator.consumeSubmittedSalt(request));
            org.junit.jupiter.api.Assertions.assertFalse(SaltValidator.consumeSubmittedSalt(request));
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
