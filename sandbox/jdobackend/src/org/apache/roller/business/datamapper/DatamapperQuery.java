
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
package org.apache.roller.business.datamapper;

/**
 *
 */
public interface DatamapperQuery {

    /** Execute the query with no parameters.
     * @return the results of the query
     */
    Object execute();

    /** Execute the query with one parameter.
     * @param param the parameter
     * @return the results of the query
     */
    Object execute(Object param);

    /** Execute the query with parameters.
     * @param params the parameters
     * @return the results of the query
     */
    Object execute(Object[] params);

    /** Set the result to be a single instance (not a List).
     * @return the instance on which this method is called
     */
    DatamapperQuery setUnique();

    /** Set the range of results for this query.
     * @param fromIncl the beginning row number
     * @param toExcl the ending row number
     * @return the instance on which this method is called
     */
    DatamapperQuery setRange(long fromIncl, long toExcl);

}
