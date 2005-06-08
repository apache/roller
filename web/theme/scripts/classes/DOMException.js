/*
 * DOMException.js
 * An exception that can occur when manipulating an XML document
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

// :REVISIT:
// not currently used

DOMException.prototype = new Error();
DOMException.prototype.constructor = DOMException;
  // DOMException : Error

DOMException.INDEX_SIZE_ERR              = 1;
DOMException.DOMSTRING_SIZE_ERR          = 2;
DOMException.HIERARCHY_REQUEST_ERR       = 3;
DOMException.WRONG_DOCUMENT_ERR          = 4;
DOMException.INVALID_CHARACTER_ERR       = 5;
DOMException.NO_DATA_ALLOWED_ERR         = 6;
DOMException.NO_MODIFICATION_ALLOWED_ERR = 7;
DOMException.NOT_FOUND_ERR               = 8;
DOMException.NOT_SUPPORTED_ERR           = 9;
DOMException.INUSE_ATTRIBUTE_ERR         = 10;

function DOMException( code, message ) {
  this.code = code;
  this.message = message;
}