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
<%-- This page is designed to be included in edit-weblog.jsp --%>
<%@ include file="/taglibs.jsp" %>
<html:hidden property="summary" />

<script type="text/javascript">
<!--
function postWeblogEntry(publish)
{
    if (publish)
        document.weblogEntryFormEx.publishEntry.value = "true";
    document.weblogEntryFormEx.submit();
}
function convertTextArea(textArea) {
    $text = textArea.value;
    
    // first off, __xxx__ to strong
    $text = $text.replace(/__([^_]*)__/g, "<strong>$1</strong>");

    // next, ''xxx'' to emphasis
    $text = $text.replace(/''([^']*)''/g, "<em>$1</em>");

    // next, {{{xxx}}} to pre
    $text = $text.replace(/\{\{\{([^\}]*)\}\}\}/g, "</p><pre>$1</pre><p>");

    // next, {{xxx}} to tt
    $text = $text.replace(/\{\{([^\}]*)\}\}/g, "<tt>$1</tt>");

    // next, ==xxx== to underline
    $text = $text.replace(/==([^=]*)==/g, "<u>$1</u>");

    // newlines
    $text = $text.replace(/-----*/g, "<hr/>");

    // --xxx-- to strike-through
    $text = $text.replace(/--([^-]*)--/g, "<s>$1</s>");

    // headings
    $text = $text.replace(/^!!!!!(.*)$/mg, "</p><h5>$1</h5><p>");
    $text = $text.replace(/^!!!!(.*)$/mg, "</p><h4>$1</h4><p>");
    $text = $text.replace(/^!!!(.*)$/mg, "</p><h3>$1</h3><p>");
    $text = $text.replace(/^!!(.*)$/mg, "</p><h2>$1</h2><p>");
    $text = $text.replace(/^!(.*)$/mg, "</p><h1>$1</h1><p>");

    // Need to be able to handle nested *'s and #'s. Need to 
    // know when to put the ul and ol in, and when to close.
    // Hard in regexp, so I'm doing it by hand 
    $text = handleList($text, "\*", "<ul>", "</ul>");
    $text = handleList($text, "\#", "<ol>", "</ol>");

    // table |..|..|.. is a row, and ||..||..||.. is a header
    // another one that will be hard to do in pure regexp
    $text = handleTable($text);

    // def lists:   ;term:definition to dl/dt/dd

    // img before http means we can have links made of images
    // img://....|...\s becomes image with alt or title
    $text = $text.replace(/\[img(:\/\/[^|]*)\|([^\]]*)\]/g, "<img src='http$1' title='$2'>");
    // img://....\s becomes an image
    $text = $text.replace(/\[img(:\/\/[^\]]*)\]/g, "<img src='http$1'/>");
    // http://....|...\s becomes an anchor
    $text = $text.replace(/\[(http:\/\/[^|]*)\|([^\]]*)\]/g, "<a href='$1'>$2</a>");
    $text = $text.replace(/\[(http:\/\/[^\]]*)\]/g, "<a href='$1'>$1</a>");

    // textareas often stick a newline at the end 
    $text = $text.replace(/<br\/>\n+$/, "<br/>");

    // lastly, all newlines to br's
    while($text.match(/\n\n/)) {
        $text = $text.replace(/\n\n/g, "<br/><br/>\n");
    }
    $text = $text.replace(/\\$/m, "<br/>\n");

    // TODO:
    //  automatic <a href for http://  ?? hard. Use [...|,,,] instead?
    //  automatic <img src for img://
    //  ability to escape any matching block with \
    //  use this for comments?
    //  add help
    
    textArea.value = $text;
}

function handleList($page, $char, $start, $end) {
    if(! $page.match("\n"+$char)) {
        return $page;
    }
    $regexpCharCount = "[^"+$char+"]";
    $regexpCharReplace = "^"+$char+"*\s";
    $lines = $page.split("\n");
    $text = "";
    $depth = 0;
    for($i=0; $i<$lines.length; $i++) {
        $line = $lines[$i];
        $count = $line.search($regexpCharCount);
        if($count < 1) { 
            if($depth != 0) {
                for($j=0; $j<$depth; $j++) {
                    $text += $end;
                }
	        // hack for css
	        if($depth == 1) {
		    $text += "<p>";
		}
                $depth = 0;
            }
            $text += $line;
            $text += "\n";
            continue;
        }
        $line = $line.substring($count+1);
        if($depth < $count) {
	    // hack for css
	    if($depth == 0) {
	        $text += "</p>";
	    }
	    while($depth < $count) {
                $depth++;
                $text += $start+"\n";
	    }
	    $text += "<li>"+$line+"</li>\n";
            continue;
        }
        if($depth > $count) {
            while($depth > $count) {
                $depth--;
                $text += $end+"\n";
	    }
            $text += "<li>"+$line+"</li>\n";
            continue;
        }
        $text += "<li>"+$line+"</li>\n";
    }
    if($depth != 0) {
        for($j=0; $j<$depth; $j++) {
            $text += $end;
        }
	// hack for css
        $text += "<p>";
    }
    return $text;
}

function handleTable($page) {
    if(! $page.match(/^\|/)) {
        return $page;
    }   
    $lines = $page.split("\n");
    $text = "";
    $inTable = false; 
    for($i=0; $i<$lines.length; $i++) {
        $line = $lines[$i];
        if($line.match(/^\|\|/)) { 
            if(!$inTable) {
                $text += "<table border='1'>\n";
                $inTable = true;
            }
            $line = $line.replace(/^\|\|([^\|]*)/, "<tr><th>$1");
            $line = $line.replace(/\|\|([^\|]*)/g, "</th><th>$1");
            $line = $line + "</th></tr>\n";
        } else
        if($line.match(/^\|/)) {
            if(!$inTable) {
                $text += "<table border='1'>\n";
                $inTable = true;
            }
            $line = $line.replace(/^\|([^\|]*)/, "<tr><td>$1");
            $line = $line.replace(/\|([^\|]*)/g, "</td><td>$1");
            $line = $line + "</td></tr>\n";
        } else { 
            if($inTable) {
                $text += "</table>\n";
                $inTable = false;
            }
        }
        $text += $line+"\n";
    }
    return $text;
}

// -->
</script>

(<a href="editors/help-wiki-js.html" alt="Wiki-js help" target="_blank">wiki-js help</a>)

<html:textarea property="text" cols="75" rows="20" style="width: 100%" tabindex="2" onchange="convertTextArea(this)"/>
