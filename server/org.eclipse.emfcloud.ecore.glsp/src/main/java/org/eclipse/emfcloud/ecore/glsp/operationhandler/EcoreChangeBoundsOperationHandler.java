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

import org.eclipse.emfcloud.ecore.enotation.Shape;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelServerAccess;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelState;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.glsp.server.operations.ChangeBoundsOperation;
import org.eclipse.glsp.server.types.ElementAndBounds;

public class EcoreChangeBoundsOperationHandler extends ModelServerAwareBasicOperationHandler<ChangeBoundsOperation> {

	@Override
	public void executeOperation(ChangeBoundsOperation changeBoundsOperation, GModelState graphicalModelState,
			EcoreModelServerAccess modelServerAccess) throws Exception {
		EcoreModelState ecoreModelState = EcoreModelState.getModelState(graphicalModelState);
		Map<Shape, ElementAndBounds> changeBoundsMap = new HashMap<>();
		for (ElementAndBounds element : changeBoundsOperation.getNewBounds()) {
			ecoreModelState.getIndex().getNotation(element.getElementId(), Shape.class).ifPresent(notationElement -> {
				changeBoundsMap.put(notationElement, element);
			});
		}
		modelServerAccess.setBounds(ecoreModelState, changeBoundsMap);
	}

	@Override
	public String getLabel() {
		return "Change bounds";
	}
}
