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
package org.eclipse.emfcloud.ecore.glsp.util;

import org.eclipse.emf.ecore.EReference;

public class EcoreEdgeUtil {
	public static String getStringId(EReference reference) {
		return Integer.toString(reference.hashCode());
	}

	/**
	*	Retrieves the Edge Id, who is associated with this label.
	*/
	public static String getEdgeId(String labelId) {
		return labelId.split("_")[0];
	}
	
	public static String getInheritanceEdgeId(String sourceId, String targetId ) {
		return sourceId + "_" + targetId;
	}
	
}