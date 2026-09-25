package org.apache.roller.weblogger.ui.core.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BootstrapSecurityTest {

    private Weblogger weblogger;
    private PropertiesManager properties;
    private UserManager users;

    @BeforeEach
    void reset() throws Exception {
        // The gate is process-scoped static state, so each test starts from
        // the pristine, pre-startup condition.
        set("digest", null);
        set("expires", 0L);
        set("completed", false);

        weblogger = mock(Weblogger.class);
        properties = mock(PropertiesManager.class);
        users = mock(UserManager.class);
        when(weblogger.getPropertiesManager()).thenReturn(properties);
        when(weblogger.getUserManager()).thenReturn(users);
    }

    private static void set(String name, Object value) throws Exception {
        Field field = BootstrapSecurity.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(null, value);
    }

    @Test
    void completionClosesBootstrapGate() {
        BootstrapSecurity.start();
        assertFalse(BootstrapSecurity.isCompleted());
        BootstrapSecurity.complete();
        assertTrue(BootstrapSecurity.isCompleted());
    }

    @Test
    void freshInstallLeavesGateOpenForFirstAdministrator() throws Exception {
        when(properties.getProperty(BootstrapSecurity.COMPLETION_PROPERTY)).thenReturn(null);
        when(users.getUserCount()).thenReturn(0L);

        assertFalse(BootstrapSecurity.completeIfInstalled(weblogger));
        assertFalse(BootstrapSecurity.isCompleted());
        verify(properties, never()).saveProperty(any());
    }

    @Test
    void existingUsersCloseGateAndRecordMarker() throws Exception {
        when(properties.getProperty(BootstrapSecurity.COMPLETION_PROPERTY)).thenReturn(null);
        when(users.getUserCount()).thenReturn(3L);

        assertTrue(BootstrapSecurity.completeIfInstalled(weblogger));
        assertTrue(BootstrapSecurity.isCompleted());

        org.mockito.ArgumentCaptor<RuntimeConfigProperty> saved =
                org.mockito.ArgumentCaptor.forClass(RuntimeConfigProperty.class);
        verify(properties).saveProperty(saved.capture());
        assertEquals(BootstrapSecurity.COMPLETION_PROPERTY, saved.getValue().getName());
        assertEquals("true", saved.getValue().getValue());
        verify(weblogger).flush();
    }

    @Test
    void existingMarkerClosesGateWithoutRewriting() throws Exception {
        when(properties.getProperty(BootstrapSecurity.COMPLETION_PROPERTY))
                .thenReturn(new RuntimeConfigProperty(BootstrapSecurity.COMPLETION_PROPERTY, "true"));

        assertTrue(BootstrapSecurity.completeIfInstalled(weblogger));
        assertTrue(BootstrapSecurity.isCompleted());
        verify(properties, never()).saveProperty(any());
        verify(users, never()).getUserCount();
    }

    @Test
    void unreadablePropertiesLeaveGateOpen() throws Exception {
        when(properties.getProperty(BootstrapSecurity.COMPLETION_PROPERTY))
                .thenThrow(new WebloggerException("no database"));

        assertFalse(BootstrapSecurity.completeIfInstalled(weblogger));
        assertFalse(BootstrapSecurity.isCompleted());
    }
}
