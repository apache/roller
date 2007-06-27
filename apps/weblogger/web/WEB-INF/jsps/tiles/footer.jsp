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
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

Powered by <a href="http://www.rollerweblogger.org">Apache Roller</a> 
<%= org.apache.roller.weblogger.business.WebloggerFactory.getRoller().getVersion() %> |

<a href="http://opensource2.atlassian.com/projects/roller/">
    <s:text name="footer.reportIssue" /></a> | 

<a href="http://cwiki.apache.org/confluence/display/ROLLER/Roller+User+Documentation">
    <s:text name="footer.userGuide" /></a> | 

<a href="http://cwiki.apache.org/confluence/display/ROLLER/Roller+Mailing+Lists">
    <s:text name="footer.mailingLists" /></a>
