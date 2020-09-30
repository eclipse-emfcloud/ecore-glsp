/********************************************************************************
 * Copyright (c) 2019-2020 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 ********************************************************************************/
package org.eclipse.emfcloud.ecore.glsp;

import org.eclipse.glsp.api.labeledit.LabelEditValidator;
import org.eclipse.glsp.api.model.GraphicalModelState;
import org.eclipse.glsp.api.types.ValidationStatus;
import org.eclipse.glsp.graph.GModelElement;

public class EcoreLabelEditValidator implements LabelEditValidator {

   @Override
   public ValidationStatus validate(final GraphicalModelState modelState, final String label,
      final GModelElement element) {
      if (label.length() < 1) {
         return ValidationStatus.error("Name must not be empty");
      }
      if (label.toLowerCase().equals("test")) {
         return ValidationStatus.error("Name cannot be test");
      }

      return ValidationStatus.ok();
   }

}