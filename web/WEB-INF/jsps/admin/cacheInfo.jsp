<!--
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
-->
<%@ include file="/taglibs.jsp" %>

<p class="subtitle"><fmt:message key="cacheInfo.subtitle" />
<p><fmt:message key="cacheInfo.prompt" />

<c:forEach var="cache" items="${cacheStats}">
    <c:if test="${!empty cache.value}">
        <table cellspacing="3" border="1">
            <tr>
                <th colspan="2"><c:out value="${cache.key}"/></th>
            </tr>

            <c:forEach var="prop" items="${cache.value}">
                <tr>
                    <td><c:out value="${prop.key}"/></td>
                    <td><c:out value="${prop.value}"/></td>
                </tr>
            </c:forEach>

            <tr>
                <td colspan="2">
                    <form action="cacheInfo.do" method="POST">
                        <input type="hidden" name="cache" value="<c:out value='${cache.key}'/>" />
                        <input type="hidden" name="method" value="clear" />
                        <input type="submit" value="<fmt:message key='cacheInfo.clear' />" />
                    </form>
                </td>
            </tr>
            
        </table>
        
        <br>
    </c:if>
</c:forEach>