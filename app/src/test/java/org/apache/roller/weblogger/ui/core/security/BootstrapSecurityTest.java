package org.apache.roller.weblogger.ui.core.security;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
class BootstrapSecurityTest {
    @Test void completionClosesBootstrapGate() {
        BootstrapSecurity.start();
        assertFalse(BootstrapSecurity.isCompleted());
        BootstrapSecurity.complete();
        assertTrue(BootstrapSecurity.isCompleted());
    }
}
