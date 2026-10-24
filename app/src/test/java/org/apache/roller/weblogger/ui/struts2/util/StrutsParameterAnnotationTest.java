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
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.ui.struts2.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.struts2.interceptor.parameter.StrutsParameterAuthorizer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Struts only binds request parameters to members annotated with
 * {@code @StrutsParameter}. Checks that every field submitted by the Struts
 * forms in the JSPs, every parameter of the Struts URLs they build, and every
 * parameter passed by a redirectAction result in struts.xml is authorized on
 * the action class it is sent to, using Struts' own authorization rules.
 */
public class StrutsParameterAnnotationTest {

    /**
     * Parameters that are submitted but intentionally not bound, keyed by
     * action class simple name and parameter name.
     */
    private static final Set<String> NOT_BOUND = Set.of(
            // set from the static <param> of the action mapping in struts.xml
            "PingTargetEdit.actionName",
            "FolderEdit.actionName",
            // no such property; the hidden fields are leftovers in the JSPs
            "Categories.categoryId",
            "WeblogConfig.defaultPlugins");

    /**
     * Forms whose target cannot be read from the JSP, keyed by JSP path and
     * form id. An empty list skips the form.
     */
    private static final Map<String, List<String>> FORM_TARGETS = Map.of(
            // posted by JavaScript to actionName + ".rol"
            "admin/PingTargets.jsp#pingTargetEditForm",
            List.of("commonPingTargetAdd", "commonPingTargetEdit"),
            // popup that is not reachable from any page
            "editor/MediaFileAddExternalInclude.jsp#entry", List.of());

    private static final Pattern STRUTS_TAG = Pattern.compile(
            "<(/?)s:(\\w+)((?:[^>\"']|\"[^\"]*\"|'[^']*')*)>");
    private static final Pattern SET_TAG = Pattern.compile(
            "<s:set\\s+var=\"(\\w+)\"\\s*>([^<]*)</s:set>");
    private static final Pattern VAR_REF = Pattern.compile("%\\{#(\\w+)}");
    private static final Set<String> FIELD_TAGS = Set.of(
            "textfield", "hidden", "password", "textarea", "checkbox",
            "checkboxlist", "select", "radio", "doubleselect", "combobox");

    private static Path jspDirectory;
    private static Map<String, Set<Class<?>>> actions;
    private static Document strutsXml;

    private final ClassAuthorizer authorizer = new ClassAuthorizer();
    private final Set<String> failures = new TreeSet<>();
    private int checked = 0;

