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
package org.eclipse.emfcloud.ecore.backend.app;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CreateEcoreApplication implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		String[] args = getArgs(context);
		String name = null;
		String prefix = null;
		String uri = null;
		String workspacePath = null;
		for (int i = 0; i < args.length; i++) {
			switch (i) {
			case 0:
				name = args[i];
				break;
			case 1:
				prefix = args[i];
				break;
			case 2:
				uri = args[i];
				break;
			case 3:
				workspacePath = args[i];
				break;
			}
		}

		if (name == null) {
			System.exit(-10);
		}
		if (prefix == null) {
			System.exit(-11);
		}
		if (uri == null) {
			System.exit(-12);
		}
		if (workspacePath == null) {
			System.exit(-13);
		}

		createEcore(name, prefix, uri, Paths.get(workspacePath, name+".ecore"));
		return null;
	}
	
	private String[] getArgs(IApplicationContext context) {
		Object object = context.getArguments().get("application.args");
		if (object instanceof String[]) {
			return (String[])object;
		}
		return new String[0];
    }
    
    public static void createEcore(String name, String nsPrefix, String nsURI, Path workspacePath) throws IOException {
		ResourceSet resourceSet = new ResourceSetImpl();
		Resource resource = resourceSet.createResource(URI.createFileURI(workspacePath.toString()));

		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		ePackage.setName(name);
		ePackage.setNsPrefix(nsPrefix);
		ePackage.setNsURI(nsURI);
		resource.getContents().add(ePackage);
		resource.save(null);
	}

	@Override
	public void stop() {
		// Nothing to do here.
	}

}
