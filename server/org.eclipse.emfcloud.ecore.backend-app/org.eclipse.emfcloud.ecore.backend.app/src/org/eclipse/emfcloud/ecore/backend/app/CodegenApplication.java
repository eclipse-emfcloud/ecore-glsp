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

import java.io.File;

import org.eclipse.emf.codegen.ecore.Generator;
import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class CodegenApplication implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		Generator codegen = new Generator();
		String[] args = getArgs(context);
		String[] genmodelArgs;
		ResourceSet resourceSet = new ResourceSetImpl();

		File genmodelFile = new File(args[0]);
		URI genmodelUri = URI.createFileURI(genmodelFile.getAbsolutePath());
		Resource genmodelResource = resourceSet.getResource(genmodelUri, true);
		GenModel genmodel = (GenModel) genmodelResource.getContents().get(0);
		String modelDirectory = genmodel.getModelDirectory();
		String basePackage = genmodel.getGenPackages().get(0).getBasePackage();
		String nsName = genmodel.getGenPackages().get(0).getNSName();

		String ecorePath = args[0].substring(0, args[0].lastIndexOf(".")) + ".ecore";

		genmodelArgs = new String[]{"-ecore2GenModel", ecorePath, basePackage, nsName};
		//TODO start Genmodel reloading here
		codegen.run(args);
		return null;
	}
	
	private String[] getArgs(IApplicationContext context) {
		Object object = context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		if (object instanceof String[]) {
			return (String[])object;
		}
		return new String[0];
	}

	@Override
	public void stop() {
		// Nothing to do here.
	}

}
