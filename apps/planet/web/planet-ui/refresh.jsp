<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@ page import="org.apache.roller.planet.tasks.*" %>
<%
RefreshPlanetTask task = new RefreshPlanetTask();
task.run();
%>