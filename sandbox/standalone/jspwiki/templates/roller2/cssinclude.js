/*
* Licensed to the Apache Software Foundation (ASF) under one or more
*  contributor license agreements.  The ASF licenses this file to You
* under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.  For additional information regarding
* copyright in this work, please see the NOTICE file in the top level
* directory of this distribution.
*/
<link rel="stylesheet" href="templates/<wiki:TemplateDir/>/jspwiki.css" />

<script type="text/javascript">
<!-- Hide script contents from old browsers

    var IE4 = (document.all && !document.getElementById) ? true : false;
    var NS4 = (document.layers) ? true : false;
    var IE5 = (document.all && document.getElementById) ? true : false;
    var NS6 = (document.getElementById && !document.all) ? true : false;
    var IE  = IE4 || IE5;
    var NS  = NS4 || NS6;
    var Mac = (navigator.platform.indexOf("Mac") == -1) ? false : true;

    var sheet;

    if( NS4 )
    {
        sheet = "jspwiki_ns.css";
    }
    else if( Mac )
    {
        sheet = "jspwiki_mac.css";
    }
    else
    {
        // Let's assume all the rest of the browsers are sane
        // and standard's compliant.
        sheet = "jspwiki_ie.css";
    }

    document.write("<link rel=\"stylesheet\" href=\"templates/<wiki:TemplateDir />/"+sheet+"\" />");

// end hiding contents from old browsers -->
</script>

<meta http-equiv="Content-Type" content="text/html; charset=<wiki:ContentEncoding />" />
<link rel="search" href="<wiki:LinkTo format="url" page="FindPage"/>"            title="Search <wiki:Variable var="ApplicationName" />" />
<link rel="help"   href="<wiki:LinkTo format="url" page="TextFormattingRules"/>" title="Help" />
<link rel="start"  href="<wiki:LinkTo format="url" page="Main"/>"                title="Front page" />

