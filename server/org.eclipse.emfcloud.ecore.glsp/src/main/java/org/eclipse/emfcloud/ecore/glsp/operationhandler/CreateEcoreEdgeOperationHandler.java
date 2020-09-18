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

import static org.eclipse.glsp.api.protocol.GLSPServerException.getOrThrow;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emfcloud.ecore.enotation.Diagram;
import org.eclipse.emfcloud.ecore.enotation.NotationElement;
import org.eclipse.emfcloud.ecore.glsp.EcoreEditorContext;
import org.eclipse.emfcloud.ecore.glsp.EcoreFacade;
import org.eclipse.emfcloud.ecore.glsp.EcoreModelIndex;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelState;
import org.eclipse.emfcloud.ecore.glsp.util.EcoreConfig.Types;
import org.eclipse.glsp.api.model.GraphicalModelState;
import org.eclipse.glsp.api.operation.Operation;
import org.eclipse.glsp.api.operation.kind.CreateEdgeOperation;
import org.eclipse.glsp.graph.GEdge;
import org.eclipse.glsp.server.operationhandler.BasicOperationHandler;

import com.google.common.collect.Lists;

public class CreateEcoreEdgeOperationHandler extends BasicOperationHandler<CreateEdgeOperation> {
	private List<String> handledElementTypeIds = Lists.newArrayList(Types.REFERENCE, Types.COMPOSITION,
			Types.INHERITANCE, Types.BIDIRECTIONAL_REFERENCE, Types.BIDIRECTIONAL_COMPOSITION);

	@Override
	public boolean handles(Operation operation) {
		if (operation instanceof CreateEdgeOperation) {
			CreateEdgeOperation connectAction = (CreateEdgeOperation) operation;
			return this.handledElementTypeIds.contains(connectAction.getElementTypeId());
		}
		return false;
	}

	@Override
	public void executeOperation(CreateEdgeOperation operation, GraphicalModelState modelState) {
		String elementTypeId = operation.getElementTypeId();

		EcoreEditorContext context = EcoreModelState.getEditorContext(modelState);
		EcoreModelIndex modelIndex = context.getModelState().getIndex();
		EcoreFacade facade = context.getEcoreFacade();
		EClass sourceEclass = getOrThrow(modelIndex.getSemantic(operation.getSourceElementId(), EClass.class),
				"No semantic EClass found for source element with id " + operation.getSourceElementId());
		EClass targetEClass = getOrThrow(modelIndex.getSemantic(operation.getTargetElementId(), EClass.class),
				"No semantic EClass found for target element with id" + operation.getTargetElementId());

		Diagram diagram = facade.getDiagram();

		if (elementTypeId.equals(Types.INHERITANCE)) {
			sourceEclass.getESuperTypes().add(targetEClass);
		} else {
			EReference reference = createReference(sourceEclass, targetEClass,
					elementTypeId.equals(Types.BIDIRECTIONAL_COMPOSITION) ? Types.COMPOSITION : elementTypeId);

			if (elementTypeId.equals(Types.BIDIRECTIONAL_REFERENCE)
					|| elementTypeId.equals(Types.BIDIRECTIONAL_COMPOSITION)) {
				EReference opposite = createReference(targetEClass, sourceEclass, elementTypeId);
				reference.setEOpposite(opposite);
				opposite.setEOpposite(reference);

				if (elementTypeId.equals(Types.BIDIRECTIONAL_REFERENCE)) {
					NotationElement sourceNotationElement = modelIndex.getNotation(sourceEclass).get();
					NotationElement targeNotationElement = modelIndex.getNotation(targetEClass).get();

					for (NotationElement element : diagram.getElements()) {
						if (element.equals(sourceNotationElement)) {
							break;
						}
						if (element.equals(targeNotationElement)) {
							reference = reference.getEOpposite();
							break;
						}
					}
				}
			}
			GEdge edge = getOrThrow(context.getGModelFactory().create(reference, GEdge.class),
					" No viewmodel factory found for element: " + reference);
			diagram.getElements().add(facade.initializeEdge(reference, edge));
		}
	}

	private EReference createReference(EClass source, EClass target, String elementTypeId) {
		EReference reference = EcoreFactory.eINSTANCE.createEReference();
		reference.setEType(target);
		reference.setName(target.getName().toLowerCase() + "s");
		if (elementTypeId.equals(Types.COMPOSITION)) {
			reference.setContainment(true);
		}
		source.getEStructuralFeatures().add(reference);
		return reference;

	}

	@Override
	public String getLabel() {
		return "Create ecore edge";
	}

}
