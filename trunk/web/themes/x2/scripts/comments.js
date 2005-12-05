///////////////////////////////////////////////////////////////////
//// Globals

var gCommentBoxTemplate = null;
var gCommentTemplate = null;
var gSubmittingComment = null;
var gMediaDoc = null;
var gMediaCallback = null;
var gUserPath = null;

///////////////////////////////////////////////////////////////////
//// Misc

function cropString(aString, aLimit)
{
  return aString.length > aLimit ? aString.substr(0, aLimit) + "..." : aString;
}

/////////////////////////////////////////////////////////////////////
//// Comments

function initComments()
{
  gCommentTemplate = new JHTemplate("commentTemplate"); 
  gCommentBoxTemplate = new JHTemplate("commentBoxTemplate"); 
  window.status="Done initializing comment templates...";
}

function toggleComments(aEntryId, userPath)
{
  var twistyElt = document.getElementById("commentTwisty" + aEntryId);
  if (twistyElt.getAttribute("open") == "true") {
    hideComments(aEntryId);
  } else {
    showComments(aEntryId, userPath);
  }
}

function hideComments(aEntryId)
{
  var twistyElt = document.getElementById("commentTwisty" + aEntryId);
  twistyElt.removeAttribute("open");
  twistyElt.className = "commentTwisty";

  twistyElt.parentNode.removeChild(twistyElt.nextSibling);
}

function showComments(aEntryId, userPath)
{
  gUserPath = userPath;
  var twistyElt = document.getElementById("commentTwisty" + aEntryId);
  twistyElt.setAttribute("open", "true");
  twistyElt.className = "commentTwisty commentTwistyOpen";

  var commentBoxElt = gCommentBoxTemplate.createTemplate();
  gCommentBoxTemplate.setAttribute(commentBoxElt, "entryid", "value", aEntryId.replace(/^0*/, ""));
  twistyElt.parentNode.insertBefore(commentBoxElt, twistyElt.nextSibling);

  var cacheKiller = new Date().getTime(); // Make url unique to prevent loading it from cache
  var url = userPath + "/comments?entryid=" + aEntryId + "&"+cacheKiller;
  loadXMLDocument(url, onCommentsLoaded);
  // populate the user's information
  getUserInformation(commentBoxElt.getElementsByTagName("form")[0]);
}

function onCommentsLoaded(aDoc)
{
  if (!aDoc || !aDoc.documentElement) {
    return;
  }
  
  var entryId = aDoc.documentElement.getAttribute("entryid");
  if (!entryId)
    return;

  var twistyElt = document.getElementById("commentTwisty" + entryId);

  var commentBoxElt = twistyElt.nextSibling;
  
  var commentCount = 0;
  var child = aDoc.documentElement.lastChild;
  while (child) {
    if (child.nodeType == 1) {
      var div = gCommentTemplate.createTemplate();
      commentBoxElt.insertBefore(div, commentBoxElt.firstChild);

      var authorElt = null, urlElt = null, timeStampElt = null, bodyElt = null;
      var commentChild = child.firstChild;
      while (commentChild) {
        if (commentChild.nodeType == 1) {
          if (commentChild.nodeName == "author") {
            authorElt = commentChild;
          } else if (commentChild.nodeName == "url")  {
            urlElt = commentChild;
          } else if (commentChild.nodeName == "timestamp")  {
            timeStampElt = commentChild;
          } else if (commentChild.nodeName == "body")  {
            bodyElt = commentChild;
          }
        }
        commentChild = commentChild.nextSibling;
      }

      if (urlElt.firstChild) {
        gCommentTemplate.setAttribute(div, "commentAuthorLink", "href", urlElt.firstChild.nodeValue);
      }
      if (authorElt.firstChild) {
        gCommentTemplate.setTextValue(div, "commentAuthorLink", authorElt.firstChild.nodeValue);
      } else {
        gCommentTemplate.setTextValue(div, "commentAuthorLink", "Anonymous");
      }
      gCommentTemplate.setTextValue(div, "commentTimestamp", timeStampElt.firstChild.nodeValue);
      if (bodyElt.firstChild) {
        gCommentTemplate.setInnerHTML(div, "commentBody", toInnerXML(bodyElt));
      }
      ++commentCount;
    }
    
    child = child.previousSibling;
  }
  
  if (commentCount > 0) {
    if (commentCount == 1) {
      twistyElt.innerHTML = commentCount + " Comment";
    } else {
      twistyElt.innerHTML = commentCount + " Comments";
    }
  }
}

