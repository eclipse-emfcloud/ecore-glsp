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

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

import java.io.IOException;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emfcloud.modelserver.common.ModelServerPathParametersV1;
import org.eclipse.emfcloud.modelserver.common.ModelServerPathsV1;
import org.eclipse.emfcloud.modelserver.common.codecs.EncodingException;
import org.eclipse.emfcloud.modelserver.emf.common.JsonResponse;
import org.eclipse.emfcloud.modelserver.emf.common.ModelController;
import org.eclipse.emfcloud.modelserver.emf.common.ModelResourceManager;
import org.eclipse.emfcloud.modelserver.emf.common.ModelServerRoutingV1;
import org.eclipse.emfcloud.modelserver.emf.common.SchemaController;
import org.eclipse.emfcloud.modelserver.emf.common.ServerController;
import org.eclipse.emfcloud.modelserver.emf.common.SessionController;
import org.eclipse.emfcloud.modelserver.emf.common.codecs.CodecsManager;
import org.eclipse.emfcloud.modelserver.emf.common.codecs.JsonCodec;

import com.google.inject.Inject;

import io.javalin.Javalin;
import io.javalin.http.Context;

public class EcoreModelServerRouting extends ModelServerRoutingV1 {

	protected final CodecsManager codecsManager;

	@Inject
	public EcoreModelServerRouting(final Javalin javalin, final ModelResourceManager resourceManager,
			final ModelController modelController, final SchemaController schemaController,
			final ServerController serverController, final SessionController sessionController,
			final CodecsManager codecsManager) {
		super(javalin, resourceManager, modelController, schemaController, serverController, sessionController);
		this.codecsManager = codecsManager;
	}

	protected void createEcoreResources(final Context ctx) {
		getResolvedFileUri(ctx, ModelServerPathParametersV1.MODEL_URI).ifPresent(modelUri -> {
			String nsUri = "";
			if (ctx.queryParamMap().containsKey(EcoreModelServerPathsParameters.NS_URI)) {
				nsUri = ctx.queryParamMap().get(EcoreModelServerPathsParameters.NS_URI).get(0);
			}
			String nsPrefix = "";
			if (ctx.queryParamMap().containsKey(EcoreModelServerPathsParameters.NS_PREFIX)) {
				nsPrefix = ctx.queryParamMap().get(EcoreModelServerPathsParameters.NS_PREFIX).get(0);
			}
			boolean result = ((EcoreModelResourceManager) resourceManager).addNewEcoreResources(modelUri, nsUri,
					nsPrefix);
			ctx.json(result ? JsonResponse.success() : JsonResponse.error());
		});
	}

	protected void createEcoreNotation(final Context ctx) {
		getResolvedFileUri(ctx, ModelServerPathParametersV1.MODEL_URI).ifPresent(modelUri -> {
			EObject result = ((EcoreModelResourceManager) resourceManager).addEnotationResource(modelUri);
			try {
				ctx.json(result != null ? JsonResponse.success(JsonCodec.encode(codecsManager.encode(ctx, result)))
						: JsonResponse.error());
			} catch (EncodingException e) {
				e.printStackTrace();
			}
		});
	}

	protected void deleteEcoreResources(final Context ctx) {
		getResolvedFileUri(ctx, ModelServerPathParametersV1.MODEL_URI).ifPresent(modelUri -> {
			try {
				((EcoreModelResourceManager) resourceManager).deleteEcoreResources(modelUri);
				ctx.json(JsonResponse.success());
			} catch (IOException e) {
				ctx.json(JsonResponse.error());
			}
		});
	}

	protected void deleteEnotationResource(final Context ctx) {
		getResolvedFileUri(ctx, ModelServerPathParametersV1.MODEL_URI).ifPresent(modelUri -> {
			try {
				((EcoreModelResourceManager) resourceManager).deleteEnotationResource(modelUri);
				ctx.json(JsonResponse.success());
			} catch (IOException e) {
				ctx.json(JsonResponse.error());
			}
		});
	}

	@Override
	public void bindRoutes() {
		javalin.routes(this::endpoints);
	}

	private void endpoints() {
		path(ModelServerPathsV1.BASE_PATH, this::apiEndpoints);
	}

	private void apiEndpoints() {
		get(EcoreModelServerPaths.ECORE_CREATE, this::createEcoreResources);
		get(EcoreModelServerPaths.ENOTATION_CREATE, this::createEcoreNotation);
		delete(EcoreModelServerPaths.ECORE_DELETE, this::deleteEcoreResources);
		delete(EcoreModelServerPaths.ENOTATION_DELETE, this::deleteEnotationResource);
	}

}
