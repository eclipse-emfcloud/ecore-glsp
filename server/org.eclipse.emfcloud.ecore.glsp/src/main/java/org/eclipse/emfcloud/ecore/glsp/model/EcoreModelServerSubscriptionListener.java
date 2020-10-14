/*******************************************************************************
 * Copyright (c) 2020 EclipseSource and others.
 *  
 *   This program and the accompanying materials are made available under the
 *   terms of the Eclipse Public License v. 2.0 which is available at
 *   http://www.eclipse.org/legal/epl-2.0.
 *  
 *   This Source Code may also be made available under the following Secondary
 *   Licenses when the conditions for such availability set forth in the Eclipse
 *   Public License v. 2.0 are satisfied: GNU General Public License, version 2
 *   with the GNU Classpath Exception which is available at
 *   https://www.gnu.org/software/classpath/license.html.
 *  
 *   SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 ******************************************************************************/
package org.eclipse.emfcloud.ecore.glsp.model;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emfcloud.modelserver.client.XmiToEObjectSubscriptionListener;
import org.eclipse.emfcloud.modelserver.command.CCommand;
import org.eclipse.glsp.server.actions.ActionDispatcher;

public class EcoreModelServerSubscriptionListener extends XmiToEObjectSubscriptionListener {

	private static Logger LOGGER = Logger.getLogger(EcoreModelServerSubscriptionListener.class);
	private ActionDispatcher actionDispatcher;
	private EcoreModelServerAccess modelServerAccess;
	private EcoreModelState modelState;

	public EcoreModelServerSubscriptionListener(EcoreModelState modelState, EcoreModelServerAccess modelServerAccess,
			ActionDispatcher actionDispatcher) {
		this.actionDispatcher = actionDispatcher;
		this.modelServerAccess = modelServerAccess;
		this.modelState = modelState;
	}

	@Override
	public void onIncrementalUpdate(CCommand command) {
		LOGGER.debug("Incremental update from model server received: " + command);
		// TODO
	}

	@Override
	public void onFullUpdate(EObject root) {
		LOGGER.debug("Full update from model server received");
		// TODO
	}

}
