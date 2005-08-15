 <%@ page import="org.roller.model.Roller" %>
<%@ page import="org.roller.pojos.UserData" %>
<%@ page import="org.roller.presentation.RollerRequest" %>
<%@ include file="/taglibs.jsp" %>
<%@ page import="java.io.*,org.roller.util.Utilities" isErrorPage="true" %>
    
<%
java.util.Locale locale = request.getLocale();
java.util.ResourceBundle bundle = 
   java.util.ResourceBundle.getBundle("ApplicationResources",locale);

final Object codeObj, messageObj, typeObj, exceptionObj;
codeObj = request.getAttribute("javax.servlet.error.status_code");
messageObj = request.getAttribute("javax.servlet.error.message");
typeObj = request.getAttribute("javax.servlet.error.type");

String code=null, message=null, type=null;
if ( null != codeObj )       code = codeObj.toString();
if ( null != messageObj ) message = messageObj.toString();
if ( null != typeObj )       type = typeObj.toString();
String reason = null != code ? code : type;

exception = (Throwable)request.getAttribute("javax.servlet.error.exception");
%>
<br />
<h2 class="error"><fmt:message key="errorPage.title" /></h2>
<p><fmt:message key="errorPage.message" /></p>
<p><%= message %></p>
<p><b><fmt:message key="errorPage.reason" /></b>: <%= reason %></p>
<% if ( null != exception ) 
{ %>
   <form><textarea rows="30" style="font-size:8pt;width:95%">
   <% exception.printStackTrace(new java.io.PrintWriter(out)); %>
   </textarea></form>
<% } %>



    
