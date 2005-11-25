/*
 * Attr.js
 * An attribute node in an XML document
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

Node.ELEMENT_NODE                =  1;
Node.ATTRIBUTE_NODE              =  2;
Node.TEXT_NODE                   =  3;
Node.CDATA_SECTION_NODE          =  4;
Node.ENTITY_REFERENCE_NODE       =  5;
Node.ENTITY_NODE                 =  6; // not used
Node.PROCESSING_INSTRUCTION_NODE =  7;
Node.COMMENT_NODE                =  8;
Node.DOCUMENT_NODE               =  9;
Node.DOCUMENT_TYPE_NODE          = 10;
Node.DOCUMENT_FRAGMENT_NODE      = 11;
Node.NOTATION_NODE               = 12; // not used

function Node() {
  this.attributes = null;
  this.childNodes = new NodeList();
  this.firstChild = null
  this.lastChild = null;
  this.nextSibling = null;
  this.nodeName = null;
  this.nodeType = null;
  this.nodeValue = null;
  this.ownerDocument = null;
  this.parentNode = null;
  this.previousSibling = null;
}

Node.prototype.getAttributes = function() { return this.attributes; }
Node.prototype.getChildNodes = function() { return this.childNodes; }
Node.prototype.getFirstChild = function() { return this.firstChild; }
Node.prototype.getLastChild = function() { return this.lastChild; }
Node.prototype.getNextSibling = function() { return this.nextSibling; }
Node.prototype.getNodeName = function() { return this.nodeName; }
Node.prototype.getNodeType = function() { return this.nodeType; }
Node.prototype.getNodeValue = function() { return this.nodeValue; }
Node.prototype.getOwnerDocument = function() { return this.ownerDocument; }
Node.prototype.getParentNode = function() { return this.parentNode; }
Node.prototype.getPreviousSibling = function() { return this.previousSibling; }

Node.prototype.setNodeValue =
function() {
  // Default behavior is to do nothing;
  // overridden in some subclasses
}

Node.prototype.appendChild =
function( childNode ) {
  if ( this.nodeType == Node.ELEMENT_NODE ||
       this.nodeType == Node.ATTRIBUTE_NODE ||
       this.nodeType == Node.DOCUMENT_NODE ||
       this.nodeType == Node.DOCUMENT_FRAGMENT_NODE ) {
    this.childNodes.add( childNode );
  }
  else {
    // :REVISIT: change to DOMException
    throw new ParseError( "Cannot append child node" );
  }

  if ( this.ownerDocument != childNode.ownerDocument ) {
    // :REVISIT: change to DOMException
    throw new ParseError( "Cannot append child to this document" );
  }
  
  if ( this.childNodes.length == 1 ) {
    this.firstChild = childNode;
  }

  this.lastChild = childNode;
  childNode.parentNode = this;
  
  var prevSibling = this.childNodes.item( -2 );
  childNode.previousSibling = prevSibling;
  
  if ( prevSibling != null ) {
    prevSibling.nextSibling = childNode;
  }
}

Node.prototype.cloneNode =
function( deep ) {
  // :REVISIT: change to DOMException
  throw new ParseError( "Not implemented" );
}

Node.prototype.hasChildNodes =
function() {
  return ( this.childNodes.length > 0 );
}

Node.prototype.insertBefore =
function( newChild, refChild ) {
  var currentChildren = this.childNodes;
  this.childNodes = new NodeList();
  for ( var i = 0; i < currentChildren.length; ) {
    var child = currentChildren.item(i);
    if ( child == refChild && refChild != null ) {
      this.appendChild( newChild );
      refChild = null;
    }
    else {
      this.appendChild( child );
      i++;
    }
  }
}

Node.prototype.removeChild =
function( oldChild ) {
  var currentChildren = this.childNodes;
  this.childNodes = new NodeList();
  for ( var i = 0; i < currentChildren.length; i++ ) {
    var child = currentChildren.item(i)
    if ( child != oldChild ) {
      this.appendChild( child );
    }
  }
}

Node.prototype.replaceChild =
function( newChild, oldChild ) {
  var oldChildren = this.childNodes;
  this.childNodes = new NodeList();
  for ( var i = 0; i < oldChildren.length; i++ ) {
    if ( oldChildren.item(i) == oldChild ) {
      this.appendChild( newChild );
    }
    else {
      this.appendChild( oldChild );
    }
  }
}