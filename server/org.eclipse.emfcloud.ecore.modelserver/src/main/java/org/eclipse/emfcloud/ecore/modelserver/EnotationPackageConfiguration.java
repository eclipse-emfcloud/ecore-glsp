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

import java.util.Collection;

import org.eclipse.emfcloud.ecore.enotation.EnotationPackage;
import org.eclipse.emfcloud.modelserver.emf.configuration.EPackageConfiguration;

import com.google.common.collect.Lists;

public class EnotationPackageConfiguration implements EPackageConfiguration {

	public String getId() {
		return EnotationPackage.eINSTANCE.getNsURI();
	}

	public Collection<String> getFileExtensions() {
		return Lists.newArrayList("enotation");
	}

	public void registerEPackage() {
		EnotationPackage.eINSTANCE.eClass();
	}

}
