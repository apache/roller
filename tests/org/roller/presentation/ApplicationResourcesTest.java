/*
 * Filename: ApplicationResourcesTest.java
 * 
 * Created on 24-May-04
 */
package org.roller.presentation;

import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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
 * @author <a href="mailto:molen@mail.com">Jaap van der Molen</a>
 * @version $Revision: 1.3 $
 */
public class ApplicationResourcesTest extends TestCase
{
	private String userDir = null;
	private Properties baseProps = null; 

	/**
	 * @param arg0
	 */
	public ApplicationResourcesTest(String name)
	{
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite();
		//suite.addTest(new ApplicationResourcesTest("testSystemProperties"));
		suite.addTest(new ApplicationResourcesTest("testApplicationResources_nl"));
		suite.addTest(new ApplicationResourcesTest("testApplicationResources_zh"));
		suite.addTest(new ApplicationResourcesTest("testApplicationResources_vi"));
		return suite;
	}
	
	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		super.setUp();
		userDir = System.getProperty("user.dir");
		
		// load base ApplicationResources.properties file
		baseProps = new Properties();
		baseProps.load(new FileInputStream( userDir + "/src/org/roller/presentation/ApplicationResources.properties"));
	}

	
	public void testApplicationResources_nl() throws Exception {
		
		// verify user-dir; should end with roller
		assertNotNull(userDir);
		assertTrue(userDir.endsWith("roller"));
		
		// load Dutch resource file
		Properties nlProps = new Properties();
		nlProps.load(new FileInputStream( userDir + "/src/org/roller/presentation/ApplicationResources_nl.properties"));
		
		Set keys = baseProps.keySet();
		boolean missingMessage = false;
		
		// check Dutch
		System.out.println("Veriyfing NL messages...");
		for (Iterator iter = keys.iterator(); iter.hasNext();)
		{
			String key = (String) iter.next();
			if (nlProps.getProperty(key)==null) {
				System.err.println(key+" = "+baseProps.getProperty(key));
				missingMessage=true;
			}
		}

		assertFalse(missingMessage);
	}

	public void testApplicationResources_zh() throws Exception {

		// verify user-dir; should end with roller
		assertNotNull(userDir);
		assertTrue(userDir.endsWith("roller"));
		
		// load Chinese resource file
		Properties zhProps = new Properties();
		zhProps.load(new FileInputStream( userDir + "/src/org/roller/presentation/ApplicationResources_zh.properties"));

		Set keys = baseProps.keySet();
		boolean missingMessage = false;

		// check Chinese
		System.out.println("Veriyfing ZH messages...");
		for (Iterator iter = keys.iterator(); iter.hasNext();)
		{
			String key = (String) iter.next();
			if (zhProps.getProperty(key)==null) {
				System.err.println(key+" = "+baseProps.getProperty(key));
				missingMessage=true;
			}
		}

		assertFalse(missingMessage);
	}
	
	public void testApplicationResources_vi() throws Exception {

		// verify user-dir; should end with roller
		assertNotNull(userDir);
		assertTrue(userDir.endsWith("roller"));
		
		// load Chinese resource file
		Properties zhProps = new Properties();
		zhProps.load(new FileInputStream( userDir + "/src/org/roller/presentation/ApplicationResources_vi.properties"));

		Set keys = baseProps.keySet();
		boolean missingMessage = false;

		// check Chinese
		System.out.println("Veriyfing VI messages...");
		for (Iterator iter = keys.iterator(); iter.hasNext();)
		{
			String key = (String) iter.next();
			if (zhProps.getProperty(key)==null) {
				System.err.println(key+" = "+baseProps.getProperty(key));
				missingMessage=true;
			}
		}

		assertFalse(missingMessage);
	}

	public void testSystemProperties() {
		Properties sysProps = System.getProperties();
		Set keys = sysProps.keySet();
		for (Iterator iter = keys.iterator(); iter.hasNext();)
		{
			String key = (String) iter.next();
			System.out.println(key + " = " + sysProps.getProperty(key));
		}
	}
}
