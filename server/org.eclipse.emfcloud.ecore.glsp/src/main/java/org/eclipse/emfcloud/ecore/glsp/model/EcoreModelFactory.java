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

import org.apache.log4j.Logger;
import org.eclipse.emfcloud.ecore.enotation.Diagram;
import org.eclipse.emfcloud.ecore.glsp.EcoreFacade;
import org.eclipse.glsp.graph.DefaultTypes;
import org.eclipse.glsp.graph.GModelRoot;
import org.eclipse.glsp.graph.builder.impl.GGraphBuilder;
import org.eclipse.glsp.server.features.core.model.GModelFactory;
import org.eclipse.glsp.server.model.GModelState;

public class EcoreModelFactory implements GModelFactory {

	private static Logger LOGGER = Logger.getLogger(EcoreModelFactory.class);
	private static final String ROOT_ID = "sprotty";

	@Override
	public void createGModel(GModelState gModelState) {
		EcoreModelState modelState = EcoreModelState.getModelState(gModelState);

		EcoreFacade ecoreFacade = EcoreModelState.getEcoreFacade(modelState);
		if (ecoreFacade == null) {
			LOGGER.error("EcoreFacade could not be found, return empty model");
			modelState.setRoot(createEmptyRoot());
			return;
		}

		Diagram diagram = ecoreFacade.getDiagram();
		GModelRoot gmodelRoot = EcoreModelState.getEditorContext(modelState).getGModelFactory()
				.create(ecoreFacade.getEPackage());
		ecoreFacade.initialize(diagram, gmodelRoot);
		modelState.setRoot(gmodelRoot);
	}

	private static GModelRoot createEmptyRoot() {
		return new GGraphBuilder(DefaultTypes.GRAPH)//
				.id(ROOT_ID) //
				.build();
	}

}
