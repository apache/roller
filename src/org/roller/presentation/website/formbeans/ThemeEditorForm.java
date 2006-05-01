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
package org.roller.presentation.website.formbeans;

import org.apache.struts.action.ActionForm;

/**
 * Holds the name of the theme chosen or
 * the template being edited.
 * 
 * @struts.form name="themeEditorForm"
 * 
 * @author llavandowska
 */
public class ThemeEditorForm extends ActionForm
{
	private String themeName;
	private String themeTemplate;
	/**
	 * Returns the themeName.
	 * @return String
	 */
	public String getThemeName()
	{
		return themeName;
	}

	/**
	 * Returns the themeTemplate.
	 * @return String
	 */
	public String getThemeTemplate()
	{
		return themeTemplate;
	}

	/**
	 * Sets the themeName.
	 * @param themeName The themeName to set
	 */
	public void setThemeName(String themeName)
	{
		this.themeName = themeName;
	}

	/**
	 * Sets the themeTemplate.
	 * @param themeTemplate The themeTemplate to set
	 */
	public void setThemeTemplate(String themeTemplate)
	{
		this.themeTemplate = themeTemplate;
	}

}
