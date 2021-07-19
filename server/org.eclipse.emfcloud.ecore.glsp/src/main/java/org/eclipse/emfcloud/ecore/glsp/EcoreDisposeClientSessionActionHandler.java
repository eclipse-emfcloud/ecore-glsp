package org.eclipse.emfcloud.ecore.glsp;

import java.util.List;

import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelServerAccess;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelState;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.actions.DisposeClientSessionAction;
import org.eclipse.glsp.server.actions.DisposeClientSessionActionHandler;
import org.eclipse.glsp.server.model.GModelState;

public class EcoreDisposeClientSessionActionHandler extends DisposeClientSessionActionHandler {

	@Override
	protected List<Action> executeAction(final DisposeClientSessionAction action,
			final GModelState graphicalModelState) {
		// Unsubscribe from ModelServer updates
		EcoreModelServerAccess modelServerAccess = EcoreModelState.getModelServerAccess(graphicalModelState);
		modelServerAccess.unsubscribe();
		return super.executeAction(action, graphicalModelState);
	}

}
