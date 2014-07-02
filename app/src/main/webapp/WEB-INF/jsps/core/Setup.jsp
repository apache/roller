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

<s:text name="index.prompt" /><br /><br />
<div style="width:75%">
    <ul>
        <%--
              Index page on Roller startup; tell the user how to complete their Roller install,
              with helpful notes and links to the appropriate places in the Roller UI.
        --%>

        <%-- STEP 1: Create a user if you don't already have one --%>
        <li><b><s:text name="index.createUser" />
                <s:if test="userCount > 0"> -
                    <s:text name="index.createUserDone">
                        <s:param value="userCount" />
                    </s:text>
                </s:if>
            </b><br /><br />
            <s:text name="index.createUserHelp" /><br /><br />
            <s:if test="userCount == 0">
                <s:text name="index.createUserBy" />
                <a id="a_createUser" href='<s:url action="register"/>'>
                    <s:text name="index.createUserPage" /></a>.
                <br /><br />
            </s:if>
            <br />
        </li>

        <%-- STEP 2: Create a weblog if you don't already have one --%>
        <li><b><s:text name="index.createWeblog" />
                <s:if test="blogCount > 0"> -
                    <s:text name="index.createWeblogDone">
                        <s:param value="blogCount" />
                    </s:text>
                </s:if>
            </b><br /><br />
            <s:text name="index.createWeblogHelp" /><br /><br />
            <s:if test="userCount > 0 && blogCount == 0">
                <s:text name="index.createWeblogBy" />
                <a id="a_createBlog" href='<s:url action="createWeblog"/>'>
                    <s:text name="index.createWeblogPage" /></a>.
            <br /><br />
            </s:if>
            <br />
        </li>

        <%-- STEP 3: Designate a weblog to be the frontpage weblog --%>
        <li><b><s:text name="index.setFrontpage" /></b><br />
            <br />
            <s:text name="index.setFrontpageHelp" /><br />
            <br />

          <s:if test="blogCount > 0">
            <s:form action="setup!save">
				<s:hidden name="salt" />

                <table style="margin-left:2em;width:70%">
                    <tr>
                        <td class="label">
                            <s:text name="frontpageConfig.frontpageBlogName" />
                        </td>
                        <td class="field">
                            <s:select list="weblogs"
                                      listKey="handle"
                                      listValue="name"
                                      name="frontpageBlog"
                                      value="frontpageBlog" />
                        </td>
                    </tr>
                    <tr>
                        <td class="label">
                            <s:text name="frontpageConfig.frontpageAggregated" />
                            <br /><br />
                        </td>
                        <td class="field">
                            <s:checkbox name="aggregated" value="aggregated" />
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" class="label">
                            <div class="control">
                                <s:submit value="%{getText('generic.save')}" />
                            </div>
                        </td>
                    </tr>
                </table>

            </s:form>
          </s:if>

        </li>

    </ul>
</div>
