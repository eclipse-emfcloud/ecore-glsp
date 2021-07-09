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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emfcloud.ecore.glsp.actions.AttributeTypesAction;
import org.eclipse.emfcloud.ecore.glsp.actions.ReturnAttributeTypesAction;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelState;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.actions.BasicActionHandler;
import org.eclipse.glsp.server.model.GModelState;

public class EcoreGetAttributeTypesActionHandler extends BasicActionHandler<AttributeTypesAction> {

	@Override
	protected List<Action> executeAction(AttributeTypesAction action, GModelState modelState) {
		List<String> types = EcoreModelState.getResourceManager(modelState).getAllETypes().stream()
				.map(ENamedElement::getName).collect(Collectors.toList());
		Collections.sort(types);
		return List.of(new ReturnAttributeTypesAction(types));
	}

}