/*
 * NamedNodeMap.js
 * A collection of nodes accessible by name in an XML document
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

function NamedNodeMap() {
  this.length = 0;
}

NamedNodeMap.prototype.getLength =
function() {
  return this.length;
}

NamedNodeMap.prototype.getNamedItem =
function( name ) {
  return ( this[ name ] || null );
}

NamedNodeMap.prototype.item =
function( index ) {
  var item;
  item = ( index < 0 ) ? this[ this.length + index ]
                       : this[ index ];
  return ( item || null );
}

NamedNodeMap.prototype.removeNamedItem =
function( name ) {
  var removed = this[ name ];

  if ( !removed ) {
    return null;
  }

  delete this[ name ];
  for ( var i = 0; i < this.length - 1; i++ ) {
    if ( !this[i] ) {
      this[i] = this[ i + 1 ];
      delete this[ i + 1 ];
    }
  }
  this.length--;
  return removed;
}

NamedNodeMap.prototype.setNamedItem =
function( node ) {
  var nodeName = node.getNodeName();
  var item = this.getNamedItem( nodeName );
  this[ nodeName ] = node;
  
  if ( item == null ) {
    this[ this.length++ ] = node;
  }
  
  return item;
}