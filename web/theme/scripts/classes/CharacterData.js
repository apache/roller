/*
 * CharacterData.js
 * A character data node in an XML document
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

CharacterData.prototype = new Node();
CharacterData.prototype.constructor = CharacterData;
  // CharacterData : Node

function CharacterData( data ) {
  this.data = data;
  
  // inherited from Node
  this.nodeValue = data;
}

CharacterData.prototype.appendData =
function( data ) {
  this.setData( this.getData() + data );
}

CharacterData.prototype.deleteData =
function( offset, count ) {
  var begin = this.getData().substring( 0, offset );
  var end = this.getData().substring( offset + count );
  this.setData( begin + end );
}

CharacterData.prototype.getData =
function() {
  return this.data;
}

CharacterData.prototype.getLength =
function() {
  return ( this.data ) ? this.data.length : 0;
}

CharacterData.prototype.insetData =
function( offset, data ) {
  var begin = this.getData().substring( 0, offset );
  var end = this.getData().substring( offset, this.getLength );
  this.setData( begin + data + end );
}

CharacterData.prototype.replaceData =
function( offset, count, data ) {
  this.deleteData( offset, count );
  this.insertData( offset, data );
}

CharacterData.prototype.setData =
function( data ) {
  this.setNodeValue( data );
}

CharacterData.prototype.setNodeValue =
function( value ) {
  this.data = value;
  this.nodeValue = value;
}

CharacterData.prototype.substringData =
function( offset, count ) {
  return this.getData().substring( offset, offset + count );
}