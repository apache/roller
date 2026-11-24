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

import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;

/**
 * A media file with an original path is what a custom theme serves at that
 * path, such as the theme's scripts and stylesheets. Changing one changes the
 * weblog's design, so only the weblog's Admins may replace, move or delete it.
 */
public final class ThemeMediaFiles {

    private ThemeMediaFiles() {
    }

    /** Whether the file is served as one of the weblog's theme resources. */
    public static boolean isThemeFile(MediaFile file) {
        return file != null && StringUtils.isNotBlank(file.getOriginalPath());
    }

    /** Whether the user may replace, move or delete the file. */
    public static boolean mayChange(Weblog weblog, User user, MediaFile file) {
        return !isThemeFile(file)
                || (user != null && weblog.hasUserPermission(user, WeblogPermission.ADMIN));
    }
}
