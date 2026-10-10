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

package org.apache.roller.weblogger.business.themes;

import java.util.List;

/**
 * Thrown by {@link WeblogTemplateEditor} when a template change breaks one or
 * more template rules. Each violation carries a message key from
 * ApplicationResources and its arguments.
 */
public class TemplateRuleException extends Exception {

    private static final long serialVersionUID = 1L;

    /** One broken rule. {@code conflict} is true when another template is in the way. */
    public static final class Violation {
        private final String messageKey;
        private final List<String> args;
        private final boolean conflict;

        public Violation(String messageKey, List<String> args, boolean conflict) {
            this.messageKey = messageKey;
            this.args = List.copyOf(args);
            this.conflict = conflict;
        }

        public String getMessageKey() {
            return messageKey;
        }

        public List<String> getArgs() {
            return args;
        }

        public boolean isConflict() {
            return conflict;
        }
    }

    private final List<Violation> violations;

    public TemplateRuleException(List<Violation> violations) {
        super(violations.isEmpty() ? "Template rule violation"
                : violations.get(0).getMessageKey() + " " + violations.get(0).getArgs());
        this.violations = List.copyOf(violations);
    }

    public List<Violation> getViolations() {
        return violations;
    }

    /** True when any violation is a conflict with an existing template. */
    public boolean isConflict() {
        return violations.stream().anyMatch(Violation::isConflict);
    }
}
