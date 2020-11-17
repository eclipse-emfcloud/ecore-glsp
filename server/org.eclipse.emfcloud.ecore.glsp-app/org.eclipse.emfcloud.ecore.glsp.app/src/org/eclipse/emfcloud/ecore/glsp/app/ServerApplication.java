package org.eclipse.emfcloud.ecore.glsp.app;

import org.eclipse.emfcloud.ecore.glsp.EcoreServerLauncher;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

/**
 * An application to start the Ecore GLSP Server.
 */
public class ServerApplication implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		String[] args = getArgs(context);
		EcoreServerLauncher.main(args);	
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
		// Nothing
	}

}
