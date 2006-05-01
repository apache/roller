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
<%@ taglib uri="/WEB-INF/jspwiki.tld" prefix="wiki" %>
<%@ page import="com.ecyrd.jspwiki.Release" %>

<p>
    <wiki:InsertPage page="LeftMenu" />
</p>

<hr />

<p>
    <wiki:UserCheck status="known">
        <b>Hello </b><br />
        <wiki:Translate>[<wiki:UserName />]</wiki:Translate>
    </wiki:UserCheck>

    <wiki:UserCheck status="unknown">
        <tt>
        Set your name in<br />
        <wiki:LinkTo page="UserPreferences">UserPreferences</wiki:LinkTo>
        </tt>    
    </wiki:UserCheck>
    <wiki:Include page="LoginBox.jsp" />
</p>

<!-- End of automatically generated page -->

