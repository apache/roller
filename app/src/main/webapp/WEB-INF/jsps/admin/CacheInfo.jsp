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

<p class="subtitle"><s:text name="cacheInfo.subtitle" />
<p><s:text name="cacheInfo.prompt" />

<s:iterator id="cache" value="stats">
    <s:if test="#cache != null && !#cache.value.isEmpty">
        <table cellspacing="3" border="1">
            <tr>
                <th colspan="2"><s:property value="#cache.key"/></th>
            </tr>

            <s:iterator id="prop" value="#cache.value">
                <tr>
                    <td><s:property value="#prop.key"/></td>
                    <td><s:property value="#prop.value"/></td>
                </tr>
            </s:iterator>

            <tr>
                <td colspan="2">
                    <s:form action="cacheInfo!clear">
						<s:hidden name="salt" />
                        <s:hidden name="cache" value="%{#cache.key}" />
                        <s:submit value="%{getText('cacheInfo.clear')}" />
                    </s:form>
                </td>
            </tr>
            
        </table>
        
        <br>
    </s:if>
</s:iterator>
