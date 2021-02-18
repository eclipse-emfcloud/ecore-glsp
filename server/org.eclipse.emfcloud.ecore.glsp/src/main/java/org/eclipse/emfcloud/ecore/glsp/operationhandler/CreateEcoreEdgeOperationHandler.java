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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emfcloud.ecore.enotation.Edge;
import org.eclipse.emfcloud.ecore.enotation.EnotationFactory;
import org.eclipse.emfcloud.ecore.enotation.NotationElement;
import org.eclipse.emfcloud.ecore.enotation.SemanticProxy;
import org.eclipse.emfcloud.ecore.glsp.EcoreEditorContext;
import org.eclipse.emfcloud.ecore.glsp.EcoreModelIndex;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelServerAccess;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelState;
import org.eclipse.emfcloud.ecore.glsp.util.EcoreConfig.Types;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.glsp.server.operations.CreateEdgeOperation;
import org.eclipse.glsp.server.operations.Operation;

import com.google.common.collect.Lists;

public class CreateEcoreEdgeOperationHandler extends ModelServerAwareBasicOperationHandler<CreateEdgeOperation> {
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
	public void executeOperation(CreateEdgeOperation operation, GModelState modelState,
			EcoreModelServerAccess modelAccess) throws Exception {
		String elementTypeId = operation.getElementTypeId();

		EcoreEditorContext context = EcoreModelState.getEditorContext(modelState);
		EcoreModelIndex modelIndex = context.getModelState().getIndex();

		EClass sourceEClass = getOrThrow(modelIndex.getSemantic(operation.getSourceElementId(), EClass.class),
				"No semantic EClass found for source element with id " + operation.getSourceElementId());
		EClass targetEClass = getOrThrow(modelIndex.getSemantic(operation.getTargetElementId(), EClass.class),
				"No semantic EClass found for target element with id" + operation.getTargetElementId());

		NotationElement sourceElement = getOrThrow(modelIndex.getNotation(operation.getSourceElementId()),
				"No NotationElement found for source element with id " + operation.getSourceElementId());
		NotationElement targetElement = getOrThrow(modelIndex.getNotation(operation.getTargetElementId()),
				"No NotationElement found for target element with id" + operation.getTargetElementId());

		if (elementTypeId.equals(Types.INHERITANCE)) {
			Edge inheritanceEdge = createInheritanceEdge(sourceElement, targetElement);
			modelAccess.addESuperType(EcoreModelState.getModelState(modelState), targetEClass, sourceEClass,
					inheritanceEdge);
		} else if (elementTypeId.equals(Types.REFERENCE) || elementTypeId.equals(Types.COMPOSITION)) {
			EReference reference = createReference(sourceEClass, targetEClass, elementTypeId);
			Edge referenceEdge = createEdge(sourceEClass, reference, sourceElement, targetElement);
			modelAccess.addEReference(EcoreModelState.getModelState(modelState), reference, sourceEClass,
					referenceEdge);
		} else if (elementTypeId.equals(Types.BIDIRECTIONAL_REFERENCE)
				|| elementTypeId.equals(Types.BIDIRECTIONAL_COMPOSITION)) {

			EReference reference = createReference(sourceEClass, targetEClass, elementTypeId);
			Edge referenceEdge = createEdge(sourceEClass, reference, sourceElement, targetElement);
			EReference opposite = createReference(targetEClass, sourceEClass, elementTypeId);
			Edge oppositeEdge = createEdge(targetEClass, opposite, targetElement, sourceElement);

			modelAccess.addEReferenceBidirectional(EcoreModelState.getModelState(modelState), reference, opposite,
					sourceEClass, targetEClass, referenceEdge, oppositeEdge,
					elementTypeId.equals(Types.BIDIRECTIONAL_REFERENCE));
		}
	}

	protected Edge createInheritanceEdge(NotationElement sourceElement, NotationElement targetElement) {
		Edge inheritanceEdge = EnotationFactory.eINSTANCE.createEdge();
		// As we cannot set a proper SemanticProxy for an inheritance edge, we skip it
		// and set the type instead
		inheritanceEdge.setType(Types.INHERITANCE);
		inheritanceEdge.setSource(sourceElement);
		inheritanceEdge.setTarget(targetElement);
		return inheritanceEdge;
	}

	protected String getSemanticProxyUri(EClass source, EReference eReference) {
		String sourceUri = EcoreUtil.getURI(source).fragment();
		return sourceUri + "/" + eReference.getName();
	}

	protected EReference createReference(EClass source, EClass target, String elementTypeId) {
		EReference reference = EcoreFactory.eINSTANCE.createEReference();
		reference.setEType(target);
		reference.setName(target.getName().toLowerCase() + "s");
		if (elementTypeId.equals(Types.COMPOSITION) || elementTypeId.equals(Types.BIDIRECTIONAL_COMPOSITION)) {
			reference.setContainment(true);
		}
		return reference;
	}

	protected Edge createEdge(EClass source, EReference eReference, NotationElement sourceElement,
			NotationElement targetElement) {
		Edge edge = EnotationFactory.eINSTANCE.createEdge();
		// As we are able to set a SemanticProxy, we do not need to set an edge type
		// here
		SemanticProxy proxy = EnotationFactory.eINSTANCE.createSemanticProxy();
		proxy.setUri(getSemanticProxyUri(source, eReference));
		edge.setSemanticElement(proxy);
		edge.setSource(sourceElement);
		edge.setTarget(targetElement);
		return edge;
	}

	@Override
	public String getLabel() {
		return "Create ecore edge";
	}

}
