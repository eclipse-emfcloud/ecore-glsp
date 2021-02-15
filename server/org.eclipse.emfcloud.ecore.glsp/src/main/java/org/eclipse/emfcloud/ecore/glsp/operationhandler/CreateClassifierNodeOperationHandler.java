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
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emfcloud.ecore.enotation.EnotationFactory;
import org.eclipse.emfcloud.ecore.enotation.SemanticProxy;
import org.eclipse.emfcloud.ecore.enotation.Shape;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelServerAccess;
import org.eclipse.emfcloud.ecore.glsp.model.EcoreModelState;
import org.eclipse.emfcloud.ecore.glsp.util.EcoreConfig.Types;
import org.eclipse.glsp.graph.GraphPackage;
import org.eclipse.glsp.graph.util.GraphUtil;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.glsp.server.operations.CreateNodeOperation;
import org.eclipse.glsp.server.operations.Operation;
import org.eclipse.glsp.server.protocol.GLSPServerException;

import com.google.common.collect.Lists;

public class CreateClassifierNodeOperationHandler
		extends ModelServerAwareBasicCreateOperationHandler<CreateNodeOperation> {

	public CreateClassifierNodeOperationHandler() {
		super(handledElementTypeIds);
	}

	private static List<String> handledElementTypeIds = Lists.newArrayList(Types.ECLASS, Types.ENUM, Types.INTERFACE,
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
	public void executeOperation(CreateNodeOperation operation, GModelState modelState,
			EcoreModelServerAccess modelAccess) throws Exception {

		EClassifier eClassifier = createClassifier(operation.getElementTypeId());
		setName(eClassifier, modelState);

		Shape shape = EnotationFactory.eINSTANCE.createShape();
		shape.setPosition(operation.getLocation().orElse(GraphUtil.point(0, 0)));

		SemanticProxy proxy = EnotationFactory.eINSTANCE.createSemanticProxy();
		proxy.setUri(getSemanticProxyUri(eClassifier));
		shape.setSemanticElement(proxy);

		if (!modelAccess.addEClassifier(EcoreModelState.getModelState(modelState), eClassifier, shape)) {
			throw new GLSPServerException(
					"Could not execute create operation on eClassifier: " + eClassifier.getName());
		}
	}

	protected String getSemanticProxyUri(EClassifier eClassifier) {
		return "//" + eClassifier.getName();
	}

	protected void setName(EClassifier classifier, GModelState modelState) {
		Function<Integer, String> nameProvider = i -> "New" + classifier.eClass().getName() + i;
		int nodeCounter = modelState.getIndex().getCounter(GraphPackage.Literals.GNODE, nameProvider);
		classifier.setName(nameProvider.apply(nodeCounter));
	}

	private EClassifier createClassifier(String elementTypeId) {
		if (elementTypeId.equals(Types.ENUM)) {
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
		return "Create ecore classifier";
	}

}
