/*******************************************************************************
 * Copyright (c) 2020 EclipseSource and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ********************************************************************************/
package org.eclipse.emfcloud.ecore.glsp.operationhandler;

import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelServerAccess;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelState;
import org.eclipse.glsp.server.internal.util.GenericsUtil;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.glsp.server.operations.BasicOperationHandler;
import org.eclipse.glsp.server.operations.Operation;

@SuppressWarnings("restriction")
public abstract class ModelServerAwareBasicOperationHandler<T extends Operation> extends BasicOperationHandler<T>
		implements ModelserverAwareOperationHandler<T> {

	@SuppressWarnings("unchecked")
	@Override
	protected Class<T> deriveOperationType() {
		return (Class<T>) (GenericsUtil.getParametrizedType(getClass(), ModelServerAwareBasicOperationHandler.class))
				.getActualTypeArguments()[0];
	}

	@Override
	public void executeOperation(final T operation, final GModelState modelState) {
		if (handles(operation)) {
			try {
				EcoreModelServerAccess modelAccess = EcoreModelState.getModelServerAccess(modelState);
				executeOperation(operationType.cast(operation), modelState, modelAccess);
			} catch (Exception ex) {
				if (ex instanceof RuntimeException) {
					// simply re-throw
					throw (RuntimeException) ex;
				} else {
					// wrap
					throw new RuntimeException(ex);
				}
			}
		}
	}
}