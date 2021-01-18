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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emfcloud.ecore.enotation.NotationElement;
import org.eclipse.emfcloud.ecore.enotation.Shape;
import org.eclipse.emfcloud.ecore.glsp.EcoreEditorContext;
import org.eclipse.emfcloud.ecore.glsp.EcoreFacade;
import org.eclipse.emfcloud.modelserver.client.XmiToEObjectSubscriptionListener;
import org.eclipse.emfcloud.modelserver.command.CCommand;
import org.eclipse.emfcloud.modelserver.command.CCompoundCommand;
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
			EcoreEditorContext editorContext = modelState.getEditorContext();
			EditingDomain editingDomain = editorContext.getResourceManager().getEditingDomain();
			commandResource = createCommandResource(editingDomain, command);

			EcoreFacade ecoreFacade = editorContext.getEcoreFacade();

			if (command.getType() == CommandKind.COMPOUND) {
				if (command instanceof CCompoundCommand) {
					((CCompoundCommand) command).getCommands().forEach(c -> {
						if (c.getType() == CommandKind.REMOVE) {
							executeRemoveCommand(c, editingDomain, ecoreFacade);
						} else if (c.getType() == CommandKind.ADD) {
							executeAddCommand(c, editingDomain, ecoreFacade);
						} else if (c.getType() == CommandKind.SET) {
							executeSetCommand(c, editingDomain, ecoreFacade);
						}
					});
				}
			} else if (command.getType() == CommandKind.ADD) {
				executeAddCommand(command, editingDomain, ecoreFacade);
			} else if (command.getType() == CommandKind.REMOVE) {
				executeRemoveCommand(command, editingDomain, ecoreFacade);
			} else if (command.getType() == CommandKind.SET) {
				executeSetCommand(command, editingDomain, ecoreFacade);
			}

			GModelRoot gmodelRoot = EcoreModelState.getEditorContext(modelState).getGModelFactory()
					.create(ecoreFacade.getEPackage());
			modelState.setRoot(gmodelRoot);

			actionDispatcher.dispatch(modelState.getClientId(), new RequestBoundsAction(gmodelRoot));

		} finally {
			if (commandResource != null) {
				commandResource.getResourceSet().getResources().remove(commandResource);
			}
		}
	}

	private void executeAddCommand(CCommand command, EditingDomain editingDomain, EcoreFacade ecoreFacade) {
		try {
			// Update semantic resource
			Command cmd = modelServerAccess.getCommandCodec().decode(editingDomain, command);
			editingDomain.getCommandStack().execute(cmd);
		} catch (DecodingException ex) {
			LOGGER.error("Could not decode command: " + command, ex);
			throw new RuntimeException(ex);
		}

		if (command.getObjectValues().size() > 0 && command.getObjectValues().get(0) instanceof EClassifier) {
			// Initialize notation element
			EClassifier newEClassifier = (EClassifier) command.getObjectValues().get(0);
			if (ecoreFacade.findUninitializedElements().size() > 0) {
				NotationElement notationElement = ecoreFacade.findUninitializedElements().get(0);
				ecoreFacade.initializeNotationElement(notationElement, newEClassifier);
			} else if (ecoreFacade.findOldShapes(newEClassifier).size() > 0) {
				// if element was removed via undo/redo its old shape is still here therefore we
				// reuse it
				NotationElement notationElement = ecoreFacade.findOldShapes(newEClassifier).get(0);
				ecoreFacade.initializeNotationElement(notationElement, newEClassifier);
			}
		}
	}

	private void executeRemoveCommand(CCommand command, EditingDomain editingDomain, EcoreFacade ecoreFacade) {
		EObject semanticElement = null;
		if (!command.getDataValues().isEmpty()) {
			String elementId = command.getDataValues().get(0);
			semanticElement = ecoreFacade.getSemanticResource().getEObject(elementId);
		} else if (!command.getIndices().isEmpty()) {
			int indexToRemove = command.getIndices().get(0);
			EObject owner = command.getOwner();
			if (owner instanceof EPackage) {
				semanticElement = ((EPackage) owner).getEClassifiers().get(indexToRemove);
			} else if (owner instanceof EEnum) {
				semanticElement = ((EEnum) owner).getELiterals().get(indexToRemove);
			} else if (owner instanceof EClass) {
				if (command.getFeature().equals("eStructuralFeatures")) {
					semanticElement = ((EClass) owner).getEStructuralFeatures().get(indexToRemove);
				}
			}
		}

		// Update notation resource
		Optional<NotationElement> notation = modelState.getIndex().getNotation(semanticElement);
		if (notation.isPresent() && notation.get() instanceof Shape) {
			// if undo is hit the shape should be restorable therefore we create a ghost
			// shape here
			ecoreFacade.createShape(Optional.of(((Shape) notation.get()).getPosition()));
			notation.ifPresent(EcoreUtil::delete);
		}
		// if element is removed via undo/redo the semantic element cannot be found
		// properly therefore we keep the notation element and reuse it in case of
		// undo/redo later

		try {
			// Update semantic resource
			Command cmd = modelServerAccess.getCommandCodec().decode(editingDomain, command);
			editingDomain.getCommandStack().execute(cmd);
		} catch (DecodingException ex) {
			LOGGER.error("Could not decode command: " + command, ex);
			throw new RuntimeException(ex);
		}
	}

	private void executeSetCommand(CCommand command, EditingDomain editingDomain, EcoreFacade ecoreFacade) {
		try {
			EObject owner = command.getOwner();
			if (owner != null && !(owner instanceof EClassifier)) {
				// Retrieve old owner
				EObject unresolved = (EObject) command.getOwner();
				String changedUri = EcoreUtil.getURI(unresolved).fragment();
				String oldUri = changedUri.substring(0, changedUri.length() - 1);
				EObject old = ecoreFacade.getSemanticResource().getEObject(oldUri);
				if (old != null) {
					command.setOwner(old);
				} else {
					List<EObject> l = new ArrayList<>();
					for (final Iterator<EObject> i = ecoreFacade.getEPackage().eAllContents(); i.hasNext();) {
						final EObject eObj = i.next();
						if (EcoreUtil.getURI(eObj).fragment().contains(changedUri)) {
							l.add(eObj);
						}
					}
					if (l.size() > 0) {
						command.setOwner(l.get(0));
					}
				}
			}

			// Update semantic resource
			Command cmd = modelServerAccess.getCommandCodec().decode(editingDomain, command);
			editingDomain.getCommandStack().execute(cmd);
		} catch (DecodingException ex) {
			LOGGER.error("Could not decode command: " + command, ex);
			throw new RuntimeException(ex);
		}

		if (command.getOwner() != null && command.getOwner() instanceof EClassifier) {
			// Initialize notation element
			EClassifier newEClassifier = (EClassifier) command.getOwner();
			if (ecoreFacade.findOldShapes(newEClassifier).size() > 0) {
				// if element was removed via undo/redo its old shape is still here therefore we
				// reuse it
				NotationElement notationElement = ecoreFacade.findOldShapes(newEClassifier).get(0);
				ecoreFacade.initializeNotationElement(notationElement, newEClassifier);
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
