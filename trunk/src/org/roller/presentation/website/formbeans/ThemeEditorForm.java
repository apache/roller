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
