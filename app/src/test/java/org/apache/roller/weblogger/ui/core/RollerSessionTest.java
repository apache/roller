package org.apache.roller.weblogger.ui.core;

import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class RollerSessionTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @Mock
    private Principal principal;

    @Mock
    private Weblogger roller;

    @Mock
    private UserManager userManager;

    @Mock
    private User user;

    private RollerSession rollerSession;
    private RollerLoginSessionManager sessionManager;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        sessionManager = RollerLoginSessionManager.getInstance();
        rollerSession = new RollerSession();

        when(request.getSession(false)).thenReturn(session);
        when(roller.getUserManager()).thenReturn(userManager);
        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class)) {
            factory.when(WebloggerFactory::getWeblogger).thenReturn(roller);
        }
    }

    @Test
    void testGetRollerSessionNewSession() {
        when(session.getAttribute(RollerSession.ROLLER_SESSION)).thenReturn(null);
        when(request.getUserPrincipal()).thenReturn(null);

        RollerSession result = RollerSession.getRollerSession(request);

        // Verify new session was created
        assertNotNull(result);
        // Verify session was stored in HTTP session
        verify(session).setAttribute(eq(RollerSession.ROLLER_SESSION), any(RollerSession.class));
    }

    @Test
    void testGetRollerSessionExistingValidSession() {
        when(session.getAttribute(RollerSession.ROLLER_SESSION)).thenReturn(rollerSession);
        when(request.getUserPrincipal()).thenReturn(null);

        RollerSession result = RollerSession.getRollerSession(request);

        // Verify session was retrieved
        assertNotNull(result);
        // Verify returned session matches existing one
        assertEquals(rollerSession, result);
    }

    @Test
    void testGetRollerSessionInvalidatedSession() throws Exception {
        String username = "testuser";
        when(session.getAttribute(RollerSession.ROLLER_SESSION)).thenReturn(rollerSession);
        when(request.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn(username);
        when(userManager.getUserByUserName(username)).thenReturn(user);
        when(user.getUserName()).thenReturn(username);

        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class)) {
            factory.when(WebloggerFactory::getWeblogger).thenReturn(roller);

            rollerSession.setAuthenticatedUser(user);
            sessionManager.invalidate(username);

            // Force creation of new session
            when(session.getAttribute(RollerSession.ROLLER_SESSION)).thenReturn(null);
            RollerSession result = RollerSession.getRollerSession(request);

            assertNotNull(result);
            assertNotEquals(rollerSession, result);
        }
    }

    @Test
    void testSetAuthenticatedUser() throws Exception {
        String username = "testuser";
        when(user.getUserName()).thenReturn(username);

        rollerSession.setAuthenticatedUser(user);

        // Verify session was registered in manager
        assertNotNull(sessionManager.get(username));
        // Verify registered session matches current one
        assertEquals(rollerSession, sessionManager.get(username));
    }

    @Test
    void testGetAuthenticatedUser() throws Exception {
        String username = "testuser";
        when(user.getUserName()).thenReturn(username);
        when(userManager.getUserByUserName(username)).thenReturn(user);

        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class)) {
            factory.when(WebloggerFactory::getWeblogger).thenReturn(roller);

            rollerSession.setAuthenticatedUser(user);
            User result = rollerSession.getAuthenticatedUser();

            // Verify authenticated user was retrieved
            assertNotNull(result);
            // Verify retrieved user matches original user
            assertEquals(user, result);
        }
    }

    @Test
    void testConcurrentSessionHandling() throws Exception {
        String username = "testuser";
        when(user.getUserName()).thenReturn(username);

        RollerSession session1 = new RollerSession();
        RollerSession session2 = new RollerSession();

        session1.setAuthenticatedUser(user);
        session2.setAuthenticatedUser(user);

        // Verify most recent session is stored
        assertEquals(session2, sessionManager.get(username));
        // Verify old session was replaced
        assertNotEquals(session1, sessionManager.get(username));
    }

    @Test
    void testSessionTimeoutBehavior() throws Exception {
        String username = "testuser";
        when(user.getUserName()).thenReturn(username);
        when(userManager.getUserByUserName(username))
              .thenReturn(user)  // First call returns user
              .thenReturn(null); // Subsequent calls return null

        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class)) {
            factory.when(WebloggerFactory::getWeblogger).thenReturn(roller);

            rollerSession.setAuthenticatedUser(user);
            sessionManager.invalidate(username);

            // Force UserManager to return null after invalidation
            when(userManager.getUserByUserName(username)).thenReturn(null);

            assertNull(sessionManager.get(username));
            assertNull(rollerSession.getAuthenticatedUser());
        }
    }
}