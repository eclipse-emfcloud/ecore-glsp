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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emfcloud.ecore.enotation.Diagram;
import org.eclipse.emfcloud.ecore.enotation.Edge;
import org.eclipse.emfcloud.ecore.enotation.NotationElement;
import org.eclipse.emfcloud.ecore.glsp.util.EcoreConfig.Types;
import org.eclipse.emfcloud.ecore.glsp.util.EcoreEdgeUtil;
import org.eclipse.glsp.graph.GModelElement;
import org.eclipse.glsp.graph.impl.GModelIndexImpl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class EcoreModelIndex extends GModelIndexImpl {
	private BiMap<String, EObject> semanticIndex;
	private BiMap<EObject, NotationElement> notationIndex;
	private Set<String> bidirectionalReferences;
	private BiMap<String, Edge> inheritanceEdges;

	private EcoreModelIndex(EObject target) {
		super(target);
		semanticIndex = HashBiMap.create();
		notationIndex = HashBiMap.create();
		bidirectionalReferences = new HashSet<>();
		inheritanceEdges = HashBiMap.create();
	}

	public static EcoreModelIndex get(GModelElement element) {
		EObject root = EcoreUtil.getRootContainer(element);
		EcoreModelIndex existingIndex = (EcoreModelIndex) EcoreUtil.getExistingAdapter(root, EcoreModelIndex.class);
		return Optional.ofNullable(existingIndex).orElseGet(() -> (create(element)));
	}

	public static EcoreModelIndex create(GModelElement element) {
		return new EcoreModelIndex(EcoreUtil.getRootContainer(element));
	}

	@Override
	public boolean isAdapterForType(Object type) {
		return super.isAdapterForType(type) || EcoreModelIndex.class.equals(type);
	}

	public void indexSemantic(String id, EObject semanticElement) {
		semanticIndex.putIfAbsent(id, semanticElement);
	}

	public void indexNotation(NotationElement notationElement) {
		if (notationElement.getSemanticElement() != null) {
			EObject semanticElement = notationElement.getSemanticElement().getResolvedElement();
			notationIndex.put(semanticElement, notationElement);
			semanticIndex.inverse().putIfAbsent(semanticElement, UUID.randomUUID().toString());
		} else if (notationElement.getType() != null && notationElement.getType().equals(Types.INHERITANCE)) {
			indexInheritanceEdge((Edge) notationElement);
		}

		if (notationElement instanceof Diagram) {
			((Diagram) notationElement).getElements().forEach(this::indexNotation);
		}
	}

	public Optional<EObject> getSemantic(String id) {
		return Optional.ofNullable(semanticIndex.get(id));
	}

	public Optional<String> getSemanticId(EObject semanticElement) {
		return Optional.ofNullable(semanticIndex.inverse().get(semanticElement));
	}

	public <T extends EObject> Optional<T> getSemantic(String id, Class<T> clazz) {
		return safeCast(Optional.ofNullable(semanticIndex.get(id)), clazz);
	}

	public Optional<EObject> getSemantic(GModelElement gModelElement) {
		return getSemantic(gModelElement.getId());
	}

	public <T extends EObject> Optional<T> getSemantic(GModelElement gModelElement, Class<T> clazz) {
		return getSemantic(gModelElement.getId(), clazz);
	}

	public Optional<NotationElement> getNotation(EObject semanticElement) {
		return Optional.ofNullable(notationIndex.get(semanticElement));
	}

	public <T extends NotationElement> Optional<T> getNotation(EObject semanticElement, Class<T> clazz) {
		return safeCast(getNotation(semanticElement), clazz);
	}

	public Optional<NotationElement> getNotation(String id) {
		return getSemantic(id).flatMap(this::getNotation);
	}

	public <T extends NotationElement> Optional<T> getNotation(String id, Class<T> clazz) {
		return safeCast(getNotation(id), clazz);
	}

	public Optional<NotationElement> getNotation(GModelElement gModelElement) {
		return getNotation(gModelElement.getId());
	}

	public <T extends NotationElement> Optional<T> getNotation(GModelElement element, Class<T> clazz) {
		return safeCast(getNotation(element), clazz);
	}

	private <T> Optional<T> safeCast(Optional<?> toCast, Class<T> clazz) {
		return toCast.filter(clazz::isInstance).map(clazz::cast);
	}

	public String add(EObject eObject) {
		if (eObject instanceof GModelElement) {
			return ((GModelElement) eObject).getId();
		}
		String id = null;
		if (eObject instanceof NotationElement) {
			EObject semanticElement = ((NotationElement) eObject).getSemanticElement().getResolvedElement();
			id = add(semanticElement);
			notationIndex.putIfAbsent(semanticElement, (NotationElement) eObject);
		} else {
			id = getSemanticId(eObject).orElse(null);
			if (id == null) {
				id = UUID.randomUUID().toString();
				indexSemantic(id, eObject);
			}

		}
		return id;

	}

	public void remove(EObject eObject) {
		if (eObject instanceof NotationElement) {
			EObject semanticElement = ((NotationElement) eObject).getSemanticElement().getResolvedElement();
			notationIndex.remove(semanticElement);
			remove(semanticElement);
			return;
		} else if (eObject instanceof GModelElement) {
			// do nothing;
			return;
		}
		semanticIndex.inverse().remove(eObject);
	}

	public Set<String> getBidirectionalReferences() {
		return bidirectionalReferences;
	}

	public void indexInheritanceEdge(Edge inheritanceEdge) {
		Optional<String> sourceId = getElementId(inheritanceEdge.getSource());
		Optional<String> targetId = getElementId(inheritanceEdge.getTarget());
		String inheritanceEdgeId = EcoreEdgeUtil.getInheritanceEdgeId(sourceId.get(), targetId.get());
		inheritanceEdges.putIfAbsent(inheritanceEdgeId, inheritanceEdge);
	}

	public Optional<Edge> getInheritanceEdge(String elementId) {
		return Optional.ofNullable(inheritanceEdges.get(elementId));
	}

	public Optional<Edge> getInheritanceEdge(EClass eClass, EClass eSuperType) {
		String sourceId = semanticIndex.inverse().get(eClass);
		String targetId = semanticIndex.inverse().get(eSuperType);
		String inheritanceEdgeId = EcoreEdgeUtil.getInheritanceEdgeId(sourceId, targetId);
		return Optional.ofNullable(inheritanceEdges.get(inheritanceEdgeId));
	}

	protected Optional<String> getElementId(NotationElement notationElement) {
		EObject semantic = notationIndex.inverse().get(notationElement);
		return Optional.of(semanticIndex.inverse().get(semantic));
	}

}
