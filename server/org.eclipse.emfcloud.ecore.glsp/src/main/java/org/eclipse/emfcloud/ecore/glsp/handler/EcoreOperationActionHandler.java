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
import java.util.Optional;

import org.eclipse.emfcloud.ecore.glsp.operationhandler.ModelserverAwareOperationHandler;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.glsp.server.operations.Operation;
import org.eclipse.glsp.server.operations.OperationActionHandler;
import org.eclipse.glsp.server.operations.OperationHandler;
import org.eclipse.glsp.server.operations.gmodel.CompoundOperationHandler;

public class EcoreOperationActionHandler extends OperationActionHandler {

	@Override
	public List<Action> executeAction(Operation operation, GModelState modelState) {
		// Disable the special handling for CreateOperation, as we don't register
		// 1 handler per element type to create.
		Optional<? extends OperationHandler> operationHandler = operationHandlerRegistry.get(operation);
		if (operationHandler.isPresent()) {
			return executeHandler(operation, operationHandler.get(), modelState);
		}
		return none();
	}

	@Override
	protected List<Action> executeHandler(Operation operation, OperationHandler handler, GModelState gModelState) {
		if (handler instanceof ModelserverAwareOperationHandler || handler instanceof CompoundOperationHandler) {
			handler.execute(operation, gModelState);
		}
		return none();
	}

}