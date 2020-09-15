package org.eclipse.emfcloud.ecore.backend.app;

import java.io.File;
import java.lang.ProcessBuilder;
import org.eclipse.emf.codegen.ecore.Generator;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
import org.eclipse.emf.codegen.ecore.Generator;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

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
