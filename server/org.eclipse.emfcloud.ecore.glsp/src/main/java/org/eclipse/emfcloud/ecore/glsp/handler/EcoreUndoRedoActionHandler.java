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

import org.apache.log4j.Logger;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelServerAccess;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelState;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.actions.ActionHandler;
import org.eclipse.glsp.server.actions.RedoAction;
import org.eclipse.glsp.server.actions.UndoAction;
import org.eclipse.glsp.server.actions.UndoRedoActionHandler;
import org.eclipse.glsp.server.model.GModelState;

import com.google.common.collect.Lists;

public class EcoreUndoRedoActionHandler implements ActionHandler {
	private static final Logger LOG = Logger.getLogger(UndoRedoActionHandler.class);

	@Override
	public List<Action> execute(Action action, GModelState modelState) {
		EcoreModelServerAccess modelServerAccess = EcoreModelState.getModelServerAccess(modelState);
		boolean success = executeOperation(action, modelServerAccess);
		if (!success) {
			LOG.warn("Cannot undo or redo");
		}
		return List.of();
	}

	private boolean executeOperation(Action action, EcoreModelServerAccess modelServerAccess) {
		if (action instanceof UndoAction) {
			return modelServerAccess.undo();
		} else if (action instanceof RedoAction) {
			return modelServerAccess.redo();
		}
		return false;
	}

	@Override
	public List<Class<? extends Action>> getHandledActionTypes() {
		return Lists.newArrayList(UndoAction.class, RedoAction.class);
	}
}
