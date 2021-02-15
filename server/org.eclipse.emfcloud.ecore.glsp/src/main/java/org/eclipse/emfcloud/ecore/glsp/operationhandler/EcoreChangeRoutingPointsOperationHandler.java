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
package org.eclipse.emfcloud.ecore.glsp.operationhandler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emfcloud.ecore.enotation.Edge;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelServerAccess;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelState;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.glsp.server.operations.ChangeRoutingPointsOperation;
import org.eclipse.glsp.server.types.ElementAndRoutingPoints;

public class EcoreChangeRoutingPointsOperationHandler
		extends ModelServerAwareBasicOperationHandler<ChangeRoutingPointsOperation> {

	@Override
	public void executeOperation(ChangeRoutingPointsOperation operation, GModelState graphicalModelState,
			EcoreModelServerAccess modelServerAccess) throws Exception {
		EcoreModelState ecoreModelState = EcoreModelState.getModelState(graphicalModelState);
		Map<Edge, ElementAndRoutingPoints> changeRoutingPointsMap = new HashMap<>();
		for (ElementAndRoutingPoints element : operation.getNewRoutingPoints()) {
			ecoreModelState.getIndex().getNotation(element.getElementId(), Edge.class).ifPresent(notationElement -> {
				changeRoutingPointsMap.put(notationElement, element);
			});
		}
		;
		modelServerAccess.setBendPoints(ecoreModelState, changeRoutingPointsMap);
	}

	@Override
	public String getLabel() {
		return "Reroute ecore edge";
	}
}