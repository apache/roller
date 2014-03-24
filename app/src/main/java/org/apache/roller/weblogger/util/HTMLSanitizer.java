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

/**
Copyright (c) 2009 Open Lab, http://www.open-lab.com/
Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package org.apache.roller.weblogger.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.validator.UrlValidator;
import org.apache.roller.weblogger.config.WebloggerConfig;

public class HTMLSanitizer {
	public static Boolean xssEnabled = WebloggerConfig.getBooleanProperty("weblogAdminsUntrusted", Boolean.FALSE);	

	public static Pattern forbiddenTags = Pattern.compile("^(script|object|embed|link|style|form|input)$");
	public static Pattern allowedTags = Pattern.compile("^(b|p|i|s|a|img|table|thead|tbody|tfoot|tr|th|td|dd|dl|dt|em|h1|h2|h3|h4|h5|h6|li|ul|ol|span|div|strike|strong|"
			+ "sub|sup|pre|del|code|blockquote|strike|kbd|br|hr|area|map|object|embed|param|link|form|small|big)$");
    // <!--.........>
    private static Pattern commentPattern = Pattern.compile("<!--.*");
    // <tag ....props.....>
    private static Pattern tagStartPattern = Pattern.compile("<(?i)(\\w+\\b)\\s*(.*)/?>$");
    // </tag .........>
    private static Pattern tagClosePattern = Pattern.compile("</(?i)(\\w+\\b)\\s*>$");
	private static Pattern standAloneTags = Pattern.compile("^(img|br|hr)$");
	private static Pattern selfClosed = Pattern.compile("<.+/>");
    // prop="...."
    private static Pattern attributesPattern = Pattern.compile("(\\w*)\\s*=\\s*\"([^\"]*)\"");
    // color:red;
    private static Pattern stylePattern = Pattern.compile("([^\\s^:]+)\\s*:\\s*([^;]+);?");
    // url('....')"
    private static Pattern urlStylePattern = Pattern.compile("(?i).*\\b\\s*url\\s*\\(['\"]([^)]*)['\"]\\)");
    // expression(....)"   thanks to Ben Summer
    private static Pattern forbiddenStylePattern = Pattern.compile("(?:(expression|eval|javascript))\\s*\\(");

	/**
	 *  This method should be used to test input.
	 *
	 * @param html
	 * @return true if the input is "valid"
	 */
	public static boolean isSanitized(String html) {
		return sanitizer(html).isValid;
	}

	/**
	 * Used to clean every html before to output it in any html page
	 *
	 * @param html
	 * @return sanitized html
	 */
	public static String sanitize(String html) {
		return sanitizer(html).html;
	}

	public static String conditionallySanitize(String ret) {
		// if XSS is enabled then sanitize HTML
		if (xssEnabled && ret != null) {
			ret = HTMLSanitizer.sanitize(ret);
		}
		return ret;
	}

	/**
	 * Used to get the text,  tags removed or encoded
	 *
	 * @param html
	 * @return sanitized text
	 */
	public static String getText(String html) {
		return sanitizer(html).text;
	}

	/**
	 * This is the main method of sanitizing. It will be used both for validation and cleaning
	 *
	 * @param html
	 * @return a SanitizeResult object
	 */
	public static SanitizeResult sanitizer(String html) {
		return sanitizer(html, allowedTags, forbiddenTags);
	}

	public static SanitizeResult sanitizer(String html, Pattern allowedTags, Pattern forbiddenTags) {
		SanitizeResult ret = new SanitizeResult();
		Stack<String> openTags = new Stack<String>();


		List<String> tokens = tokenize(html);

		// -------------------   LOOP for every token --------------------------
		for (String token : tokens) {
			boolean isAcceptedToken = false;

			Matcher startMatcher = tagStartPattern.matcher(token);
			Matcher endMatcher = tagClosePattern.matcher(token);


			//--------------------------------------------------------------------------------  COMMENT    <!-- ......... -->
			if (commentPattern.matcher(token).find()) {
				ret.val = ret.val + token + (token.endsWith("-->") ? "" : "-->");
				ret.invalidTags.add(token + (token.endsWith("-->") ? "" : "-->"));
				continue;


				//--------------------------------------------------------------------------------  OPEN TAG    <tag .........>
			} else if (startMatcher.find()) {

				//tag name extraction
				String tag = startMatcher.group(1).toLowerCase();


				//-----------------------------------------------------  FORBIDDEN TAG   <script .........>
				if (forbiddenTags.matcher(tag).find()) {
					ret.invalidTags.add("<" + tag + ">");
					continue;


					// --------------------------------------------------  WELL KNOWN TAG
				} else if (allowedTags.matcher(tag).find()) {


					String cleanToken = "<" + tag;
					String tokenBody = startMatcher.group(2);


					//first test table consistency
					//table tbody tfoot thead th tr td
					if ("thead".equals(tag) || "tbody".equals(tag) || "tfoot".equals(tag) || "tr".equals(tag)) {
						if (openTags.search("table") < 1) {
							ret.invalidTags.add("<" + tag + ">");
							continue;
						}
					} else if ("td".equals(tag) || "th".equals(tag)) {
						if (openTags.search("tr") < 1) {
							ret.invalidTags.add("<" + tag + ">");
							continue;
						}
					}


					// then test properties
					Matcher attributes = attributesPattern.matcher(tokenBody);

					boolean foundURL = false; // URL flag
					while (attributes.find()) {

						String attr = attributes.group(1).toLowerCase();
						String val = attributes.group(2);

						// we will accept href in case of <A>
						if ("a".equals(tag) && "href".equals(attr)) {    // <a href="......">
							String[] customSchemes = {"http", "https"};
							if (new UrlValidator(customSchemes).isValid(val)) {
								foundURL = true;
							} else {
								// may be it is a mailto?
								//  case <a href="mailto:pippo@pippo.com?subject=...."
								if (val.toLowerCase().startsWith("mailto:") && val.indexOf('@') >= 0) {
									String val1 = "http://www." + val.substring(val.indexOf('@') + 1);
									if (new UrlValidator(customSchemes).isValid(val1)) {
										foundURL = true;
									} else {
										ret.invalidTags.add(attr + " " + val);
										val = "";
									}
								} else {
									ret.invalidTags.add(attr + " " + val);
									val = "";
								}
							}

						} else if (tag.matches("img|embed") && "src".equals(attr)) { // <img src="......">
							String[] customSchemes = {"http", "https"};
							if (new UrlValidator(customSchemes).isValid(val)) {
								foundURL = true;
							} else {
								ret.invalidTags.add(attr + " " + val);
								val = "";
							}

						} else if ("href".equals(attr) || "src".equals(attr)) { // <tag src/href="......">   skipped
							ret.invalidTags.add(tag + " " + attr + " " + val);
							continue;


						} else if (attr.matches("width|height")) { // <tag width/height="......">
							if (!val.toLowerCase().matches("\\d+%|\\d+$")) { // test numeric values
								ret.invalidTags.add(tag + " " + attr + " " + val);
								continue;
							}

						} else if ("style".equals(attr)) { // <tag style="......">


							// then test properties
							Matcher styles = stylePattern.matcher(val);
							String cleanStyle = "";

							while (styles.find()) {
								String styleName = styles.group(1).toLowerCase();
								String styleValue = styles.group(2);

								// suppress invalid styles values
								if (forbiddenStylePattern.matcher(styleValue).find()) {
									ret.invalidTags.add(tag + " " + attr + " " + styleValue);
									continue;
								}

								// check if valid url
								Matcher urlStyleMatcher = urlStylePattern.matcher(styleValue);
								if (urlStyleMatcher.find()) {
									String[] customSchemes = {"http", "https"};
									String url = urlStyleMatcher.group(1);
									if (!new UrlValidator(customSchemes).isValid(url)) {
										ret.invalidTags.add(tag + " " + attr + " " + styleValue);
										continue;
									}
								}

								cleanStyle = cleanStyle + styleName + ":" + encode(styleValue) + ";";

							}
							val = cleanStyle;

						} else if (attr.startsWith("on")) {  // skip all javascript events
							ret.invalidTags.add(tag + " " + attr + " " + val);
							continue;

						} else {  // by default encode all properies
							val = encode(val);
						}

						cleanToken = cleanToken + " " + attr + "=\"" + val + "\"";
					}
					cleanToken = cleanToken + ">";

					isAcceptedToken = true;

					// for <img> and <a>
					if (tag.matches("a|img|embed") && !foundURL) {
						isAcceptedToken = false;
						cleanToken = "";
					}

					token = cleanToken;


					// push the tag if require closure and it is accepted (otherwirse is encoded)
					if (isAcceptedToken && !(standAloneTags.matcher(tag).find() || selfClosed.matcher(tag).find())) {
						openTags.push(tag);
					}

					// --------------------------------------------------------------------------------  UNKNOWN TAG
				} else {
					ret.invalidTags.add(token);
					ret.val = ret.val + token;
					continue;


				}

				// --------------------------------------------------------------------------------  CLOSE TAG </tag>
			} else if (endMatcher.find()) {
				String tag = endMatcher.group(1).toLowerCase();

				//is self closing
				if (selfClosed.matcher(tag).find()) {
					ret.invalidTags.add(token);
					continue;
				}
				if (forbiddenTags.matcher(tag).find()) {
					ret.invalidTags.add("/" + tag);
					continue;
				}
				if (!allowedTags.matcher(tag).find()) {
					ret.invalidTags.add(token);
					ret.val = ret.val + token;
					continue;
				} else {


					String cleanToken = "";

					// check tag position in the stack
					int pos = openTags.search(tag);
					// if found on top ok
					for (int i = 1; i <= pos; i++) {
						//pop all elements before tag and close it
						String poppedTag = openTags.pop();
						cleanToken = cleanToken + "</" + poppedTag + ">";
						isAcceptedToken = true;
					}

					token = cleanToken;
				}

			}

			ret.val = ret.val + token;

			if (isAcceptedToken) {
				ret.html = ret.html + token;
				//ret.text = ret.text + " ";
			} else {
				String sanToken = htmlEncodeApexesAndTags(token);
				ret.html = ret.html + sanToken;
				ret.text = ret.text + htmlEncodeApexesAndTags(removeLineFeed(token));
			}


		}

		// must close remaining tags
		while (openTags.size() > 0) {
			//pop all elements before tag and close it
			String poppedTag = openTags.pop();
			ret.html = ret.html + "</" + poppedTag + ">";
			ret.val = ret.val + "</" + poppedTag + ">";
		}

		//set boolean value
		ret.isValid = ret.invalidTags.size() == 0;

		return ret;
	}

	/**
	 * Splits html tag and tag content <......>.
	 *
	 * @param html
	 * @return a list of token
	 */
	private static List<String> tokenize(String html) {
		ArrayList tokens = new ArrayList();
		int pos = 0;
		String token = "";
		int len = html.length();
		while (pos < len) {
			char c = html.charAt(pos);

			String ahead = html.substring(pos, pos > len - 4 ? len : pos + 4);

			//a comment is starting
			if ("<!--".equals(ahead)) {
				//store the current token
				if (token.length() > 0) {
					tokens.add(token);
				}

				//clear the token
				token = "";

				// serch the end of <......>
				int end = moveToMarkerEnd(pos, "-->", html);
				tokens.add(html.substring(pos, end));
				pos = end;


				// a new "<" token is starting
			} else if ('<' == c) {

				//store the current token
				if (token.length() > 0) {
					tokens.add(token);
				}

				//clear the token
				token = "";

				// serch the end of <......>
				int end = moveToMarkerEnd(pos, ">", html);
				tokens.add(html.substring(pos, end));
				pos = end;

			} else {
				token = token + c;
				pos++;
			}

		}

		//store the last token
		if (token.length() > 0) {
			tokens.add(token);
		}

		return tokens;
	}

	private static int moveToMarkerEnd(int pos, String marker, String s) {
		int i = s.indexOf(marker, pos);
		if (i > -1) {
			pos = i + marker.length();
		} else {
			pos = s.length();
		}
		return pos;
	}

	/**
	 * Contains the sanitizing results.
	 * html is the sanitized html encoded  ready to be printed. Unaccepted tag are encode, text inside tag is always encoded    MUST BE USED WHEN PRINTING HTML
	 * text is the text inside valid tags. Contains invalid tags encoded                                                        SHOULD BE USED TO PRINT EXCERPTS
	 * val  is the html source cleaned from unaccepted tags. It is not encoded:                                                 SHOULD BE USED IN SAVE ACTIONS
	 * isValid is true when every tag is accepted without forcing encoding
	 * invalidTags is the list of encoded-killed tags
	 */
	static class SanitizeResult {

		public String html = "";
		public String text = "";
		public String val = "";
		public boolean isValid = true;
		public List<String> invalidTags = new ArrayList();
	}

	public static String encode(String s) {
		return convertLineFeedToBR(htmlEncodeApexesAndTags(s == null ? "" : s));
	}

	public static final String htmlEncodeApexesAndTags(String source) {
		return htmlEncodeTag(htmlEncodeApexes(source));
	}

	public static final String htmlEncodeApexes(String source) {
		if (source != null) {
			String result = replaceAllNoRegex(source, new String[]{"\"", "'"}, new String[]{"&quot;", "&#39;"});
			return result;
		} else {
			return null;
		}
	}

	public static final String htmlEncodeTag(String source) {
		if (source != null) {
			String result = replaceAllNoRegex(source, new String[]{"<", ">"}, new String[]{"&lt;", "&gt;"});
			return result;
		} else {
			return null;
		}
	}

	public static String convertLineFeedToBR(String text) {
		if (text != null) {
			return replaceAllNoRegex(text, new String[]{"\n", "\f", "\r"}, new String[]{"<br>", "<br>", " "});
		} else {
			return null;
		}
	}

	public static String removeLineFeed(String text) {

		if (text != null) {
			return replaceAllNoRegex(text, new String[]{"\n", "\f", "\r"}, new String[]{" ", " ", " "});
		} else {
			return null;
		}
	}

	public static final String replaceAllNoRegex(String source, String searches[], String replaces[]) {
		int k;
		String tmp = source;
		for (k = 0; k < searches.length; k++) {
			tmp = replaceAllNoRegex(tmp, searches[k], replaces[k]);
		}
		return tmp;
	}

	public static final String replaceAllNoRegex(String source, String search, String replace) {
		StringBuilder buffer = new StringBuilder();
		if (source != null) {
			if (search.length() == 0) {
				return source;
			}
			int oldPos, pos;
			for (oldPos = 0, pos = source.indexOf(search, oldPos); pos != -1; oldPos = pos + search.length(), pos = source.indexOf(search, oldPos)) {
				buffer.append(source.substring(oldPos, pos));
				buffer.append(replace);
			}
			if (oldPos < source.length()) {
				buffer.append(source.substring(oldPos));
			}
		}
		return new String(buffer);
	}
}
