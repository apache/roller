/*
 *  Copyright 2007 Sun Microsystems, Inc.  All rights reserved.
 *  Use is subject to license terms.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License"); you
 *  may not use this file except in compliance with the License. You may
 *  obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.roller.weblogger.webservices.atomprotocol;

import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import org.rometools.propono.atom.server.AtomException;
import org.rometools.propono.atom.server.AtomRequest;


/**
 * Collection of weblog entry comments.
 * @author davidm.johnson@sun.com
 */
public class CommentCollection {
    
    public String postEntry(AtomRequest areq, Entry entry) throws AtomException {
        return null;
    }
    
    public Entry getEntry(AtomRequest areq) throws AtomException {
        return null;
    }
    
    public Feed getCollection(AtomRequest areg) throws AtomException {
        return null;
    }
    
    public void putEntry(AtomRequest areq, Entry entry) throws AtomException {
    }
    
    public void deleteEntry(AtomRequest areq) throws AtomException {
    }
}