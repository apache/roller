/*
 * DocumentType.js
 * A doctype node in an XML document
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

DocumentType.prototype = new Node();
DocumentType.prototype.constructor = DocumentType;
  // DocumentType : Node

function DocumentType( ownderDoc, name ) {
  this.name = name;
  this.entities = null;
  this.notations = null;
  
  // inherited from Node
  this.nodeName = name;
  this.nodeType = Node.DOCUMENT_TYPE_NODE;
  this.ownerDocument = ownderDoc;
}

DocumentType.prototype.getEntities = 
function() {
  // :REVISIT: change to DOMException
  throw new ParseError( "Not implemented" );
}

DocumentType.prototype.getName = 
function() {
  return this.name;
}

DocumentType.prototype.getNotations = 
function() {
  // :REVISIT: change to DOMException
  throw new ParseError( "Not implemented" );
}