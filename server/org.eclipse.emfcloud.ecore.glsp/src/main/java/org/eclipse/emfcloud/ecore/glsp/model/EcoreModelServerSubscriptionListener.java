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

import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emfcloud.ecore.enotation.NotationElement;
import org.eclipse.emfcloud.ecore.glsp.EcoreEditorContext;
import org.eclipse.emfcloud.ecore.glsp.EcoreFacade;
import org.eclipse.emfcloud.modelserver.client.XmiToEObjectSubscriptionListener;
import org.eclipse.emfcloud.modelserver.command.CCommand;
import org.eclipse.emfcloud.modelserver.command.CommandKind;
import org.eclipse.emfcloud.modelserver.common.codecs.DecodingException;
import org.eclipse.glsp.graph.GModelRoot;
import org.eclipse.glsp.server.actions.ActionDispatcher;
import org.eclipse.glsp.server.actions.RequestBoundsAction;
import org.eclipse.glsp.server.actions.SetDirtyStateAction;

public class EcoreModelServerSubscriptionListener extends XmiToEObjectSubscriptionListener {

	private static final String TEMP_COMMAND_RESOURCE_URI = "command$1.command";

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
		Resource commandResource = null;
		try {
			// execute command on semantic resource
			EcoreEditorContext editorContext = modelState.getEditorContext();
			EditingDomain editingDomain = editorContext.getResourceManager().getEditingDomain();
			commandResource = createCommandResource(editingDomain, command);
			Command cmd = modelServerAccess.getCommandCodec().decode(editingDomain, command);
			editingDomain.getCommandStack().execute(cmd);

			// update notation resource
			EcoreFacade ecoreFacade = editorContext.getEcoreFacade();
			if (command.getType() == CommandKind.ADD) {
				// TODO always getting first element is not ideal, maybe you have a better idea
				EClassifier newEClassifier = (EClassifier) command.getObjectValues().get(0);
				NotationElement notationElement = ecoreFacade.findUninitializedElements().get(0);
				ecoreFacade.initializeNotationElement(notationElement, newEClassifier);
			} else if (command.getType() == CommandKind.REMOVE) {
				// TODO
			}

			GModelRoot gmodelRoot = EcoreModelState.getEditorContext(modelState).getGModelFactory()
					.create(ecoreFacade.getEPackage());
			modelState.setRoot(gmodelRoot);

			actionDispatcher.dispatch(modelState.getClientId(), new RequestBoundsAction(gmodelRoot));

		} catch (DecodingException ex) {
			LOGGER.error("Could not decode command: " + command, ex);
			throw new RuntimeException(ex);
		} finally {
			if (commandResource != null) {
				commandResource.getResourceSet().getResources().remove(commandResource);
			}
		}
	}

	private Resource createCommandResource(EditingDomain editingDomain, CCommand command) {
		Resource resource = editingDomain.createResource(TEMP_COMMAND_RESOURCE_URI);
		resource.getContents().add(command);
		return resource;
	}

	@Override
	public void onDirtyChange(boolean isDirty) {
		LOGGER.debug("Dirty State Changed: " + isDirty + " for clientId: " + modelState.getClientId());
		actionDispatcher.dispatch(modelState.getClientId(), new SetDirtyStateAction(isDirty));
	}

	@Override
	public void onFullUpdate(EObject newRoot) {
		LOGGER.debug("Full update from model server received");
		EcoreFacade ecoreFacade = EcoreModelState.getEcoreFacade(modelState);

		ecoreFacade.resetSemanticResource(newRoot);

		GModelRoot gmodelRoot = EcoreModelState.getEditorContext(modelState).getGModelFactory().create();
		modelState.setRoot(gmodelRoot);

		ecoreFacade.resetNotationResource(gmodelRoot);

		actionDispatcher.dispatch(modelState.getClientId(), new RequestBoundsAction(modelState.getRoot()));
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
