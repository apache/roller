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

package org.apache.roller.weblogger.business;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;

/**
 * A custom theme serves the media files it keeps in the media library at their
 * original path, and serves its scripts and stylesheets inline so that the
 * weblog's pages can load them. Changing one of those changes what every
 * visitor's browser runs, so only the weblog's Admins may replace, rename, move
 * or delete it, or change any file's original path.
 */
public final class ThemeMediaFiles {

    private static final String[] THEME_ASSET_EXTENSIONS = {".css", ".js", ".mjs"};

    private ThemeMediaFiles() {
    }

    /** Whether the file is a script or stylesheet a custom theme serves. */
    public static boolean isThemeFile(MediaFile file) {
        return file != null && isThemeAssetPath(file.getOriginalPath());
    }

    /** Whether a custom theme would serve the path as a script or stylesheet. */
    public static boolean isThemeAssetPath(String path) {
        if (StringUtils.isBlank(path)) {
            return false;
        }
        String lower = path.trim().toLowerCase(Locale.ENGLISH);
        for (String extension : THEME_ASSET_EXTENSIONS) {
            if (lower.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    /** Whether the user is one of the weblog's Admins. */
    public static boolean isWeblogAdmin(Weblog weblog, User user) {
        return weblog != null && user != null
                && weblog.hasUserPermission(user, WeblogPermission.ADMIN);
    }

    /** Whether the user may replace, rename, move or delete the file. */
    public static boolean mayChange(Weblog weblog, User user, MediaFile file) {
        return !isThemeFile(file) || isWeblogAdmin(weblog, user);
    }

    /** Whether the user may change a file's original path from one value to another. */
    public static boolean mayChangeOriginalPath(Weblog weblog, User user,
                                                String currentPath, String newPath) {
        return StringUtils.equals(StringUtils.trimToNull(currentPath),
                StringUtils.trimToNull(newPath))
                || isWeblogAdmin(weblog, user);
    }
}
