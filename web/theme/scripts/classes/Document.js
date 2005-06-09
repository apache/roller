/*
 * Document.js
 * A document node in an XML document
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

Document.prototype = new Node();
Document.prototype.constructor = Document;
  // Document : Node

function Document() {
  this.doctype = null;
  this.implementation = null;
  this.documentElement = null;

  // inherited from Node
  this.childNodes = new NodeList();
  this.nodeName = "#document";
  this.nodeType = Node.DOCUMENT_NODE;
  this.ownerDocument = this;
}

Document.prototype.createAttribute =
function( name, value ) {
  return new Attr( this, name, value );
}

Document.prototype.createCDATASection =
function( data ) {
  return new CDATASection( this, data );
}

Document.prototype.createComment =
function( data ) {
  return new Comment( this, data );
}

Document.prototype.createDocumentFragment =
function() {
  return new DocumentFragment( this );
}

Document.prototype.createElement =
function( tagName ) {
  return new Element( this, tagName );
}

Document.prototype.createEntityReference =
function( name ) {
  return new EntityReference( this, name );
}

Document.prototype.createProcessingInstruction =
function( target, data ) {
  return new ProcessingInstruction( this, target, data );
}

Document.prototype.createTextNode =
function( data ) {
  return new Text( this, data );
}

Document.prototype.getDoctype =
function() {
  return this.doctype;
}

Document.prototype.getDocumentElement =
function() {
  return this.documentElement;
}

Document.prototype.getElementsByTagName =
function( tagName ) {
  return new DeepNodeList( this, tagName );
}

Document.prototype.getImplementation =
function() {
  // :REVISIT:
  return this.implementation;
}