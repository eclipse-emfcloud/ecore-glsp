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
import org.eclipse.glsp.graph.GPoint;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.glsp.server.operations.BasicOperationHandler;
import org.eclipse.glsp.server.operations.ChangeRoutingPointsOperation;
import org.eclipse.glsp.server.types.ElementAndRoutingPoints;

public class EcoreChangeRoutingPointsOperationHandler extends BasicOperationHandler<ChangeRoutingPointsOperation> {

    @Override
    public void executeOperation(ChangeRoutingPointsOperation operation, GModelState modelState) {
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
            element.getBendPoints().addAll(points);
        }
    }

    @Override
    public String getLabel() {
        return "Reroute ecore edge";
    }
}