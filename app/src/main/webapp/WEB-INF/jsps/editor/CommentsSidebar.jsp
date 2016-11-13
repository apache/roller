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

  Source file modified from the original ASF source; all changes made
  are also under Apache License.
--%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<link rel="stylesheet" media="all" href='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.css"/>' />
<script src="<s:url value="/tb-ui/scripts/jquery-2.2.3.min.js" />"></script>
<script src='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js"/>'></script>

<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">

<div class="sidebarInner">

<h3><s:text name="commentManagement.sidebarTitle" /></h3>
<hr size="1" noshade="noshade" />

<p><s:text name="commentManagement.sidebarDescription" /></p>
    
 <s:form action="comments!query" id="commentsQuery">
    <sec:csrfInput/>
    <s:hidden name="weblogId" />
    
    <div class="sideformrow">
        <label for="searchText" class="sideformrow"><s:text name="commentManagement.searchString" />:</label>
        <s:textfield name="bean.searchText" size="15" onBlur="this.value=this.value.trim()"/>
    </div>
    <br />
    <br />
    
    <div class="sideformrow">
        <label for="startDateString" class="sideformrow"><s:text name="commentManagement.startDate" />:</label>
            <script>
            $(function() {
                $( "#commentsQuery_startDateString" ).datepicker({
                    showOn: "button",
                    buttonImage: "../../images/calendar.png",
                    buttonImageOnly: true,
                    changeMonth: true,
                    changeYear: true
                });
            });
            </script>
            <s:textfield name="startDateString" size="12" readonly="true"/>
    </div>
        
    <div class="sideformrow">
        <label for="endDateString" class="sideformrow"><s:text name="commentManagement.endDate" />:</label>
            <script>
            $(function() {
                $( "#commentsQuery_endDateString" ).datepicker({
                    showOn: "button",
                    buttonImage: "../../images/calendar.png",
                    buttonImageOnly: true,
                    changeMonth: true,
                    changeYear: true
                });
            });
            </script>
            <s:textfield name="endDateString" size="12" readonly="true"/>
    </div>
    <br />
    <br />
  
    <div class="sideformrow">
        <label for="status" class="sideformrow">
            <s:text name="commentManagement.pendingStatus" />
            <br />
            <br />
            <br />
            <br />            
            <br />
        </label> 
        <div>
            <s:radio theme="strutsoverride" name="bean.status" list="commentStatusOptions" listKey="left" listValue="right" />
        </div>
    </div> 
    <br />

    <s:submit value="%{getText('commentManagement.query')}" />
            
</s:form>

<br />
<br />
</div> <!-- sidebarInner -->

        </div>
    </div>
</div>
