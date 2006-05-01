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

      <p>
      This is a page which allows you to set up all sorts of interesting things.
      You need to have cookies enabled for this to work, though.
      </p>

      <form action="<wiki:Variable var="baseURL"/>UserPreferences.jsp" 
            method="POST"
            accept-charset="UTF-8">

         <b>User name:</b> <input type="text" name="username" size="30" value="<wiki:UserName/>" />
         <i>This must be a proper WikiName, no punctuation.</i>
         <br /><br />
         <input type="submit" name="ok" value="Set my preferences!" />
         <input type="hidden" name="action" value="save" />
      </form>

      <hr />

      <h3>Removing your preferences</h3>

      <p>In some cases, you may need to remove the above preferences from the computer.
      Click the button below to do that.  Note that it will remove all preferences
      you've set up, permanently.  You will need to enter them again.</p>

      <div align="center">
      <form action="<wiki:Variable var="baseURL"/>UserPreferences.jsp"
            method="POST"
            accept-charset="UTF-8">
      <input type="submit" name="clear" value="Remove preferences from this computer" />
      </form>
      </div>
