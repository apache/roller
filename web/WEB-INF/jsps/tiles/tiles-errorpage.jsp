<%--
  Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  The ASF licenses this file to You
  under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.  For additional information regarding
  copyright in this work, please see the NOTICE file in the top level
  directory of this distribution.
--%>
<%@ include file="/WEB-INF/jsps/taglibs-error.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><c:out value="${model.title}" /></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<tiles:insert page="/WEB-INF/jsps/tiles/head.jsp"  />
<style type="text/css">
<tiles:insert page="/WEB-INF/jsps/tiles/styles.jsp" />
</style>
</head>
<body>
    
<div id="wrapper"> 
    <div id="leftcontent_wrap">
        <div id="leftcontent"> 
        </div>
    </div>
    
    <div id="centercontent_wrap">
        <div id="centercontent">   
            <h1><c:out value="${model.title}" /></h1>
            
            <%-- Success Messages --%>
            <logic:messagesPresent message="true">
                <div id="messages" class="messages">
                    <html:messages id="message" message="true">
                        <c:out value="${message}" escapeXml="false"/><br />
                    </html:messages>
                </div>
            </logic:messagesPresent>
            
            <%-- Error Messages --%>
            <logic:messagesPresent>
                <div id="errors" class="errors">
                    <html:messages id="error">
                        <c:out value="${error}" /><br />
                    </html:messages>
                </div>
            </logic:messagesPresent>
            
            <tiles:insert attribute="content" />    
        </div>
    </div>
    
    <div id="rightcontent_wrap">
        <div id="rightcontent"> 
        </div>
    </div>
</div>

<div id="footer">
    Powered by <a href="http://www.rollerweblogger.org">Apache Roller (incubating)</a> 
    <%= RollerFactory.getRoller().getVersion() %> |
    
    <a href="http://opensource2.atlassian.com/projects/roller/">
    <fmt:message key="footer.reportIssue" /></a> | 
    
    <a href="http://www.rollerweblogger.org/wiki/Wiki.jsp?page=UserGuide">
    <fmt:message key="footer.userGuide" /></a> | 
    
    <a href="http://rollerweblogger.org/wiki/Wiki.jsp?page=RollerMailingLists">
    <fmt:message key="footer.mailingLists" /></a>
    
</div>

<div id="datetagdiv" 
   style="position:absolute;visibility:hidden;background-color:white;layer-background-color:white;">
</div>
</body>
</html>
