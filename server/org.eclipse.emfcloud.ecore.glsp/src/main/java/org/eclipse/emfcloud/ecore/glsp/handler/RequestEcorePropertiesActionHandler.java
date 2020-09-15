package org.eclipse.emfcloud.ecore.glsp.handler;

import static org.eclipse.glsp.api.jsonrpc.GLSPServerException.getOrThrow;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emfcloud.ecore.glsp.EcoreFacade;
import org.eclipse.emfcloud.ecore.glsp.EcoreModelIndex;
import org.eclipse.emfcloud.ecore.glsp.actions.RequestEcorePropertiesAction;
import org.eclipse.emfcloud.ecore.glsp.actions.SetEcorePropertiesAction;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelState;
import org.eclipse.emfcloud.ecore.glsp.properties.EcoreProperties;
import org.eclipse.glsp.api.action.Action;
import org.eclipse.glsp.api.model.GraphicalModelState;
import org.eclipse.glsp.graph.GNode;
import org.eclipse.glsp.server.actionhandler.BasicActionHandler;

public class RequestEcorePropertiesActionHandler extends BasicActionHandler<RequestEcorePropertiesAction> {

	@Override
	protected List<Action> executeAction(RequestEcorePropertiesAction action, GraphicalModelState modelState) {
		List<Action> actionList = new ArrayList<>();
		System.out.println(action.getGraphicElementId());

		EcoreFacade facade = EcoreModelState.getEcoreFacade(modelState);
		EPackage ePackage = facade.getEPackage();

		if (action.getGraphicElementId().isEmpty()) {
			EcoreProperties props = new EcoreProperties(action.getGraphicElementId(), ePackage.getName(), ePackage.getNsPrefix(),
					ePackage.getNsURI());
			actionList.addAll(listOf(new SetEcorePropertiesAction(props)));
		} else {
			EcoreModelIndex modelIndex = EcoreModelState.getModelState(modelState).getIndex();

			GNode node = getOrThrow(modelIndex.findElementByClass(action.getGraphicElementId(), GNode.class),
					"No parent Node for element with id " + action.getGraphicElementId() + " found");

			EObject semanticElement = getOrThrow(modelIndex.getSemantic(node),
					"No semantic element for labelContainer with id " + node.getId() + " found");

			EcoreProperties props = new EcoreProperties(
					action.getGraphicElementId(),
					((EClass) semanticElement).getName(),
					((EClass) semanticElement).getInstanceClassName(), ((EClass) semanticElement).getInstanceTypeName(),
					((EClass) semanticElement).isAbstract(), ((EClass) semanticElement).isInterface(),
					((EClass) semanticElement).getESuperTypes().size() > 0);
			actionList.addAll(listOf(new SetEcorePropertiesAction(props)));
		}

		return actionList.isEmpty() ? none() : actionList;
	}

}
