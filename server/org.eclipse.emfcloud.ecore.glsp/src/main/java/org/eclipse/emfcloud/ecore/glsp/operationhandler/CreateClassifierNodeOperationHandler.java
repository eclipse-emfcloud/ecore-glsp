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

import java.util.List;
import java.util.function.Function;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emfcloud.ecore.enotation.Diagram;
import org.eclipse.emfcloud.ecore.enotation.Shape;
import org.eclipse.emfcloud.ecore.glsp.EcoreEditorContext;
import org.eclipse.emfcloud.ecore.glsp.EcoreFacade;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelState;
import org.eclipse.emfcloud.ecore.glsp.util.EcoreConfig.Types;
import org.eclipse.emfcloud.ecore.glsp.util.EcoreEdgeUtil;
import org.eclipse.glsp.api.model.GraphicalModelState;
import org.eclipse.glsp.api.operation.Operation;
import org.eclipse.glsp.api.operation.kind.CreateNodeOperation;
import org.eclipse.glsp.graph.GraphPackage;
import org.eclipse.glsp.server.operationhandler.BasicOperationHandler;

import com.google.common.collect.Lists;

public class CreateClassifierNodeOperationHandler extends BasicOperationHandler<CreateNodeOperation> {

	private List<String> handledElementTypeIds = Lists.newArrayList(Types.ECLASS, Types.ENUM, Types.INTERFACE,
			Types.ABSTRACT, Types.DATATYPE);

	@Override
	public boolean handles(Operation execAction) {
		if (execAction instanceof CreateNodeOperation) {
			CreateNodeOperation action = (CreateNodeOperation) execAction;
			return handledElementTypeIds.contains(action.getElementTypeId());
		}
		return false;
	}

	@Override
	public void executeOperation(CreateNodeOperation operation, GraphicalModelState modelState) {
		String elementTypeId = operation.getElementTypeId();
		EcoreEditorContext context = EcoreModelState.getEditorContext(modelState);
		EcoreFacade facade = context.getEcoreFacade();
		EPackage ePackage = facade.getEPackage();
		EClassifier eClassifier = createClassifier(elementTypeId);

		setName(eClassifier, modelState);
		ePackage.getEClassifiers().add(eClassifier);
		Diagram diagram = facade.getDiagram();
		Shape shape = facade.initializeShape(eClassifier);
		if (operation.getLocation() != null) {
			operation.getLocation().ifPresent(location -> shape.setPosition(EcoreEdgeUtil.copy(location)));
		}
		diagram.getElements().add(shape);
	}

	protected void setName(EClassifier classifier, GraphicalModelState modelState) {
		Function<Integer, String> nameProvider = i -> "New" + classifier.eClass().getName() + i;
		int nodeCounter = modelState.getIndex().getCounter(GraphPackage.Literals.GNODE, nameProvider);
		classifier.setName(nameProvider.apply(nodeCounter));
	}

	private EClassifier createClassifier(String elementTypeId) {
		if (elementTypeId.equals((Types.ENUM))) {
			return EcoreFactory.eINSTANCE.createEEnum();
		} else if (elementTypeId.equals(Types.DATATYPE)) {
			EDataType dataType = EcoreFactory.eINSTANCE.createEDataType();
			dataType.setInstanceClass(Object.class);
			dataType.setInstanceClassName("java.lang.Object");
			return dataType;
		} else {
			EClass eClass = EcoreFactory.eINSTANCE.createEClass();
			if (elementTypeId.equals(Types.ABSTRACT)) {
				eClass.setAbstract(true);
			} else if (elementTypeId.equals(Types.INTERFACE)) {
				eClass.setInterface(true);
			}
			return eClass;
		}
	}

	@Override
	public String getLabel() {
		return "Create ecore edge";
	}

}
