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

import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelState;
import org.eclipse.glsp.graph.GModelRoot;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.actions.ActionDispatcher;
import org.eclipse.glsp.server.actions.ActionMessage;
import org.eclipse.glsp.server.features.core.model.ComputedBoundsAction;
import org.eclipse.glsp.server.features.core.model.ComputedBoundsActionHandler;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.glsp.server.operations.LayoutOperation;
import org.eclipse.glsp.server.utils.LayoutUtil;

import com.google.inject.Inject;

public class EcoreComputedBoundsActionHandler extends ComputedBoundsActionHandler {

	@Inject
	private ActionDispatcher actionDispatcher;

	@Override
	public List<Action> executeAction(ComputedBoundsAction computedBoundsAction, GModelState graphicalModelState) {

		EcoreModelState ecoreModelState = EcoreModelState.getModelState(graphicalModelState);

		synchronized (submissionHandler.getModelLock()) {
			GModelRoot model = ecoreModelState.getRoot();
			if (model != null && model.getRevision() == computedBoundsAction.getRevision()) {
				LayoutUtil.applyBounds(model, computedBoundsAction, graphicalModelState);
				if (ecoreModelState.getEditorContext().getEcoreFacade().diagramNeedsAutoLayout()) {
					ActionMessage layoutMessage = new ActionMessage(ecoreModelState.getClientId(),
							new LayoutOperation());
					actionDispatcher.dispatch(layoutMessage);
				}
				return submissionHandler.submitModel(ecoreModelState);
			}
		}

		return none();

	}

}
