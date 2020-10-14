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

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emfcloud.modelserver.client.ModelServerClient;
import org.eclipse.emfcloud.modelserver.client.NotificationSubscriptionListener;
import org.eclipse.emfcloud.modelserver.edit.CommandCodec;

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

}
