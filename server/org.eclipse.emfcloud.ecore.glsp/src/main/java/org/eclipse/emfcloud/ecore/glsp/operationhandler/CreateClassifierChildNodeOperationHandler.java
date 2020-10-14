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

import static org.eclipse.glsp.server.protocol.GLSPServerException.getOrThrow;

import java.util.List;
import java.util.function.Function;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelServerAccess;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelState;
import org.eclipse.emfcloud.ecore.glsp.util.EcoreConfig.Types;
import org.eclipse.glsp.graph.GraphPackage;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.glsp.server.operations.CreateNodeOperation;
import org.eclipse.glsp.server.operations.Operation;

public class CreateClassifierChildNodeOperationHandler
		extends ModelServerAwareBasicOperationHandler<CreateNodeOperation> {

	private List<String> handledElementTypeIds = List.of(Types.ATTRIBUTE, Types.OPERATION, Types.ENUMLITERAL);

	@Override
	public boolean handles(Operation operationAction) {
		if (operationAction instanceof CreateNodeOperation) {
			CreateNodeOperation action = (CreateNodeOperation) operationAction;
			return handledElementTypeIds.contains(action.getElementTypeId());
		}
		return false;
	}

	@Override
	public void executeOperation(CreateNodeOperation operation, GModelState graphicalModelState,
			EcoreModelServerAccess modelAccess) throws Exception {
		EcoreModelState modelState = EcoreModelState.getModelState(graphicalModelState);
		EClassifier container = getOrThrow(modelState.getIndex().getSemantic(operation.getContainerId()),
				EClassifier.class, "No valid container with id " + operation.getContainerId() + " found");
		String elementTypeId = operation.getElementTypeId();
		if (elementTypeId.equals(Types.ATTRIBUTE) && container instanceof EClass) {
			EAttribute attribute = createEAttribute(modelState);
			modelState.getIndex().add(attribute);
			((EClass) container).getEStructuralFeatures().add(attribute);
		} else if (elementTypeId.contentEquals(Types.ENUMLITERAL) && container instanceof EEnum) {
			EEnumLiteral literal = createEEnumLiteral(modelState);
			modelState.getIndex().add(literal);
			((EEnum) container).getELiterals().add(literal);
		}

	}

	protected int setName(ENamedElement namedElement, EcoreModelState modelState) {
		Function<Integer, String> nameProvider = i -> "New" + namedElement.eClass().getName() + i;
		int nodeCounter = modelState.getIndex().getCounter(GraphPackage.Literals.GLABEL, nameProvider);
		namedElement.setName(nameProvider.apply(nodeCounter));
		return nodeCounter;
	}

	protected EEnumLiteral createEEnumLiteral(EcoreModelState modelState) {
		EEnumLiteral literal = EcoreFactory.eINSTANCE.createEEnumLiteral();
		int counter = setName(literal, modelState);
		literal.setValue(counter);
		return literal;

	}

	protected EAttribute createEAttribute(EcoreModelState modelState) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		setName(attribute, modelState);
		attribute.setEType(EcorePackage.eINSTANCE.getEString());
		return attribute;
	}

	@Override
	public String getLabel() {
		return "Create EClassifier child node";
	}

}
