/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  The ASF licenses this file to You
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
package org.apache.roller.weblogger.webservices.atomprotocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.validate.rng.CompactSchemaReader;

/** Validates XML against a RELAX NG Compact schema with Jing. */
final class RelaxNgValidator {

    private RelaxNgValidator() {
    }

    /**
     * Validates {@code xml} against the schema in {@code schema}; returns the
     * errors, empty when the document is valid.
     */
    static List<String> validate(InputStream schema, String schemaName, byte[] xml)
            throws IOException, SAXException {
        List<String> errors = new ArrayList<>();
        ErrorHandler handler = new ErrorHandler() {
            @Override public void warning(SAXParseException e) { /* ignore warnings */ }
            @Override public void error(SAXParseException e) { errors.add(e.getMessage()); }
            @Override public void fatalError(SAXParseException e) { errors.add(e.getMessage()); }
        };
        PropertyMapBuilder props = new PropertyMapBuilder();
        props.put(ValidateProperty.ERROR_HANDLER, handler);
        ValidationDriver driver =
                new ValidationDriver(props.toPropertyMap(), CompactSchemaReader.getInstance());
        if (!driver.loadSchema(new InputSource(schema))) {
            throw new IllegalStateException("schema " + schemaName + " failed to compile: " + errors);
        }
        driver.validate(new InputSource(new ByteArrayInputStream(xml)));
        return errors;
    }

    /** Validates against a classpath schema. */
    static List<String> validateResource(String schemaResource, byte[] xml)
            throws IOException, SAXException {
        try (InputStream schema = RelaxNgValidator.class.getResourceAsStream(schemaResource)) {
            return validate(schema, schemaResource, xml);
        }
    }
}
