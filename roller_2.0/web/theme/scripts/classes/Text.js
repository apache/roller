/*
 * Text.js
 * An text node in an XML document
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

Text.prototype = new CharacterData();
Text.prototype.constructor = Text;
  // Text : CharacterData

function Text( ownerDoc, data ) {
  // inherited from CharacterData
  this.data = data;
  
  // inherited from CharacterData : Node
  this.nodeName = "#text";
  this.nodeType = Node.TEXT_NODE;
  this.nodeValue = data;
  this.ownerDocument = ownerDoc;
}

Text.prototype.splitText =
function( offset ) {
  // :REVISIT:
  // check for index out of bounds condition
  
  var newText =
    this.getOwnerDocument().createTextNode( this.data.substring( offset ) );
  
  var parentNode = this.getParentNode();
  if ( parentNode != null ) {
    parentNode.insetBefore( newText, this.nextSibling );
  }
  
  return newText;
}