/********************************************************************************
 * Copyright (c) 2021 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ********************************************************************************/
package org.eclipse.emfcloud.ecore.modelserver;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emfcloud.ecore.enotation.Diagram;
import org.eclipse.emfcloud.ecore.enotation.Edge;
import org.eclipse.emfcloud.ecore.enotation.EnotationFactory;
import org.eclipse.emfcloud.ecore.enotation.NotationElement;
import org.eclipse.emfcloud.ecore.enotation.SemanticProxy;
import org.eclipse.emfcloud.ecore.enotation.Shape;
import org.eclipse.emfcloud.modelserver.emf.common.DefaultModelResourceManager;
import org.eclipse.emfcloud.modelserver.emf.configuration.EPackageConfiguration;
import org.eclipse.emfcloud.modelserver.emf.configuration.ServerConfiguration;
import org.eclipse.glsp.graph.util.GraphUtil;

import com.google.inject.Inject;

public class EcoreModelResourceManager extends DefaultModelResourceManager {

	public static final String ECORE_EXTENSION = "ecore";
	public static final String NOTATION_EXTENSION = "enotation";

	public static final int DEFAULT_SHAPE_HEIGHT = 75;
	public static final int DEFAULT_SHAPE_WIDTH = 175;
	public static final int DEFAULT_POSITION_X = 10;
	public static final int DEFAULT_POSITION_Y = 10;

	@Inject
	public EcoreModelResourceManager(Set<EPackageConfiguration> configurations, AdapterFactory adapterFactory,
			ServerConfiguration serverConfiguration) {
		super(configurations, adapterFactory, serverConfiguration);
	}

	@Override
	protected void loadSourceResources(final String directoryPath) {
		if (directoryPath == null || directoryPath.isEmpty()) {
			return;
		}
		File directory = new File(directoryPath);
		for (File file : directory.listFiles()) {
			if (isSourceDirectory(file)) {
				loadSourceResources(file.getAbsolutePath());
			} else if (file.isFile()) {
				URI absolutePath = createURI(file.getAbsolutePath());
				if (absolutePath.fileExtension().equals(ECORE_EXTENSION)) {
					resourceSets.put(createURI(file.getAbsolutePath()), new ResourceSetImpl());
				}
				loadResource(file.getAbsolutePath(),
						false /* do not remove unloadable resources on workspace startup */);
			}
		}
	}

	@Override
	public ResourceSet getResourceSet(final String modeluri) {
		if (createURI(modeluri).fileExtension().equals(NOTATION_EXTENSION)) {
			URI semanticUri = createURI(modeluri).trimFileExtension().appendFileExtension(ECORE_EXTENSION);
			return resourceSets.get(semanticUri);
		}
		return resourceSets.get(createURI(modeluri));
	}

	@Override
	public boolean save(final String modeluri) {
		boolean result = false;
		for (Resource resource : getResourceSet(modeluri).getResources()) {
			result = saveResource(resource);
		}
		if (result) {
			getEditingDomain(getResourceSet(modeluri)).saveIsDone();
		}
		return result;
	}

	public boolean addNewEcoreResources(final String modeluri, final String nsUri, final String nsPrefix) {
		URI ecoreModelUri = createURI(modeluri);
		ResourceSet resourceSet = new ResourceSetImpl();

		final EPackage newEPackage = createNewEPackage(ecoreModelUri);
		newEPackage.setNsURI(nsUri);
		newEPackage.setNsPrefix(nsPrefix);
		resourceSets.put(ecoreModelUri, resourceSet);

		try {
			final Resource ecoreResource = resourceSet.createResource(ecoreModelUri);
			resourceSet.getResources().add(ecoreResource);
			ecoreResource.getContents().add(newEPackage);
			ecoreResource.save(null);

			final Resource enotationResource = resourceSet
					.createResource(ecoreModelUri.trimFileExtension().appendFileExtension(NOTATION_EXTENSION));
			resourceSet.getResources().add(enotationResource);
			enotationResource.getContents().add(createNewDiagram(newEPackage));
			enotationResource.save(null);
			createEditingDomain(resourceSet);

		} catch (IOException e) {
			return false;
		}

		return true;
	}

