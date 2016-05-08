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

package org.apache.roller.weblogger.ui.rendering.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.roller.weblogger.business.WebloggerFactory;


/**
 * Represents a set of functionality to be used at rendering.  Most models require specific objects
 * to be provided within their init() methods, see the implementation of each Model to determine
 * its requirements.
 */
public interface Model {
    
    /**
     * Name to be used when referring to this model (e.g., the $(model name). seen within templates).
     */
    String getModelName();
    
    
    /**
     * Initialize.
     * @throws IllegalArgumentException if the model is not fed the specific objects it is expecting
     */
    void init(Map params) throws IllegalStateException;

    static Map<String, Object> getModelMap(String modelBean, Map<String, Object> initData) {
        HashMap<String, Object> modelMap = new HashMap<>();
        Set modelSet = WebloggerFactory.getContext().getBean(modelBean, Set.class);
        for (Object obj : modelSet) {
            Model m = (Model) obj;
            m.init(initData);
            modelMap.put(m.getModelName(), m);
        }
        return modelMap;
    }

}
