/*
* Licensed to the Apache Software Foundation (ASF) under one or more
*  contributor license agreements.  The ASF licenses this file to You
* under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.  For additional information regarding
* copyright in this work, please see the NOTICE file in the top level
* directory of this distribution.
*/
package org.apache.roller.weblogger.webservices.adminapi;

import java.io.IOException;
import org.apache.roller.weblogger.webservices.adminprotocol.sdk.MemberEntrySet;
import org.apache.roller.weblogger.webservices.adminprotocol.sdk.UnexpectedRootElementException;
import org.jdom.JDOMException;

public class MemberHandlerTest extends HandlerBaseTest {
    public void testHandler() {
        try {
            createSampleUser();
            createSampleWeblog();
            
            // delete user-weblog permission
            MemberEntrySet mesDelete = deleteSampleMember(true);
            assertNotNull(mesDelete);
            assertEquals(0, mesDelete.getEntries().length);            
            
            //create
            MemberEntrySet mesCreate = createSampleMember();
            assertNotNull(mesCreate);
            assertNotNull(mesCreate.getEntries());
            assertEquals(mesCreate.getEntries().length, 1);
            assertEquals(mesCreate, getSampleMemberEntrySet());
            
            //get
            MemberEntrySet mesFetch = fetchSampleMember();
            assertNotNull(mesFetch);
            assertNotNull(mesFetch.getEntries());
            assertEquals(mesFetch.getEntries().length, 1);
            assertEquals(mesFetch, mesCreate);
            
            //update
            MemberEntrySet mesUpdate = updateSampleMember();
            assertNotNull(mesUpdate);
            assertNotNull(mesUpdate.getEntries());
            assertEquals(mesUpdate.getEntries().length, 1);
            assertEquals(mesUpdate, updateSampleMemberEntrySet(getSampleMemberEntrySet()));
            
            //delete
            mesDelete = deleteSampleMember(true);
            assertNotNull(mesDelete);
            assertNotNull(mesCreate.getEntries());
            assertEquals(mesCreate.getEntries().length, 1);
            assertEquals(0, mesDelete.getEntries().length);
            
        } catch (IOException ioe) {
            ioe.printStackTrace();            
            fail(ioe.getMessage());
        } catch (JDOMException je) {
            je.printStackTrace();
            fail(je.getMessage());
        } catch (UnexpectedRootElementException uree) {
            uree.printStackTrace();            
            fail(uree.getMessage());
        }
    }
}

