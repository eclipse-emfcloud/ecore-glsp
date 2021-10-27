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

import org.eclipse.emfcloud.ecore.glsp.actions.ReturnAttributeTypesAction;
import org.eclipse.emfcloud.ecore.glsp.actions.SetSemanticUriAction;
import org.eclipse.emfcloud.ecore.glsp.handler.EcoreComputedBoundsActionHandler;
import org.eclipse.emfcloud.ecore.glsp.handler.EcoreGetAttributeTypesActionHandler;
import org.eclipse.emfcloud.ecore.glsp.handler.EcoreOperationActionHandler;
import org.eclipse.emfcloud.ecore.glsp.handler.EcoreSaveModelActionHandler;
import org.eclipse.emfcloud.ecore.glsp.handler.EcoreUndoRedoActionHandler;
import org.eclipse.emfcloud.ecore.glsp.handler.RequestSemanticUriActionHandler;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelFactory;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelSourceLoader;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelStateProvider;
import org.eclipse.emfcloud.ecore.glsp.operationhandler.CreateClassifierChildNodeOperationHandler;
import org.eclipse.emfcloud.ecore.glsp.operationhandler.CreateClassifierNodeOperationHandler;
import org.eclipse.emfcloud.ecore.glsp.operationhandler.CreateEcoreEdgeOperationHandler;
import org.eclipse.emfcloud.ecore.glsp.operationhandler.EcoreChangeBoundsOperationHandler;
import org.eclipse.emfcloud.ecore.glsp.operationhandler.EcoreChangeRoutingPointsOperationHandler;
import org.eclipse.emfcloud.ecore.glsp.operationhandler.EcoreDeleteOperationHandler;
import org.eclipse.emfcloud.ecore.glsp.operationhandler.EcoreLabelEditOperationHandler;
import org.eclipse.emfcloud.ecore.glsp.operationhandler.EcoreLayoutOperationHandler;
import org.eclipse.emfcloud.ecore.glsp.palette.EcoreToolPaletteItemProvider;
import org.eclipse.emfcloud.ecore.glsp.registry.EcoreDIOperationHandlerRegistry;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.actions.ActionHandler;
import org.eclipse.glsp.server.actions.SaveModelActionHandler;
import org.eclipse.glsp.server.di.DefaultGLSPModule;
import org.eclipse.glsp.server.diagram.DiagramConfiguration;
import org.eclipse.glsp.server.features.core.model.ComputedBoundsActionHandler;
import org.eclipse.glsp.server.features.core.model.GModelFactory;
import org.eclipse.glsp.server.features.core.model.ModelSourceLoader;
import org.eclipse.glsp.server.features.directediting.ApplyLabelEditOperationHandler;
import org.eclipse.glsp.server.features.toolpalette.ToolPaletteItemProvider;
import org.eclipse.glsp.server.features.undoredo.UndoRedoActionHandler;
import org.eclipse.glsp.server.layout.ILayoutEngine;
import org.eclipse.glsp.server.model.ModelStateProvider;
import org.eclipse.glsp.server.operations.OperationActionHandler;
import org.eclipse.glsp.server.operations.OperationHandler;
import org.eclipse.glsp.server.operations.OperationHandlerRegistry;
import org.eclipse.glsp.server.operations.gmodel.ChangeBoundsOperationHandler;
import org.eclipse.glsp.server.operations.gmodel.ChangeRoutingPointsHandler;
import org.eclipse.glsp.server.operations.gmodel.CompoundOperationHandler;
import org.eclipse.glsp.server.operations.gmodel.DeleteOperationHandler;
import org.eclipse.glsp.server.operations.gmodel.LayoutOperationHandler;
import org.eclipse.glsp.server.protocol.GLSPServer;
import org.eclipse.glsp.server.utils.MultiBinding;

public class EcoreGLSPModule extends DefaultGLSPModule {

	@Override
	protected void configureActionHandlers(MultiBinding<ActionHandler> bindings) {
		super.configureActionHandlers(bindings);
		bindings.add(EcoreGetAttributeTypesActionHandler.class);
		bindings.rebind(SaveModelActionHandler.class, EcoreSaveModelActionHandler.class);
		bindings.rebind(ComputedBoundsActionHandler.class, EcoreComputedBoundsActionHandler.class);
		bindings.rebind(OperationActionHandler.class, EcoreOperationActionHandler.class);
		bindings.rebind(UndoRedoActionHandler.class, EcoreUndoRedoActionHandler.class);
		bindings.add(RequestSemanticUriActionHandler.class);
	}

	@Override
	public Class<? extends ModelSourceLoader> bindSourceModelLoader() {
		return EcoreModelSourceLoader.class;
	}

	@Override
	public Class<? extends GModelFactory> bindGModelFactory() {
		return EcoreModelFactory.class;
	}

	@Override
	protected Class<? extends ILayoutEngine> bindLayoutEngine() {
		return EcoreLayoutEngine.class;
	}

	@Override
	protected Class<? extends ToolPaletteItemProvider> bindToolPaletteItemProvider() {
		return EcoreToolPaletteItemProvider.class;
	}

	@Override
	protected void configureClientActions(MultiBinding<Action> bindings) {
		super.configureClientActions(bindings);
		bindings.add(ReturnAttributeTypesAction.class);
		bindings.add(SetSemanticUriAction.class);
	}

	@Override
	protected Class<? extends OperationHandlerRegistry> bindOperationHandlerRegistry() {
		return EcoreDIOperationHandlerRegistry.class;
	}

	@Override
	protected void configureOperationHandlers(MultiBinding<OperationHandler> bindings) {
		super.configureOperationHandlers(bindings);
		bindings.add(CompoundOperationHandler.class);
		bindings.rebind(ChangeBoundsOperationHandler.class, EcoreChangeBoundsOperationHandler.class);
		bindings.rebind(DeleteOperationHandler.class, EcoreDeleteOperationHandler.class);
		bindings.add(CreateClassifierNodeOperationHandler.class);
		bindings.add(CreateEcoreEdgeOperationHandler.class);
		bindings.add(CreateClassifierChildNodeOperationHandler.class);
		bindings.rebind(ApplyLabelEditOperationHandler.class, EcoreLabelEditOperationHandler.class);
		bindings.rebind(ChangeRoutingPointsHandler.class, EcoreChangeRoutingPointsOperationHandler.class);
		bindings.rebind(LayoutOperationHandler.class, EcoreLayoutOperationHandler.class);
	}

	@Override
	protected Class<? extends ModelStateProvider> bindModelStateProvider() {
		return EcoreModelStateProvider.class;
	}

	@Override
	protected void configureDiagramConfigurations(MultiBinding<DiagramConfiguration> bindings) {
		bindings.add(EcoreDiagramConfiguration.class);
	}

	@Override
	protected Class<? extends GLSPServer> bindGLSPServer() {
		return EcoreGLSPServer.class;
	}

	@Override
	public void configure() {
		super.configure();
		bind(ModelServerClientProvider.class).asEagerSingleton();
	}

}
