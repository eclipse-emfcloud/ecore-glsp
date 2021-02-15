/*******************************************************************************
 * Copyright (c) 2020 EclipseSource and others.
 *  
 *   This program and the accompanying materials are made available under the
 *   terms of the Eclipse Public License v. 2.0 which is available at
 *   http://www.eclipse.org/legal/epl-2.0.
 *  
 *   This Source Code may also be made available under the following Secondary
 *   Licenses when the conditions for such availability set forth in the Eclipse
 *   Public License v. 2.0 are satisfied: GNU General Public License, version 2
 *   with the GNU Classpath Exception which is available at
 *   https://www.gnu.org/software/classpath/license.html.
 *  
 *   SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 ******************************************************************************/
package org.eclipse.emfcloud.ecore.glsp.operationhandler;

import java.util.List;

import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelServerAccess;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelState;
import org.eclipse.glsp.server.internal.util.GenericsUtil;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.glsp.server.operations.BasicCreateOperationHandler;
import org.eclipse.glsp.server.operations.CreateOperation;

import com.google.common.collect.Lists;

@SuppressWarnings("restriction")
public abstract class ModelServerAwareBasicCreateOperationHandler<T extends CreateOperation>
		extends BasicCreateOperationHandler<T> implements ModelserverAwareOperationHandler<T> {

	public ModelServerAwareBasicCreateOperationHandler(final String... elementTypeIds) {
		super(Lists.newArrayList(elementTypeIds));
	}

	public ModelServerAwareBasicCreateOperationHandler(final List<String> handledElementTypeIds) {
		super(handledElementTypeIds);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Class<T> deriveOperationType() {
		return (Class<T>) (GenericsUtil.getParametrizedType(getClass(),
				ModelServerAwareBasicCreateOperationHandler.class)).getActualTypeArguments()[0];
	}

	@Override
	public void executeOperation(final T operation, final GModelState modelState) {
		if (handles(operation)) {
			try {
				EcoreModelServerAccess modelServerAccess = EcoreModelState.getModelServerAccess(modelState);
				executeOperation(operationType.cast(operation), modelState, modelServerAccess);
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
