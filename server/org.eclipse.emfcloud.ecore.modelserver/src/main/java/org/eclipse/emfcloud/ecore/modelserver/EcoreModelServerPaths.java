/********************************************************************************
 * Copyright (c) 2021 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ********************************************************************************/
package org.eclipse.emfcloud.ecore.modelserver;

import org.eclipse.emfcloud.modelserver.common.ModelServerPaths;

public interface EcoreModelServerPaths extends ModelServerPaths {

	String ECORE_CREATE = "ecore/create";
	String ENOTATION_CREATE = "enotation/create";
	
	String ECORE_DELETE = "ecore/delete";
	String ENOTATION_DELETE = "enotation/delete";

}