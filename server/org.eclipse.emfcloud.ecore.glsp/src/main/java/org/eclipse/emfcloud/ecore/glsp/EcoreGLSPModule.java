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

import org.eclipse.emfcloud.ecore.glsp.actions.ReturnAttributeTypesAction;
import org.eclipse.emfcloud.ecore.glsp.handler.EcoreComputedBoundsActionHandler;
import org.eclipse.emfcloud.ecore.glsp.handler.EcoreGetAttributeTypesActionHandler;
import org.eclipse.emfcloud.ecore.glsp.handler.EcoreOperationActionHandler;
import org.eclipse.emfcloud.ecore.glsp.handler.EcoreSaveModelActionHandler;
import org.eclipse.emfcloud.ecore.glsp.handler.EcoreUndoRedoActionHandler;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelFactory;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelStateProvider;
import org.eclipse.emfcloud.ecore.glsp.operationhandler.ChangeRoutingPointsOperationHandler;
import org.eclipse.emfcloud.ecore.glsp.operationhandler.CreateClassifierChildNodeOperationHandler;
import org.eclipse.emfcloud.ecore.glsp.operationhandler.CreateClassifierNodeOperationHandler;
import org.eclipse.emfcloud.ecore.glsp.operationhandler.CreateEcoreEdgeOperationHandler;
import org.eclipse.emfcloud.ecore.glsp.operationhandler.EcoreChangeBoundsOperationHandler;
import org.eclipse.emfcloud.ecore.glsp.operationhandler.EcoreDeleteOperationHandler;
import org.eclipse.emfcloud.ecore.glsp.operationhandler.EcoreLabelEditOperationHandler;
import org.eclipse.emfcloud.ecore.glsp.palette.EcoreToolPaletteItemProvider;
import org.eclipse.emfcloud.ecore.glsp.registry.EcoreDIOperationHandlerRegistry;
import org.eclipse.glsp.server.DefaultGLSPModule;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.actions.ActionHandler;
import org.eclipse.glsp.server.actions.ComputedBoundsActionHandler;
import org.eclipse.glsp.server.actions.SaveModelActionHandler;
import org.eclipse.glsp.server.actions.UndoRedoActionHandler;
import org.eclipse.glsp.server.diagram.DiagramConfiguration;
import org.eclipse.glsp.server.factory.ModelFactory;
import org.eclipse.glsp.server.features.toolpalette.ToolPaletteItemProvider;
import org.eclipse.glsp.server.layout.ILayoutEngine;
import org.eclipse.glsp.server.layout.ServerLayoutConfiguration;
import org.eclipse.glsp.server.model.ModelStateProvider;
import org.eclipse.glsp.server.operations.OperationActionHandler;
import org.eclipse.glsp.server.operations.OperationHandler;
import org.eclipse.glsp.server.operations.OperationHandlerRegistry;
import org.eclipse.glsp.server.operations.gmodel.CompoundOperationHandler;
import org.eclipse.glsp.server.operations.gmodel.LayoutOperationHandler;
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
	}
	
	@Override
	public Class<? extends ModelFactory> bindModelFactory() {
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
	}
	
	@Override
	protected Class<? extends OperationHandlerRegistry> bindOperationHandlerRegistry() {
		return EcoreDIOperationHandlerRegistry.class;
	}

	@Override
	protected void configureOperationHandlers(MultiBinding<OperationHandler> bindings) {
		bindings.add(CompoundOperationHandler.class);
		bindings.add(EcoreChangeBoundsOperationHandler.class);
		bindings.add(EcoreDeleteOperationHandler.class);
		bindings.add(CreateClassifierNodeOperationHandler.class);
		bindings.add(CreateEcoreEdgeOperationHandler.class);
		bindings.add(CreateClassifierChildNodeOperationHandler.class);
		bindings.add(EcoreLabelEditOperationHandler.class);
		bindings.add(ChangeRoutingPointsOperationHandler.class);
		bindings.add(LayoutOperationHandler.class);
	}

	@Override
	protected Class<? extends ServerLayoutConfiguration> bindServerLayoutConfiguration() {
		return EcoreServerConfiguration.class;
	}

	@Override
	protected Class<? extends ModelStateProvider> bindModelStateProvider() {
		return EcoreModelStateProvider.class;
	}

	@Override
	protected void configureDiagramConfigurations(MultiBinding<DiagramConfiguration> bindings) {
		bindings.add(EcoreDiagramConfiguration.class);
	}

}
