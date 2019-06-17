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

<p style="margin-bottom:2em"><s:text name="index.prompt"/> </p>

<%--
      Index page on Roller startup; tell the user how to complete their Roller install,
      with helpful notes and links to the appropriate places in the Roller UI.
--%>


<%-- STEP 1: Create a user if you don't already have one --%>

<div class="panel panel-default">
    <div class="panel-heading">
        <h3 class="panel-title">
            <s:text name="index.createUser"/>
            <s:if test="userCount > 0"> -
                <s:text name="index.createUserDone">
                    <s:param value="userCount"/>
                </s:text>
            </s:if>
        </h3>
    </div>

    <div class="panel-body">

        <p><s:text name="index.createUserHelp"/></p>
        <p><s:if test="userCount == 0">
            <s:text name="index.createUserBy"/>
            <a id="a_createUser" href='<s:url action="register"/>'>
                <s:text name="index.createUserPage"/></a>.
        </s:if></p>

    </div>
</div>

<%-- STEP 2: Create a weblog if you don't already have one --%>

<div class="panel panel-default">
    <div class="panel-heading">
        <h3 class="panel-title">
            <s:text name="index.createWeblog"/>
            <s:if test="blogCount > 0"> -
                <s:text name="index.createWeblogDone">
                    <s:param value="blogCount"/>
                </s:text>
            </s:if>
        </h3>
    </div>

    <div class="panel-body">

        <s:text name="index.createWeblogHelp"/><br/><br/>
        <s:if test="userCount > 0 && blogCount == 0">
            <s:text name="index.createWeblogBy"/>
            <a id="a_createBlog" href='<s:url action="createWeblog"/>'>
                <s:text name="index.createWeblogPage"/></a>.
        </s:if>

    </div>
</div>


<%-- STEP 3: Designate a weblog to be the frontpage weblog --%>

<div class="panel panel-default">
    <div class="panel-heading">
        <h3 class="panel-title">
            <s:text name="index.setFrontpage"/>
        </h3>
    </div>

    <div class="panel-body">

        <p><s:text name="index.setFrontpageHelp"/></p>

        <s:if test="blogCount > 0">

            <s:form action="setup!save" theme="bootstrap" cssClass="form-horizontal">
                <s:hidden name="salt"/>

                <s:select list="weblogs"
                          listKey="handle"
                          listValue="name"
                          label="%{getText('frontpageConfig.frontpageBlogName')}"
                          name="frontpageBlog"
                          value="frontpageBlog"/>

                <s:checkbox name="aggregated" value="aggregated"
                            label="%{getText('frontpageConfig.frontpageAggregated')}"/>

                <s:submit value="%{getText('generic.save')}" cssClass="btn btn-default"/>

            </s:form>

        </s:if>
    </div>
</div>