    @BeforeAll
    public static void loadConfiguration() throws Exception {
        Path projectDirectory = Path.of(
                System.getProperty("project.build.directory")).getParent();
        jspDirectory = projectDirectory.resolve("src/main/webapp/WEB-INF/jsps");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(
                "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        try (InputStream in = StrutsParameterAnnotationTest.class.getResourceAsStream("/struts.xml")) {
            strutsXml = factory.newDocumentBuilder().parse(in);
        }

        actions = new HashMap<>();
        NodeList nodes = strutsXml.getElementsByTagName("action");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element action = (Element) nodes.item(i);
            if (!action.getAttribute("class").isEmpty()) {
                actions.computeIfAbsent(action.getAttribute("name"), k -> new LinkedHashSet<>())
                        .add(Class.forName(action.getAttribute("class"), false,
                                StrutsParameterAnnotationTest.class.getClassLoader()));
            }
        }
    }

    @Test
    public void testFormFieldsAreAuthorized() throws IOException {
        int forms = 0;
        for (Path jsp : jsps()) {
            String relative = jspDirectory.relativize(jsp).toString().replace('\\', '/');
            String content = Files.readString(jsp);
            Map<String, List<String>> vars = setVariables(content);

            Matcher m = STRUTS_TAG.matcher(content);
            String formKey = null;
            Set<String> targets = null;
            Set<String> fields = null;
            while (m.find()) {
                boolean closing = !m.group(1).isEmpty();
                String tag = m.group(2);
                String attrs = m.group(3);
                if ("form".equals(tag) && !closing) {
                    formKey = relative + "#" + attr(attrs, "id");
                    targets = new LinkedHashSet<>();
                    fields = new LinkedHashSet<>();
                    targets.addAll(resolve(attr(attrs, "action"), vars));
                } else if ("form".equals(tag)) {
                    if (fields != null && !fields.isEmpty()) {
                        forms++;
                        checkForm(formKey, targets, fields);
                    }
                    formKey = null;
                    fields = null;
                } else if (fields != null && !closing) {
                    if ("submit".equals(tag)) {
                        targets.addAll(resolve(attr(attrs, "action"), vars));
                    } else if (FIELD_TAGS.contains(tag)) {
                        String name = attr(attrs, "name");
                        if (name != null && !name.contains("%{")
                                && !name.startsWith("action:") && !name.startsWith("method:")) {
                            fields.add(name);
                        }
                    }
                }
            }
        }
        assertNoFailures();
        assertTrue(forms > 40, "Only " + forms + " forms were found in " + jspDirectory);
        assertTrue(checked > 250, "Only " + checked + " form fields were checked");
    }

    @Test
    public void testUrlParametersAreAuthorized() throws IOException {
        Pattern url = Pattern.compile(
                "<s:url\\b((?:[^>\"']|\"[^\"]*\"|'[^']*')*[^/])>(.*?)</s:url>", Pattern.DOTALL);
        Pattern param = Pattern.compile("<s:param\\s+name=\"([^\"]+)\"");
        for (Path jsp : jsps()) {
            String content = Files.readString(jsp);
            Map<String, List<String>> vars = setVariables(content);
            Matcher m = url.matcher(content);
            while (m.find()) {
                for (String action : resolve(attr(m.group(1), "action"), vars)) {
                    Matcher p = param.matcher(m.group(2));
                    while (p.find()) {
                        check(jsp.getFileName() + " url " + action, action, p.group(1));
                    }
                }
            }
        }
        assertNoFailures();
    }

    @Test
    public void testRedirectActionParametersAreAuthorized() {
        NodeList results = strutsXml.getElementsByTagName("result");
        for (int i = 0; i < results.getLength(); i++) {
            Element result = (Element) results.item(i);
            if (!"redirectAction".equals(result.getAttribute("type"))) {
                continue;
            }
            Map<String, String> params = new HashMap<>();
            NodeList children = result.getElementsByTagName("param");
            for (int j = 0; j < children.getLength(); j++) {
                Element param = (Element) children.item(j);
                params.put(param.getAttribute("name"), param.getTextContent().trim());
            }
            String action = stripMethod(params.getOrDefault("actionName",
                    result.getTextContent().trim()));
            if (!actions.containsKey(action)) {
                continue;
            }
            for (String name : params.keySet()) {
                if (!"actionName".equals(name) && !"namespace".equals(name)) {
                    check("struts.xml redirect to " + action, action, name);
                }
            }
        }
        assertNoFailures();
    }

    private void checkForm(String formKey, Set<String> targets, Set<String> fields) {
        List<String> explicit = FORM_TARGETS.get(formKey);
        if (explicit != null) {
            targets = new LinkedHashSet<>(explicit);
            if (explicit.isEmpty()) {
                return;
            }
        }
        if (targets.isEmpty()) {
            failures.add(formKey + ": cannot tell which action the form submits to");
            return;
        }
        for (String action : targets) {
            for (String field : fields) {
                check(formKey + " -> " + action, action, field);
                checked++;
            }
        }
    }

    private void check(String source, String action, String parameter) {
        Set<Class<?>> classes = actions.get(action);
        if (classes == null) {
            failures.add(source + ": no action named '" + action + "' in struts.xml");
            return;
        }
        for (Class<?> actionClass : classes) {
            if (!NOT_BOUND.contains(actionClass.getSimpleName() + "." + parameter)
                    && !authorizer.isAuthorized(parameter, actionClass, actionClass)) {
                failures.add(source + ": '" + parameter + "' is not a @StrutsParameter of "
                        + actionClass.getName());
            }
        }
    }

    private void assertNoFailures() {
        assertTrue(failures.isEmpty(), "Unauthorized request parameters:\n  "
                + String.join("\n  ", failures));
    }

    private static List<Path> jsps() throws IOException {
        try (Stream<Path> files = Files.walk(jspDirectory)) {
            return files.filter(f -> f.toString().endsWith(".jsp")).sorted().toList();
        }
    }

    /** Values of the {@code <s:set var="x">literal</s:set>} tags in a JSP. */
    private static Map<String, List<String>> setVariables(String content) {
        Map<String, List<String>> vars = new HashMap<>();
        Matcher m = SET_TAG.matcher(content);
        while (m.find()) {
            vars.computeIfAbsent(m.group(1), k -> new ArrayList<>()).add(m.group(2).trim());
        }
        return vars;
    }

    /** Action names an action attribute can refer to, empty if unknown. */
    private static Set<String> resolve(String action, Map<String, List<String>> vars) {
        Set<String> resolved = new LinkedHashSet<>();
        if (action == null || action.isBlank()) {
            return resolved;
        }
        Matcher m = VAR_REF.matcher(action);
        if (m.find()) {
            for (String value : vars.getOrDefault(m.group(1), List.of())) {
                resolved.addAll(resolve(action.replace(m.group(), value), vars));
            }
        } else if (!action.contains("%{")) {
            resolved.add(stripMethod(action));
        }
        return resolved;
    }

    private static String stripMethod(String action) {
        int bang = action.indexOf('!');
        return bang < 0 ? action : action.substring(0, bang);
    }

    private static String attr(String attrs, String name) {
        Matcher m = Pattern.compile("\\b" + name + "\\s*=\\s*(\"([^\"]*)\"|'([^']*)')").matcher(attrs);
        if (!m.find()) {
            return null;
        }
        return m.group(2) != null ? m.group(2) : m.group(3);
    }

    /**
     * Struts' own authorizer, checking an action class rather than an
     * instance so that the actions need not be created.
     */
    private static final class ClassAuthorizer extends StrutsParameterAuthorizer {

        ClassAuthorizer() {
            setRequireAnnotations("true");
        }

        @Override
        protected Class<?> ultimateClass(Object target) {
            return (Class<?>) target;
        }

        @Override
        protected BeanInfo getBeanInfo(Object target) {
            try {
                return Introspector.getBeanInfo((Class<?>) target);
            } catch (IntrospectionException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
