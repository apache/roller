    
package org.roller.jspwiki;

import java.io.IOException;
import java.util.Properties;

import com.ecyrd.jspwiki.NoRequiredPropertyException;
import com.ecyrd.jspwiki.providers.BasicAttachmentProvider;

public class AttachmentProvider extends BasicAttachmentProvider {
	
	public void initialize(Properties properties)
			throws NoRequiredPropertyException, IOException {
		
		String rollerStorageDir = System.getProperty("rollerStorageDir");
		if (rollerStorageDir != null) {
            System.out.println(getClass().getName() 
               + " Using storageDir=" + rollerStorageDir);
			properties.put(PROP_STORAGEDIR, rollerStorageDir);
		}
		
		super.initialize(properties);
	}
}
