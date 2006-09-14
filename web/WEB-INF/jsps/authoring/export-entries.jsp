<!--
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
-->
<%@ include file="/taglibs.jsp" %>
<%@ page import="org.apache.roller.ui.core.BasePageModel" %>
<%
BasePageModel model = (BasePageModel)request.getAttribute("model");
%>

<h1><fmt:message key="weblogEntryExport.title" /></h1>

<roller:StatusMessage/>

<html:form action="/roller-ui/authoring/exportEntries" method="post" focus="title">

    <html:hidden name="method" property="method" value="export"/>

    <h3><fmt:message key="weblogEntryQuery.section.dateRange" /></h3>
    
    <div class="row">
        <label style="width:20%; float:left; text-align:right; padding: 2px" for="startDateString">
           <fmt:message key="weblogEntryQuery.label.startDate" />:
        </label>
        <roller:Date property="startDateString" dateFormat='<%= model.getShortDateFormat() %>' />
    </div>
    
    <div class="row">
        <label style="width:20%; float:left; text-align:right; padding: 2px" for="endDateString">
           <fmt:message key="weblogEntryQuery.label.endDate" />:
        </label>
        <roller:Date property="endDateString" dateFormat='<%= model.getShortDateFormat() %>' />
    </div>
    
    <h3><fmt:message key="weblogEntryQuery.section.format" /></h3>
    
    <div class="row">
        <label style="width:20%; float:left; text-align:right; padding: 2px" for="fileBy">
            <fmt:message key="weblogEntryQuery.label.separateEntries" />:
        </label>
        <select name="fileBy">
            <option><fmt:message key="weblogEntryQuery.label.day" /></option>
            <option selected="selected"><fmt:message key="weblogEntryQuery.label.month" /></option>
            <option><fmt:message key="weblogEntryQuery.label.year" /></option>
        </select>
    </div>
    
    <div class="row">
        <label style="width:20%; float:left; text-align:right; padding: 2px" for="exportFormat">
            <fmt:message key="weblogEntryQuery.label.exportTo" />:
        </label>
        <input type="radio" name="exportFormat" value="Atom" checked="checked">
            <fmt:message key="weblogEntryQuery.label.atom" /></input>
        <br />
        <input type="radio" name="exportFormat" value="RSS" >
            <fmt:message key="weblogEntryQuery.label.rss" /></input>
    </div>
    
    <div class="buttonBox">
        <input type="button" name="post" 
            value='<fmt:message key="weblogEntryQuery.button.export" />' onclick="submit()" />
    </div>
    
</html:form>

