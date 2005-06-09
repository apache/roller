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

Attr.prototype = new Node();
Attr.prototype.constructor = Attr;
  // Attr : Node

function Attr( ownerDoc, name ) {
  this.name = name;
  this.specified = true;
  this.value = null;
  
  // inherited from Node
  this.childNodes = new NodeList();
  this.nodeName = name;
  this.nodeType = Node.ATTRIBUTE_NODE;
  this.nodeValue = null;
  this.ownerDocument = ownerDoc;
}

Attr.prototype.getName =
function() {
  return this.name;
}

Attr.prototype.getNodeValue =
function() {
  return this.getValue();
}

Attr.prototype.getSpecified =
function() {
  return this.specified;
}

Attr.prototype.getValue =
function() {
  // :REVISIT:
  var value = "";
  for ( var i = 0; i < this.childNodes.length; i++ ) {
    value += this.childNodes.item(i).getNodeValue();
  }
  return value;
}

Attr.prototype.setValue =
function( value ) {
  // :REVISIT:
  this.childNodes = new NodeList();
  this.firstChild = null;
  this.lastChild = null;
  
  if ( value != null ) {
    this.appendChild( this.ownerDocument.createTextNode( value ) );
  }
}