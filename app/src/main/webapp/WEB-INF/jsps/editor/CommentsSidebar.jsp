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

<s:if test="actionName == 'comments'">
    <s:set var="mainAction">comments</s:set>
</s:if>
<s:else>
    <s:set var="mainAction">globalCommentManagement</s:set>
</s:else>

<h3><s:text name="commentManagement.sidebarTitle"/></h3>
<hr size="1" noshade="noshade"/>

<p><s:text name="commentManagement.sidebarDescription"/></p>

<s:form action="%{#mainAction}!query" id="commentsQuery" theme="bootstrap" cssClass="form-vertical">

    <s:hidden name="salt"/>
    <s:hidden name="weblog"/>

    <%-- ========================================================= --%>
    <%-- filter by search string --%>

    <s:textfield name="bean.searchString" label="%{getText('commentManagement.searchString')}" size="15"/>

    <script>
        $(function () {
            $("#entries_bean_startDateString").datepicker();
        });
    </script>

    <%-- ========================================================= --%>
    <%-- filter by date --%>

    <script>
        // jQuery UI Date Picker
        $(function () {
            $("#commentsQuery_bean_startDateString").datepicker();
        });
    </script>
    
    <div class="control-group">
        <label for="bean.startDateString" class="control-label">
            <s:text name="commentManagement.startDate"/>
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

    <script>
        // jQuery UI Date Picker
        $(function () {
            $("#commentsQuery_bean_endDateString").datepicker();
        });
    </script>

    <div class="control-group">
        <label for="bean.endDateString" class="control-label">
            <s:text name="commentManagement.endDate"/>
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
    <%-- filter by status--%>

    <s:radio name="bean.approvedString" 
             label="%{getText('commentManagement.pendingStatus')}" 
             list="commentStatusOptions" listKey="key" listValue="value"/>

    
    <%-- ========================================================= --%>
    <%-- filter button --%>

    <s:submit cssClass="btn btn-default" value="%{getText('commentManagement.query')}"/>

</s:form>

