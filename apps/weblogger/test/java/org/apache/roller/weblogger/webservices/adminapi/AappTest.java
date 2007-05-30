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

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import junit.framework.TestCase;
import org.apache.roller.weblogger.webservices.adminprotocol.sdk.MemberEntry;
import org.apache.roller.weblogger.webservices.adminprotocol.sdk.MemberEntrySet;
import org.apache.roller.weblogger.webservices.adminprotocol.sdk.UserEntry;
import org.apache.roller.weblogger.webservices.adminprotocol.sdk.UserEntrySet;
import org.apache.roller.weblogger.webservices.adminprotocol.sdk.WeblogEntry;
import org.apache.roller.weblogger.webservices.adminprotocol.sdk.WeblogEntrySet;

public abstract class AappTest extends TestCase {
    private static final Date sampleDate = new Date();
    private static final String DEFAULT_ENDPOINT_URL = "http://localhost:8080/roller/roller-services/aapp";
    private static final String DEFAULT_USER = "jtb";
    private static final String DEFAULT_PASSWORD = "iplanet";
        
    protected static String getEndpointUrl() {
        String endpoint = System.getProperty("aapp.endpoint");
        if (endpoint == null) {
            endpoint = DEFAULT_ENDPOINT_URL;
        }
        
        //System.err.println("endpoint=" + endpoint);
        return endpoint;
    }
    
    protected static String getUser() {
        String user = System.getProperties().getProperty("aapp.user");
        if (user == null) {
            user = DEFAULT_USER;
        }
        
        //System.err.println("user=" + user);
        return user;
    }
    
    protected static String getPassword() {
        String password = System.getProperties().getProperty("aapp.password");
        if (password == null) {
            password = DEFAULT_PASSWORD;
        }
        
        //System.err.println("password=" + password);
        return password;
    }
    
    protected static UserEntry getSampleUserEntry() {
        UserEntry ue = new UserEntry("foo", getEndpointUrl());
        ue.setEmailAddress("foo@bar.org");
        ue.setFullName("Foo Bar");
        ue.setLocale(Locale.getDefault());
        ue.setTimezone(TimeZone.getDefault());
        ue.setPassword("foo");
        ue.setEnabled(Boolean.TRUE);
        
        return ue;
    }
    
    protected static MemberEntry getSampleMemberEntry() {
        MemberEntry me = new MemberEntry("fooblog", "foo", getEndpointUrl());
        me.setPermission(MemberEntry.Permissions.AUTHOR);
        return me;
    }
    
    protected static UserEntry updateSampleUserEntry(UserEntry ue) {
        UserEntry ueUpdate = new UserEntry(ue.getName(), getEndpointUrl());
        ueUpdate.setEmailAddress("billy@bob.org");
        ueUpdate.setFullName("Billy Bob");
        ueUpdate.setLocale(new Locale("ms", "MY"));
        ueUpdate.setTimezone(TimeZone.getTimeZone("Asia/Kuala_Lumpur"));
        ueUpdate.setPassword("billy");
        ueUpdate.setEnabled(Boolean.FALSE);
        
        return ueUpdate;
    }
    
    protected static WeblogEntry updateSampleWeblogEntry(WeblogEntry we) {
        WeblogEntry weUpdate = new WeblogEntry(we.getHandle(), getEndpointUrl());
        weUpdate.setEmailAddress("billy@bob.org");
        weUpdate.setName("Billy Bob Weblog Name");
        weUpdate.setLocale(new Locale("ms", "MY"));
        weUpdate.setTimezone(TimeZone.getTimeZone("Asia/Kuala_Lumpur"));
        weUpdate.setDescription("Billy Bob Weblog Description");
        weUpdate.setCreatingUser(we.getCreatingUser());
        weUpdate.setEnabled(Boolean.FALSE);
        
        return weUpdate;
    }
    
    protected static MemberEntry updateSampleMemberEntry(MemberEntry me) {
        MemberEntry meUpdate = new MemberEntry(me.getHandle(), me.getName(), getEndpointUrl());
        meUpdate.setPermission(MemberEntry.Permissions.LIMITED);
        
        return meUpdate;
    }
    
    protected static UserEntrySet updateSampleUserEntrySet(UserEntrySet ues) {
        UserEntry ue = (UserEntry)ues.getEntries()[0];
        UserEntry ueUpdated = updateSampleUserEntry(ue);
        UserEntrySet uesUpdated = new UserEntrySet(getEndpointUrl());
        uesUpdated.setEntries(new UserEntry[] { ueUpdated });
        
        return uesUpdated;
    }
    
    protected static WeblogEntrySet updateSampleWeblogEntrySet(WeblogEntrySet wes) {
        WeblogEntry we = (WeblogEntry)wes.getEntries()[0];
        WeblogEntry weUpdated = updateSampleWeblogEntry(we);
        WeblogEntrySet wesUpdated = new WeblogEntrySet(getEndpointUrl());
        wesUpdated.setEntries(new WeblogEntry[] { weUpdated });
        
        return wesUpdated;
    }
    
    protected static MemberEntrySet updateSampleMemberEntrySet(MemberEntrySet mes) {
        MemberEntry me = (MemberEntry)mes.getEntries()[0];
        MemberEntry meUpdated = updateSampleMemberEntry(me);
        MemberEntrySet mesUpdated = new MemberEntrySet(getEndpointUrl());
        mesUpdated.setEntries(new MemberEntry[] { meUpdated });
        
        return mesUpdated;
    }
    
    protected static WeblogEntry getSampleWeblogEntry() {
        WeblogEntry we = new WeblogEntry("fooblog", getEndpointUrl());
        we.setEmailAddress("foo@bar.org");
        we.setCreatingUser("foo");
        we.setDescription("Foo Weblog Description");
        we.setLocale(Locale.getDefault());
        we.setTimezone(TimeZone.getDefault());
        we.setName("Foo Weblog Name");
        we.setEnabled(Boolean.TRUE);
        
        return we;
    }
    
    protected static UserEntrySet getSampleUserEntrySet() {
        UserEntry ue = getSampleUserEntry();
        UserEntrySet ues = new UserEntrySet(getEndpointUrl());
        ues.setEntries(new UserEntry[] { ue });
        
        return ues;
    }
    
    protected static WeblogEntrySet getSampleWeblogEntrySet() {
        WeblogEntry we = getSampleWeblogEntry();
        WeblogEntrySet wes = new WeblogEntrySet(getEndpointUrl());
        wes.setEntries(new WeblogEntry[] { we });
        
        return wes;
    }
    
    protected static MemberEntrySet getSampleMemberEntrySet() {
        MemberEntry me = getSampleMemberEntry();
        MemberEntrySet mes = new MemberEntrySet(getEndpointUrl());
        mes.setEntries(new MemberEntry[] { me });
        
        return mes;
    }
    
}
