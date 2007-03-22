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
package org.apache.roller.webservices.adminapi;

import java.io.IOException;
import org.apache.roller.webservices.adminprotocol.sdk.UnexpectedRootElementException;
import org.apache.roller.webservices.adminprotocol.sdk.UserEntry;
import org.apache.roller.webservices.adminprotocol.sdk.UserEntrySet;
import org.jdom.JDOMException;

/**
 *
 * @author jtb
 */
public class UserHandlerTest extends HandlerBaseTest {
    public void testHandler() {
        try {
            //create
            UserEntrySet uesCreate = createSampleUser();
            assertNotNull(uesCreate);
            assertNotNull(uesCreate.getEntries());
            assertEquals(uesCreate.getEntries().length, 1);
            assertEquals(uesCreate, getSampleUserEntrySet());
            
            //get
            UserEntrySet uesFetch = fetchSampleUser();
            assertNotNull(uesFetch);
            assertNotNull(uesFetch.getEntries());
            assertEquals(uesFetch.getEntries().length, 1);
            assertEquals(uesFetch, uesCreate);
            
            //update
            UserEntrySet uesUpdate = updateSampleUser();
            assertNotNull(uesUpdate);
            assertNotNull(uesUpdate.getEntries());
            assertEquals(uesUpdate.getEntries().length, 1);
            assertEquals(uesUpdate, updateSampleUserEntrySet(getSampleUserEntrySet()));
            
            //delete
            UserEntrySet uesDelete = deleteSampleUser(true);
            assertNotNull(uesDelete);
            assertNotNull(uesCreate.getEntries());
            assertEquals(uesCreate.getEntries().length, 1);
            assertEquals(uesDelete, uesUpdate);
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

    public void testEnabled() {
        try {
            UserEntrySet ues = createSampleUser();
            UserEntry ue = (UserEntry)ues.getEntries()[0];
            assertEquals(Boolean.TRUE, ue.getEnabled());
            
            ues = updateSampleUser();
            ue = (UserEntry)ues.getEntries()[0];
            assertEquals(Boolean.FALSE, ue.getEnabled());

            ues = fetchSampleUser();
            ue = (UserEntry)ues.getEntries()[0];
            assertEquals(Boolean.FALSE, ue.getEnabled());
        } catch (IOException ioe) {
            fail(ioe.getMessage());
            ioe.printStackTrace();
        } catch (JDOMException je) {
            fail(je.getMessage());
            je.printStackTrace();
        } catch (UnexpectedRootElementException uree) {
            fail(uree.getMessage());
            uree.printStackTrace();
        }
    }    
    
}
