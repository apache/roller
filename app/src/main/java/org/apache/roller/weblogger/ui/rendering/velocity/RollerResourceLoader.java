/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.roller.weblogger.ui.rendering.velocity;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.TemplateRendition;
import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.util.ExtProperties;

/**
 * The RollerResourceLoader is a Velocity template loader which loads templates
 * from custom themes.
 * 
 * RollerResourceLoader makes use of WebloggerFactory.
 * 
 * @author <a href="mailto:lance@brainopolis.com">Lance Lavandowska</a>
 * @version $Id: RollerResourceLoader.java,v 1.9 2005/01/15 03:32:49 snoopdave
 *          Exp $
 */
public class RollerResourceLoader extends ResourceLoader {

	private static final Log logger = LogFactory.getLog(RollerResourceLoader.class);

    @Override
	public void init(ExtProperties configuration) {
		if (logger.isDebugEnabled()) {
			logger.debug(configuration);
		}
	}

	/**
	 * Get an Reader so that the Runtime can build a template with it.
	 * 
	 * @param name
	 *            name of template
	 * @return Reader containing template
     * @throws ResourceNotFoundException
	 */
    @Override
    public Reader getResourceReader(String name, String encoding) {

		logger.debug("Looking for: " + name);

		if (name == null || name.length() == 0) {
			throw new ResourceNotFoundException(
					"Need to specify a template name!");
		}

		// theme templates name are <template>|<deviceType>
		RenditionType renditionType = RenditionType.STANDARD;
		if (name.contains("|")) {
			String[] pair = name.split("\\|");
			name = pair[0];
			renditionType = RenditionType.valueOf(pair[1].toUpperCase());
		}

		logger.debug("   Actually, it's " + name);

		try {
			WeblogTemplate page = WebloggerFactory.getWeblogger()
					.getWeblogManager().getTemplate(name);

			if (page == null || !inRenderScope(page)) {
				throw new ResourceNotFoundException(
						"RollerResourceLoader: page \"" + name + "\" not found");
			}
			String contents = "";
			TemplateRendition templateCode = page.getTemplateRendition(renditionType);
            if (templateCode == null && renditionType != RenditionType.STANDARD) {
                // fall back to standard rendition if mobile or other unavailable
                templateCode = page.getTemplateRendition(RenditionType.STANDARD);
            }
			if (templateCode != null) {
				contents = templateCode.getTemplate();
			}
			return new InputStreamReader(new ByteArrayInputStream(contents.getBytes(encoding)));

		} catch (UnsupportedEncodingException uex) {
			// This should never actually happen. We expect UTF-8 in all JRE
			// installation.
//			logger.error(uex);
			throw new RuntimeException(uex);

		} catch (WebloggerException | ResourceNotFoundException re) {
			String msg = "RollerResourceLoader Error: "
					+ "database problem trying to load resource " + name;
//			logger.error(msg, re);
			throw new ResourceNotFoundException(msg, re);
		}
	}

	/**
	 * While a template is being rendered, the only stored templates it may
	 * load (by #parse or #include) are those of the weblog being rendered, so
	 * one weblog cannot read or run another's templates by id. Outside
	 * rendering, the template being loaded is the one the servlet chose. This
	 * relies on resource.loader.roller.cache=false in velocity.properties, so
	 * every load comes through here rather than from Velocity's cache.
	 */
	private static boolean inRenderScope(WeblogTemplate page) {
		RenderSession session = RenderSession.current();
		if (session == null) {
			return true;
		}
		String weblogId = session.getWeblogId();
		boolean allowed = weblogId != null && page.getWeblog() != null
				&& weblogId.equals(page.getWeblog().getId());
		if (!allowed) {
			logger.debug("Refusing template " + page.getId()
					+ " outside the weblog being rendered");
		}
		return allowed;
	}

	/**
	 * Files loaded by this resource loader are not reloadable here, as they are
	 * stored in custom themes and there is no way velocity can trigger a
	 * reload.
	 * 
	 * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#isSourceModified(org.apache.velocity.runtime.resource.Resource)
	 */
    @Override
	public boolean isSourceModified(Resource resource) {
		return false;
	}

	/**
	 * Defaults to return 0.
	 * 
	 * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#getLastModified(org.apache.velocity.runtime.resource.Resource)
	 */
    @Override
	public long getLastModified(Resource resource) {
		return 0;
	}

}
