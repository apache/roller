package org.apache.roller.weblogger.ui.core.filters;

import java.io.IOException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.ui.core.security.BootstrapSecurity;

/** Prevents anonymous access to installer and first-user actions. */
public class BootstrapSecurityFilter implements Filter {
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest r = (HttpServletRequest) req;
        HttpServletResponse p = (HttpServletResponse) res;
        String uri = r.getRequestURI();
        boolean tokenPage = uri != null && (uri.endsWith("/bootstrap-token.rol")
                || uri.endsWith("/bootstrap-token!redeem.rol"));
        boolean installer = uri != null && (tokenPage || uri.contains("/roller-ui/install/")
                || uri.endsWith("/roller-ui/register.rol")
                || uri.endsWith("/roller-ui/register!save.rol")
                || uri.endsWith("/roller-ui/setup.rol"));
        if (installer && !BootstrapSecurity.isCompleted()) {
            if (tokenPage) { chain.doFilter(req, res); return; }
            if (!BootstrapSecurity.isValid(r)) { p.sendRedirect(r.getContextPath() + "/roller-ui/bootstrap-token.rol"); return; }
        }
        chain.doFilter(req, res);
    }
    public void init(FilterConfig c) { }
    public void destroy() { }
}
