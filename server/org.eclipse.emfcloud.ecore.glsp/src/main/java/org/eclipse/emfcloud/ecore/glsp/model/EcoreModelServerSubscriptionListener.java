/*******************************************************************************
 * Copyright (c) 2020-2021 EclipseSource and others.
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

import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emfcloud.modelserver.client.XmiToEObjectSubscriptionListener;
import org.eclipse.emfcloud.modelserver.command.CCommandExecutionResult;
import org.eclipse.glsp.server.actions.ActionDispatcher;
import org.eclipse.glsp.server.actions.SetDirtyStateAction;
import org.eclipse.glsp.server.features.core.model.ModelSubmissionHandler;
import org.eclipse.glsp.server.features.core.model.RequestBoundsAction;

public class EcoreModelServerSubscriptionListener extends XmiToEObjectSubscriptionListener {

	private static Logger LOGGER = Logger.getLogger(EcoreModelServerSubscriptionListener.class);
	private ActionDispatcher actionDispatcher;
	private EcoreModelState modelState;
	protected final ModelSubmissionHandler submissionHandler;

	public EcoreModelServerSubscriptionListener(final EcoreModelState modelState,
			final ActionDispatcher actionDispatcher, final ModelSubmissionHandler submissionHandler) {
		this.actionDispatcher = actionDispatcher;
		this.modelState = modelState;
		this.submissionHandler = submissionHandler;
	}

	protected void refresh() {
		// reload models
		modelState.loadSourceModels();
		// refresh GModelRoot
		submissionHandler.submitModel(modelState);
		// requestboundsaction in submissionhandler not enough?
		actionDispatcher.dispatch(modelState.getClientId(), new RequestBoundsAction(modelState.getRoot()));
	}

	@Override
	public void onIncrementalUpdate(final CCommandExecutionResult commandResult) {
		LOGGER.debug("Incremental update from model server received: " + commandResult);
		this.refresh();
	}

	@Override
	public void onFullUpdate(final EObject fullUpdate) {
		LOGGER.debug("Full update from model server received: " + fullUpdate);
		this.refresh();
	}

	@Override
	public void onDirtyChange(boolean isDirty) {
		LOGGER.debug("Dirty State Changed: " + isDirty + " for clientId: " + modelState.getClientId());
		actionDispatcher.dispatch(modelState.getClientId(), new SetDirtyStateAction(isDirty));
	}

	@Override
	public void onError(final Optional<String> message) {
		LOGGER.debug("Error from model server received: " + message.get());
	}

	@Override
	public void onClosing(final int code, final String reason) {
		LOGGER.debug("Closing connection to model server, reason: " + reason);
	}

	@Override
	public void onClosed(final int code, final String reason) {
		LOGGER.debug("Closed connection to model server, reason: " + reason);
	}

}
