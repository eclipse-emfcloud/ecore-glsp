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
package org.eclipse.emfcloud.ecore.glsp;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emfcloud.ecore.enotation.Diagram;
import org.eclipse.emfcloud.ecore.enotation.Edge;
import org.eclipse.emfcloud.ecore.enotation.EnotationFactory;
import org.eclipse.emfcloud.ecore.enotation.NotationElement;
import org.eclipse.emfcloud.ecore.enotation.Point;
import org.eclipse.emfcloud.ecore.enotation.SemanticProxy;
import org.eclipse.emfcloud.ecore.enotation.Shape;
import org.eclipse.emfcloud.ecore.glsp.util.EcoreEdgeUtil;
import org.eclipse.glsp.graph.GEdge;
import org.eclipse.glsp.graph.GModelElement;
import org.eclipse.glsp.graph.GModelRoot;
import org.eclipse.glsp.graph.GNode;
import org.eclipse.glsp.graph.GShapeElement;

import com.google.common.base.Preconditions;

public class EcoreFacade {

	private final Resource semanticResource;
	private final Resource notationResource;
	private final EPackage ePackage;

	private boolean diagramIsNewlyCreated = false;

	private Diagram diagram;
	private EcoreModelIndex modelIndex;

	public EcoreFacade(Resource semanticResource, Resource notationResource, EcoreModelIndex modelIndex) {
		this.semanticResource = semanticResource;
		this.notationResource = notationResource;
		this.modelIndex = modelIndex;
		this.ePackage = semanticResource.getContents().stream().filter(EPackage.class::isInstance)
				.map(EPackage.class::cast).findFirst().orElseThrow();
		EcoreUtil.resolveAll(ePackage);
	}

	public Resource getSemanticResource() {
		return semanticResource;
	}

	public Resource getNotationResource() {
		return notationResource;
	}

	public EPackage getEPackage() {
		return this.ePackage;
	}

	public Diagram getDiagram() {
		if (diagram == null) {
			getOrCreateDiagram();
		}
		return diagram;
	}

	private Diagram getOrCreateDiagram() {
		Optional<Diagram> existingDiagram = findDiagram();
		diagram = existingDiagram.isPresent() ? existingDiagram.get() : createDiagram();
		findUnresolvedElements(diagram).forEach(e -> e.setSemanticElement(resolved(e.getSemanticElement())));
		modelIndex.indexNotation(diagram);
		return diagram;

	}

	public Diagram initialize(Diagram diagram, GModelRoot gRoot) {
		Preconditions.checkArgument(diagram.getSemanticElement().getResolvedElement() == ePackage);
		gRoot.getChildren().forEach(child -> {
			modelIndex.getNotation(child).ifPresentOrElse(n -> updateNotationElement(n, child),
					() -> initializeNotationElement(child).ifPresent(diagram.getElements()::add));

		});
		return diagram;
	}

	public boolean diagramNeedsAutoLayout() {
		boolean oldValue = this.diagramIsNewlyCreated;
		this.diagramIsNewlyCreated = false;
		return oldValue;
	}

	public Optional<? extends NotationElement> initializeNotationElement(GModelElement gModelElement) {
		Optional<? extends NotationElement> result = Optional.empty();
		if (gModelElement instanceof GNode) {
			result = initializeShape((GNode) gModelElement);
		} else if (gModelElement instanceof GEdge) {
			result = initializeEdge((GEdge) gModelElement);
		}
		return result;
	}

	public List<NotationElement> findUnresolvedElements(Diagram diagram) {
		return diagram.getElements().stream()
				.filter(element -> resolved(element.getSemanticElement()).getResolvedElement() == null)
				.collect(Collectors.toList());
	}

	private Diagram createDiagram() {
		Diagram diagram = EnotationFactory.eINSTANCE.createDiagram();
		diagram.setSemanticElement(createProxy(ePackage));
		diagram.setGraphicId(generateId());
		notationResource.getContents().add(diagram);
		diagramIsNewlyCreated = true;
		return diagram;
	}
	
