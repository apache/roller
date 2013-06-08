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

package org.apache.roller.weblogger.ui.struts2.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.ui.struts2.editor.EntryAdd;
import org.apache.roller.weblogger.ui.struts2.editor.EntryAddWithMediaFile;
import org.apache.roller.weblogger.ui.struts2.editor.EntryEdit;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.MethodFilterInterceptor;

/**
 * A struts2 interceptor for doing custom prepare logic.
 */
public class UIActionPrepareInterceptor extends MethodFilterInterceptor {

	private static final long serialVersionUID = 7770184756145702592L;
	private static Log log = LogFactory
			.getLog(UIActionPrepareInterceptor.class);

	public String doIntercept(ActionInvocation invocation) throws Exception {

		log.debug("Entering UIActionPrepareInterceptor");

		final Object action = invocation.getAction();
		//final ActionContext context = invocation.getInvocationContext();

		// is this one of our own UIAction classes?
		if (action instanceof UIActionPreparable) {

			log.debug("action is UIActionPreparable, calling myPrepare() method");

			// The EntryAdd->EntryEdit chain is the one place where we need
			// to pass a parameter along the chain, thus this somewhat ugly hack
			if (invocation.getStack().getRoot().size() > 1) {
				Object action0 = invocation.getStack().getRoot().get(0);
				Object action1 = invocation.getStack().getRoot().get(1);
				if (action0 instanceof EntryEdit && action1 instanceof EntryAdd) {
					EntryEdit editAction = (EntryEdit) action0;
					EntryAdd addAction = (EntryAdd) action1;
					editAction.getBean().setId(addAction.getBean().getId());
				} else if (action0 instanceof EntryAdd
						&& action1 instanceof EntryAddWithMediaFile) {
					EntryAdd addAction = (EntryAdd) action0;
					EntryAddWithMediaFile mediaAction = (EntryAddWithMediaFile) action1;
					addAction.setBean(mediaAction.getBean());
				}
			}

			UIActionPreparable theAction = (UIActionPreparable) action;
			theAction.myPrepare();
		}

		return invocation.invoke();
	}

}
