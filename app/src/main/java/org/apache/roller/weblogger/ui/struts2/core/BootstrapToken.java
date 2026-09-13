package org.apache.roller.weblogger.ui.struts2.core;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.roller.weblogger.ui.core.security.BootstrapSecurity;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
public class BootstrapToken extends UIAction implements ServletRequestAware, ServletResponseAware {
    private HttpServletRequest request; private HttpServletResponse response; private String token;
    public String execute() { return INPUT; }
    public String redeem() {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Referrer-Policy", "no-referrer");
        if (!"POST".equalsIgnoreCase(request.getMethod())) { addActionError("POST required"); return INPUT; }
        if (BootstrapSecurity.redeem(request, token)) return SUCCESS;
        addActionError("Invalid or expired setup token"); return INPUT;
    }
    public void setToken(String token) { this.token = token; }
    public void setServletRequest(HttpServletRequest request) { this.request = request; }
    public void setServletResponse(HttpServletResponse response) { this.response = response; }
    public boolean isUserRequired() { return false; }
    public boolean isWeblogRequired() { return false; }
}
