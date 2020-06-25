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
package org.eclipse.emfcloud.ecore.glsp.model;

import org.eclipse.emfcloud.ecore.enotation.Diagram;
import org.eclipse.emfcloud.ecore.glsp.EcoreEditorContext;
import org.eclipse.emfcloud.ecore.glsp.EcoreFacade;
import org.eclipse.glsp.api.action.kind.RequestModelAction;
import org.eclipse.glsp.api.factory.ModelFactory;
import org.eclipse.glsp.api.model.GraphicalModelState;
import org.eclipse.glsp.graph.DefaultTypes;
import org.eclipse.glsp.graph.GModelRoot;
import org.eclipse.glsp.graph.builder.impl.GGraphBuilder;

public class EcoreModelFactory implements ModelFactory {
	private static final String ROOT_ID = "sprotty";

	@Override
	public GModelRoot loadModel(RequestModelAction action, GraphicalModelState graphicalModelState) {
		EcoreModelState modelState = EcoreModelState.getModelState(graphicalModelState);
		graphicalModelState.setClientOptions(action.getOptions());

		EcoreEditorContext context = new EcoreEditorContext(modelState);

		modelState.setEditorContext(context);

		EcoreFacade ecoreFacade = context.getEcoreFacade();
		if (ecoreFacade == null) {
			return createEmptyRoot();
		}
		Diagram diagram = ecoreFacade.getDiagram();

		GModelRoot gmodelRoot = context.getGModelFactory().create(ecoreFacade.getEPackage());
		ecoreFacade.initialize(diagram, gmodelRoot);
		modelState.setRoot(gmodelRoot);
		return gmodelRoot;
	}

	private static GModelRoot createEmptyRoot() {
		return new GGraphBuilder(DefaultTypes.GRAPH)//
				.id(ROOT_ID) //
				.build();
	}

}