	private String generateId() {
		return UUID.randomUUID().toString();
	}

	public Optional<Shape> initializeShape(GShapeElement shapeElement) {
		return modelIndex.getSemantic(shapeElement)
				.map(semanticElement -> initializeShape(semanticElement, shapeElement));

	}

	public Shape initializeShape(EObject semanticElement) {
		return initializeShape(semanticElement, null);
	}

	public Shape initializeShape(EObject semanticElement, GShapeElement shapeElement) {
		Shape shape = EnotationFactory.eINSTANCE.createShape();
		shape.setSemanticElement(createProxy(semanticElement));
		shape.setGraphicId(generateId());
		if (shapeElement != null) {
			updateShape(shape, shapeElement);

		}
		modelIndex.indexNotation(shape);
		return shape;
	}

	public Optional<Edge> initializeEdge(GEdge gEdge) {
		return modelIndex.getSemantic(gEdge).map(semanticElement -> initializeEdge(semanticElement, gEdge));
	}

	public Edge initializeEdge(EObject semanticElement) {
		return initializeEdge(semanticElement, null);
	}

	public Edge initializeEdge(EObject semanticElement, GEdge gEdge) {
		Edge edge = EnotationFactory.eINSTANCE.createEdge();
		edge.setSemanticElement(createProxy(semanticElement));
		edge.setGraphicId(generateId());
		if (gEdge != null) {
			updateEdge(edge, gEdge);
		}
		modelIndex.indexNotation(edge);
		return edge;
	}

	public SemanticProxy createProxy(EObject eObject) {
		SemanticProxy proxy = EnotationFactory.eINSTANCE.createSemanticProxy();
		proxy.setResolvedElement(eObject);
		proxy.setUri(semanticResource.getURIFragment(eObject));
		return proxy;
	}

	public SemanticProxy resolved(SemanticProxy proxy) {
		if (proxy.getResolvedElement() != null) {
			return proxy;
		}
		return reResolved(proxy);
	}

	public SemanticProxy reResolved(SemanticProxy proxy) {
		proxy.setResolvedElement(semanticResource.getEObject(proxy.getUri()));
		return proxy;
	}

	public void updateNotationElement(NotationElement notation, GModelElement modelElement) {
		if (notation instanceof Shape && modelElement instanceof GShapeElement) {
			updateShape((Shape) notation, (GShapeElement) modelElement);
		} else if (notation instanceof Edge && modelElement instanceof GEdge) {
			updateEdge((Edge) notation, (GEdge) modelElement);
		}
	}

	public void updateShape(Shape shape, GShapeElement shapeElement) {
		if (shapeElement.getSize() != null) {
			shape.setSize(EcoreEdgeUtil.copy(shapeElement.getSize()));

		}
		if (shapeElement.getPosition() != null) {
			shape.setPosition(EcoreEdgeUtil.copy(shapeElement.getPosition()));
		}
	}

	public void updateEdge(Edge edge, GEdge gEdge) {
		edge.getBendPoints().clear();
		if (gEdge.getRoutingPoints() != null) {
			ArrayList<Point> points = new ArrayList<>();
			gEdge.getRoutingPoints().forEach(p -> points.add(EcoreEdgeUtil.copy(p)));
			edge.getBendPoints().addAll(points);
		}
	}

	public void updateNotationElement(GModelElement modelElement) {
		modelIndex.getNotation(modelElement).ifPresent(notation -> updateNotationElement(notation, modelElement));
	}

	private Optional<Diagram> findDiagram() {
		return notationResource.getContents().stream().filter(eObject -> isDiagramForEPackage(eObject))
				.map(Diagram.class::cast).findFirst();
	}

	private boolean isDiagramForEPackage(EObject eObject) {
		if (eObject instanceof Diagram) {
			Diagram diagram = (Diagram) eObject;

			return resolved(diagram.getSemanticElement()).getResolvedElement() == ePackage;

		}
		return false;
	}

}
