package org.eclipse.emfcloud.ecore.glsp.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emfcloud.ecore.glsp.EcoreFacade;
import org.eclipse.emfcloud.ecore.glsp.EcoreModelIndex;
import org.eclipse.emfcloud.ecore.glsp.actions.RequestSemanticUriAction;
import org.eclipse.emfcloud.ecore.glsp.actions.SetSemanticUriAction;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelState;
import org.eclipse.glsp.graph.GModelElement;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.actions.BasicActionHandler;
import org.eclipse.glsp.server.model.GModelState;

public class RequestSemanticUriActionHandler extends BasicActionHandler<RequestSemanticUriAction> {

	@Override
	protected List<Action> executeAction(RequestSemanticUriAction action, GModelState modelState) {
		List<Action> actionList = new ArrayList<>();

		EcoreFacade facade = EcoreModelState.getEcoreFacade(modelState);
		EPackage ePackage = facade.getEPackage();

		String modelUri = facade.getSemanticResource().getURI().toFileString();

		if (action.getGraphicElementId().isEmpty()) {
			actionList.add(new SetSemanticUriAction(modelUri, facade.getDiagram().getSemanticElement().getUri(),
					ePackage.eClass().getName()));
		} else {
			EcoreModelIndex modelIndex = EcoreModelState.getModelState(modelState).getIndex();

			Optional<GModelElement> element = modelIndex.findElementByClass(action.getGraphicElementId(),
					GModelElement.class);
			if (element.isPresent()) {
				Optional<EObject> semanticElement = modelIndex.getSemantic(element.get());
				if (semanticElement.isPresent()) {
					String fragment = facade.getSemanticResource().getURIFragment(semanticElement.get());
					actionList.add(
							new SetSemanticUriAction(modelUri, fragment, semanticElement.get().eClass().getName()));
				}
			}

		}

		return actionList.isEmpty() ? none() : actionList;
	}

}
