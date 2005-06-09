/*
 * Element.js
 * An element node in an XML document
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

Element.prototype = new Node();
Element.prototype.constructor = Element;
  // Element : Node

function Element( ownerDoc, name ) {
  this.tagName = name;
  
  // inherited from Node
  this.attributes = new NamedNodeMap();
  this.childNodes = new NodeList();
  this.nodeType = Node.ELEMENT_NODE;
  this.nodeName = name;
  this.ownerDocument = ownerDoc;
}

Element.prototype.getAttribute =
function( name ) {
  var attr = this.attributes.getNamedItem( name );
  return ( attr == null ) ? "" : attr.getValue();
}

Element.prototype.getAttributeNode =
function( name ) {
  return this.attributes.getNamedItem( name );
}

Element.prototype.getElementsByTagName =
function( tagName ) {
  return new DeepNodeList( this, tagName );
}

Element.prototype.getTagName =
function() {
  return this.tagName;
}

Element.prototype.normalize =
function() {
  var child, next;
  for ( child = this.getFirstChild(); child != null; child = next ) {
    next = child.getNextSibling();
    if ( child.getNodeType() == Node.TEXT_NODE ) {
      if ( next != null && next.getNodeType() == Node.TEXT_NODE ) {
        child.appendData( next.getNodeValue() );
        this.removeChild( next );
        next = child;
      }
      else {
        if ( child.getNodeValue().length == 0 ) {
          this.removeChild( child );
        }
      }
    }
    else if ( child.getNodeType() == Node.ELEMENT_NODE ) {
      child.normalize();
    }
  }
}

Element.prototype.removeAttribute = 
function( name ) {
  this.attributes.removeNamedItem( name );
}

Element.prototype.removeAttributeNode =
function( attr ) {
  return this.attributes.removeNamedItem( attr.nodeName );
}

Element.prototype.setAttribute =
function( name, value ) {
  var attr = this.ownerDocument.createAttribute( name );
  arrt.setValue( value );
  this.attributes.setNamedItem( attr );
}

Element.prototype.setAttributeNode =
function( attr ) {
  return this.attributes.setNamedItem( attr );
}