/*
 * Filename: ApplicationResourcesTest.java
 * 
 * Created on 24-May-04
 */
package org.apache.roller.presentation;

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
 * @version $Revision: 1.7 $
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
		suite.addTest(
			new ApplicationResourcesTest("testApplicationResources_nl"));
		suite.addTest(
			new ApplicationResourcesTest("testApplicationResources_zh_cn"));
		suite.addTest(
			new ApplicationResourcesTest("testApplicationResources_zh_tw"));
		suite.addTest(
			new ApplicationResourcesTest("testApplicationResources_vi"));
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
		baseProps.load(new FileInputStream( 
                userDir + "/WEB-INF/classes/ApplicationResources.properties"));
	}

	/**
	 * Test Dutch stuff.
	 * 
	 * @throws Exception
	 */
	public void testApplicationResources_nl() throws Exception
	{
		verifyResourceBundle("ApplicationResources_nl");
	}

	/**
	 * Test Simple Chinese stuff.
	 * 
	 * @throws Exception
	 */
	public void testApplicationResources_zh_cn() throws Exception
	{
		verifyResourceBundle("ApplicationResources_zh_cn");
	}

	/**
	 * Test Traditional Chinese stuff.
	 * 
	 * @throws Exception
	 */
	public void testApplicationResources_zh_tw() throws Exception
	{
		verifyResourceBundle("ApplicationResources_zh_tw");
	}

	/**
	 * Test Vietnamese stuff.
	 * 
	 * @throws Exception
	 */
	public void testApplicationResources_vi() throws Exception
	{
		verifyResourceBundle("ApplicationResources_vi");
	}

	public void testSystemProperties()
	{
		Properties sysProps = System.getProperties();
		Set keys = sysProps.keySet();
		for (Iterator iter = keys.iterator(); iter.hasNext();)
		{
			String key = (String) iter.next();
			System.out.println(key + " = " + sysProps.getProperty(key));
		}
	}

	/**
	 * Helper method to do the actual testing.
	 * 
	 * @param bundle name of bundle to test
	 * @throws Exception if file not found, or if io ecxeption occurs.
	 */
	private void verifyResourceBundle(String bundle) throws Exception
	{
		// verify user-dir; should end with roller
		assertNotNull(userDir);
		assertTrue(userDir.endsWith("roller"));
		
		// load Chinese resource file
		Properties props = new Properties();
		props.load(
			new FileInputStream(
				userDir
					+ "/web/WEB-INF/classes/"
					+ bundle
					+ ".properties"));

		Set keys = baseProps.keySet();
		boolean missingMessage = false;

		// check Chinese
		System.out.println("Veriyfing " + bundle + "...");
		for (Iterator iter = keys.iterator(); iter.hasNext();)
		{
			String key = (String) iter.next();
			if (props.getProperty(key) == null)
			{
				System.err.println(key + " = " + baseProps.getProperty(key));
				missingMessage = true;
			}
		}

		assertFalse(missingMessage);
	}

}
