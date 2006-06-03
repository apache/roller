/*
 * Copyright (c) 2005
 * Anil R. Gangolli. All rights reserved.
 *
 * Distributed with the Roller Weblogger Project under the terms of the Roller Software
 * License
 */

package org.roller.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Unit test for org.roller.util.PropertyExpander.
 */
public class PropertyExpanderTest extends TestCase
{
    private static final Map props = new HashMap();

    static
    {
        props.put("defined.property.one", "value one");
        props.put("defined.property.two", "value two");
        props.put("defined.property.with.dollar.sign.in.value", "$2");
    }

    public void testExpansion() throws Exception
    {
        String expanded =
            PropertyExpander.expandProperties("String with ${defined.property.one} and ${defined.property.two} and ${defined.property.with.dollar.sign.in.value} and ${undefined.property} and some stuff.", props);

        assertEquals("Expanded string doesn't match expected",
            "String with value one and value two and $2 and ${undefined.property} and some stuff.",
            expanded);
    }

    public void testSystemProperty() throws Exception
    {
        String expanded =
            PropertyExpander.expandSystemProperties("${java.home}");
        assertEquals("Expanded string doesn't match expected",
            System.getProperty("java.home"),
            expanded);
    }

}
