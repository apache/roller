/*
 * DeepNodeList.js
 * An list containing nodes nested at any level in an XML document
 *
 * Copyright (C) 2001 David A. Lindquist (dave@gazingus.org)
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

DeepNodeList.prototype = new NodeList();
DeepNodeList.prototype.constructor = DeepNodeList;
  // DeepNodeList : NodeList

function DeepNodeList( rootNode, tagName ) {
  this.rootNode = rootNode;
  this.tagName = tagName;
  this.getElementsByTagName( rootNode );
}

DeepNodeList.prototype.getElementsByTagName =
function( contextNode ) {
  var nextNode;
  while ( contextNode != null ) {
    if ( contextNode.hasChildNodes() ) {
      contextNode = contextNode.firstChild;
    }
    else if ( contextNode != this.rootNode &&
              null != ( next = contextNode.nextSibling ) ) {
      contextNode = next;
    }
    else {
      next = null;
      for ( ; contextNode != this.rootNode;
           contextNode = contextNode.parentNode ) {
        next = contextNode.nextSibling;
        if ( next != null ) {
          break;
        }
      }
      contextNode = next;
    }
    if ( contextNode != this.rootNode &&
         contextNode != null &&
         contextNode.nodeType == Node.ELEMENT_NODE ) {
      if ( this.tagName == "*" || contextNode.tagName == this.tagName ) {
        this.add( contextNode );
      }
    }
  }
  return null;
}