/*
 * Comment.js
 * An comment node in an XML document
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

Comment.prototype = new CharacterData();
Comment.prototype.constructor = Comment;
  // Comment : CharacterData

function Comment( ownerDoc, data ) {
  // inherited from CharacterData
  this.data = data;
  
  // inherited from CharacterData : Node
  this.nodeName = "#comment";
  this.nodeType = Node.COMMENT_NODE;
  this.nodeValue = data;
  this.ownerDocument = ownerDoc;
}