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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emfcloud.ecore.enotation.Edge;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelState;
import org.eclipse.emfcloud.ecore.glsp.util.EcoreConfig.CSS;
import org.eclipse.emfcloud.ecore.glsp.util.EcoreConfig.Types;
import org.eclipse.emfcloud.ecore.glsp.util.EcoreEdgeUtil;
import org.eclipse.glsp.graph.GEdge;
import org.eclipse.glsp.graph.GGraph;
import org.eclipse.glsp.graph.GLabel;
import org.eclipse.glsp.graph.GModelElement;
import org.eclipse.glsp.graph.GModelRoot;
import org.eclipse.glsp.graph.GPoint;
import org.eclipse.glsp.graph.builder.impl.GEdgeBuilder;
import org.eclipse.glsp.graph.builder.impl.GEdgePlacementBuilder;
import org.eclipse.glsp.graph.builder.impl.GGraphBuilder;
import org.eclipse.glsp.graph.builder.impl.GLabelBuilder;
import org.eclipse.glsp.graph.util.GConstants;
import org.eclipse.glsp.graph.util.GraphUtil;
import org.eclipse.glsp.server.protocol.GLSPServerException;

public class GModelFactory extends AbstractGModelFactory<EObject, GModelElement> {

	private ClassifierNodeFactory classifierNodeFactory;
	private LabelFactory labelFactory;

	public GModelFactory(EcoreModelState modelState) {
		super(modelState);
		classifierNodeFactory = new ClassifierNodeFactory(modelState, this);
		labelFactory = new LabelFactory(modelState);
		getOrCreateRoot();

	}

	@Override
	public GModelElement create(EObject semanticElement) {
		GModelElement result = null;
		if (semanticElement instanceof EClassifier) {
			result = classifierNodeFactory.create((EClassifier) semanticElement);
		} else if (semanticElement instanceof EPackage) {
			result = create((EPackage) semanticElement);
		} else if (semanticElement instanceof EReference) {
			result = create((EReference) semanticElement);
		} else if (semanticElement instanceof ENamedElement) {
			result = labelFactory.create((ENamedElement) semanticElement);
		}
		if (result == null) {
			throw createFailed(semanticElement);
		}
		return result;
	}

	public GGraph create() {
		return create(modelState.getEditorContext().getEcoreFacade().getEPackage());
	}

	public GGraph create(EPackage ePackage) {
		GGraph graph = getOrCreateRoot();
		graph.setId(toId(ePackage));

		graph.getChildren().addAll(ePackage.getEClassifiers().stream()//
				.map(this::create)//
				.collect(Collectors.toList()));

		graph.getChildren().addAll(ePackage.getEClassifiers().stream() //
				.filter(EClass.class::isInstance) //
				.map(EClass.class::cast) //
				.flatMap(eClass -> createEdges(eClass).stream()) //
				.collect(Collectors.toList()));
		return graph;

	}

	private List<GModelElement> createEdges(EClass eClass) {
		List<GModelElement> children = new ArrayList<>();
		// create reference edges
		eClass.getEReferences().stream().map(this::create).filter(Objects::nonNull).forEach(children::add);
		// create inheritance edges
		eClass.getESuperTypes().stream().map(s -> create(eClass, s)).forEach(children::add);
		return children;
	}

	public GEdge create(EReference eReference) {
		String source = toId(eReference.getEContainingClass());
		String target = toId(eReference.getEReferenceType());
		String id = toId(eReference);

		GEdgeBuilder builder = new GEdgeBuilder().id(id) //
				.addCssClass(CSS.ECORE_EDGE) //
				.addCssClass(eReference.isContainment() ? CSS.COMPOSITION : null) //
				.sourceId(source) //
				.targetId(target) //
				.routerKind(GConstants.RouterKind.MANHATTAN);

		if (eReference.getEOpposite() != null) {
			if (!createBidirectionalEdge(eReference, builder)) {
				return null;
			}
		} else {

			String labelMultiplicity = createMultiplicity(eReference);
			String labelName = eReference.getName();
			builder.type(eReference.isContainment() ? Types.COMPOSITION : Types.REFERENCE) //
					.add(createEdgeMultiplicityLabel(labelMultiplicity, id + "_label_multiplicity", 0.5d))
					.add(createEdgeNameLabel(labelName, id + "_label_name", 0.5d));
		}

		modelState.getIndex().getNotation(eReference, Edge.class).ifPresent(edge -> {

			if (edge.getBendPoints() != null) {
				ArrayList<GPoint> gPoints = new ArrayList<>();
				edge.getBendPoints().forEach(p -> gPoints.add(GraphUtil.copy(p)));
				builder.addRoutingPoints(gPoints);
			}
		});
		return builder.build();
	}

