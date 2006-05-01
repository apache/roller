/*
 * Filename: LanguageUtilTest.java
 * 
 * Created on 13-Jul-04
 */
package org.apache.roller.presentation;

import java.util.Locale;
import java.util.Vector;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.mockrunner.mock.web.MockServletContext;

/**
 * The purpose of this class is to 
 * 
 * @author <a href="mailto:molen@mail.com">Jaap van der Molen</a>
 * @version $Revision: 1.1 $
 */
public class LanguageUtilTest extends TestCase
{
	private static Log logger = LogFactory.getLog(LanguageUtilTest.class);

	private static String supportedLanguages = "en,nl,vi,zh_cn,zh_tw";

	private ServletContext servletContext = null;

	/**
	 * @param arg0
	 */
	public LanguageUtilTest(String name)
	{
		super(name);
		BasicConfigurator.configure();
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		super.setUp();
		servletContext = new MockServletContext();
		servletContext.setAttribute(
			LanguageUtil.SUPPORTED_LANGUAGES,
			LanguageUtil.extractLanguages(supportedLanguages));
	}

	public static Test suite()
	{
		TestSuite suite = new TestSuite();
		suite.addTest(new LanguageUtilTest("testSupportedLanguages"));
		suite.addTest(new LanguageUtilTest("testIsSupported"));
		return suite;
	}
	
	public void testSupportedLanguages() {
		Locale[] l = LanguageUtil.getSupportedLanguages(servletContext);

		assertNotNull(l);

		for (int i=0; i<l.length; i++) {
			logger.debug("locale: "+l[i]);
		}

		assertEquals(l.length, 5);
		assertEquals(l[0], new Locale("en"));
		assertEquals(l[1], new Locale("nl"));
		assertEquals(l[2], new Locale("vi"));
		assertEquals(l[3], new Locale("zh", "cn"));
		assertEquals(l[4], new Locale("zh", "tw"));
		
	}
	
	public void testIsSupported() {
		assertTrue(LanguageUtil.isSupported( new Locale("en", "GB"), servletContext));
		assertFalse(LanguageUtil.isSupported( new Locale("de"), servletContext));
		assertTrue(LanguageUtil.isSupported( "en_GB", servletContext));
		assertFalse(LanguageUtil.isSupported( "de", servletContext));
	}

}
