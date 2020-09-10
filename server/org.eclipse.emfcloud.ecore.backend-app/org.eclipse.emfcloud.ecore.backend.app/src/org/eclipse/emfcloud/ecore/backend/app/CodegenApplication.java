package org.eclipse.emfcloud.ecore.backend.app;

import org.eclipse.emf.codegen.ecore.Generator;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class CodegenApplication implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		Generator codegen = new Generator();
		String[] args = getArgs(context);
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
