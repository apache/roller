
package org.roller.jspwiki;

import java.io.IOException;
import java.util.Properties;

import com.ecyrd.jspwiki.NoRequiredPropertyException;
import com.ecyrd.jspwiki.providers.VersioningFileProvider;

public class PageProvider extends VersioningFileProvider {
	
	public void initialize(Properties properties)
			throws NoRequiredPropertyException, IOException {
		
		String rollerPageDir = System.getProperty("rollerPageDir");
		if (rollerPageDir != null) {
            System.out.println(getClass().getName() 
               + " Using pageDir=" + rollerPageDir);
			properties.put(PROP_PAGEDIR, rollerPageDir);
		} 
		
		super.initialize(properties);
	}
}
