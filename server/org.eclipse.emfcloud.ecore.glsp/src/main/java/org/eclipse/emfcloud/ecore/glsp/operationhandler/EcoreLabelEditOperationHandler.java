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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emfcloud.ecore.glsp.EcoreModelIndex;
import org.eclipse.emfcloud.ecore.glsp.ResourceManager;
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
		Optional<String> type = modelIndex.findElementByClass(editLabelOperation.getLabelId(), GModelElement.class)
				.map(e -> e.getType());
		if (type.isPresent()) {
			switch (type.get()) {
			case Types.LABEL_NAME:
				modelIndex.findElementByClass(editLabelOperation.getLabelId(), GNode.class)
						.ifPresentOrElse(notationElement -> {
							modelIndex.getSemantic(notationElement).ifPresentOrElse(semanticElement -> {
								if (semanticElement instanceof EClassifier) {
									if (!modelAccess.setName(EcoreModelState.getModelState(graphicalModelState),
											(EClassifier) semanticElement, editLabelOperation.getText().trim())) {
										throw new GLSPServerException(
												"Could not rename node to: " + editLabelOperation.getText().trim());
									}
								}
							}, () -> new GLSPServerException("No semantic element for labelContainer with id "
									+ notationElement.getId() + " found"));
						}, () -> new GLSPServerException(
								"No parent Node for element with id " + editLabelOperation.getLabelId() + " found"));

				break;

			case Types.LABEL_INSTANCE:
				modelIndex.findElementByClass(editLabelOperation.getLabelId(), GNode.class)
						.ifPresentOrElse(notationElement -> {
							modelIndex.getSemantic(notationElement).ifPresentOrElse(semanticElement -> {
								if (semanticElement instanceof EClassifier) {
									if (!modelAccess.setInstanceName(EcoreModelState.getModelState(graphicalModelState),
											(EClassifier) semanticElement, editLabelOperation.getText().trim())) {
										throw new GLSPServerException(
												"Could not rename node to: " + editLabelOperation.getText().trim());
									}
								}
							}, () -> new GLSPServerException("No semantic element for labelContainer with id "
									+ notationElement.getId() + " found"));
						}, () -> new GLSPServerException(
								"No parent Node for element with id " + editLabelOperation.getLabelId() + " found"));
				break;

			case Types.ATTRIBUTE:
				EAttribute attribute_semantic = (EAttribute) getOrThrow(
						modelIndex.getSemantic(editLabelOperation.getLabelId()),
						"No semantic element for label with id " + editLabelOperation.getLabelId() + " found");

				String inputText = editLabelOperation.getText();
				String attributeName;
				if (inputText.contains(":")) {
					String[] split = inputText.split(":");
					attributeName = split[0].trim();

					Optional<EClassifier> datatype = parseStringToEType(split[1].trim(),
							EcoreModelState.getResourceManager(graphicalModelState));
					if (datatype.isPresent()) {
						if (!modelAccess.setAttributeType(EcoreModelState.getModelState(graphicalModelState),
								attribute_semantic, datatype.get())) {
							throw new GLSPServerException("Could not change type to: " + datatype.get().toString());
						}
					}
				} else {
					attributeName = inputText.trim();
				}
				if (!inputText.isEmpty()) {
					if (!modelAccess.setAttributeName(EcoreModelState.getModelState(graphicalModelState),
							attribute_semantic, attributeName)) {
						throw new GLSPServerException(
								"Could not rename node to: " + editLabelOperation.getText().trim());
					}
				}
				break;

			case Types.ENUMLITERAL:
				EEnumLiteral literal_semantic = (EEnumLiteral) getOrThrow(
						modelIndex.getSemantic(editLabelOperation.getLabelId()),
						"No semantic element for label with id " + editLabelOperation.getLabelId() + " found");
				String text = editLabelOperation.getText().trim();
				if (!text.isEmpty()) {
					if (!modelAccess.setLiteralName(EcoreModelState.getModelState(graphicalModelState),
							literal_semantic, editLabelOperation.getText().trim())) {
						throw new GLSPServerException(
								"Could not rename node to: " + editLabelOperation.getText().trim());
					}
				}
				break;

			case Types.LABEL_EDGE_NAME:
				modelIndex.findElementByClass(editLabelOperation.getLabelId(), GEdge.class)
						.ifPresentOrElse(notationElement -> {
							modelIndex.getSemantic(notationElement).ifPresentOrElse(semanticElement -> {
								if (semanticElement instanceof EReference) {
									if (!modelAccess.setEdgeName(EcoreModelState.getModelState(graphicalModelState),
											(EReference) semanticElement, editLabelOperation.getText().trim())) {
										throw new GLSPServerException(
												"Could not rename edge to: " + editLabelOperation.getText().trim());
									}
								}
							}, () -> new GLSPServerException("No semantic element for labelContainer with id "
									+ notationElement.getId() + " found"));
						}, () -> new GLSPServerException("No semantic element for labelContainer with id "
								+ editLabelOperation.getLabelId() + " found"));

				break;

			case Types.LABEL_EDGE_MULTIPLICITY:
				String edgeId = EcoreEdgeUtil.getEdgeId(editLabelOperation.getLabelId());
				EReference reference_semantic = (EReference) getOrThrow(modelIndex.getSemantic(edgeId),
						"No semantic element for labelContainer with id " + edgeId + " found");
				Pattern pattern = Pattern.compile("\\s*\\[\\s*(\\d+)\\s*\\.+\\s*(\\*|\\d+|\\-1)\\s*\\]\\s*");
				Matcher matcher = pattern.matcher(editLabelOperation.getText());
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
					throw new GLSPServerException("Multiplicity of reference with id " + editLabelOperation.getLabelId()
							+ " has a wrong input format", new IllegalArgumentException());
				}
				break;
			}
		}
	}

	private Optional<EClassifier> parseStringToEType(String name, ResourceManager resManager) {
		for (EClassifier type : getAllEAttributeTypes(resManager)) {
			if (type.getName().toLowerCase().equals(name.toLowerCase())) {
				return Optional.ofNullable(type);
			}
		}
		return Optional.empty();
	}

	public static List<EClassifier> getAllEAttributeTypes(ResourceManager resManager) {
		List<EClassifier> listOfTypes = new ArrayList<>(EcorePackage.eINSTANCE.getEClassifiers());
		listOfTypes.removeIf(e -> !(e instanceof EDataType));
		TreeIterator<Notifier> resourceSetContent = resManager.getEditingDomain().getResourceSet().getAllContents();
		while (resourceSetContent.hasNext()) {
			Notifier res = resourceSetContent.next();
			if (res instanceof EDataType) {
				listOfTypes.add((EClassifier) res);
			}
		}
		return listOfTypes;
	}

	@Override
	public String getLabel() {
		return "Apply label";
	}
}
