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
package org.eclipse.emfcloud.ecore.glsp.actions;

import java.util.List;

import org.eclipse.glsp.api.action.kind.ResponseAction;

public class ReturnAttributeTypesAction extends ResponseAction {

	public static String KIND = "returnAttributeTypes";
	private List<String> types;
	
	public ReturnAttributeTypesAction() {
		super(KIND);
	}

	public ReturnAttributeTypesAction(List<String> types) {
		super(KIND);
		this.types = types;
	}

	public List<String> getTypes() {
		return types;
	}

	public void setTypes(List<String> types) {
		this.types = types;
	}
}