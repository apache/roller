/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tightblog.rendering.thymeleaf;

import org.attoparser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelMessage;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.exceptions.TemplateInputException;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.tightblog.pojos.Template;
import org.tightblog.pojos.WeblogTemplate;
import org.tightblog.rendering.cache.CachedContent;
import org.tightblog.util.WebloggerException;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;

public class ThymeleafRendererTest {

    private ThymeleafRenderer renderer;
    private WeblogTemplate template;
    private Map<String, Object> model = new HashMap<>();

    @Before
    public void initialize() {
        model.put("foo", 123);
        model.put("bar", "banana");
        renderer = new ThymeleafRenderer();
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolvers(new HashSet<>());
        renderer.setTemplateEngine(templateEngine);
        renderer = Mockito.spy(renderer);
        template = new WeblogTemplate();
        template.setName("test template");
        template.setId("mytestid");
        template.setRole(Template.ComponentType.WEBLOG);
    }

    @Test
    public void testSuccessfulRendering() throws IOException, WebloggerException {
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        doNothing().when(renderer).runTemplateEngine(eq("mytestid"), contextCaptor.capture(), any());

        CachedContent content = renderer.render(template, model);
        assertNotNull(content);
        assertEquals(Template.ComponentType.WEBLOG, content.getComponentType());

        Context context = contextCaptor.getValue();
        assertEquals(123, context.getVariable("foo"));
        assertEquals("banana", context.getVariable("bar"));
    }

    @Test
    public void testContextVariablesForErrorPage() throws IOException, WebloggerException {
        SpelEvaluationException see = new SpelEvaluationException(SpelMessage.CANNOT_INDEX_INTO_NULL_VALUE);
        TemplateProcessingException tpe = new TemplateProcessingException("TPE Message", see);
        ParseException pe = new ParseException("ParseException Message", tpe);
        TemplateInputException tie = new TemplateInputException("TIE Message", pe);
        doThrow(tie).when(renderer).runTemplateEngine(eq("mytestid"), any(IContext.class), any());

        // set up captor on the processing of the error template
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);

        doNothing().when(renderer).runTemplateEngine(eq("error-page"), contextCaptor.capture(), any());
        CachedContent content = renderer.render(template, model);
        assertEquals(Template.ComponentType.CUSTOM_EXTERNAL, content.getComponentType());
        Context context = contextCaptor.getValue();
        assertEquals("test template", context.getVariable("templateName"));
        assertEquals("ParseException Message", context.getVariable("firstMessage"));
        assertEquals("org.springframework.expression.spel.SpelEvaluationException: EL1012E: Cannot index into a null value",
                context.getVariable("secondMessage"));
    }
}