	protected EPackage createNewEPackage(final URI modelUri) {
		EPackage newEPackage = EcoreFactory.eINSTANCE.createEPackage();
		String modelName = modelUri.lastSegment().split("." + modelUri.fileExtension())[0];
		newEPackage.setName(modelName);
		return newEPackage;
	}

	protected Diagram createNewDiagram(final EPackage ePackage) {
		Diagram newDiagram = EnotationFactory.eINSTANCE.createDiagram();
		SemanticProxy semanticProxy = EnotationFactory.eINSTANCE.createSemanticProxy();
		semanticProxy.setUri(EcoreUtil.getURI(ePackage).fragment());
		newDiagram.setSemanticElement(semanticProxy);
		// create shapes
		for (EClassifier classifier : ePackage.getEClassifiers()) {
			Shape shape = createShape(classifier);
			newDiagram.getElements().add(shape);
		}

		// create edges
		for (EClassifier classifier : ePackage.getEClassifiers()) {
			// create edges from references / inheritances
			if (classifier instanceof EClass) {
				EClass eClass = (EClass) classifier;
				eClass.getEStructuralFeatures().forEach(feature -> {
					if (feature instanceof EReference) {
						Edge edge = EnotationFactory.eINSTANCE.createEdge();
						edge.setSemanticElement(createSemanticProxy(feature));
						edge.setSource(getNotationElement(newDiagram, eClass));
						edge.setTarget(getNotationElement(newDiagram, feature.getEType()));
						newDiagram.getElements().add(edge);
					}
				});
				eClass.getESuperTypes().forEach(superType -> {
					Edge edge = EnotationFactory.eINSTANCE.createEdge();
					edge.setType("edge:inheritance");
					edge.setSource(getNotationElement(newDiagram, eClass));
					edge.setTarget(getNotationElement(newDiagram, superType));
					newDiagram.getElements().add(edge);
				});
			}
		}
		return newDiagram;
	}

	protected NotationElement getNotationElement(final Diagram diagram, final EObject semanticElement) {
		String semanticUri = EcoreUtil.getURI(semanticElement).fragment();
		for (NotationElement element : diagram.getElements()) {
			if (element.getSemanticElement() != null && element.getSemanticElement().getUri().equals(semanticUri)) {
				return element;
			}
		}
		return null;
	}

	protected Shape createShape(final EClassifier classifier) {
		Shape shape = EnotationFactory.eINSTANCE.createShape();
		shape.setPosition(GraphUtil.point(DEFAULT_POSITION_X, DEFAULT_POSITION_Y));
		shape.setSize(GraphUtil.dimension(DEFAULT_SHAPE_WIDTH, DEFAULT_SHAPE_HEIGHT));
		shape.setSemanticElement(createSemanticProxy(classifier));
		return shape;
	}

	protected SemanticProxy createSemanticProxy(final EObject semanticElement) {
		SemanticProxy proxy = EnotationFactory.eINSTANCE.createSemanticProxy();
		proxy.setUri(EcoreUtil.getURI(semanticElement).fragment());
		return proxy;
	}

	public EObject addNotationResource(final String modeluri) {
		URI ecoreModelUri = createURI(modeluri);

		EPackage existingEPackage = loadModel(modeluri, EPackage.class).get();
		Diagram notationDiagram = createNewDiagram(existingEPackage);
		final Resource existingEcoreResource = loadResource(modeluri).get();
		ResourceSet existingResourceSet = getResourceSet(modeluri);
		editingDomains.remove(existingResourceSet);
		resourceSets.remove(ecoreModelUri);

		ResourceSet newResourceSet = new ResourceSetImpl();

		resourceSets.put(ecoreModelUri, newResourceSet);

		try {
			newResourceSet.getResources().add(existingEcoreResource);

			final Resource enotationResource = newResourceSet
					.createResource(ecoreModelUri.trimFileExtension().appendFileExtension(NOTATION_EXTENSION));
			newResourceSet.getResources().add(enotationResource);
			enotationResource.getContents().add(notationDiagram);
			enotationResource.save(null);
			createEditingDomain(newResourceSet);

		} catch (IOException e) {
			return null;
		}

		return notationDiagram;
	}

}
