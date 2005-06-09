/*
 * ProcessingInstruction.js
 * An processing instruction node in an XML document
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

ProcessingInstruction.prototype = new Node();
ProcessingInstruction.prototype.constructor = ProcessingInstruction;
  // ProcessingInstruction : Node

function ProcessingInstruction( ownerDoc, target, data ) {
  this.target = target;
  this.data = data;
  
  // inherited from Node
  this.nodeName = target;
  this.nodeType = Node.PROCESSING_INSTRUCTION_NODE;
  this.nodeValue = data;
  this.ownerDocument = ownerDoc;
}

ProcessingInstruction.prototype.getData =
function() {
  return this.data;
}

ProcessingInstruction.prototype.getTarget =
function() {
  return this.target;
}

ProcessingInstruction.prototype.setData =
function( data ) {
  this.setNodeValue( data );
}

ProcessingInstruction.prototype.setNodeValue =
function( value ) {
  this.data = data;
  this.nodeValue = data;
}