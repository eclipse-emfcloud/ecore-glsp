/********************************************************************************
 * Copyright (c) 2020-2021 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ********************************************************************************/
package org.eclipse.emfcloud.ecore.modelserver;

import org.eclipse.emfcloud.modelserver.emf.launch.ModelServerLauncher;

public class EcoreModelServerLauncher {

	public static void main(String[] args) {
		final ModelServerLauncher launcher = new ModelServerLauncher(new EcoreModelServerModule());
		ModelServerLauncher.configureLogger();
		launcher.run();
	}

}
