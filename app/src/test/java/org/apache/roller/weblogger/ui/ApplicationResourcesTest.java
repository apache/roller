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
/*
 * Filename: ApplicationResourcesTest.java
 * 
 * Created on 24-May-04
 */
package org.apache.roller.weblogger.ui;

import org.junit.jupiter.api.BeforeEach;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;


/**
 * The purpose of this class is to verify that all messages in
 * the base ApplicationResources.properties file also appear
 * in the localized properties files.
 * 
 * If messages do not appear, the test fails and the 'evil-doers' are
 * printed to System.out.  
 * 
 * Note: we need to make sure that new property files are added to this
 * test.
 *
 * Note: commented out - all translations need update
 * 
 * @author <a href="mailto:molen@mail.com">Jaap van der Molen</a>
 * @version $Revision: 1.7 $
 */
public class ApplicationResourcesTest {
	//private String userDir = null;
	private Properties baseProps = null; 

	@BeforeEach
	public void setUp() throws Exception {
		//userDir = System.getProperty("user.dir");
		
		// load base ApplicationResources.properties file
		baseProps = new Properties();
		baseProps.load(new FileInputStream( 
                System.getProperty("project.build.directory") + "/classes/ApplicationResources.properties"));
	}

	/**
	 * Test Simple Chinese stuff.
	 */
	public void testApplicationResources_zh_cn() throws Exception {
		verifyResourceBundle("ApplicationResources_zh_cn");
	}

    public void testSystemProperties() {
        Properties sysProps = System.getProperties();
        for (Object key : sysProps.keySet()) {
            System.out.println(key + " = " + sysProps.getProperty((String)key));
        }
    }

	/**
	 * Helper method to do the actual testing.
	 * 
	 * @param bundle name of bundle to test
	 * @throws Exception if file not found, or if io ecxeption occurs.
	 */
	private void verifyResourceBundle(String bundle) throws Exception {
		// verify user-dir; should end with roller
		//assertNotNull(userDir);
		//assertTrue(userDir.endsWith("roller"));
		
		// load Chinese resource file
		Properties props = new Properties();
		props.load(
			new FileInputStream(
				   System.getProperty("project.build.directory")
                    + "/classes/"
					+ bundle
					+ ".properties"));

		Set keys = baseProps.keySet();
		boolean missingMessage = false;

		// check Chinese
		System.out.println("Verifying " + bundle + "...");
        for (Object key : baseProps.keySet()) {
            if (props.getProperty((String) key) == null)
            {
                System.err.println(key + " = " + baseProps.getProperty((String) key));
                missingMessage = true;
            }
        }

		assertFalse(missingMessage);
	}

}
