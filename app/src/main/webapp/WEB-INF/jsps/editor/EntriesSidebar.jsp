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

<link rel="stylesheet" media="all" href='<s:url value="/roller-ui/jquery-ui-1.11.0/jquery-ui.min.css"/>' />
<script src="<s:url value="/roller-ui/scripts/jquery-2.1.1.min.js" />"></script>
<script src='<s:url value="/roller-ui/jquery-ui-1.11.0/jquery-ui.min.js"/>'></script>

<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">

            <div class="sidebarInner">

                <h3><s:text name="weblogEntryQuery.sidebarTitle" /></h3>
                <hr size="1" noshade="noshade" />

                <p><s:text name="weblogEntryQuery.sidebarDescription" /></p>

                <s:form action="entries">
					<s:hidden name="salt" />
                    <s:hidden name="weblog" />

                    <%-- ========================================================= --%>
                    <%-- filter by category --%>

                    <div class="sideformrow">
                        <label for="categoryId" class="sideformrow">
                        <s:text name="weblogEntryQuery.label.category" /></label>
                        <s:select name="bean.categoryName" list="categories" listKey="name" listValue="name" size="1" />
                    </div>
                    <br />
                    <br />

                    <%-- ========================================================= --%>
                    <%-- filter by tag --%>

                    <div class="sideformrow">
                        <label for="tags" class="sideformrow">
                        <s:text name="weblogEntryQuery.label.tags" /></label>
                        <s:textfield name="bean.tagsAsString" size="14" />
                    </div>
                    <br />
                    <br />

                    <%-- ========================================================= --%>
                    <%-- filter by text --%>

                    <div class="sideformrow">
                        <label for="text" class="sideformrow">
                        <s:text name="weblogEntryQuery.label.text" /></label>
                        <s:textfield name="bean.text" size="14" />
                    </div>
                    <br />
                    <br />

                    <%-- ========================================================= --%>
                    <%-- filter by date --%>

                    <div class="sideformrow">
                        <label for="startDateString" class="sideformrow"><s:text name="weblogEntryQuery.label.startDate" />:</label>
                        <script>
                        $(function() {
                            $( "#entries_bean_startDateString" ).datepicker({
                                showOn: "button",
                                buttonImage: "../../images/calendar.png",
                                buttonImageOnly: true,
                                changeMonth: true,
                                changeYear: true
                            });
                        });
                        </script>
                        <s:textfield name="bean.startDateString" size="12" readonly="true"/>
                    </div>

                    <div class="sideformrow">
                        <label for="endDateString" class="sideformrow"><s:text name="weblogEntryQuery.label.endDate" />:</label>
                        <script>
                        $(function() {
                            $( "#entries_bean_endDateString" ).datepicker({
                                showOn: "button",
                                buttonImage: "../../images/calendar.png",
                                buttonImageOnly: true,
                                changeMonth: true,
                                changeYear: true
                            });
                        });
                        </script>
                        <s:textfield name="bean.endDateString" size="12" readonly="true"/>
                    </div>
                    <br />
                    <br />

                    <%-- ========================================================= --%>
                    <%-- filter by status --%>

                    <div class="sideformrow">
                        <label for="status" class="sideformrow">
                            <s:text name="weblogEntryQuery.label.status" />:
                            <br />
                            <br />
                            <br />
                            <br />
                            <br />
                            <br />
                        </label>
                        <div>
                            <s:radio theme="roller" name="bean.status" list="statusOptions" listKey="key" listValue="value" />
                        </div>
                    </div>

                    <%-- ========================================================= --%>
                    <%-- sort by --%>

                    <div class="sideformrow">
                        <label for="status" class="sideformrow">
                            <s:text name="weblogEntryQuery.label.sortby" />:
                            <br />
                            <br />
                        </label>
                        <div>
                            <s:radio theme="roller" name="bean.sortBy" list="sortByOptions" listKey="key" listValue="value" />
                        </div>
                    </div>

                    <%-- ========================================================= --%>
                    <%-- search button --%>

                    <br />

                    <s:submit value="%{getText('weblogEntryQuery.button.query')}" />

                </s:form>

                <br />
                <br />
            </div> <!-- sidebarInner -->

        </div>
    </div>
</div>
