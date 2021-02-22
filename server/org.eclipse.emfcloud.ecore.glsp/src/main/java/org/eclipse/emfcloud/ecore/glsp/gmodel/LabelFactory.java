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
package org.eclipse.emfcloud.ecore.glsp.gmodel;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelState;
import org.eclipse.emfcloud.ecore.glsp.util.EcoreConfig.Types;
import org.eclipse.glsp.graph.GLabel;
import org.eclipse.glsp.graph.builder.impl.GLabelBuilder;

public class LabelFactory extends AbstractGModelFactory<ENamedElement, GLabel> {

	public LabelFactory(EcoreModelState modelState) {
		super(modelState);
	}

	@Override
	public GLabel create(ENamedElement semanticElement) {
		if (semanticElement instanceof EAttribute) {
			return create((EAttribute) semanticElement);
		} else if (semanticElement instanceof EEnumLiteral) {
			return create((EEnumLiteral) semanticElement);
		} else if (semanticElement instanceof EOperation) {
			return create((EOperation) semanticElement);
		}
		return null;
	}

	public GLabel create(EAttribute eAttribute) {
		String label = eAttribute.getName();
		if (eAttribute.getEType() != null) {
			label = label.concat(" : " + eAttribute.getEType().getName());
		}
		return new GLabelBuilder(Types.ATTRIBUTE) //
				.id(toId(eAttribute))//
				.text(label) //
				.addCssClass(getOccurrenceClass(eAttribute.getLowerBound(), eAttribute.getUpperBound())) //
				.build();
	}

	public GLabel create(EEnumLiteral eEnumLiteral) {
		String label = eEnumLiteral.getLiteral();

		return new GLabelBuilder(Types.ENUMLITERAL) //
				.id(toId(eEnumLiteral)) //
				.text(label) //
				.build();
	}

	public GLabel create(EOperation eOperation) {
		String label = eOperation.getName().concat("()");
		if (eOperation.getEType() != null) {
			label = label.concat(" : " + eOperation.getEType().getName());
		}
		return new GLabelBuilder(Types.OPERATION) //
				.id(toId(eOperation))//
				.text(label) //
				.addCssClass(getOccurrenceClass(eOperation.getLowerBound(), eOperation.getUpperBound())) //
				.addCssClass(getEExceptionsClass(eOperation.getEExceptions())) //
				.build();
	}
	
	private String getEExceptionsClass(List<EClassifier> exceptions) {
		if (!exceptions.isEmpty()) {
			return String.join("-", exceptions.stream().map(EClassifier::getName).collect(Collectors.toList()));
		}
		return "none";
	}

	private String getOccurrenceClass(int lowerBound, int upperBound) {
		if (lowerBound == 0) {
			if (upperBound == 0) {
				return "eoccurrencezero";
			} else if (upperBound == 1) {
				return "eoccurrencezerotoone";
			} else if (upperBound > 1) {
				return "eoccurrencezeroton";
			} else if (upperBound < 0) {
				return "eoccurrencezerotounbounded";
			} else {
				return "eoccurrencezerotounspecified";
			}
		} else if (lowerBound == 1) {
			if (upperBound == 1) {
				return "eoccurrenceone";
			} else if (upperBound > 1) {
				return "eoccurrenceoneton";
			} else if (upperBound < 0) {
				return "eoccurrenceonetounbounded";
			} else {
				return "eoccurrenceoneunspecified";
			}
		} else if (lowerBound > 1) {
			if (lowerBound == upperBound) {
				return "eoccurrencen";
			} else if (upperBound > 1) {
				return "eoccurrencentom";
			} else if (upperBound < 0) {
				return "eoccurrencentounbounded";
			} else {
				return "eoccurrencentounspecified";
			}
		}
		return "none";
	}

}