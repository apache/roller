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
package org.roller;

/**
 * Thrown when persistence session user lacks one or more required permissions.
 */ 
public class RollerPermissionsException extends RollerException
{
	/**
	 * Construct RollerException, wrapping existing throwable.
	 * @param s Error message.
	 * @param t Throwable to be wrapped
	 */
	public RollerPermissionsException(String s,Throwable t)
	{
		super(s,t);
	}
	/**
	 * Construct RollerException, wrapping existing throwable.
	 * @param t Throwable to be wrapped
	 */
	public RollerPermissionsException(Throwable t)
	{
        super(t);
	}
	/**
	 * Construct RollerException, with error message.
	 * @param s Error message
	 */
	public RollerPermissionsException(String s)
	{
		super(s);
	}
}