	private boolean createBidirectionalEdge(EReference eReference, GEdgeBuilder builder) {
		Set<String> referenceSet = this.modelState.getIndex().getBidirectionalReferences();

		if ((!eReference.isContainment() && referenceSet.contains(EcoreEdgeUtil.getStringId(eReference.getEOpposite())))
				|| eReference.isContainer()) {
			return false;
		}

		referenceSet.add(EcoreEdgeUtil.getStringId(eReference));

		String sourceLabelMultiplicity = createMultiplicity(eReference.getEOpposite());
		String sourceLabelName = eReference.getEOpposite().getName();
		String sourceId = toId(eReference.getEOpposite());

		String targetLabelMultiplicity = createMultiplicity(eReference);
		String targetLabelName = eReference.getName();
		String targetId = toId(eReference);

		builder.type(eReference.isContainment() ? Types.BIDIRECTIONAL_COMPOSITION : Types.BIDIRECTIONAL_REFERENCE) //
				.add(createEdgeMultiplicityLabel(sourceLabelMultiplicity, sourceId + "_sourcelabel_multiplicity", 0.1d))//
				.add(createEdgeNameLabel(sourceLabelName, sourceId + "_sourcelabel_name", 0.1d))//
				.add(createEdgeMultiplicityLabel(targetLabelMultiplicity, targetId + "_targetlabel_multiplicity", 0.9d))//
				.add(createEdgeNameLabel(targetLabelName, targetId + "_targetlabel_name", 0.9d));
		return true;

	}

	private String createMultiplicity(EReference eReference) {
		return String.format("[%s..%s]", eReference.getLowerBound(),
				eReference.getUpperBound() == -1 ? "*" : eReference.getUpperBound());
	}

	private GLabel createEdgeMultiplicityLabel(String value, String id, double position) {
		return createEdgeLabel(value, position, id, Types.LABEL_EDGE_MULTIPLICITY, GConstants.EdgeSide.BOTTOM);
	}

	private GLabel createEdgeNameLabel(String name, String id, double position) {
		return createEdgeLabel(name, position, id, Types.LABEL_EDGE_NAME, GConstants.EdgeSide.TOP);
	}

	private GLabel createEdgeLabel(String name, double position, String id, String type, String side) {
		return new GLabelBuilder(type) //
				.edgePlacement(new GEdgePlacementBuilder()//
						.side(side)//
						.position(position)//
						.offset(2d) //
						.rotate(false) //
						.build())//
				.id(id) //
				.text(name).build();
	}

	public GEdge create(EClass baseClass, EClass superClass) {
		String sourceId = toId(baseClass);
		String targetId = toId(superClass);
		if (sourceId.isEmpty() || sourceId.isEmpty()) {
			return null;
		}
		String id = sourceId + "_" + targetId;
		return new GEdgeBuilder(Types.INHERITANCE) //
				.id(id)//
				.addCssClass(CSS.ECORE_EDGE) //
				.addCssClass(CSS.INHERITANCE) //
				.sourceId(sourceId) //
				.targetId(targetId) //
				.routerKind(GConstants.RouterKind.MANHATTAN)//
				.build();
	}

	public static GLSPServerException createFailed(EObject semanticElement) {
		return new GLSPServerException("Error during model initialization!", new Throwable(
				"No matching GModelElement found for the semanticElement of type: " + semanticElement.getClass()));
	}

	private GGraph getOrCreateRoot() {
		GModelRoot existingRoot = modelState.getRoot();
		if (existingRoot != null && existingRoot instanceof GGraph) {
			GGraph graph = (GGraph) existingRoot;
			graph.getChildren().clear();
			return graph;
		} else {
			GGraph graph = new GGraphBuilder().build();
			modelState.setRoot(graph);
			return graph;
		}
	}
}
