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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
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
		String[] args = getArgs(context);
		if (args.length != 1) {
			throw new IllegalArgumentException("Expected argument: genmodel path; got "+Arrays.deepToString(args));
		}
		String genmodelPath = args[0];
		
		ResourceSet resourceSet = new ResourceSetImpl();

		File genmodelFile = new File(genmodelPath);
		URI genmodelUri = URI.createFileURI(genmodelFile.getAbsolutePath());
		Resource genmodelResource = resourceSet.getResource(genmodelUri, true);
		GenModel genmodel = (GenModel) genmodelResource.getContents().get(0);
		IPath modelDir = new Path(genmodel.getModelProjectDirectory());
		
		// FIXME: At the moment, the Eclipse/Codegen workspace is the same as the Theia Workspace
		// Ideally, the Codegen App should use a different workspace, and we should add another
		// parameter to retrieve the Theia workspace.
		IPath theiaWsPath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		
		String rootLocation = theiaWsPath.append(modelDir.segment(0)).toString();
		
		// Before running codegen, make sure to clean all projects. This is required
		// to avoid codegen errors, if a project no longer has a .project file
		// Since we target the real filesystem, and not the eclipse filesystem,
		// we don't necessarily expect valid eclipse .project files.
		for (IProject project: ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			// Remove project from workspace, but do not delete any contents
			try {
				project.delete(false, true, null);
			} catch (CoreException ex) {
				// Log the exception, but try generating the code nonetheless. If it's 
				// an unrelated project, it might still work. 
				ex.printStackTrace();
			}
		}
		
		// Generator arguments:
		// 		"-reconcile",
		// 		genmodelPath,
		// 		rootProjectPath (first segment after Theia workspace)
		List<String> codeGenArgs = new ArrayList<>();
		codeGenArgs.add("-reconcile"); // Refresh genmodel before regenerating
		codeGenArgs.add(genmodelPath);
		codeGenArgs.add(rootLocation);
		Generator codegen = new Generator();
		return codegen.run(codeGenArgs.toArray(String[]::new));
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
