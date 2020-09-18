/*******************************************************************************
 * Copyright (c) 2019 EclipseSource and others.
 *  
 *   This program and the accompanying materials are made available under the
 *   terms of the Eclipse Public License v. 2.0 which is available at
 *   http://www.eclipse.org/legal/epl-2.0.
 *  
 *   This Source Code may also be made available under the following Secondary
 *   Licenses when the conditions for such availability set forth in the Eclipse
 *   Public License v. 2.0 are satisfied: GNU General Public License, version 2
 *   with the GNU Classpath Exception which is available at
 *   https://www.gnu.org/software/classpath/license.html.
 *  
 *   SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 ******************************************************************************/
package org.eclipse.emfcloud.ecore.glsp.model;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emfcloud.ecore.enotation.EnotationPackage;
import org.eclipse.emfcloud.ecore.glsp.EcoreFacade;
import org.eclipse.emfcloud.ecore.glsp.EcoreModelIndex;
import org.eclipse.emfcloud.modelserver.client.ModelServerClient;
import org.eclipse.emfcloud.modelserver.client.Response;
import org.eclipse.emfcloud.modelserver.command.CCommand;
import org.eclipse.emfcloud.modelserver.common.codecs.EncodingException;
import org.eclipse.emfcloud.modelserver.edit.CommandCodec;

import com.google.common.base.Preconditions;

public class EcoreModelServerAccess {
	private static Logger LOGGER = Logger.getLogger(EcoreModelServerAccess.class);

	private static final String FORMAT_XMI = "xmi";

	private String sourceURI;
	private ResourceSet resourceSet;

	private EcoreFacade ecoreFacade;

	private ModelServerClient modelServerClient;

	private EditingDomain editingDomain;
	private CommandCodec commandCodec;

	// Added for now, should be removed later
	private EcoreModelIndex modelIndex;

	public EcoreModelServerAccess(String sourceURI, ModelServerClient modelServerClient, EcoreModelIndex modelIndex) {
		Preconditions.checkNotNull(modelServerClient);
		this.sourceURI = sourceURI;
		this.modelServerClient = modelServerClient;
		this.resourceSet = setupResourceSet();
		this.modelIndex = modelIndex;
	}

	public void update() {
		EObject root = ecoreFacade.getSemanticResource().getContents().get(0);
		modelServerClient.update(sourceURI, root, FORMAT_XMI);
	}

	public ResourceSet setupResourceSet() {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getPackageRegistry().put(EcorePackage.eINSTANCE.getNsURI(), EcorePackage.eINSTANCE);
		resourceSet.getPackageRegistry().put(EnotationPackage.eINSTANCE.getNsURI(), EnotationPackage.eINSTANCE);
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		return resourceSet;
	}

	public EcoreFacade getEcoreFacade() {
		if (ecoreFacade == null) {
			createEcoreFacade();
		}
		return ecoreFacade;
	}

	public void setEcoreFacade(EcoreFacade ecoreFacade) {
		this.ecoreFacade = ecoreFacade;
	}

	protected EcoreFacade createEcoreFacade() {
		try {
			Resource notationResource = loadResource(convertToFile(sourceURI).getAbsolutePath()); // leave local for now
			EObject root = modelServerClient.get(sourceURI, FORMAT_XMI).thenApply(res -> res.body()).get();

			Resource semanticResource = loadResource(convertToFile(sourceURI).getAbsolutePath(), root);
			ecoreFacade = new EcoreFacade(semanticResource, notationResource, modelIndex);
			return ecoreFacade;
		} catch (IOException | InterruptedException | ExecutionException e) {
			LOGGER.error(e);
			return null;
		}
	}

	private File convertToFile(String sourceURI) {
		if (sourceURI != null) {
			return new File(sourceURI);
		}
		return null;
	}

	public static String toXMI(Resource resource) throws IOException {
		OutputStream out = new ByteArrayOutputStream();
		resource.save(out, Collections.EMPTY_MAP);
		return out.toString();
	}

	private Resource loadResource(String path, EObject root) throws IOException {
		Resource resource = createResource(path);
		resource.getContents().clear();
		resource.getContents().add(root);
		return resource;
	}

	private Resource loadResource(String path) throws IOException {
		Resource resource = createResource(path);
		resource.load(Collections.EMPTY_MAP);
		return resource;
	}

	private Resource createResource(String path) {
		return resourceSet.createResource(URI.createFileURI(path));
	}

	public void save() {
		try {
			ecoreFacade.getNotationResource().save(Collections.emptyMap());
		} catch (IOException e) {
		}

	}

	public ModelServerClient getModelServerClient() {
		return this.modelServerClient;
	}

	public EditingDomain getEditingDomain() {
		return editingDomain;
	}

	public CommandCodec getCommandCodec() {
		return commandCodec;
	}

	public CompletableFuture<Response<Boolean>> edit(Command command) throws EncodingException {
		CCommand ccommand = getCommandCodec().encode(command);
		return this.modelServerClient.edit(sourceURI, ccommand, FORMAT_XMI);
	}
}
