/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.ui.struts2.editor;

import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

class EntryEditEnclosureTest {

    @Test
    void unchangedInvalidLegacyMetadataIsClearedWithAWarning() throws Exception {
        EntryEdit action = action();
        action.setActionName("entryEdit");
        WeblogEntry entry = new WeblogEntry();
        entry.putEntryAttribute("att_mediacast_url", "ftp://legacy/audio");
        entry.putEntryAttribute("att_mediacast_type", "broken");
        entry.putEntryAttribute("att_mediacast_length", "unknown");
        action.setEntry(entry);
        setEnclosure(action.getBean(), "ftp://legacy/audio", "broken", "unknown");

        assertNull(action.validateEnclosure());
        assertFalse(action.hasActionErrors());
        assertTrue(action.hasActionMessages());
        assertTrue(action.getActionMessages().contains(
                "weblogEdit.enclosureMetadataRemoved"));
        assertNull(action.getBean().getEnclosureURL());
        assertNull(action.getBean().getEnclosureType());
        assertNull(action.getBean().getEnclosureLength());
    }

    @Test
    void newlySubmittedInvalidMetadataGetsAFieldError() {
        EntryEdit action = action();
        action.setActionName("entryEdit");
        action.setEntry(new WeblogEntry());
        setEnclosure(action.getBean(), "https://example.org/audio", "broken", "12");

        assertNull(action.validateEnclosure());
        assertTrue(action.hasActionErrors());
        assertTrue(action.getActionErrors().contains(
                "weblogEdit.enclosureTypeInvalid"));
        assertFalse(action.hasActionMessages());
        assertEquals("broken", action.getBean().getEnclosureType());
    }

    @Test
    void failedNewEntryValidationRestoresTheUnsavedStatus() {
        EntryEdit action = action();
        action.setActionName("entryAdd");
        action.getBean().setStatus(WeblogEntry.PubStatus.PUBLISHED.name());
        setEnclosure(action.getBean(), "file:///tmp/audio", "audio/ogg", "12");

        assertEquals(EntryEdit.INPUT, action.save());
        assertTrue(action.hasActionErrors());
        assertNull(action.getBean().getStatus());
    }

    private void setEnclosure(EntryBean bean, String url, String type, String length) {
        bean.setEnclosureURL(url);
        bean.setEnclosureType(type);
        bean.setEnclosureLength(length);
    }

    private EntryEdit action() {
        EntryEdit action = spy(new EntryEdit());
        doAnswer(invocation -> invocation.getArgument(0))
                .when(action).getText(anyString());
        return action;
    }
}
