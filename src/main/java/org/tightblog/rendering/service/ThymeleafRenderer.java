/*
   Copyright 2017 Glen Mazza

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.tightblog.rendering.service;

import org.attoparser.ParseException;
import org.springframework.expression.spel.SpelEvaluationException;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.exceptions.TemplateInputException;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.tightblog.domain.Template;
import org.tightblog.rendering.cache.CachedContent;
import org.tightblog.util.Utilities;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;

public class ThymeleafRenderer {

    private SpringTemplateEngine templateEngine;

    public void setTemplateEngine(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public CachedContent render(Template template, Map<String, Object> model) throws IOException {
        CachedContent rendererOutput = new CachedContent(template.getRole());

        try (ByteArrayOutputStream outStream = new ByteArrayOutputStream(Utilities.TWENTYFOUR_KB_IN_BYTES);
             Writer writer = new PrintWriter(new OutputStreamWriter(outStream, "UTF-8"))) {
            try {
                Context ctx = new Context();
                ctx.setVariables(model);
                runTemplateEngine(template.getId(), ctx, writer);
            } catch (TemplateInputException e) {
                // Provide end-user friendly error messages for at least two common errors:
                // unknown method call and unknown property name
                Throwable firstCause = e.getCause();
                if (firstCause instanceof ParseException) {
                    rendererOutput.setRole(Template.Role.CUSTOM_EXTERNAL);
                    Context ctx = new Context();
                    ctx.setVariable("templateName", template.getName());
                    // output commonly "Exception evaluating Spring EL expression..."
                    ctx.setVariable("firstMessage", firstCause.getMessage());
                    Throwable secondCause = firstCause.getCause();
                    if (secondCause instanceof TemplateProcessingException) {
                        Throwable thirdCause = secondCause.getCause();
                        if (thirdCause instanceof SpelEvaluationException || thirdCause instanceof FileNotFoundException) {
                            // output commonly "unknown [method|property] XXX..."
                            ctx.setVariable("secondMessage", thirdCause.getClass().getName() + ": " +
                                    thirdCause.getMessage());
                        }
                    }
                    runTemplateEngine("error-page", ctx, writer);
                } else {
                    throw e;
                }
            }
            writer.flush();
            rendererOutput.setContent(outStream.toByteArray());
        }
        return rendererOutput;
    }

    /*
     * templateEngine.process(...) is a final method and hence can't normally be mocked by testing frameworks
     * factoring out here to allow for spying/mocking.
     */
    void runTemplateEngine(String template, IContext context, Writer writer) {
        templateEngine.process(template, context, writer);
    }
}
