/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.weblogger.ui.struts2.editor;

import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.struts2.config.ConfigurationManager;
import org.apache.struts2.config.providers.StrutsDefaultConfigurationProvider;
import org.apache.struts2.inject.Container;
import org.apache.struts2.util.ValueStack;
import org.apache.struts2.util.ValueStackFactory;
import org.apache.struts2.util.reflection.ReflectionContextState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * No editor action lets a request parameter create the domain
 * object it works on. Those objects are loaded only by the action, from the
 * weblog, by id.
 */
class EditorActionTargetBindingTest {

    private ValueStack stack;

    @BeforeEach
    void setUp() {
        ConfigurationManager configurationManager = new ConfigurationManager("xwork");
        configurationManager.addContainerProvider(new StrutsDefaultConfigurationProvider());
        Container container = configurationManager.getConfiguration().getContainer();
        stack = container.getInstance(ValueStackFactory.class).createValueStack();
    }

    private void tryBind(Object action, String name, String value) {
        stack.push(action);
        ReflectionContextState.setCreatingNullObjects(stack.getContext(), true);
        ReflectionContextState.setDenyMethodExecution(stack.getContext(), true);
        try {
            stack.setParameter(name, value);
        } catch (RuntimeException expectedForUnwritableProperty) {
            // A non-bindable property may raise; that is the point.
        } finally {
            ReflectionContextState.setCreatingNullObjects(stack.getContext(), false);
            stack.pop();
        }
    }

    private static class Case {
        final String label;
        final Supplier<Object> make;
        final String param;
        final Function<Object, Object> read;
        Case(String label, Supplier<Object> make, String param, Function<Object, Object> read) {
            this.label = label; this.make = make; this.param = param; this.read = read;
        }
    }

    private List<Case> cases() {
        return Arrays.asList(
            new Case("CategoryRemove.category", CategoryRemove::new, "category.name",
                a -> ((CategoryRemove) a).getCategory()),
            new Case("EntryRemove.removeEntry", EntryRemove::new, "removeEntry.anchor",
                a -> ((EntryRemove) a).getRemoveEntry()),
            new Case("EntryEdit.entry", EntryEdit::new, "entry.anchor",
                a -> ((EntryEdit) a).getEntry()),
            new Case("MediaFileEdit.directory", MediaFileEdit::new, "directory.name",
                a -> ((MediaFileEdit) a).getDirectory()),
            new Case("MediaFileAdd.directory", MediaFileAdd::new, "directory.name",
                a -> ((MediaFileAdd) a).getDirectory()),
            new Case("MediaFileView.currentDirectory", MediaFileView::new, "currentDirectory.name",
                a -> ((MediaFileView) a).getCurrentDirectory()),
            new Case("MediaFileImageChooser.currentDirectory", MediaFileImageChooser::new, "currentDirectory.name",
                a -> ((MediaFileImageChooser) a).getCurrentDirectory()),
            new Case("StylesheetEdit.template", StylesheetEdit::new, "template.name",
                a -> ((StylesheetEdit) a).getTemplate()),
            new Case("TemplateEdit.template", TemplateEdit::new, "template.name",
                a -> ((TemplateEdit) a).getTemplate()),
            new Case("Bookmarks.folder", Bookmarks::new, "folder.name",
                a -> ((Bookmarks) a).getFolder())
        );
    }

    @TestFactory
    Iterable<DynamicTest> requestCannotCreateTheTargetObject() {
        return cases().stream().map(c -> DynamicTest.dynamicTest(c.label, (Executable) () -> {
            Object action = c.make.get();
            tryBind(action, c.param, "http://attacker.example/");
            assertNull(c.read.apply(action), c.label + " must stay null after a bind attempt");
        })).collect(java.util.stream.Collectors.toList());
    }
}
