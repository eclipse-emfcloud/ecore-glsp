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

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.eclipse.elk.alg.layered.options.LayeredMetaDataProvider;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.change.ChangePackage;
import org.eclipse.emfcloud.ecore.enotation.EnotationPackage;
import org.eclipse.glsp.layout.ElkLayoutEngine;
import org.eclipse.glsp.server.launch.DefaultGLSPServerLauncher;
import org.eclipse.glsp.server.launch.GLSPServerLauncher;

public class EcoreServerLauncher {

	private static final Logger LOG = Logger.getLogger(EcoreServerLauncher.class);

	private static final int DEFAULT_PORT = 5007;

	public static void main(String[] args) {
		int port = getPort(args);
		configureLogger();
		registerEPackages();
		ElkLayoutEngine.initialize(new LayeredMetaDataProvider());
		GLSPServerLauncher launcher = new DefaultGLSPServerLauncher(new EcoreGLSPModule());
		launcher.start("localhost", port);
	}

	private static int getPort(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if ("--port".contentEquals(args[i])) {
				return Integer.parseInt(args[i + 1]);
			}
		}
		LOG.info("The server port was not specified; using default port 5007");
		return DEFAULT_PORT;
	}

	public static void configureLogger() {
		Logger root = Logger.getRootLogger();
		if (!root.getAllAppenders().hasMoreElements()) {
			root.addAppender(new ConsoleAppender(new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN)));
		}
		root.setLevel(Level.INFO);
	}

	public static void registerEPackages() {
		EcorePackage.eINSTANCE.eClass();
		EnotationPackage.eINSTANCE.eClass();
		ChangePackage.eINSTANCE.eClass();
	}
}
