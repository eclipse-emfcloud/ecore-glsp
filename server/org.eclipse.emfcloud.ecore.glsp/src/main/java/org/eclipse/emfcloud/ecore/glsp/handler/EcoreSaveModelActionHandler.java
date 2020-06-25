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
package org.eclipse.emfcloud.ecore.glsp.handler;

import java.util.List;

import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelState;
import org.eclipse.glsp.api.action.Action;
import org.eclipse.glsp.api.action.kind.SaveModelAction;
import org.eclipse.glsp.api.action.kind.SetDirtyStateAction;
import org.eclipse.glsp.api.model.GraphicalModelState;
import org.eclipse.glsp.server.actionhandler.BasicActionHandler;

public class EcoreSaveModelActionHandler extends BasicActionHandler<SaveModelAction> {

	@Override
	protected List<Action> executeAction(SaveModelAction action, GraphicalModelState modelState) {
		EcoreModelState.getResourceManager(modelState).save();
		modelState.saveIsDone();
		return List.of(new SetDirtyStateAction(modelState.isDirty()));
	}

}
