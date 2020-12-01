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
package org.eclipse.emfcloud.ecore.glsp;

import org.apache.log4j.Logger;
import org.eclipse.emfcloud.modelserver.client.ModelServerClient;
import org.eclipse.emfcloud.modelserver.client.Response;
import org.eclipse.glsp.server.jsonrpc.DefaultGLSPServer;

import com.google.inject.Inject;

public class EcoreGLSPServer extends DefaultGLSPServer<EcoreInitializeOptions> {
	static Logger LOGGER = Logger.getLogger(EcoreGLSPServer.class);

	@Inject
	private ModelServerClientProvider modelServerClientProvider;

	public EcoreGLSPServer() {
		super(EcoreInitializeOptions.class);
	}

	@Override
	public boolean handleOptions(EcoreInitializeOptions options) {
		if (options != null) {
			LOGGER.debug(String.format("[%s] Pinging modelserver", options.getTimestamp()));

			try {
				ModelServerClient client = new ModelServerClient(options.getModelServerURL());
				boolean alive = client.ping().thenApply(Response<Boolean>::body).get();
				if (alive) {
					modelServerClientProvider.setModelServerClient(client);
					return true;
				}

			} catch (Exception e) {
				LOGGER.error("Error during initialization of modelserver connection", e);
			}
		}
		return true;
	}

}
