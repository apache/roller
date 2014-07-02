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

<p class="subtitle">
   <s:text name="oauthKeys.description" >
       <s:param value="authenticatedUser.userName" />
   </s:text>
</p>

<p class="pagetip">
   <s:text name="oauthKeys.tip" />
</p>


<h2><s:text name="oauthKeys.userKeys" /></h2>

<p><s:text name="oauthKeys.userKeysTip" /></p>

    <p style="margin-left:2em"><b><s:text name="oauthKeys.consumerKey" /></b>:
        <s:property value="userConsumer.consumerKey" /></p>

    <p style="margin-left:2em"><b><s:text name="oauthKeys.consumerSecret" /></b>:
        <s:property value="userConsumer.consumerSecret" /></p>


<s:if test="siteWideConsumer">

<h2><s:text name="oauthKeys.siteWideKeys" /></h2>

<p><s:text name="oauthKeys.siteWideKeysTip" /></p>

    <p style="margin-left:2em"><b><s:text name="oauthKeys.consumerKey" /></b>:
        <s:property value="siteWideConsumer.consumerKey" /></p>

    <p style="margin-left:2em"><b><s:text name="oauthKeys.consumerSecret" /></b>:
        <s:property value="siteWideConsumer.consumerSecret" /></p>

</s:if>


<h2><s:text name="oauthKeys.urls" /></h2>

<p><s:text name="oauthKeys.urlsTip" /></p>

    <p style="margin-left:2em"><b><s:text name="oauthKeys.requestTokenURL" /></b>:
        <s:property value="requestTokenURL" /></p>

    <p style="margin-left:2em"><b><s:text name="oauthKeys.authorizationURL" /></b>:
        <s:property value="authorizationURL" /></p>

    <p style="margin-left:2em"><b><s:text name="oauthKeys.accessTokenURL" /></b>:
        <s:property value="accessTokenURL" /></p>

<br />

<input type="button" value="<s:text name="generic.cancel"/>" onclick="window.location='<s:url action="menu"/>'" />
