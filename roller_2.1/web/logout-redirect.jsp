<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="org.roller.presentation.RollerSession" %>
<%@ page import="javax.servlet.http.Cookie" %>
<%@ page import="org.acegisecurity.ui.rememberme.TokenBasedRememberMeServices" %>

<%
request.getSession().removeAttribute(RollerSession.ROLLER_SESSION);
request.getSession().invalidate(); 

Cookie terminate = new Cookie(TokenBasedRememberMeServices.ACEGI_SECURITY_HASHED_REMEMBER_ME_COOKIE_KEY, null);

terminate.setMaxAge(0);
response.addCookie(terminate);

response.sendRedirect("index.jsp"); 
%>

