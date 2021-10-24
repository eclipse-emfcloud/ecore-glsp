/********************************************************************************
 * Copyright (c) 2019-2021 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ********************************************************************************/
package org.eclipse.emfcloud.ecore.glsp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreAdapterFactory;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emfcloud.ecore.enotation.EnotationPackage;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelServerAccess;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelState;
import org.eclipse.glsp.server.protocol.GLSPServerException;
import org.eclipse.glsp.server.utils.ClientOptions;
import org.eclipse.glsp.server.utils.MapUtil;

public class ResourceManager {
	public static final String ECORE_EXTENSION = ".ecore";
	public static final String NOTATION_EXTENSION = ".enotation";

	private static Logger LOGGER = Logger.getLogger(ResourceManager.class);

	private ResourceSet resourceSet;
	private String baseSourceUri;
	private EcoreFacade ecoreFacade;
	private EditingDomain editingDomain;

	public ResourceManager(EcoreModelState modelState, EcoreModelServerAccess modelServerAccess) {
		String sourceURI = MapUtil.getValue(modelState.getClientOptions(), ClientOptions.SOURCE_URI)
				.orElseThrow(() -> new GLSPServerException("No source uri given to load model!"));
		if (!sourceURI.endsWith(ECORE_EXTENSION) && !sourceURI.endsWith(NOTATION_EXTENSION)) {
			throw new GLSPServerException("Could not setup ResourceManager: \n Invalid file extension: " + sourceURI);
		}

		this.baseSourceUri = sourceURI.substring(0, sourceURI.lastIndexOf('.'));
		this.resourceSet = setupResourceSet();
		createEcoreFacade(modelState, modelServerAccess);
	}

	protected ResourceSet setupResourceSet() {
		editingDomain = new AdapterFactoryEditingDomain(new EcoreAdapterFactory(), new BasicCommandStack());
		ResourceSet resourceSet = editingDomain.getResourceSet();
		resourceSet.getPackageRegistry().put(EcorePackage.eINSTANCE.getNsURI(), EcorePackage.eINSTANCE);
		resourceSet.getPackageRegistry().put(EnotationPackage.eINSTANCE.getNsURI(), EnotationPackage.eINSTANCE);
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		return resourceSet;
	}

	public EditingDomain getEditingDomain() {
		return editingDomain;
	}

	public EcoreFacade getEcoreFacade() {
		return ecoreFacade;
	}

	protected EcoreFacade createEcoreFacade(EcoreModelState modelState, EcoreModelServerAccess modelServerAccess) {
		try {
			EObject semanticRoot = modelServerAccess.getModel();
			Resource semanticResource = loadResource(convertToFile(getSemanticURI()), semanticRoot);

			boolean needsInitialAutoLayout = false;
			EObject notationRoot = modelServerAccess.getNotationModel();
			if (notationRoot == null) {
				needsInitialAutoLayout = true;
				notationRoot = modelServerAccess.createEcoreNotation();
			}
			Resource notationResource = loadResource(convertToFile(getNotationURI()), notationRoot);
			ecoreFacade = new EcoreFacade(semanticResource, notationResource, modelState.getIndex(), needsInitialAutoLayout);
			return ecoreFacade;
		} catch (IOException e) {
			LOGGER.error(e);
			throw new GLSPServerException("Error during model loading", e);
		}
	}

	public String getSemanticURI() {
		return baseSourceUri + ECORE_EXTENSION;
	}

	public String getNotationURI() {
		return baseSourceUri + NOTATION_EXTENSION;
	}

	private File convertToFile(String sourceURI) {
		if (sourceURI != null) {
			return new File(sourceURI);
		}
		return null;
	}

	private Resource loadResource(File file, EObject root) throws IOException {
		Resource resource = createResource(file.getAbsolutePath());
		resource.getContents().clear();
		resource.getContents().add(root);
		configureResource(resource);
		return resource;
	}

	private void configureResource(Resource resource) {
		if (resource instanceof XMLResource) {
			XMLResource xmlResource = (XMLResource) resource;
			xmlResource.getDefaultLoadOptions().put(XMLResource.OPTION_KEEP_DEFAULT_CONTENT, Boolean.TRUE);
			xmlResource.getDefaultSaveOptions().put(XMLResource.OPTION_PROCESS_DANGLING_HREF,
					XMLResource.OPTION_PROCESS_DANGLING_HREF_RECORD);
		}
	}

	private Resource createResource(String path) {
		return resourceSet.createResource(URI.createFileURI(path));
	}

	public List<EClassifier> getAllEClassifiers() {
		List<EClassifier> listOfClassifiers = new ArrayList<>(EcorePackage.eINSTANCE.getEClassifiers());
		TreeIterator<Notifier> resourceSetContent = editingDomain.getResourceSet().getAllContents();
		while (resourceSetContent.hasNext()) {
			Notifier res = resourceSetContent.next();
			if (res instanceof EDataType) {
				listOfClassifiers.add((EDataType) res);
			}
		}
		return listOfClassifiers;
	}

	public List<EDataType> getAllETypes() {
		return getAllEClassifiers().stream().filter(EDataType.class::isInstance).map(EDataType.class::cast)
				.collect(Collectors.toList());
	}

	public Optional<EDataType> getETypeFromString(String eTypeName) {
		for (EDataType type : getAllETypes()) {
			if (type.getName().toLowerCase().equals(eTypeName.toLowerCase())) {
				return Optional.ofNullable(type);
			}
		}
		return Optional.empty();
	}

}
