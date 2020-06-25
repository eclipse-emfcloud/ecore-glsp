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
import org.eclipse.emfcloud.ecore.glsp.EcoreEditorContext;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelState;
import org.eclipse.glsp.api.action.Action;
import org.eclipse.glsp.api.action.kind.RedoAction;
import org.eclipse.glsp.api.action.kind.RequestBoundsAction;
import org.eclipse.glsp.api.action.kind.SetDirtyStateAction;
import org.eclipse.glsp.api.action.kind.UndoAction;
import org.eclipse.glsp.api.handler.ActionHandler;
import org.eclipse.glsp.api.model.GraphicalModelState;
import org.eclipse.glsp.graph.GModelRoot;
import org.eclipse.glsp.server.actionhandler.UndoRedoActionHandler;

import com.google.common.collect.Lists;

public class EcoreUndoRedoActionHandler implements ActionHandler {
	private static final Logger LOG = Logger.getLogger(UndoRedoActionHandler.class);

	@Override
	public List<Action> execute(Action action, GraphicalModelState modelState) {
		EcoreEditorContext context = EcoreModelState.getEditorContext(modelState);
		boolean success = executeOperation(action, modelState);
		if (success) {
			GModelRoot newRoot = context.getGModelFactory().create();
			return List.of(new RequestBoundsAction(newRoot), new SetDirtyStateAction(modelState.isDirty()));
		}
		LOG.warn("Cannot undo or redo");
		return List.of();
	}

	private boolean executeOperation(Action action, GraphicalModelState modelState) {
		if (action instanceof UndoAction && modelState.canUndo()) {
			modelState.undo();
			return true;
		} else if (action instanceof RedoAction && modelState.canRedo()) {
			modelState.redo();
			return true;
		}
		return false;
	}

	@Override
	public List<Class<? extends Action>> getHandledActionTypes() {
		return Lists.newArrayList(UndoAction.class, RedoAction.class);
	}
}
