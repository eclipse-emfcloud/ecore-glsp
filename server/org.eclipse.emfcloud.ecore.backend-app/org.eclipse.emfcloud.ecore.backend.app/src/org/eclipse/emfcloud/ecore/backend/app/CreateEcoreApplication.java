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
        createEcore(args[0], args[1], args[2], Paths.get(args[3], args[0]+".ecore"));
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
