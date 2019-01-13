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

<h3><s:text name="weblogEntryQuery.sidebarTitle"/></h3>
<hr size="1" noshade="noshade"/>

<p><s:text name="weblogEntryQuery.sidebarDescription"/></p>

<s:form action="entries" theme="bootstrap" cssClass="form-vertical">
    <s:hidden name="salt"/>
    <s:hidden name="weblog"/>

    <%-- ========================================================= --%>
    <%-- filter by category --%>

    <s:select name="bean.categoryName"
              label="%{getText('weblogEntryQuery.label.category')}"
              list="categories" listKey="name" listValue="name" size="1"/>

    <%-- ========================================================= --%>
    <%-- filter by tag --%>

    <s:textfield name="bean.tagsAsString" size="14"
                 label="%{getText('weblogEntryQuery.label.tags')}"/>

    <%-- ========================================================= --%>
    <%-- filter by text --%>

    <s:textfield name="bean.text" size="14"
                 label="%{getText('weblogEntryQuery.label.text')}"/>

    <%-- ========================================================= --%>
    <%-- filter by date --%>

    <div class="control-group">
        <label for="bean.startDateString" class="control-label">
            <s:text name="weblogEntryQuery.label.startDate"/>
        </label>
        <div class="controls">
            <div class="input-group">

                <s:textfield name="bean.startDateString" readonly="true"
                             theme="simple" cssClass="date-picker form-control"/>
                <label for="bean.startDateString" class="input-group-addon btn">
                    <span class="glyphicon glyphicon-calendar"></span>
                </label>

            </div>
        </div>
    </div>

    <div class="control-group">
        <label for="bean.endDateString" class="control-label">
            <s:text name="weblogEntryQuery.label.endDate"/>
        </label>
        <div class="controls">
            <div class="input-group">

                <s:textfield name="bean.endDateString" readonly="true"
                             theme="simple" cssClass="date-picker form-control"/>
                <label for="bean.endDateString" class="input-group-addon btn">
                    <span class="glyphicon glyphicon-calendar"></span>
                </label>

            </div>
        </div>
    </div>

    <br/>

    <%-- ========================================================= --%>
    <%-- filter by status --%>

    <s:radio name="bean.status"
             label="%{getText('weblogEntryQuery.label.status')}"
             list="statusOptions" listKey="key" listValue="value"/>

    <%-- ========================================================= --%>
    <%-- sort by --%>

    <s:radio name="bean.sortBy"
             label="%{getText('weblogEntryQuery.label.sortby')}"
             list="sortByOptions" listKey="key" listValue="value"/>

    
    <%-- ========================================================= --%>
    <%-- filter button --%>

    <s:submit cssClass="btn" value="%{getText('weblogEntryQuery.button.query')}"/>

</s:form>

<script>

    $(document).ready(function () {
        $("#entries_bean_startDateString").datepicker();
        $("#entries_bean_endDateString").datepicker();
    });

</script>