function onSubmitComments(aEntryId)
{
  gSubmittingComment = aEntryId;
  setTimeout("onCommentSubmitted()", 500);
}

function onCommentSubmitted()
{
  if (gSubmittingComment) {
    hideComments(gSubmittingComment);
    showComments(gSubmittingComment, gUserPath);
    gSubmittingComment = null;
  }
}

/////////////////////////////////////////////////////////////////////
//// XML Document loading

function loadXMLDocument(aURL, aCallback)
{ 
  gMediaCallback = aCallback;
  
  if (window.ActiveXObject) {
    // Internet Explorer XML loading syntax
    gMediaDoc = new ActiveXObject(getControlPrefix() + ".XmlDom");
    gMediaDoc.onreadystatechange = onMediaReadyStateChange;
    gMediaDoc.async = true;
    gMediaDoc.load(aURL);
  } else {
    // Mozilla XML loading syntax
    gMediaDoc = document.implementation.createDocument("", "", null);
	var xmlHttp = new XMLHttpRequest();
    xmlHttp.overrideMimeType("text/xml");
	xmlHttp.open("GET", aURL, false);
	xmlHttp.send(null);
    gMediaDoc.loadXML(xmlHttp.responseXML.xml);
    onMediaLoaded();
  }
}

function onMediaLoaded()
{
  if (gMediaCallback) {
    gMediaCallback(gMediaDoc);
  }
}

function onMediaReadyStateChange()
{
  if (gMediaDoc.readyState == 4)
    onMediaLoaded();
}

/////////////////////////////////////////////////////////////////////
//// XML helper courtesy of http://webfx.eae.net/

function getControlPrefix()
{
  if (getControlPrefix.prefix)
    return getControlPrefix.prefix;

  var prefixes = ["MSXML2", "Microsoft", "MSXML", "MSXML3"];
  var o, o2;
  for (var i = 0; i < prefixes.length; i++) {
    try {
      // try to create the objects
      o = new ActiveXObject(prefixes[i] + ".XmlHttp");
      o2 = new ActiveXObject(prefixes[i] + ".XmlDom");
      return getControlPrefix.prefix = prefixes[i];
    } catch (ex) { };
  }

  throw new Error("Could not find an installed XML parser");
}


//------------------ dom.js ------------------------------- //
///////////////////////////////////////////////////////////////////
//// Positioning

function getRelativeTop(aBoxElt, aCommonAncestorElt)
{
  var top = 0;
  while (aBoxElt && aBoxElt != aCommonAncestorElt.parentNode) {
    top += aBoxElt.offsetTop;
    aBoxElt = aBoxElt.parentNode;
  }
  
  return top;
}

function getRelativeLeft(aBoxElt, aCommonAncestorElt)
{
  var left = 0;
  while (aBoxElt && aBoxElt != aCommonAncestorElt.parentNode) {
    left += aBoxElt.offsetLeft;
    aBoxElt = aBoxElt.parentNode;
  }
  
  return left;
}

///////////////////////////////////////////////////////////////////
//// Events

function getEventTargetByNodeName(aRoot, aNodeName)
{
  // Walk up to the target element.
  var t = aRoot;
  while (t) {
    if (t.nodeType == 1 && t.nodeName == aNodeName) {
      break;
    }
    t = t.parentNode;
  }
  
  return t;
}

function getEventTargetByAttr(aRoot, aAttr)
{
  // Walk up to the target element.
  var t = aRoot;
  while (t) {
    if (t.nodeType == 1 && t.getAttribute(aAttr)) {
      break;
    }
    t = t.parentNode;
  }
  
  return t;
}

///////////////////////////////////////////////////////////////////
//// Serialization

function toInnerXML(aElt)
{
  var xml = "";
  var child = aElt.firstChild;
  while (child) {
    xml += toXML(child);
    child = child.nextSibling;
  }
  
  return xml;
}

