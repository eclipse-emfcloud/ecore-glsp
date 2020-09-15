package org.eclipse.emfcloud.ecore.backend.app;

import java.io.File;
import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.codegen.ecore.Generator;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

public class GenModelApplication implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		Generator codegen = new Generator();
		String[] args = getArgs(context);
		ResourceSet resourceSet = new ResourceSetImpl();
		File ecoreFile = new File(args[0]);
		if (!ecoreFile.exists()) {
			throw new RuntimeException("Ecore file not found: " + ecoreFile.getAbsolutePath());
		}
		URI uri = URI.createFileURI(ecoreFile.getAbsolutePath());
		Resource ecorePackageResource = resourceSet.getResource(uri, true);
		EPackage rootPackage = (EPackage) ecorePackageResource.getContents().get(0);
		if(args[1].equals("") || args[2].equals("")){
			//get default values
			if(args[1].equals("")){
				args[1] = rootPackage.getName();
			}
			if(args[2].equals("")){
				args[2] = "src";
			}
		}
		//remove leading /
		if(args[2].charAt(0) == '/') args[2] = args[2].substring(1);
		//initialize args for genmodel creation
		String[] genmodelArgs = new String[]{"-ecore2GenModel", args[0], args[1], rootPackage.getNsPrefix()};
		codegen.run(genmodelArgs);
		String genmodelPath = args[0].substring(0, args[0].lastIndexOf(".")) + ".genmodel";
		File genmodelFile = new File(genmodelPath);
		URI genmodelUri = URI.createFileURI(genmodelFile.getAbsolutePath());
		Resource genmodelResource = resourceSet.getResource(genmodelUri, true);
		GenModel genmodel = (GenModel) genmodelResource.getContents().get(0);
		genmodel.setModelDirectory("/" + args[2]);
		genmodelResource.save(null);
		return null;
	}
	
	private String[] getArgs(IApplicationContext context) {
		Object object = context.getArguments().get("application.args");
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
