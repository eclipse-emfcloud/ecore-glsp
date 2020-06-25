/********************************************************************************
 * Copyright (c) 2019-2020 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ********************************************************************************/
package org.eclipse.emfcloud.ecore.glsp.handler;

import java.util.List;

import org.eclipse.emfcloud.ecore.enotation.Shape;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelState;
import org.eclipse.glsp.api.action.Action;
import org.eclipse.glsp.api.action.ActionMessage;
import org.eclipse.glsp.api.action.ActionProcessor;
import org.eclipse.glsp.api.action.kind.ComputedBoundsAction;
import org.eclipse.glsp.api.model.GraphicalModelState;
import org.eclipse.glsp.api.operation.kind.LayoutOperation;
import org.eclipse.glsp.api.types.ElementAndBounds;
import org.eclipse.glsp.api.utils.LayoutUtil;
import org.eclipse.glsp.graph.GDimension;
import org.eclipse.glsp.graph.GModelRoot;
import org.eclipse.glsp.graph.GPoint;
import org.eclipse.glsp.server.actionhandler.ComputedBoundsActionHandler;

import com.google.inject.Inject;

public class EcoreComputedBoundsActionHandler extends ComputedBoundsActionHandler {

	@Inject
	private ActionProcessor actionProcessor;

	@Override
	public List<Action> executeAction(ComputedBoundsAction computedBoundsAction, GraphicalModelState graphicalModelState) {
		EcoreModelState modelState = EcoreModelState.getModelState(graphicalModelState);

		for (ElementAndBounds element : computedBoundsAction.getBounds()) {
			modelState.getIndex().getNotation(element.getElementId(), Shape.class)
					.ifPresent(notationElement -> changeElementBounds(notationElement, element.getNewSize(),
							element.getNewPosition()));
		}
		synchronized (submissionHandler.getModelLock()) {
			GModelRoot model = modelState.getRoot();
			if (model != null && model.getRevision() == computedBoundsAction.getRevision()) {
				LayoutUtil.applyBounds(model, computedBoundsAction, graphicalModelState);
				if (modelState.getEditorContext().getEcoreFacade().diagramNeedsAutoLayout()) {
					ActionMessage layoutMessage = new ActionMessage(modelState.getClientId(), new LayoutOperation());
					actionProcessor.process(layoutMessage);
				}
				return submissionHandler.doSubmitModel(true, modelState);
			}
		}
		return List.of();

	}

	private void changeElementBounds(Shape element, GDimension dimension, GPoint position) {
		if (position != null) {
			element.setPosition(position);
		}
		if (dimension != null) {
			element.setSize(dimension);
		}
	}
}
