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

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emfcloud.ecore.glsp.EcoreModelIndex;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelServerAccess;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelState;
import org.eclipse.emfcloud.ecore.glsp.util.EcoreConfig.Types;
import org.eclipse.emfcloud.ecore.glsp.util.EcoreEdgeUtil;
import org.eclipse.glsp.graph.GEdge;
import org.eclipse.glsp.graph.GModelElement;
import org.eclipse.glsp.graph.GNode;
import org.eclipse.glsp.server.features.directediting.ApplyLabelEditOperation;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.glsp.server.protocol.GLSPServerException;

public class EcoreLabelEditOperationHandler extends ModelServerAwareBasicOperationHandler<ApplyLabelEditOperation> {

	@Override
	public void executeOperation(ApplyLabelEditOperation editLabelOperation, GModelState graphicalModelState,
			EcoreModelServerAccess modelAccess) throws Exception {
		EcoreModelIndex modelIndex = EcoreModelState.getModelState(graphicalModelState).getIndex();

		String inputText = editLabelOperation.getText().trim();
		String elementId = editLabelOperation.getLabelId();

		Optional<String> type = modelIndex.findElementByClass(elementId, GModelElement.class).map(e -> e.getType());

		if (type.isPresent()) {
			switch (type.get()) {
			case Types.LABEL_NAME:
				modelIndex.findElementByClass(elementId, GNode.class).ifPresentOrElse(notationElement -> {
					modelIndex.getSemantic(notationElement).ifPresentOrElse(semanticElement -> {
						if (semanticElement instanceof EClassifier) {
							if (!modelAccess.setName(EcoreModelState.getModelState(graphicalModelState),
									(EClassifier) semanticElement, inputText)) {
								throw new GLSPServerException("Could not rename node to: " + inputText);
							}
						}
					}, () -> new GLSPServerException(
							"No semantic element for labelContainer with id " + notationElement.getId() + " found"));
				}, () -> new GLSPServerException("No parent Node for element with id " + elementId + " found"));

				break;

			case Types.LABEL_INSTANCE:
				modelIndex.findElementByClass(elementId, GNode.class).ifPresentOrElse(notationElement -> {
					modelIndex.getSemantic(notationElement).ifPresentOrElse(semanticElement -> {
						if (semanticElement instanceof EClassifier) {
							if (!modelAccess.setInstanceName(EcoreModelState.getModelState(graphicalModelState),
									(EClassifier) semanticElement, inputText)) {
								throw new GLSPServerException("Could not rename node to: " + inputText);
							}
						}
					}, () -> new GLSPServerException(
							"No semantic element for labelContainer with id " + notationElement.getId() + " found"));
				}, () -> new GLSPServerException("No parent Node for element with id " + elementId + " found"));

				break;

			case Types.ENUMLITERAL:
				EEnumLiteral eEnumLiteral = (EEnumLiteral) getOrThrow(modelIndex.getSemantic(elementId),
						"No semantic element for label with id " + elementId + " found");
				String newLiteralName = inputText;
				if (!newLiteralName.isEmpty()) {
					if (!modelAccess.setLiteralName(EcoreModelState.getModelState(graphicalModelState), eEnumLiteral,
							newLiteralName)) {
						throw new GLSPServerException("Could not rename node to: " + newLiteralName);
					}
				}

				break;

			case Types.ATTRIBUTE:
				EAttribute eAttribute = (EAttribute) getOrThrow(modelIndex.getSemantic(elementId),
						"No semantic element for label with id " + elementId + " found");

				String eAttributeName = getNameFromInput(inputText);
				EDataType eAttributeEType = getEDataTypeFromInput(inputText, graphicalModelState);

				if (!modelAccess.setAttribute(EcoreModelState.getModelState(graphicalModelState), eAttribute,
						eAttribute.getName().equals(eAttributeName) ? null : eAttributeName, eAttributeEType)) {
					throw new GLSPServerException("Could not rename attribute to: " + inputText);
				}

				break;

			case Types.OPERATION:
				EOperation eOperation = (EOperation) getOrThrow(modelIndex.getSemantic(elementId),
						"No semantic element for label with id " + elementId + " found");

				String eOperationName = getNameFromInput(inputText, "\\(.*\\)");
				EDataType eOperationEType = getEDataTypeFromInput(inputText, graphicalModelState);

				if (!modelAccess.setOperation(EcoreModelState.getModelState(graphicalModelState), eOperation,
						eOperation.getName().equals(eOperationName) ? null : eOperationName, eOperationEType)) {
					throw new GLSPServerException("Could not rename operation to: " + inputText);
				}

				break;

			case Types.LABEL_EDGE_NAME:
				modelIndex.findElementByClass(elementId, GEdge.class).ifPresentOrElse(notationElement -> {
					modelIndex.getSemantic(notationElement).ifPresentOrElse(semanticElement -> {
						if (semanticElement instanceof EReference) {
							if (!modelAccess.setEdgeName(EcoreModelState.getModelState(graphicalModelState),
									(EReference) semanticElement, inputText)) {
								throw new GLSPServerException("Could not rename edge to: " + inputText);
							}
						}
					}, () -> new GLSPServerException(
							"No semantic element for labelContainer with id " + notationElement.getId() + " found"));
				}, () -> new GLSPServerException(
						"No semantic element for labelContainer with id " + elementId + " found"));

				break;

			case Types.LABEL_EDGE_MULTIPLICITY:
				String edgeId = EcoreEdgeUtil.getEdgeId(elementId);
				EReference reference_semantic = (EReference) getOrThrow(modelIndex.getSemantic(edgeId),
						"No semantic element for labelContainer with id " + edgeId + " found");
				Pattern pattern = Pattern.compile("\\s*\\[\\s*(\\d+)\\s*\\.+\\s*(\\*|\\d+|\\-1)\\s*\\]\\s*");
				Matcher matcher = pattern.matcher(inputText);
				if (matcher.matches()) {
					String lowerBound = matcher.group(1);
					String upperBound = matcher.group(2);
					int lower = (lowerBound.equals("*")) ? -1 : Integer.valueOf(lowerBound);
					int upper = (upperBound.equals("*")) ? -1 : Integer.valueOf(upperBound);
					if (!modelAccess.setLowerMultiplicity(EcoreModelState.getModelState(graphicalModelState),
							reference_semantic, lower)) {
						throw new GLSPServerException("Could not change lowerBound to: " + lower);
					}
					if (!modelAccess.setUpperMultiplicity(EcoreModelState.getModelState(graphicalModelState),
							reference_semantic, upper)) {
						throw new GLSPServerException("Could not rename edge to: " + upper);
					}
				} else {
					throw new GLSPServerException(
							"Multiplicity of reference with id " + elementId + " has a wrong input format",
							new IllegalArgumentException());
				}

				break;
			}
		}
	}

	private String getNameFromInput(String inputText) {
		return getNameFromInput(inputText, "");
	}

	private String getNameFromInput(String inputText, String replaceRegex) {
		String name = "";
		if (inputText.contains(":") && inputText.split(":").length >= 2) {
			String[] split = inputText.split(":");
			name = split[0].trim();
		} else {
			name = inputText.trim().replace(":", "");
		}
		return name.replaceAll(replaceRegex, "");
	}

	private EDataType getEDataTypeFromInput(String inputText, GModelState graphicalModelState) {
		if (inputText.contains(":") && inputText.split(":").length >= 2) {
			String[] split = inputText.split(":");
			String eDataTypeName = split[1].trim();
			Optional<EDataType> datatype = EcoreModelState.getResourceManager(graphicalModelState)
					.getETypeFromString(eDataTypeName);
			if (datatype.isPresent()) {
				return datatype.get();
			}
		}
		return null;
	}

	@Override
	public String getLabel() {
		return "Apply label";
	}
}
