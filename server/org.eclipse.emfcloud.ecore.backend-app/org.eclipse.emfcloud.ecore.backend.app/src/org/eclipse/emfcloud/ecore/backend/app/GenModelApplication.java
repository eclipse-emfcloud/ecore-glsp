package org.eclipse.emfcloud.ecore.backend.app;

import java.io.File;
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
		if(args[2].equals("") || args[3].equals("")){
			File ecoreFile = new File(args[1]);
			if (!ecoreFile.exists()) {
				throw new RuntimeException("Ecore file not found: " + ecoreFile.getAbsolutePath());
			}
			
			System.out.println("Opening ecore file: " + ecoreFile.getAbsolutePath());
			ResourceSet resourceSet = new ResourceSetImpl();
			URI uri =  URI.createFileURI(ecoreFile.getAbsolutePath());
			Resource ecorePackageResource = resourceSet.getResource(uri, true);
			EPackage rootPackage = (EPackage) ecorePackageResource.getContents().get(0);
			if(args[2].equals("")){
				args[2] = rootPackage.getName();
			}
			if(args[3].equals("")){
				args[3] = rootPackage.getNsPrefix();
			}
		}
		codegen.run(args);
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
