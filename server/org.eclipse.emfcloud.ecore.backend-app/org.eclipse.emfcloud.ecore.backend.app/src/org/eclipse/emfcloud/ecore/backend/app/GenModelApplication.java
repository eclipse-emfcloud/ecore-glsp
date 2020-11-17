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
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class GenModelApplication implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		Generator codegen = new Generator();
		String[] args = getArgs(context);

		String ecoreFilePath = null;
		String customRootPackage = null;
		String customOutputFolder = null;

		for (int i = 0; i < args.length; i++) {
			switch (i) {
				case 0:
					ecoreFilePath = args[i];
					break;
				case 1:
					customRootPackage = args[i];
					break;
				case 2:
					customOutputFolder = args[i];
					break;
			}
		}
		if (ecoreFilePath == null) {
			System.exit(-10);
		}
		if (customRootPackage == null) {
			System.exit(-11);
		}
		if (customOutputFolder == null) {
			System.exit(-12);
		}

		ResourceSet resourceSet = new ResourceSetImpl();
		File ecoreFile = new File(ecoreFilePath);
		if (!ecoreFile.exists()) {
			throw new RuntimeException("Ecore file not found: " + ecoreFile.getAbsolutePath());
		}
		URI uri = URI.createFileURI(ecoreFile.getAbsolutePath());
		Resource ecorePackageResource = resourceSet.getResource(uri, true);
		EPackage rootPackage = (EPackage) ecorePackageResource.getContents().get(0);

		//get default values
		if(customRootPackage.equals("")){
			customRootPackage = rootPackage.getName();
		}
		if(customOutputFolder.equals("")){
			customOutputFolder = "src";
		}

		//remove leading /
		if(customOutputFolder.charAt(0) == '/') customOutputFolder = customOutputFolder.substring(1);
		//initialize args for genmodel creation
		String[] genmodelArgs = new String[]{"-ecore2GenModel", ecoreFilePath, customRootPackage, rootPackage.getNsPrefix()};
		codegen.run(genmodelArgs);

		String genmodelPath = ecoreFilePath.substring(0, ecoreFilePath.lastIndexOf(".")) + ".genmodel";
		File genmodelFile = new File(genmodelPath);
		URI genmodelUri = URI.createFileURI(genmodelFile.getAbsolutePath());
		Resource genmodelResource = resourceSet.getResource(genmodelUri, true);
		GenModel genmodel = (GenModel) genmodelResource.getContents().get(0);
		genmodel.setModelDirectory("/" + customOutputFolder);
		genmodelResource.save(null);

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
