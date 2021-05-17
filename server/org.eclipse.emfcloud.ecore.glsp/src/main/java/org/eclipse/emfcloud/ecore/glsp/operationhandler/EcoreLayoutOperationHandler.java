package org.eclipse.emfcloud.ecore.glsp.operationhandler;

import org.eclipse.emfcloud.ecore.glsp.EcoreLayoutEngine;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelServerAccess;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelState;
import org.eclipse.glsp.graph.GModelElement;
import org.eclipse.glsp.server.diagram.DiagramConfigurationRegistry;
import org.eclipse.glsp.server.features.core.model.ModelSubmissionHandler;
import org.eclipse.glsp.server.layout.ILayoutEngine;
import org.eclipse.glsp.server.layout.ServerLayoutKind;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.glsp.server.operations.LayoutOperation;

import com.google.inject.Inject;

public class EcoreLayoutOperationHandler extends ModelServerAwareBasicOperationHandler<LayoutOperation> {

	@Inject
	protected ILayoutEngine layoutEngine;
	@Inject
	protected ModelSubmissionHandler modelSubmissionHandler;
	@Inject
	protected DiagramConfigurationRegistry diagramConfigurationRegistry;

	@Override
	public void executeOperation(LayoutOperation operation, GModelState graphicalModelState,
			EcoreModelServerAccess modelServerAccess) throws Exception {

		if (diagramConfigurationRegistry.get(graphicalModelState).getLayoutKind() == ServerLayoutKind.MANUAL) {
			if (layoutEngine != null && layoutEngine instanceof EcoreLayoutEngine) {
				GModelElement layoutedRoot = ((EcoreLayoutEngine) layoutEngine).layoutRoot(graphicalModelState);
				modelServerAccess.setLayout(EcoreModelState.getModelState(graphicalModelState), layoutedRoot);
			}
		}
	}

}
