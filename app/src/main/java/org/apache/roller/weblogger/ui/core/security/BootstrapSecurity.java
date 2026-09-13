package org.apache.roller.weblogger.ui.core.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Process-scoped gate for the unauthenticated installation flow. */
public final class BootstrapSecurity {
    public static final String COMPLETION_PROPERTY = "bootstrap.completed";
    private static final Log LOG = LogFactory.getLog(BootstrapSecurity.class);
    private static final long LIFETIME_MS = 60 * 60 * 1000L;
    private static final String SESSION = BootstrapSecurity.class.getName() + ".grant";
    private static byte[] digest;
    private static long expires;
    private static volatile boolean completed;
    private static final ThreadLocal<Boolean> INITIAL = new ThreadLocal<>();
    private BootstrapSecurity() { }

    public static synchronized void start() {
        if (completed || digest != null) return;
        byte[] raw = new byte[32];
        new SecureRandom().nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        digest = sha256(token);
        expires = System.currentTimeMillis() + LIFETIME_MS;
        LOG.warn("Roller initial setup is locked. The one-time setup token (expires in 60 minutes) is: " + token);
    }

    public static boolean isCompleted() { return completed; }
    public static void complete() { completed = true; digest = null; }
    public static void beginInitialAdmin() { INITIAL.set(Boolean.TRUE); }
    public static void endInitialAdmin() { INITIAL.remove(); }
    public static boolean initialAdminScope() { return Boolean.TRUE.equals(INITIAL.get()); }
    public static boolean isValid(HttpServletRequest request) {
        Object grant = request.getSession(false) == null ? null : request.getSession(false).getAttribute(SESSION);
        return !completed && grant instanceof Long && ((Long) grant) >= System.currentTimeMillis();
    }
    public static synchronized boolean redeem(HttpServletRequest request, String token) {
        if (completed || digest == null || token == null || System.currentTimeMillis() > expires) return false;
        byte[] supplied = sha256(token);
        if (!MessageDigest.isEqual(digest, supplied)) return false;
        request.getSession(true).setAttribute(SESSION, System.currentTimeMillis() + LIFETIME_MS);
        digest = null;
        return true;
    }
    private static byte[] sha256(String value) {
        try { return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }
}
