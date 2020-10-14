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

import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emfcloud.ecore.glsp.EcoreEditorContext;
import org.eclipse.emfcloud.ecore.glsp.EcoreFacade;
import org.eclipse.emfcloud.ecore.glsp.EcoreModelIndex;
import org.eclipse.emfcloud.ecore.glsp.ResourceManager;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.glsp.server.model.GModelStateImpl;

public class EcoreModelState extends GModelStateImpl implements GModelState {

	private EcoreEditorContext editorContext;

	public static EcoreModelState getModelState(GModelState state) {
		if (!(state instanceof EcoreModelState)) {
			throw new IllegalArgumentException("Argument must be a EcoreModelState");
		}
		return ((EcoreModelState) state);
	}

	public static EcoreEditorContext getEditorContext(GModelState state) {
		return getModelState(state).getEditorContext();
	}

	public static ResourceManager getResourceManager(GModelState modelState) {
		return getEditorContext(modelState).getResourceManager();
	}

	public static EcoreFacade getEcoreFacade(GModelState modelState) {
		return getEditorContext(modelState).getEcoreFacade();
	}

	public EcoreEditorContext getEditorContext() {
		return editorContext;
	}

	public void setEditorContext(EcoreEditorContext editorContext) {
		this.editorContext = editorContext;
		setCommandStack((BasicCommandStack) editorContext.getResourceManager().getEditingDomain().getCommandStack());
	}

	@Override
	public EcoreModelIndex getIndex() {
		return EcoreModelIndex.get(getRoot());
	}

}
