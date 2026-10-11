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

<div class="row">
    <div class="col-sm-8 col-sm-offset-2 col-md-6 col-md-offset-3">
        <h2><s:text name="installer.bootstrap.heading" /></h2>
        <p><s:text name="installer.bootstrap.instructions" /></p>

        <s:form action="bootstrap-token!redeem" method="post" theme="simple">
            <s:hidden name="salt" />
            <div class="form-group">
                <label for="setup-token"><s:text name="installer.bootstrap.tokenLabel" /></label>
                <s:textfield id="setup-token" name="token" theme="simple"
                        cssClass="form-control" autocomplete="off" autofocus="autofocus"
                        required="required"
                        oninput="document.getElementById('setup-token-submit').disabled = !this.value.trim()" />
                <p class="help-block"><s:text name="installer.bootstrap.tokenHelp" /></p>
            </div>
            <div class="form-group">
                <s:submit id="setup-token-submit" value="%{getText('installer.bootstrap.submit')}"
                        theme="simple" disabled="true"
                        cssClass="btn btn-primary" />
            </div>
        </s:form>
    </div>
</div>
