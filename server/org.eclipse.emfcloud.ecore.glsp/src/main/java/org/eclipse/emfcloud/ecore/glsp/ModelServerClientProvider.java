/********************************************************************************
 * Copyright (c) 2019-2021 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ********************************************************************************/
package org.eclipse.emfcloud.ecore.glsp;

import java.util.Optional;

import org.eclipse.emfcloud.ecore.modelserver.EcoreModelServerClient;

import com.google.inject.Singleton;

@Singleton
public class ModelServerClientProvider {
	private EcoreModelServerClient modelServerClient;

	public Optional<EcoreModelServerClient> get() {
		return Optional.ofNullable(modelServerClient);
	}

	public void setModelServerClient(EcoreModelServerClient modelServerClient) {
		this.modelServerClient = modelServerClient;
	}

}