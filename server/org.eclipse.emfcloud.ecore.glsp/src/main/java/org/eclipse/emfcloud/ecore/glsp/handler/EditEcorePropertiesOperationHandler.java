package org.eclipse.emfcloud.ecore.glsp.handler;

import static org.eclipse.glsp.api.jsonrpc.GLSPServerException.getOrThrow;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emfcloud.ecore.enotation.Shape;
import org.eclipse.emfcloud.ecore.glsp.EcoreFacade;
import org.eclipse.emfcloud.ecore.glsp.EcoreModelIndex;
import org.eclipse.emfcloud.ecore.glsp.actions.EditEcorePropertiesOperation;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelState;
import org.eclipse.glsp.api.model.GraphicalModelState;
import org.eclipse.glsp.graph.GNode;
import org.eclipse.glsp.server.operationhandler.BasicOperationHandler;

public class EditEcorePropertiesOperationHandler extends BasicOperationHandler<EditEcorePropertiesOperation> {

	@Override
	protected void executeOperation(EditEcorePropertiesOperation operation, GraphicalModelState modelState) {
		String packageName = operation.getProperties().getName();
		System.out.println("Edited this packages name! " + operation.getProperties().getName());

		EcoreFacade facade = EcoreModelState.getEcoreFacade(modelState);
		EPackage ePackage = facade.getEPackage();
		ePackage.setName(packageName);

		EcoreModelIndex modelIndex = EcoreModelState.getModelState(modelState).getIndex();
		GNode node = getOrThrow(modelIndex.findElementByClass(operation.getProperties().getGraphicId(), GNode.class),
				"No parent Node for element with id " + operation.getProperties().getGraphicId() + " found");

		EObject semanticElement = getOrThrow(modelIndex.getSemantic(node),
				"No semantic element for labelContainer with id " + node.getId() + " found");

		Shape shape = getOrThrow(modelIndex.getNotation(semanticElement), Shape.class,
				"No shape element for label with id " + operation.getProperties().getName() + " found");

		if (semanticElement instanceof EClassifier) {
			((EClassifier) semanticElement).setName(operation.getProperties().getName().trim());
			// nameChange== uri change so we have to recreate the proxy here
			shape.setSemanticElement(facade.createProxy(semanticElement));
		}
	}

}
