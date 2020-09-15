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

import java.util.List;

import org.eclipse.emfcloud.ecore.enotation.Edge;
import org.eclipse.emfcloud.ecore.glsp.EcoreModelIndex;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelState;
import org.eclipse.emfcloud.ecore.glsp.util.EcoreEdgeUtil;
import org.eclipse.glsp.api.model.GraphicalModelState;
import org.eclipse.glsp.api.operation.kind.ChangeRoutingPointsOperation;
import org.eclipse.glsp.api.types.ElementAndRoutingPoints;
import org.eclipse.glsp.graph.GPoint;
import org.eclipse.glsp.server.operationhandler.BasicOperationHandler;

public class ChangeRoutingPointsOperationHandler extends BasicOperationHandler<ChangeRoutingPointsOperation> {

    @Override
    public void executeOperation(ChangeRoutingPointsOperation operation, GraphicalModelState modelState) {
        EcoreModelIndex index = EcoreModelState.getModelState(modelState).getIndex();
        rerouteEdge(operation, index);
    }

    private void rerouteEdge(ChangeRoutingPointsOperation operation, EcoreModelIndex index) {
        for (ElementAndRoutingPoints element : operation.getNewRoutingPoints()) {
            index.getNotation(element.getElementId(), Edge.class)
                    .ifPresent(notationElement -> changeEdgePoints(notationElement, element.getNewRoutingPoints()));
        };
    }

    private void changeEdgePoints(Edge element, List<GPoint> points) {
        if (points != null) {
            element.getBendPoints().clear();
            points.forEach(p -> element.getBendPoints().add(EcoreEdgeUtil.copy(p)));
        }
    }

    @Override
    public String getLabel() {
        return "Reroute ecore edge";
    }
}