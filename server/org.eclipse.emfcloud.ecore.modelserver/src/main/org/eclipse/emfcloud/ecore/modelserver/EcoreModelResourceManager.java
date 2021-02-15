/********************************************************************************
 * Copyright (c) 2021 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ********************************************************************************/
package org.eclipse.emfcloud.ecore.modelserver;

import java.io.File;
import java.util.Set;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emfcloud.modelserver.emf.common.DefaultModelResourceManager;
import org.eclipse.emfcloud.modelserver.emf.configuration.EPackageConfiguration;
import org.eclipse.emfcloud.modelserver.emf.configuration.ServerConfiguration;

import com.google.inject.Inject;

public class EcoreModelResourceManager extends DefaultModelResourceManager {

	@Inject
	public EcoreModelResourceManager(Set<EPackageConfiguration> configurations, AdapterFactory adapterFactory,
			ServerConfiguration serverConfiguration) {
		super(configurations, adapterFactory, serverConfiguration);
	}

	@Override
	protected void loadSourceResources(final String directoryPath) {
		if (directoryPath == null || directoryPath.isEmpty()) {
			return;
		}
		File directory = new File(directoryPath);
		for (File file : directory.listFiles()) {
			if (isSourceDirectory(file)) {
				loadSourceResources(file.getAbsolutePath());
			} else if (file.isFile()) {
				URI absolutePath = createURI(file.getAbsolutePath());
				if (absolutePath.fileExtension().equals("ecore")) {
					resourceSets.put(createURI(file.getAbsolutePath()), new ResourceSetImpl());
				}
				loadResource(file.getAbsolutePath(),
						false /* do not remove unloadable resources on workspace startup */);
			}
		}
	}

	@Override
	public ResourceSet getResourceSet(final String modeluri) {
		if (createURI(modeluri).fileExtension().equals("enotation")) {
			URI semanticUri = createURI(modeluri).trimFileExtension().appendFileExtension("ecore");
			return resourceSets.get(semanticUri);
		}
		return resourceSets.get(createURI(modeluri));
	}

	@Override
	public boolean save(final String modeluri) {
		boolean result = false;
		for (Resource resource : getResourceSet(modeluri).getResources()) {
			result = saveResource(resource);
		}
		if (result) {
			getEditingDomain(getResourceSet(modeluri)).saveIsDone();
		}
		return result;
	}

}
