/*
 * Copyright 2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package org.apache.roller.business.datamapper;

import java.util.List;

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

    /** Remove instances selected by the query with no parameters.
     * @return the results of the query
     */
    void removeAll();

    /** Remove instances selected by the query with one parameter.
     * @param param the parameter
     * @return the results of the query
     */
    void removeAll(Object param);

    /** Remove instances selected by the query with parameters.
     * @param params the parameters
     * @return the results of the query
     */
    void removeAll(Object[] params);

    /** Set the result to be a single instance (not a List).
     * @result the instance on which this method is called
     */
    DatamapperQuery setUnique();

    /** Set the types of the parameters. This is only needed if the 
     * parameter types are temporal types, e.g. Date, Time, Calendar.
     * @param the types of the parameters in corresponding positions.
     * @result the instance on which this method is called
     */
    DatamapperQuery setTypes(Object[] types);

    /** Set the range of results for this query.
     * @fromIncl the beginning row number
     * @toExcl the ending row number
     * @return the instance on which this method is called
     */
    DatamapperQuery setRange(long fromIncl, long toExcl);

}
