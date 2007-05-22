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
                
                <h3><s:text name="weblogEntryQuery.sidebarTitle" /></h3>
                <hr size="1" noshade="noshade" />
                
                <p><s:text name="weblogEntryQuery.sidebarDescription" /></p>
                
                <s:form action="entries">
                    <s:hidden name="weblog" />
                    <s:hidden name="bean.count" />
                    <s:hidden name="bean.offset" />
                    
                    <%-- ========================================================= --%>
                    <%-- filter by category --%>
  
                    <div class="sideformrow">
                        <label for="categoryId" class="sideformrow">
                        <s:text name="weblogEntryQuery.label.category" /></label>          
                        <s:select name="bean.categoryPath" list="actionWeblog.weblogCategories" listKey="path" listValue="path" size="1" />
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
                        <label for="startDateString" class="sideformrow">
                            <s:text name="weblogEntryQuery.label.startDate" />:
                        </label>
                        <s:datetimepicker name="bean.startDate" />
                    </div>
                    
                    <div class="sideformrow">
                        <label for="endDateString" class="sideformrow">
                            <s:text name="weblogEntryQuery.label.endDate" />:
                        </label>
                        <s:datetimepicker name="bean.endDate" />
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
                            <s:radio name="bean.status" list="statusOptions" listKey="key" listValue="value" />
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
                            <s:radio name="bean.sortBy" list="sortByOptions" listKey="key" listValue="value" />
                        </div>
                    </div>
                    
                    <%-- ========================================================= --%>
                    <%-- search button --%>
  
                    <br />
                    
                    <s:submit key="weblogEntryQuery.button.query" />
                    
                </s:form>
                
                <br />
                <br />
            </div> <!-- sidebarInner -->

        </div>
    </div>
</div>
