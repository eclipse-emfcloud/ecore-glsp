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

import org.eclipse.emfcloud.ecore.glsp.actions.AttributeTypesAction;
import org.eclipse.emfcloud.ecore.glsp.handler.EcoreRequestMarkersActionHandler;
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
import org.eclipse.glsp.api.configuration.ServerConfiguration;
import org.eclipse.glsp.api.diagram.DiagramConfiguration;
import org.eclipse.glsp.api.factory.ModelFactory;
import org.eclipse.glsp.api.handler.ActionHandler;
import org.eclipse.glsp.api.handler.OperationHandler;
import org.eclipse.glsp.api.layout.ILayoutEngine;
import org.eclipse.glsp.api.model.ModelStateProvider;
import org.eclipse.glsp.api.provider.ToolPaletteItemProvider;
import org.eclipse.glsp.api.registry.OperationHandlerRegistry;
import org.eclipse.glsp.server.actionhandler.ComputedBoundsActionHandler;
import org.eclipse.glsp.server.actionhandler.OperationActionHandler;
import org.eclipse.glsp.server.actionhandler.RequestMarkersHandler;
import org.eclipse.glsp.server.actionhandler.SaveModelActionHandler;
import org.eclipse.glsp.server.actionhandler.UndoRedoActionHandler;
import org.eclipse.glsp.server.di.DefaultGLSPModule;
import org.eclipse.glsp.server.di.MultiBindConfig;
import org.eclipse.glsp.server.operationhandler.CompoundOperationHandler;
import org.eclipse.glsp.server.operationhandler.LayoutOperationHandler;

public class EcoreGLSPModule extends DefaultGLSPModule {

	@Override
	protected void configureActionHandlers(MultiBindConfig<ActionHandler> bindings) {
		super.configureActionHandlers(bindings);
		bindings.add(EcoreGetAttributeTypesActionHandler.class);
		bindings.rebind(RequestMarkersHandler.class, EcoreRequestMarkersActionHandler.class);
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
	protected Class<? extends OperationHandlerRegistry> bindOperationHandlerRegistry() {
		return EcoreDIOperationHandlerRegistry.class;
	}

	@Override
	protected void configureOperationHandlers(MultiBindConfig<OperationHandler> bindings) {
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
	protected Class<? extends ServerConfiguration> bindServerConfiguration() {
		return EcoreServerConfiguration.class;
	}

	@Override
	protected Class<? extends ModelStateProvider> bindModelStateProvider() {
		return EcoreModelStateProvider.class;
	}

	@Override
	protected void configureDiagramConfigurations(MultiBindConfig<DiagramConfiguration> bindings) {
		bindings.add(EcoreDiagramConfiguration.class);
	}

}
