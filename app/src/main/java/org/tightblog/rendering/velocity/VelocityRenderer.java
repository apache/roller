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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.tightblog.rendering.velocity;

import java.io.Writer;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.tightblog.util.WebloggerException;
import org.tightblog.pojos.Template;
import org.tightblog.rendering.Renderer;
import org.tightblog.rendering.model.UtilitiesModel;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.VelocityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Renderer that renders using the Velocity template engine.
 */
public class VelocityRenderer implements Renderer {

    private static Logger log = LoggerFactory.getLogger(VelocityRenderer.class);

    public VelocityRenderer() {}

    @Override
    public void render(Template template, Map<String, Object> model, Writer out) throws WebloggerException {

        if (!Template.Parser.VELOCITY.equals(template.getParser())) {
            throw new IllegalArgumentException("Template " + template.getName()
                    + " uses unsupported parser " + template.getParser());
        }

        org.apache.velocity.Template velocityTemplate;

        try {
            // make sure that we can locate the template
            // if we can't then this will throw an exception
            velocityTemplate = VelocityEngineWrapper.getTemplate(template.getId());

        } catch (ResourceNotFoundException ex) {
            // velocity couldn't find the resource so lets log a warning
            log.warn("Error creating renderer for {} due to [{}]", template.getId(), ex.getMessage());

            // then just rethrow so that the caller knows this instantiation
            // failed
            throw ex;

        } catch (VelocityException ex) {
            // in the case of a parsing error we want to render an
            // error page instead so the user knows what was wrong
            renderException(ex, template, model, out, "templates/error-page.vm");
            return;
        }

        try {
            long startTime = System.currentTimeMillis();

            // convert model to Velocity Context
            Context ctx = new VelocityContext(model);
            velocityTemplate.merge(ctx, out);

            long endTime = System.currentTimeMillis();
            long renderTime = (endTime - startTime) / DateUtils.MILLIS_PER_SECOND;

            log.debug("Rendered [{}] in {} secs", template.getId(), renderTime);

        } catch (VelocityException ex) {
            // in the case of a parsing error including a macro we want to
            // render an error page instead so the user knows what was wrong
            renderException(ex, template, model, out, "templates/error-parse.vm");
        } catch (Exception ex) {
            // wrap and rethrow so caller can deal with it
            throw new WebloggerException("Error during rendering", ex);
        }
    }

    private void renderException(VelocityException ex, Template template, Map<String, Object> model, Writer out, String errorTemplate)
            throws WebloggerException {
        try {
            org.apache.velocity.Template velocityTemplate = VelocityEngineWrapper.getTemplate(errorTemplate);

            Context ctx = new VelocityContext(model);
            ctx.put("exception", ex);
            ctx.put("exceptionSource", template.getId());
            ctx.put("utils", new UtilitiesModel());

            // render output to Writer
            velocityTemplate.merge(ctx, out);
        } catch (Exception e) {
            // wrap and rethrow so caller can deal with it
            throw new WebloggerException("Error during rendering", e);
        }
    }
}
