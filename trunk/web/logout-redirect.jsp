<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="org.roller.presentation.RollerSession" %>
<% 
request.getSession().removeAttribute(RollerSession.ROLLER_SESSION);
request.getSession().invalidate(); 
response.sendRedirect("index.jsp"); 
%>

