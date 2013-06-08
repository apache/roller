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

<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">

<div class="sidebarInner">

<h3><s:text name="commentManagement.sidebarTitle" /></h3>
<hr size="1" noshade="noshade" />

<p><s:text name="commentManagement.sidebarDescription" /></p>
    
 <s:form action="comments!query" id="commentsQuery">
	<s:hidden name="salt" />
    <s:hidden name="weblog" />
    
    <div class="sideformrow">
        <label for="searchString" class="sideformrow"><s:text name="commentManagement.searchString" />:</label>   
        <s:textfield name="bean.searchString" size="15" />
    </div>
    <br />
    <br />
    
    <div class="sideformrow">
        <label for="startDateString" class="sideformrow"><s:text name="commentManagement.startDate" />:</label>
            <script type="text/javascript" >
            <!--
            if (document.layers) { // Netscape 4 hack
                var calStart = new CalendarPopup();
            } else {
                var calStart = new CalendarPopup("datetagdiv");
                document.write(calStart.getStyles());
            }
            // -->
            </script>
            <s:textfield name="bean.startDateString" size="12" />
            <a href="#" id="anchorCalStart" name="anchorCalStart"
               onclick="calStart.select(document.getElementById('commentsQuery_bean_startDateString'),'anchorCalStart','MM/dd/yy'); return false">
            <img src='<s:url value="/images/calendar.png"/>' class="calIcon" alt="Calendar" /></a>
    </div>
        
    <div class="sideformrow">
        <label for="endDateString" class="sideformrow"><s:text name="commentManagement.endDate" />:</label>
            <script type="text/javascript" >
            <!--
            if (document.layers) { // Netscape 4 hack
                var calEnd = new CalendarPopup();
            } else {
                var calEnd = new CalendarPopup("datetagdiv");
                document.write(calEnd.getStyles());
            }
            // -->
            </script>
            <s:textfield name="bean.endDateString" size="12" />
            <a href="#" id="anchorCalEnd" name="anchorCalEnd"
               onclick="calEnd.select(document.getElementById('commentsQuery_bean_endDateString'),'anchorCalEnd','MM/dd/yy'); return false">
            <img src='<s:url value="/images/calendar.png"/>' class="calIcon" alt="Calendar" /></a>
    </div>
    <br />
    <br />
  
    <div class="sideformrow">
        <label for="approvedString" class="sideformrow">
            <s:text name="commentManagement.pendingStatus" />
            <br />
            <br />
            <br />
            <br />            
            <br />
        </label> 
        <div>
            <s:radio theme="roller" name="bean.approvedString" list="commentStatusOptions" listKey="key" listValue="value" />
        </div>
    </div> 
    <br />

    <div class="sideformrow">
        <label for="spamString" class="sideformrow">
            <s:text name="commentManagement.spamStatus" />   
            <br />
            <br />
            <br />
            <br />
        </label>
        <div>
            <s:radio theme="roller" name="bean.spamString" list="spamStatusOptions" listKey="key" listValue="value" />
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
