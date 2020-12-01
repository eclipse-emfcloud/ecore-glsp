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

import java.util.Optional;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelServerAccess;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelState;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.glsp.server.operations.DeleteOperation;
import org.eclipse.glsp.server.protocol.GLSPServerException;

public class EcoreDeleteOperationHandler extends ModelServerAwareBasicOperationHandler<DeleteOperation> {

	@Override
	public void executeOperation(final DeleteOperation operation, final GModelState graphicalModelState,
			EcoreModelServerAccess modelAccess) throws Exception {

		EcoreModelState modelState = EcoreModelState.getModelState(graphicalModelState);
		operation.getElementIds().forEach(elementId -> {

			Optional<EObject> semantic = modelState.getIndex().getSemantic(elementId);

			semantic.ifPresent(element -> {
				if (element instanceof EReference) {
					if (!modelAccess.removeEReference(modelState, (EReference) element)) {
						throw new GLSPServerException(
								"Could not execute delete operation on EReference: " + element.toString());
					}
				} else if (element instanceof EClassifier) {
					if (!modelAccess.removeEClassifier(modelState, (EClassifier) element)) {
						throw new GLSPServerException(
								"Could not execute delete operation on EClassifier: " + element.toString());
					}
				} else if (element instanceof EEnumLiteral) {
					if (!modelAccess.removeEEnumLiteral(modelState, (EEnumLiteral) element)) {
						throw new GLSPServerException(
								"Could not execute delete operation on EEnumLiteral: " + element.toString());
					}
				} else if (element instanceof EAttribute) {
					if (!modelAccess.removeEAttribute(modelState, (EAttribute) element)) {
						throw new GLSPServerException(
								"Could not execute delete operation on EAttribute: " + element.toString());
					}
				}

			});
		});

	}

}