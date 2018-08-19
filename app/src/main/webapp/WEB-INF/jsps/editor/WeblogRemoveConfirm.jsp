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
    <s:text name="websiteRemove.subtitle" />
</p>

<p style="margin-bottom: 3em">
    <s:text name="websiteRemove.youSure"> 
        <s:param value="actionWeblog.name" />
    </s:text>
    <br/>
    <br/>
    <span class="warning">
        <s:text name="websiteSettings.removeWebsiteWarning" />
    </span>
</p>

<div class="row">
    <div class="col-md-2">

        <s:form action="weblogRemove!remove" theme="bootstrap" cssClass="form-horizontal">
            <s:hidden name="salt" />
            <s:hidden name="weblog" value="%{actionWeblog.handle}" />
            <s:submit cssClass="btn btn-danger" value="%{getText('generic.yesRemove')}" />
        </s:form>

    </div>
    <div class="col-md-2">

        <s:form action="weblogConfig" method="post" theme="bootstrap" cssClass="form-horizontal">
            <s:hidden name="salt" />
            <s:hidden name="weblog" value="%{actionWeblog.handle}" />
            <s:submit cssClass="btn btn-success" value="%{getText('generic.cancel')}" />
        </s:form>

    </div>
    <div class="col-md-8"></div>
</div>






