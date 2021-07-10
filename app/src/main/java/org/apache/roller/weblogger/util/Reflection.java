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

package org.apache.roller.weblogger.util;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.roller.weblogger.config.WebloggerConfig;

/**
 * Utility methods for common reflection tasks.
 */
public final class Reflection {

    private Reflection() {}
    
        
    public static Object newInstance(String className) throws ReflectiveOperationException {
        return newInstance(Class.forName(className));
    }
    
    public static <T> T newInstance(Class<T> clazz) throws ReflectiveOperationException {
        Constructor<T> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }
        
    public static <T> List<T> newInstances(String[] classList) throws ReflectiveOperationException {
        
        List<T> instances = new ArrayList<>();
            
        for (String klass : classList) {
            @SuppressWarnings("unchecked")
            T instance = (T) Reflection.newInstance(klass);  // throws CCE if instance dos not match T
            instances.add(instance);
        }
        
        return instances;
    }
    
    public static <T> List<T> newInstancesFromProperty(String property) throws ReflectiveOperationException {
        
        String classList = WebloggerConfig.getProperty(property);
        
        if (classList != null && !classList.isBlank()) {
            return newInstances(classList.split(","));
        }
        
        return Collections.emptyList();
    }
    
    /**
     * Returns true if the given class directly implements the given interface.
     */
    public static boolean implementsInterface(Class<?> clazz, Class<?> interfaze) {
        for (Class<?> inter : clazz.getInterfaces())
            if (inter.equals(interfaze))
                return true;
        return false;
    }
    
}
