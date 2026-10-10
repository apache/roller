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

package org.apache.roller.weblogger.pojos.wrapper;

import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class WeblogCategoryWrapperTest {

    @Test
    void imageIsGivenToTemplatesOnlyAsAnHttpUrl() {
        assertEquals("https://example.com/a.png", image("https://example.com/a.png"));
        assertEquals("http://example.com/a.png", image("http://example.com/a.png"));
        assertNull(image("javascript:alert(1)"));
        assertNull(image("https://example.com/a\" b"));
        assertNull(image("/images/a.png"));
        assertNull(image(null));
    }

    private static String image(String stored) {
        WeblogCategory category = new WeblogCategory();
        category.setImage(stored);
        return WeblogCategoryWrapper.wrap(category, mock(URLStrategy.class)).getImage();
    }
}