function toXML(aElt)
{
  if (aElt.nodeType == 1) {
    var xml = "<" + aElt.nodeName;
    
    var child = aElt.firstChild;
    if (child) {
      xml += ">";
      while (child) {
        if (child.nodeType == 1) { // element node
          xml += toInnerXML(child);
        } else if (child.nodeType == 3) { // text node
          xml += child.nodeValue;
        }
        child = child.nextSibling;
      }
      xml += "</" + aElt.nodeName + ">";
    } else {
      xml += "/>";
    }
  
    return xml;
  } else {
    return aElt.nodeValue;
  }
}

//------------------ template.js ------------------------------- //
function JHTemplate(aId)
{
  this.init(aId);
}

JHTemplate.prototype = 
{
  mTemplateElt: null,
  mPaths: null,
  
  init: function (aId)
  {
    this.mPaths = {};
    
    this.mTemplateElt = document.getElementById(aId);
    if (this.mTemplateElt) {
      // Rip the template out of the document and cleanse it of inpurities
      this.mTemplateElt.parentNode.removeChild(this.mTemplateElt);
      this.mTemplateElt.removeAttribute("id");
      //this.removeTextNodes(this.mTemplateElt);
    
      this.buildPaths(this.mTemplateElt, [-1]);
    }
  
  },
  
  //////////////////////////////////////////////////////////////
  //// JHTemplate public

  createTemplate: function()
  {
    return this.mTemplateElt.cloneNode(true);
  },

  getInsertionChild: function(aElt, aInsertionId)
  {
    if (!(aInsertionId in this.mPaths))
      return null;

    var path = this.mPaths[aInsertionId];
    
    var child = aElt;
    for (var i = 0; child && i < path.length; ++i)
      child = child.childNodes[path[i]];

    return child;
  },

  setAttribute: function(aElt, aId, aAttr, aValue)
  {
    var elt = this.getInsertionChild(aElt, aId);
    elt.setAttribute(aAttr, aValue);
  },  

  setTextValue: function(aElt, aId, aValue)
  {
    var elt = this.getInsertionChild(aElt, aId);
    elt.appendChild(document.createTextNode(aValue));
  },  

  setInnerHTML: function(aElt, aId, aHTML)
  {
    var elt = this.getInsertionChild(aElt, aId);

    elt.innerHTML = aHTML;
    
    /*var range = document.createRange();
    range.selectNodeContents(elt);
    var fragment = range.createContextualFragment(aHTML);
    elt.appendChild(fragment);*/
  },  

  //////////////////////////////////////////////////////////////
  //// JHTemplate private
  
  buildPaths: function(aElt, aPath)
  {
    var child = aElt.firstChild;
    while (child) {
      aPath[aPath.length-1] += 1;
      
      if (child.nodeType == 1) {
        if (child.getAttribute("id")) {
          var templId = child.getAttribute("id");
          child.removeAttribute("id");
          this.mPaths[templId] = aPath.slice(0, aPath.length);
        }

        aPath.push(-1);
        this.buildPaths(child, aPath);
        aPath.pop();
      }
            
      child = child.nextSibling;
    }
  },
    
  removeTextNodes: function(aElement)
  {
    var child = aElement.firstChild;
    while (child) {
      var nextChild = child.nextSibling;
      if (child.nodeType == 3)
        aElement.removeChild(child);
      else
        this.removeTextNodes(child);
      child = nextChild;
    }
  }

};

function getUserInformation(theForm) {
    var author = getCookie("commentAuthor");
    var email = getCookie("commentEmail");
    var url = getCookie("commentUrl");
    // check each field - IE will render "null"
    if (author) {        
    	theForm.name.value = author;
    }
    if (email) {
        theForm.email.value = email;
    }
    if (url) {
        theForm.url.value = url;
    }

    if (author || email || url) {
        theForm.rememberInfo.checked = true;
    }
}

function fixURL(theForm) {
    if (theForm.url.value != "" && 
        theForm.url.value.indexOf("http://") == -1) { //prepend http://
            theForm.url.value = "http://"+theForm.url.value;
    }
    saveUserInformation(theForm);
}

function saveUserInformation(theForm) {
    if (theForm.rememberInfo.checked) {
        rememberUser(theForm);
    } else {
        forgetUser(theForm);
    }
}

function validateComments(theForm) {
    if (theForm.content.value == "") {
        alert("Please enter a comment.");
        theForm.content.focus();
        return false;
    }   
}