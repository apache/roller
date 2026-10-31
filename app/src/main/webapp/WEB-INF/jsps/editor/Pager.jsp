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

<%-- First / Newer / Older / Last buttons for pages whose action has a LinkPager named "pager".
     MediaFileView.jsp draws the same control with buttons that resubmit its search form. --%>

<s:if test="pager.prevLink != null || pager.nextLink != null">
    <nav class="roller-pager" aria-label="<s:text name='generic.pagination'/>">

        <s:if test="pager.firstLink != null">
            <a class="btn btn-outline-primary btn-sm" href="<s:property value='pager.firstLink'/>">
                <span class="bi bi-chevron-double-left" aria-hidden="true"></span>
                <s:text name="generic.first"/></a>
        </s:if>
        <s:else>
            <span class="btn btn-outline-secondary btn-sm disabled" aria-disabled="true">
                <span class="bi bi-chevron-double-left" aria-hidden="true"></span>
                <s:text name="generic.first"/></span>
        </s:else>

        <s:if test="pager.prevLink != null">
            <a class="btn btn-outline-primary btn-sm" href="<s:property value='pager.prevLink'/>">
                <span class="bi bi-chevron-left" aria-hidden="true"></span>
                <s:text name="generic.newer"/></a>
        </s:if>
        <s:else>
            <span class="btn btn-outline-secondary btn-sm disabled" aria-disabled="true">
                <span class="bi bi-chevron-left" aria-hidden="true"></span>
                <s:text name="generic.newer"/></span>
        </s:else>

        <span class="roller-pager-page">
            <s:text name="generic.pageNumber">
                <s:param value="pager.pageNumber"/>
                <s:param value="pager.pageCount"/>
            </s:text>
        </span>

        <s:if test="pager.nextLink != null">
            <a class="btn btn-outline-primary btn-sm" href="<s:property value='pager.nextLink'/>">
                <s:text name="generic.older"/>
                <span class="bi bi-chevron-right" aria-hidden="true"></span></a>
        </s:if>
        <s:else>
            <span class="btn btn-outline-secondary btn-sm disabled" aria-disabled="true">
                <s:text name="generic.older"/>
                <span class="bi bi-chevron-right" aria-hidden="true"></span></span>
        </s:else>

        <s:if test="pager.lastLink != null">
            <a class="btn btn-outline-primary btn-sm" href="<s:property value='pager.lastLink'/>">
                <s:text name="generic.last"/>
                <span class="bi bi-chevron-double-right" aria-hidden="true"></span></a>
        </s:if>
        <s:else>
            <span class="btn btn-outline-secondary btn-sm disabled" aria-disabled="true">
                <s:text name="generic.last"/>
                <span class="bi bi-chevron-double-right" aria-hidden="true"></span></span>
        </s:else>

    </nav>
</s:if>
