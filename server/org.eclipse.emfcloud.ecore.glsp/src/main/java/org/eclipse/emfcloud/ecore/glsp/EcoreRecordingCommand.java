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

import org.eclipse.emf.common.command.AbstractCommand;
import org.eclipse.emf.ecore.change.ChangeDescription;
import org.eclipse.emf.ecore.change.util.ChangeRecorder;

public class EcoreRecordingCommand extends AbstractCommand {

	private Runnable runnable;
	private ChangeDescription change;
	private EcoreEditorContext context;

	public EcoreRecordingCommand(EcoreEditorContext context, String label, Runnable runnable) {
		super(label);
		this.context = context;
		this.runnable = runnable;
	}

	@Override
	protected boolean prepare() {
		return change == null;
	}

	@Override
	public void execute() {
		ChangeRecorder recorder = new ChangeRecorder(context.getResourceManager().getEditingDomain().getResourceSet());
		try {
			runnable.run();
		} finally {
			change = recorder.endRecording();
			recorder.dispose();
			runnable = null;
		}
	}

	@Override
	public boolean canUndo() {
		return change != null;
	}

	@Override
	public void undo() {
		applyChanges();
	}

	@Override
	public void redo() {
		applyChanges();
	}

	private void applyChanges() {
		EcoreModelIndex index = context.getModelState().getIndex();
		change.getObjectsToDetach().forEach(index::remove);
		change.getObjectsToAttach().forEach(index::add);
		change.applyAndReverse();
	}

}