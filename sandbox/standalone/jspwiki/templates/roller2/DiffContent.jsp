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
<%@ page import="com.ecyrd.jspwiki.tags.InsertDiffTag" %>
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%!
    String getVersionText( Integer ver )
    {
        return ver.intValue() > 0 ? ("version "+ver) : "current version";
    }
%>

      <wiki:PageExists>
          Difference between 
          <%=getVersionText((Integer)pageContext.getAttribute(InsertDiffTag.ATTR_OLDVERSION, PageContext.REQUEST_SCOPE))%> 
          and 
          <%=getVersionText((Integer)pageContext.getAttribute(InsertDiffTag.ATTR_NEWVERSION, PageContext.REQUEST_SCOPE))%>:
          <div>
          <wiki:InsertDiff>
              <i>No difference detected.</i>
          </wiki:InsertDiff>
          </div>

      </wiki:PageExists>

      <wiki:NoSuchPage>
             This page does not exist.  Why don't you go and
             <wiki:EditLink>create it</wiki:EditLink>?
      </wiki:NoSuchPage>

      <p>
      Back to <wiki:LinkTo><wiki:PageName/></wiki:LinkTo>,
       or to the <wiki:PageInfoLink>Page History</wiki:PageInfoLink>.
       </p>
