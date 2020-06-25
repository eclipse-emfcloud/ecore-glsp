/********************************************************************************
 * Copyright (c) 2019-2020 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ********************************************************************************/
package org.eclipse.emfcloud.ecore.backend;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public class EcoreProvider {
    public static void main(String[] args) throws IOException
    {
		String name = null;
		String prefix = null;
		String uri = null;
		Path workspacePath = null;
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			switch (arg) {
			case "-name":
				i++;
				name = args[i];
				break;
			case "-prefix":
				i++;
				prefix = args[i];
				break;
			case "-uri":
				i++;
				uri = args[i];
				break;
			case "-workspacePath":
				i++;
				workspacePath = Paths.get(args[i], name+".ecore");
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

		createEcore(name, prefix, uri, workspacePath);
	}
	
	public static void createEcore(String name, String nsPrefix, String nsURI, Path workspacePath) throws IOException {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		XMLResource resource = (XMLResource) resourceSet.createResource(URI.createFileURI(workspacePath.toString()));

		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		ePackage.setName(name);
		ePackage.setNsPrefix(nsPrefix);
		ePackage.setNsURI(nsURI);
		resource.setEncoding("UTF-8");
		resource.getContents().add(ePackage);
		resource.save(null);
	}

}
