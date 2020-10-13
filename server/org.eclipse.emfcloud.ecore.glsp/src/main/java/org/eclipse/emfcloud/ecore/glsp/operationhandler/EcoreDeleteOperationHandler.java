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
package org.eclipse.emfcloud.ecore.glsp.operationhandler;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.EcoreUtil.UsageCrossReferencer;
import org.eclipse.emfcloud.ecore.enotation.NotationElement;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelState;
import org.eclipse.emfcloud.ecore.glsp.util.EcoreEdgeUtil;
import org.eclipse.glsp.graph.GModelIndex;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.glsp.server.operations.gmodel.DeleteOperationHandler;

public class EcoreDeleteOperationHandler extends DeleteOperationHandler {
	@Override
	protected boolean delete(String elementId, GModelIndex index, GModelState graphicalModelState) {
		super.delete(elementId, index, graphicalModelState);
		EcoreModelState modelState = EcoreModelState.getModelState(graphicalModelState);
		
		Optional<EObject> semantic = modelState.getIndex().getSemantic(elementId);
		Optional<NotationElement> notation = modelState.getIndex().getNotation(elementId);
		
		semantic.ifPresent(element -> {
			if(element instanceof EReference && ((EReference) element).getEOpposite() != null) {
				EcoreUtil.delete(((EReference) element).getEOpposite());
				modelState.getIndex().getBidirectionalReferences().remove(EcoreEdgeUtil.getStringId((EReference)element));
			}
			if (element instanceof EClassifier) {
				// Manually clean-up all EReferences/EAttributes typed by this classifier, to avoid leaving untyped
				// features (Which is legal although invalid in EMF; and not well supported by Ecore GLSP Diagrams)
				Collection<Setting> usages = UsageCrossReferencer.find(element, element.eResource().getResourceSet());
				for (Setting setting : usages) {
					if (setting.getEStructuralFeature().isChangeable() && setting.getEObject() instanceof EStructuralFeature) {
						EObject settingSource = setting.getEObject();
						EcoreUtil.delete(settingSource);
					}
				}
			}
		});
		
		notation.ifPresent(EcoreUtil::delete);
		semantic.ifPresent(EcoreUtil::delete);
		
		return true;
	}
	
}
