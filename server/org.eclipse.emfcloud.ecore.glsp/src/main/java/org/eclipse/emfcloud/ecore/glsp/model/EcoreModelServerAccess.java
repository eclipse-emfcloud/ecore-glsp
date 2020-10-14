/********************************************************************************
 * Copyright (c) 2020 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ********************************************************************************/
package org.eclipse.emfcloud.ecore.glsp.model;

import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emfcloud.ecore.glsp.EcoreFacade;
import org.eclipse.emfcloud.modelserver.client.ModelServerClient;
import org.eclipse.emfcloud.modelserver.client.NotificationSubscriptionListener;
import org.eclipse.emfcloud.modelserver.command.CCommand;
import org.eclipse.emfcloud.modelserver.common.codecs.EncodingException;
import org.eclipse.emfcloud.modelserver.edit.CommandCodec;
import org.eclipse.glsp.server.model.GModelState;

import com.google.common.base.Preconditions;

public class EcoreModelServerAccess {

	private static Logger LOGGER = Logger.getLogger(EcoreModelServerAccess.class);

	private static final String FORMAT_XMI = "xmi";

	private String modelUri;

	private ModelServerClient modelServerClient;
	private NotificationSubscriptionListener<EObject> subscriptionListener;
	private CommandCodec commandCodec;

	public EcoreModelServerAccess(final String modelUri, final ModelServerClient modelServerClient,
			final CommandCodec commandCodec) {
		Preconditions.checkNotNull(modelServerClient);
		this.modelUri = modelUri;
		this.modelServerClient = modelServerClient;
		this.commandCodec = commandCodec;
	}

	public CommandCodec getCommandCodec() {
		return commandCodec;
	}

	public ModelServerClient getModelServerClient() {
		return modelServerClient;
	}

	public void update(GModelState graphicalModelState) {
		EcoreFacade facade = EcoreModelState.getEcoreFacade(graphicalModelState);
		EObject root = facade.getSemanticResource().getContents().get(0);
		// trigger full model update
		modelServerClient.update(modelUri, root, FORMAT_XMI);
	}

	public void subscribe(NotificationSubscriptionListener<EObject> subscriptionListener) {
		LOGGER.debug("EcoreModelServerAccess - subscribe");
		this.subscriptionListener = subscriptionListener;
		this.modelServerClient.subscribe(modelUri, subscriptionListener, FORMAT_XMI);
	}

	public void unsubscribe() {
		LOGGER.debug("EcoreModelServerAccess - unsubscribe");
		if (subscriptionListener != null) {
			this.modelServerClient.unsubscribe(modelUri);
		}
	}

	private EPackage getEPackage(EcoreModelState modelState) {
		EcoreFacade facade = EcoreModelState.getEcoreFacade(modelState);
		return facade.getEPackage();
	}

	public boolean addEClassifier(EcoreModelState modelState, EClassifier newEClassifier) {
		return this.add(modelState, newEClassifier, EcorePackage.eINSTANCE.getEPackage_EClassifiers());
	}

	private boolean add(EcoreModelState modelState, EObject newEClassifier, Object owner) {
		EPackage ePackage = getEPackage(modelState);
		Command addCommand = AddCommand.create(
				EcoreModelState.getEditorContext(modelState).getResourceManager().getEditingDomain(), ePackage, owner,
				newEClassifier);
		return this.edit(addCommand);
	}

	public boolean delete(EcoreModelState modelState, EObject element) {
		EPackage ePackage = getEPackage(modelState);
		Command removeCommand = RemoveCommand.create(
				EcoreModelState.getEditorContext(modelState).getResourceManager().getEditingDomain(), ePackage,
				EcorePackage.eINSTANCE.getEPackage_EClassifiers(), element);
		return this.edit(removeCommand);
	}

	private boolean edit(Command command) {
		try {
			CCommand ccommand = getCommandCodec().encode(command);
			return this.modelServerClient.edit(modelUri, ccommand, FORMAT_XMI).thenApply(res -> res.body()).get();
		} catch (InterruptedException | ExecutionException | EncodingException e) {
			return false;
		}
	}

	public boolean save() {
		try {
			return this.modelServerClient.save(modelUri).thenApply(res -> res.body()).get();
		} catch (InterruptedException | ExecutionException e) {
			return false;
		}
	}

}
