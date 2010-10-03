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

<div class="searchSidebarHead">
    <div class="menu-tr">
        <div class="menu-tl">
           <h3>&nbsp;</h3>
        </div>
    </div>
</div>

<div class="searchSidebarBody">

     <h3><s:text name="mainPage.searchWeblogs" /></h3>

     <form id="searchForm" method="get"
        action="<c:out value="${baseURL}" />/sitesearch.do"
        style="margin: 0; padding: 0" onsubmit="return validateSearch(this)">
        <input type="text" id="q" name="q" size="20"
            maxlength="255" value="<c:out value="${param.q}" />" />
        <input value="&nbsp;»&nbsp;" class="searchButton" type="submit">
     </form>
     <script type="text/javascript"> 
        // <!--
        function validateSearch(form) {
            if (form.q.value == "") {
                alert("Please enter a search term to continue.");
                form.q.focus();
                return false;
            }
            return true;
        } 
        // -->
     </script>

</div>

