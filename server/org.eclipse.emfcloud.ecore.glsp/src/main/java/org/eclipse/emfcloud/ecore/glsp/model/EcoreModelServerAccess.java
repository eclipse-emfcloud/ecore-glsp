/********************************************************************************
 * Copyright (c) 2020-2021 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ********************************************************************************/
package org.eclipse.emfcloud.ecore.glsp.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.EcoreUtil.UsageCrossReferencer;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emfcloud.ecore.enotation.Diagram;
import org.eclipse.emfcloud.ecore.enotation.Edge;
import org.eclipse.emfcloud.ecore.enotation.EnotationFactory;
import org.eclipse.emfcloud.ecore.enotation.EnotationPackage;
import org.eclipse.emfcloud.ecore.enotation.NotationElement;
import org.eclipse.emfcloud.ecore.enotation.SemanticProxy;
import org.eclipse.emfcloud.ecore.enotation.Shape;
import org.eclipse.emfcloud.ecore.glsp.EcoreFacade;
import org.eclipse.emfcloud.ecore.modelserver.EcoreModelServerClient;
import org.eclipse.emfcloud.modelserver.client.ModelServerClientApi;
import org.eclipse.emfcloud.modelserver.client.NotificationSubscriptionListener;
import org.eclipse.emfcloud.modelserver.command.CCommand;
import org.eclipse.emfcloud.modelserver.command.CCommandFactory;
import org.eclipse.emfcloud.modelserver.command.CCompoundCommand;
import org.eclipse.emfcloud.modelserver.edit.EMFCommandType;
import org.eclipse.emfcloud.modelserver.edit.command.AddCommandContribution;
import org.eclipse.emfcloud.modelserver.edit.command.RemoveCommandContribution;
import org.eclipse.emfcloud.modelserver.edit.command.SetCommandContribution;
import org.eclipse.glsp.graph.GEdge;
import org.eclipse.glsp.graph.GModelElement;
import org.eclipse.glsp.graph.GNode;
import org.eclipse.glsp.graph.GPoint;
import org.eclipse.glsp.graph.GraphPackage;
import org.eclipse.glsp.server.protocol.GLSPServerException;
import org.eclipse.glsp.server.types.ElementAndBounds;
import org.eclipse.glsp.server.types.ElementAndRoutingPoints;

import com.google.common.base.Preconditions;

public class EcoreModelServerAccess {

	private static Logger LOGGER = Logger.getLogger(EcoreModelServerAccess.class);

	private static final String FORMAT_XMI = "xmi";
	public static final String ECORE_EXTENSION = ".ecore";
	public static final String NOTATION_EXTENSION = ".enotation";

	private String baseSourceUri;

	private EcoreModelServerClient modelServerClient;
	private NotificationSubscriptionListener<EObject> subscriptionListener;

	public EcoreModelServerAccess(final String sourceURI, final EcoreModelServerClient modelServerClient) {
		Preconditions.checkNotNull(modelServerClient);
		this.baseSourceUri = sourceURI.substring(0, sourceURI.lastIndexOf('.'));
		this.modelServerClient = modelServerClient;
	}

	public String getSemanticURI() {
		return baseSourceUri + ECORE_EXTENSION;
	}

	public String getNotationURI() {
		return baseSourceUri + NOTATION_EXTENSION;
	}

	public ModelServerClientApi<EObject> getModelServerClient() {
		return modelServerClient;
	}

	public EObject getModel() {
		try {
			return modelServerClient.get(getSemanticURI(), FORMAT_XMI).thenApply(res -> res.body()).get();
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.error(e);
			throw new GLSPServerException("Error during model loading", e);
		}
	}

	public EObject getNotationModel() {
		try {
			return modelServerClient.get(getNotationURI(), FORMAT_XMI).thenApply(res -> res.body()).get();
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.error(e);
//			throw new GLSPServerException("Error during model loading", e);
			LOGGER.error("Error during model loading");
		}
		return null;
	}

	public void subscribe(NotificationSubscriptionListener<EObject> subscriptionListener) {
		LOGGER.debug("EcoreModelServerAccess - subscribe");
		this.subscriptionListener = subscriptionListener;
		this.modelServerClient.subscribe(getSemanticURI(), subscriptionListener, FORMAT_XMI);
	}

	public void unsubscribe() {
		LOGGER.debug("EcoreModelServerAccess - unsubscribe");
		if (subscriptionListener != null) {
			this.modelServerClient.unsubscribe(getSemanticURI());
		}
	}

	private EPackage getEPackage(EcoreModelState modelState) {
		EcoreFacade facade = EcoreModelState.getEcoreFacade(modelState);
		return facade.getEPackage();
	}

	private Diagram getDiagram(EcoreModelState modelState) {
		EcoreFacade facade = EcoreModelState.getEcoreFacade(modelState);
		return facade.getDiagram();
	}

	public boolean addEClassifier(EcoreModelState modelState, EClassifier newEClassifier, Shape newShape) {
		CCompoundCommand compoundCommand = CCommandFactory.eINSTANCE.createCompoundCommand();
		compoundCommand.setType(EMFCommandType.COMPOUND);

		AddCommand addEClassifier = createAddCommand(modelState, getEPackage(modelState),
				EcorePackage.Literals.EPACKAGE__ECLASSIFIERS, newEClassifier);
		AddCommand addShape = createAddCommand(modelState, getDiagram(modelState),
				EnotationPackage.Literals.DIAGRAM__ELEMENTS, newShape);

		compoundCommand.getCommands().add(AddCommandContribution.clientCommand(addEClassifier));
		compoundCommand.getCommands().add(AddCommandContribution.clientCommand(addShape));

		return this.edit(compoundCommand);
	}

	public boolean addEReference(EcoreModelState modelState, EReference newEReference, EClassifier source,
			Edge newEdge) {
		CCompoundCommand compoundCommand = CCommandFactory.eINSTANCE.createCompoundCommand();
		compoundCommand.setType(EMFCommandType.COMPOUND);

		AddCommand addEReference = createAddCommand(modelState, source,
				EcorePackage.Literals.ECLASS__ESTRUCTURAL_FEATURES, newEReference);
		AddCommand addEdge = createAddCommand(modelState, getDiagram(modelState),
				EnotationPackage.Literals.DIAGRAM__ELEMENTS, newEdge);

		compoundCommand.getCommands().add(AddCommandContribution.clientCommand(addEReference));
		compoundCommand.getCommands().add(AddCommandContribution.clientCommand(addEdge));

		return this.edit(compoundCommand);
	}

	public boolean addEReferenceBidirectional(EcoreModelState modelState, EReference newEReference,
			EReference newOpposite, EClassifier source, EClassifier target, Edge newEReferenceEdge,
			Edge newOppositeEdge, boolean setOpposites) {

		CCompoundCommand compoundCommand = CCommandFactory.eINSTANCE.createCompoundCommand();
		compoundCommand.setType(EMFCommandType.COMPOUND);

		AddCommand addEReference = createAddCommand(modelState, source,
				EcorePackage.Literals.ECLASS__ESTRUCTURAL_FEATURES, newEReference);
		AddCommand addOpposite = createAddCommand(modelState, target,
				EcorePackage.Literals.ECLASS__ESTRUCTURAL_FEATURES, newOpposite);
		AddCommand addReferenceEdge = createAddCommand(modelState, getDiagram(modelState),
				EnotationPackage.Literals.DIAGRAM__ELEMENTS, newEReferenceEdge);
		AddCommand addOppositeEdge = createAddCommand(modelState, getDiagram(modelState),
				EnotationPackage.Literals.DIAGRAM__ELEMENTS, newOppositeEdge);

		compoundCommand.getCommands().add(AddCommandContribution.clientCommand(addEReference));
		compoundCommand.getCommands().add(AddCommandContribution.clientCommand(addOpposite));
		compoundCommand.getCommands().add(AddCommandContribution.clientCommand(addReferenceEdge));
		compoundCommand.getCommands().add(AddCommandContribution.clientCommand(addOppositeEdge));

		if (setOpposites) {
			SetCommand setReferenceOpposite = createSetCommand(modelState, newEReference,
					EcorePackage.eINSTANCE.getEReference_EOpposite(), newOpposite);
			SetCommand setOppositeOpposite = createSetCommand(modelState, newOpposite,
					EcorePackage.eINSTANCE.getEReference_EOpposite(), newEReference);

			compoundCommand.getCommands().add(SetCommandContribution.clientCommand(setReferenceOpposite));
			compoundCommand.getCommands().add(SetCommandContribution.clientCommand(setOppositeOpposite));
		}

		return this.edit(compoundCommand);
	}

	public boolean addESuperType(EcoreModelState modelState, EClassifier newESuperType, EClassifier parent,
			Edge newEdge) {
		CCompoundCommand compoundCommand = CCommandFactory.eINSTANCE.createCompoundCommand();
		compoundCommand.setType(EMFCommandType.COMPOUND);

		AddCommand addESuperType = createAddCommand(modelState, parent, EcorePackage.Literals.ECLASS__ESUPER_TYPES,
				newESuperType);
		AddCommand addEdge = createAddCommand(modelState, getDiagram(modelState),
				EnotationPackage.Literals.DIAGRAM__ELEMENTS, newEdge);

		compoundCommand.getCommands().add(AddCommandContribution.clientCommand(addESuperType));
		compoundCommand.getCommands().add(AddCommandContribution.clientCommand(addEdge));

		return this.edit(compoundCommand);
	}

	private AddCommand createAddAttributeCommand(EcoreModelState modelState, EAttribute newEAttribute, EClass parent) {
		return createAddCommand(modelState, parent, EcorePackage.Literals.ECLASS__ESTRUCTURAL_FEATURES, newEAttribute);
	}

	public boolean addEAttribute(EcoreModelState modelState, EAttribute newEAttribute, EClass parent) {
		return this.edit(
				AddCommandContribution.clientCommand(createAddAttributeCommand(modelState, newEAttribute, parent)));
	}

	public boolean addEEnumLiteral(EcoreModelState modelState, EEnumLiteral newEEnumLiteral, EEnum parent) {
		return this.add(modelState, parent, EcorePackage.Literals.EENUM__ELITERALS, newEEnumLiteral);
	}

	private AddCommand createAddOperationCommand(EcoreModelState modelState, EOperation newEOperation, EClass parent) {
		return createAddCommand(modelState, parent, EcorePackage.Literals.ECLASS__EOPERATIONS, newEOperation);
	}

	public boolean addEOperation(EcoreModelState modelState, EOperation newEOperation, EClass parent) {
		return this.edit(
				AddCommandContribution.clientCommand(createAddOperationCommand(modelState, newEOperation, parent)));
	}

	private AddCommand createAddCommand(EcoreModelState modelState, EObject owner, EReference feature,
			EObject addObject) {
		return (AddCommand) AddCommand.create(
				EcoreModelState.getEditorContext(modelState).getResourceManager().getEditingDomain(), owner, feature,
				addObject);
	}

	private boolean add(EcoreModelState modelState, EObject owner, EReference feature, EObject addObject) {
		return this.edit(AddCommandContribution.clientCommand(createAddCommand(modelState, owner, feature, addObject)));
	}

	public boolean setOpposite(EcoreModelState modelState, EReference eReference, EReference opposite) {
		return this.set(modelState, eReference, EcorePackage.Literals.EREFERENCE__EOPPOSITE, opposite);
	}

	public boolean setInstanceName(EcoreModelState modelState, EClassifier eClassifier, String name) {
		return this.set(modelState, eClassifier, EcorePackage.Literals.ECLASSIFIER__INSTANCE_CLASS_NAME, name);
	}

	private SemanticProxy createProxyFromOldElement(NotationElement oldElement, String oldName, String newName) {
		SemanticProxy proxy = EnotationFactory.eINSTANCE.createSemanticProxy();
		proxy.setUri(oldElement.getSemanticElement().getUri().replace(oldName, newName));
		return proxy;
	}

	public boolean setName(EcoreModelState modelState, EClassifier eClassifier, String newName) {
		CCompoundCommand compoundCommand = CCommandFactory.eINSTANCE.createCompoundCommand();
		compoundCommand.setType(EMFCommandType.COMPOUND);

		SetCommand setEClassifierName = createSetCommand(modelState, eClassifier,
				EcorePackage.Literals.ENAMED_ELEMENT__NAME, newName);

		Shape shape = (Shape) getNotationElement(modelState, eClassifier);
		RemoveCommand removeOldShape = createRemoveNotationElementCommand(modelState, shape);
		Shape newShape = EnotationFactory.eINSTANCE.createShape();
		newShape.setPosition(shape.getPosition());
		newShape.setSemanticElement(createProxyFromOldElement(shape, eClassifier.getName(), newName));
		AddCommand addNewShape = createAddCommand(modelState, getDiagram(modelState),
				EnotationPackage.Literals.DIAGRAM__ELEMENTS, newShape);

		compoundCommand.getCommands().add(SetCommandContribution.clientCommand(setEClassifierName));
		compoundCommand.getCommands().add(RemoveCommandContribution.clientCommand(removeOldShape));
		compoundCommand.getCommands().add(AddCommandContribution.clientCommand(addNewShape));

		// Update notation elements for EStructuralFeatures and ESuperTypes
		if (eClassifier instanceof EClass) {
			EClass eClass = ((EClass) eClassifier);
			for (EStructuralFeature eStructuralFeature : eClass.getEStructuralFeatures()) {
				NotationElement notationElement = getNotationElement(modelState, eStructuralFeature);
				if (notationElement != null) {
					SetCommand setSemanticProxyCommand = createSetCommand(modelState, notationElement,
							EnotationPackage.Literals.NOTATION_ELEMENT__SEMANTIC_ELEMENT,
							createProxyFromOldElement(notationElement, eClassifier.getName(), newName));
					compoundCommand.getCommands().add(SetCommandContribution.clientCommand(setSemanticProxyCommand));
					SetCommand setEdgeSourceCommand = createSetCommand(modelState, notationElement,
							EnotationPackage.Literals.EDGE__SOURCE, newShape);
					compoundCommand.getCommands().add(SetCommandContribution.clientCommand(setEdgeSourceCommand));
				}
			}
			for (EClass eSuperType : eClass.getESuperTypes()) {
				NotationElement notationElement = getNotationElement(modelState, eClass, eSuperType);
				if (notationElement != null) {
					SetCommand setEdgeSourceCommand = createSetCommand(modelState, notationElement,
							EnotationPackage.Literals.EDGE__SOURCE, newShape);
					compoundCommand.getCommands().add(SetCommandContribution.clientCommand(setEdgeSourceCommand));
				}
			}
		}

		// Update usages
		Collection<Setting> usages = UsageCrossReferencer.find(eClassifier, eClassifier.eResource().getResourceSet());
		if (!usages.isEmpty()) {
			for (Setting setting : usages) {
				EObject eObject = setting.getEObject();
				if (setting.getEStructuralFeature().isChangeable() && eObject.eContainer() instanceof EClass) {
					if (eObject instanceof EStructuralFeature) {
						NotationElement notationElement = getNotationElement(modelState, (EStructuralFeature) eObject);
						if (notationElement != null) {
							SetCommand setEdgeSourceCommand = createSetCommand(modelState, notationElement,
									EnotationPackage.Literals.EDGE__TARGET, newShape);
							compoundCommand.getCommands()
									.add(SetCommandContribution.clientCommand(setEdgeSourceCommand));
						}
					} else if (eObject instanceof EGenericType
							&& ((EGenericType) eObject).getEClassifier() instanceof EClass) {
						EClass eClass = (EClass) eObject.eContainer();
						EClass eSuperType = (EClass) ((EGenericType) eObject).getEClassifier();
						NotationElement notationElement = getNotationElement(modelState, eClass, eSuperType);
						if (notationElement != null) {
							SetCommand setEdgeSourceCommand = createSetCommand(modelState, notationElement,
									EnotationPackage.Literals.EDGE__TARGET, newShape);
							compoundCommand.getCommands()
									.add(SetCommandContribution.clientCommand(setEdgeSourceCommand));
						}
					}
				}
			}
		}

		return this.edit(compoundCommand);
	}

	private SetCommand createSetAttributeTypeCommand(EcoreModelState modelState, EAttribute eAttribute,
			EDataType newType) {
		return createSetCommand(modelState, eAttribute, EcorePackage.Literals.ETYPED_ELEMENT__ETYPE, newType);
	}

	public boolean setAttribute(EcoreModelState modelState, EAttribute eAttribute, String newName, EDataType newType) {
		CCompoundCommand compoundCommand = CCommandFactory.eINSTANCE.createCompoundCommand();
		compoundCommand.setType(EMFCommandType.COMPOUND);

		if (newName != null) {
			RemoveCommand removeEAttributeNameCommand = createRemoveEStructuralFeatureCommand(modelState, eAttribute);
			compoundCommand.getCommands().add(RemoveCommandContribution.clientCommand(removeEAttributeNameCommand));

			EAttribute newEAttribute = EcoreFactory.eINSTANCE.createEAttribute();
			newEAttribute.setName(newName);

			AddCommand addEAttributeCommand = createAddAttributeCommand(modelState, newEAttribute,
					eAttribute.getEContainingClass());
			compoundCommand.getCommands().add(AddCommandContribution.clientCommand(addEAttributeCommand));

			SetCommand setEAttributeTypeCommand = createSetAttributeTypeCommand(modelState, newEAttribute, newType);
			compoundCommand.getCommands().add(SetCommandContribution.clientCommand(setEAttributeTypeCommand));
		} else {
			SetCommand setEAttributeTypeCommand = createSetAttributeTypeCommand(modelState, eAttribute, newType);
			compoundCommand.getCommands().add(SetCommandContribution.clientCommand(setEAttributeTypeCommand));
		}

		if (compoundCommand.getCommands().isEmpty()) {
			return false;
		}
		return this.edit(compoundCommand);
	}

	public boolean setLiteralName(EcoreModelState modelState, EEnumLiteral eEnumLiteral, String newName) {
		return this.set(modelState, eEnumLiteral, EcorePackage.Literals.ENAMED_ELEMENT__NAME, newName);
	}

	private SetCommand createSetOperationTypeCommand(EcoreModelState modelState, EOperation eOperation,
			EDataType newType) {
		return createSetCommand(modelState, eOperation, EcorePackage.Literals.ETYPED_ELEMENT__ETYPE, newType);
	}

	public boolean setOperation(EcoreModelState modelState, EOperation eOperation, String newName, EDataType newType) {
		CCompoundCommand compoundCommand = CCommandFactory.eINSTANCE.createCompoundCommand();
		compoundCommand.setType(EMFCommandType.COMPOUND);

		if (newName != null) {
			RemoveCommand removeEAttributeNameCommand = createRemoveEOperationCommand(modelState, eOperation);
			compoundCommand.getCommands().add(RemoveCommandContribution.clientCommand(removeEAttributeNameCommand));

			EOperation newEOperation = EcoreFactory.eINSTANCE.createEOperation();
			newEOperation.setName(newName);

			AddCommand addEAttributeCommand = createAddOperationCommand(modelState, newEOperation,
					eOperation.getEContainingClass());
			compoundCommand.getCommands().add(AddCommandContribution.clientCommand(addEAttributeCommand));

			SetCommand setEAttributeTypeCommand = createSetOperationTypeCommand(modelState, newEOperation, newType);
			compoundCommand.getCommands().add(SetCommandContribution.clientCommand(setEAttributeTypeCommand));
		} else {
			SetCommand setEAttributeTypeCommand = createSetOperationTypeCommand(modelState, eOperation, newType);
			compoundCommand.getCommands().add(SetCommandContribution.clientCommand(setEAttributeTypeCommand));
		}

		if (compoundCommand.getCommands().isEmpty()) {
			return false;
		}
		return this.edit(compoundCommand);
	}

	public boolean setEdgeName(EcoreModelState modelState, EReference eReference, String newName) {
		CCompoundCommand compoundCommand = CCommandFactory.eINSTANCE.createCompoundCommand();
		compoundCommand.setType(EMFCommandType.COMPOUND);

		SetCommand setEReferenceName = createSetCommand(modelState, eReference,
				EcorePackage.Literals.ENAMED_ELEMENT__NAME, newName);

		Edge edge = (Edge) getNotationElement(modelState, eReference);
		RemoveCommand removeOldEdge = createRemoveNotationElementCommand(modelState, edge);
		Edge newEdge = EnotationFactory.eINSTANCE.createEdge();
		newEdge.getBendPoints().addAll(edge.getBendPoints());
		newEdge.setSemanticElement(createProxyFromOldElement(edge, eReference.getName(), newName));
		newEdge.setSource(edge.getSource());
		newEdge.setTarget(edge.getTarget());
		AddCommand addNewEdge = createAddCommand(modelState, getDiagram(modelState),
				EnotationPackage.Literals.DIAGRAM__ELEMENTS, newEdge);

		compoundCommand.getCommands().add(SetCommandContribution.clientCommand(setEReferenceName));
		compoundCommand.getCommands().add(RemoveCommandContribution.clientCommand(removeOldEdge));
		compoundCommand.getCommands().add(AddCommandContribution.clientCommand(addNewEdge));

		return this.edit(compoundCommand);
	}

	public boolean setLowerMultiplicity(EcoreModelState modelState, EStructuralFeature eStructuralFeature,
			int multiplicity) {
		return this.set(modelState, eStructuralFeature, EcorePackage.Literals.ETYPED_ELEMENT__LOWER_BOUND,
				multiplicity);
	}

	public boolean setUpperMultiplicity(EcoreModelState modelState, EStructuralFeature eStructuralFeature,
			int multiplicity) {
		return this.set(modelState, eStructuralFeature, EcorePackage.Literals.ETYPED_ELEMENT__UPPER_BOUND,
				multiplicity);
	}

	private SetCommand createSetCommand(EcoreModelState modelState, EObject owner, EStructuralFeature feature,
			Object setObject) {
		EditingDomain editingDomain = EcoreModelState.getEditorContext(modelState).getResourceManager()
				.getEditingDomain();
		return (SetCommand) SetCommand.create(editingDomain, owner, feature, setObject);
	}

	private boolean set(EcoreModelState modelState, EObject owner, EStructuralFeature feature, Object setObject) {
		return this.edit(SetCommandContribution.clientCommand(createSetCommand(modelState, owner, feature, setObject)));
	}

	public boolean setBounds(EcoreModelState modelState, Map<Shape, ElementAndBounds> changeBoundsMap) {
		CCompoundCommand compoundCommand = CCommandFactory.eINSTANCE.createCompoundCommand();
		compoundCommand.setType(EMFCommandType.COMPOUND);
		changeBoundsMap.forEach((Shape shape, ElementAndBounds newBounds) -> {

			if (newBounds.getNewPosition() != null) {
				SetCommand setXPosition = createSetCommand(modelState, shape.getPosition(),
						GraphPackage.Literals.GPOINT__X, newBounds.getNewPosition().getX());
				SetCommand setYPosition = createSetCommand(modelState, shape.getPosition(),
						GraphPackage.Literals.GPOINT__Y, newBounds.getNewPosition().getY());
				compoundCommand.getCommands().add(SetCommandContribution.clientCommand(setXPosition));
				compoundCommand.getCommands().add(SetCommandContribution.clientCommand(setYPosition));
			}

			if (newBounds.getNewSize() != null) {
				SetCommand setHeight = createSetCommand(modelState, shape.getSize(),
						GraphPackage.Literals.GBOUNDS__HEIGHT, newBounds.getNewSize().getHeight());
				SetCommand setWidth = createSetCommand(modelState, shape.getSize(),
						GraphPackage.Literals.GBOUNDS__WIDTH, newBounds.getNewSize().getWidth());
				compoundCommand.getCommands().add(SetCommandContribution.clientCommand(setHeight));
				compoundCommand.getCommands().add(SetCommandContribution.clientCommand(setWidth));
			}

		});
		if (compoundCommand.getCommands().isEmpty()) {
			return false;
		}
		return this.edit(compoundCommand);
	}

	public boolean setBendPoints(EcoreModelState modelState, Map<Edge, ElementAndRoutingPoints> changeBendPointsMap) {
		CCompoundCommand compoundCommand = CCommandFactory.eINSTANCE.createCompoundCommand();
		compoundCommand.setType(EMFCommandType.COMPOUND);
		changeBendPointsMap.forEach((Edge edge, ElementAndRoutingPoints newRoutingPoints) -> {
			// clear old bend points first if any
			if (!edge.getBendPoints().isEmpty()) {
				RemoveCommand removeBendPoints = createRemoveCommand(modelState, edge,
						EnotationPackage.Literals.EDGE__BEND_POINTS, edge.getBendPoints());
				compoundCommand.getCommands().add(RemoveCommandContribution.clientCommand(removeBendPoints));
			}

			// add all new points
			newRoutingPoints.getNewRoutingPoints().forEach((GPoint newPoint) -> {
				AddCommand addBendPoint = createAddCommand(modelState, edge,
						EnotationPackage.Literals.EDGE__BEND_POINTS, newPoint);
				compoundCommand.getCommands().add(AddCommandContribution.clientCommand(addBendPoint));
			});

		});
		if (compoundCommand.getCommands().isEmpty()) {
			return false;
		}
		return this.edit(compoundCommand);
	}

	private CCompoundCommand createLayoutCommand(EcoreModelState modelState, GModelElement layoutedRoot) {
		CCompoundCommand compoundCommand = CCommandFactory.eINSTANCE.createCompoundCommand();
		compoundCommand.setType(EMFCommandType.COMPOUND);

		layoutedRoot.getChildren().forEach(gModelElement -> {
			if (gModelElement instanceof GNode) {
				modelState.getIndex().getNotation(gModelElement.getId(), Shape.class).ifPresent(shape -> {
					if (((GNode) gModelElement).getPosition() != null) {
						SetCommand setXPosition = createSetCommand(modelState, shape.getPosition(),
								GraphPackage.Literals.GPOINT__X, ((GNode) gModelElement).getPosition().getX());
						SetCommand setYPosition = createSetCommand(modelState, shape.getPosition(),
								GraphPackage.Literals.GPOINT__Y, ((GNode) gModelElement).getPosition().getY());
						compoundCommand.getCommands().add(SetCommandContribution.clientCommand(setXPosition));
						compoundCommand.getCommands().add(SetCommandContribution.clientCommand(setYPosition));
					}

					if (((GNode) gModelElement).getSize() != null) {
						SetCommand setHeight = createSetCommand(modelState, shape.getSize(),
								GraphPackage.Literals.GBOUNDS__HEIGHT, ((GNode) gModelElement).getSize().getHeight());
						SetCommand setWidth = createSetCommand(modelState, shape.getSize(),
								GraphPackage.Literals.GBOUNDS__WIDTH, ((GNode) gModelElement).getSize().getWidth());
						compoundCommand.getCommands().add(SetCommandContribution.clientCommand(setHeight));
						compoundCommand.getCommands().add(SetCommandContribution.clientCommand(setWidth));
					}
				});

			} else if (gModelElement instanceof GEdge) {
				modelState.getIndex().getNotation(gModelElement.getId(), Edge.class).ifPresent(edge -> {
					// clear existing bend points if any
					if (!edge.getBendPoints().isEmpty()) {
						RemoveCommand removeBendPoints = createRemoveCommand(modelState, edge,
								EnotationPackage.Literals.EDGE__BEND_POINTS, edge.getBendPoints());
						compoundCommand.getCommands().add(RemoveCommandContribution.clientCommand(removeBendPoints));
					}
					// add all new points
					for (GPoint newPoint : ((GEdge) gModelElement).getRoutingPoints()) {
						AddCommand addBendPoint = createAddCommand(modelState, edge,
								EnotationPackage.Literals.EDGE__BEND_POINTS, EcoreUtil.copy(newPoint));
						compoundCommand.getCommands().add(AddCommandContribution.clientCommand(addBendPoint));
					}
				});

			}
		});

		return compoundCommand;
	}

	public boolean setLayout(EcoreModelState modelState, GModelElement layoutedRoot) {
		CCompoundCommand compoundCommand = createLayoutCommand(modelState, layoutedRoot);
		if (compoundCommand.getCommands().isEmpty()) {
			return false;
		}
		return this.edit(compoundCommand);
	}

	private RemoveCommand createRemoveCommand(EcoreModelState modelState, EObject owner, EStructuralFeature feature,
			int index) {
		EditingDomain editingDomain = EcoreModelState.getEditorContext(modelState).getResourceManager()
				.getEditingDomain();
		return (RemoveCommand) RemoveCommand.create(editingDomain, owner, feature, index);
	}

	private RemoveCommand createRemoveCommand(EcoreModelState modelState, EObject owner, EStructuralFeature feature,
			EObject element) {
		return createRemoveCommand(modelState, owner, feature, List.of(element));
	}

	private RemoveCommand createRemoveCommand(EcoreModelState modelState, EObject owner, EStructuralFeature feature,
			Collection<?> values) {
		EditingDomain editingDomain = EcoreModelState.getEditorContext(modelState).getResourceManager()
				.getEditingDomain();
		return (RemoveCommand) RemoveCommand.create(editingDomain, owner, feature, values);
	}

	private NotationElement getNotationElement(EcoreModelState modelState, EObject semanticElement) {
		return modelState.getIndex().getNotation(semanticElement).orElse(null);
	}

	private RemoveCommand createRemoveEStructuralFeatureCommand(EcoreModelState modelState,
			EStructuralFeature eStructuralFeature) {
		return createRemoveCommand(modelState, eStructuralFeature.getEContainingClass(),
				EcorePackage.Literals.ECLASS__ESTRUCTURAL_FEATURES, eStructuralFeature);
	}

	public boolean removeEAttribute(EcoreModelState modelState, EAttribute eAttribute) {
		return this.edit(
				RemoveCommandContribution.clientCommand(createRemoveEStructuralFeatureCommand(modelState, eAttribute)));
	}

	public boolean removeEReference(EcoreModelState modelState, EReference eReference) {
		CCompoundCommand compoundCommand = CCommandFactory.eINSTANCE.createCompoundCommand();
		compoundCommand.setType(EMFCommandType.COMPOUND);

		RemoveCommand removeCommand = createRemoveEStructuralFeatureCommand(modelState, eReference);
		compoundCommand.getCommands().add(RemoveCommandContribution.clientCommand(removeCommand));

		RemoveCommand removeNotationElementCommand = createRemoveNotationElementCommand(modelState,
				getNotationElement(modelState, eReference));
		compoundCommand.getCommands().add(RemoveCommandContribution.clientCommand(removeNotationElementCommand));

		if (eReference.getEOpposite() != null) {
			EReference eOpposite = eReference.getEOpposite();
			RemoveCommand removeOppositeCommand = createRemoveEStructuralFeatureCommand(modelState, eOpposite);
			compoundCommand.getCommands().add(RemoveCommandContribution.clientCommand(removeOppositeCommand));

			RemoveCommand removeOppositeNotationElementCommand = createRemoveNotationElementCommand(modelState,
					getNotationElement(modelState, eOpposite));
			compoundCommand.getCommands()
					.add(RemoveCommandContribution.clientCommand(removeOppositeNotationElementCommand));
		}
		return this.edit(compoundCommand);
	}

	private RemoveCommand createRemoveEClassifierCommand(EcoreModelState modelState, EClassifier eClassifier) {
		return createRemoveCommand(modelState, eClassifier.getEPackage(), EcorePackage.Literals.EPACKAGE__ECLASSIFIERS,
				eClassifier);
	}

	private RemoveCommand createRemoveNotationElementCommand(EcoreModelState modelState,
			NotationElement notationElement) {
		return createRemoveCommand(modelState, getDiagram(modelState), EnotationPackage.Literals.DIAGRAM__ELEMENTS,
				notationElement);
	}

	public boolean removeEClassifier(EcoreModelState modelState, EClassifier eClassifier) {
		RemoveCommand removeEClassifierCommand = createRemoveEClassifierCommand(modelState, eClassifier);

		Collection<Setting> usages = UsageCrossReferencer.find(eClassifier, eClassifier.eResource().getResourceSet());
		if (!usages.isEmpty()) {
			CCompoundCommand compoundCommand = CCommandFactory.eINSTANCE.createCompoundCommand();
			compoundCommand.setType(EMFCommandType.COMPOUND);

			compoundCommand.getCommands().add(RemoveCommandContribution.clientCommand(removeEClassifierCommand));
			RemoveCommand removeNotationElementCommand = createRemoveNotationElementCommand(modelState,
					getNotationElement(modelState, eClassifier));
			compoundCommand.getCommands().add(RemoveCommandContribution.clientCommand(removeNotationElementCommand));

			// Remove notation elements for EStructuralFeatures and ESuperTypes
			if (eClassifier instanceof EClass) {
				EClass eClass = ((EClass) eClassifier);
				for (EStructuralFeature eStructuralFeature : eClass.getEStructuralFeatures()) {
					NotationElement notationElement = getNotationElement(modelState, eStructuralFeature);
					if (notationElement != null) {
						RemoveCommand removeNotationCommand = createRemoveNotationElementCommand(modelState,
								notationElement);
						compoundCommand.getCommands()
								.add(RemoveCommandContribution.clientCommand(removeNotationCommand));
					}
				}
				for (EClass eSuperType : eClass.getESuperTypes()) {
					NotationElement notationElement = getNotationElement(modelState, eClass, eSuperType);
					if (notationElement != null) {
						RemoveCommand removeNotationCommand = createRemoveNotationElementCommand(modelState,
								notationElement);
						compoundCommand.getCommands()
								.add(RemoveCommandContribution.clientCommand(removeNotationCommand));
					}
				}
			}

			// Remove usages
			for (Setting setting : usages) {
				EObject eObject = setting.getEObject();
				if (setting.getEStructuralFeature().isChangeable() && eObject.eContainer() instanceof EClass) {
					if (eObject instanceof EStructuralFeature) {
						RemoveCommand removeCommand = createRemoveEStructuralFeatureCommand(modelState,
								(EStructuralFeature) eObject);
						compoundCommand.getCommands().add(RemoveCommandContribution.clientCommand(removeCommand));
						NotationElement notationElement = getNotationElement(modelState, eObject);
						if (notationElement != null) {
							RemoveCommand removeNotationCommand = createRemoveNotationElementCommand(modelState,
									notationElement);
							compoundCommand.getCommands()
									.add(RemoveCommandContribution.clientCommand(removeNotationCommand));
						}
					} else if (eObject instanceof EGenericType
							&& ((EGenericType) eObject).getEClassifier() instanceof EClass) {
						EClass eClass = (EClass) eObject.eContainer();
						EClass eSuperType = (EClass) ((EGenericType) eObject).getEClassifier();
						RemoveCommand removeCommand = createRemoveESuperTypeCommand(modelState, eClass, eSuperType);
						compoundCommand.getCommands().add(RemoveCommandContribution.clientCommand(removeCommand));
						NotationElement notationElement = getNotationElement(modelState, eClass, eSuperType);
						if (notationElement != null) {
							RemoveCommand removeNotationCommand = createRemoveNotationElementCommand(modelState,
									notationElement);
							compoundCommand.getCommands()
									.add(RemoveCommandContribution.clientCommand(removeNotationCommand));
						}
					}
				}
			}
			return this.edit(compoundCommand);
		} else {
			return this.edit(RemoveCommandContribution.clientCommand(removeEClassifierCommand));
		}
	}

	private RemoveCommand createRemoveEEnumLiteralCommand(EcoreModelState modelState, EEnumLiteral eEnumLiteral) {
		return createRemoveCommand(modelState, eEnumLiteral.getEEnum(), EcorePackage.Literals.EENUM__ELITERALS,
				eEnumLiteral);
	}

	public boolean removeEEnumLiteral(EcoreModelState modelState, EEnumLiteral eEnumLiteral) {
		return this.edit(
				RemoveCommandContribution.clientCommand(createRemoveEEnumLiteralCommand(modelState, eEnumLiteral)));
	}

	private RemoveCommand createRemoveEOperationCommand(EcoreModelState modelState, EOperation eOperation) {
		return createRemoveCommand(modelState, eOperation.getEContainingClass(),
				EcorePackage.Literals.ECLASS__EOPERATIONS, eOperation);
	}

	public boolean removeEOperation(EcoreModelState modelState, EOperation eOperation) {
		return this
				.edit(RemoveCommandContribution.clientCommand(createRemoveEOperationCommand(modelState, eOperation)));
	}

	private RemoveCommand createRemoveESuperTypeCommand(EcoreModelState modelState, EClass eClass, EClass eSuperType) {
		int index = eClass.getESuperTypes().indexOf(eSuperType);
		return createRemoveCommand(modelState, eClass, EcorePackage.Literals.ECLASS__ESUPER_TYPES, index);
	}

	private NotationElement getNotationElement(EcoreModelState modelState, String inheritanceEdgeId) {
		return modelState.getIndex().getInheritanceEdge(inheritanceEdgeId).orElse(null);
	}

	private NotationElement getNotationElement(EcoreModelState modelState, EClass eClass, EClass eSuperType) {
		return modelState.getIndex().getInheritanceEdge(eClass, eSuperType).orElse(null);
	}

	public boolean removeESuperType(EcoreModelState modelState, EClass eClass, EClass eSuperType,
			String inheritanceEdgeId) {
		CCompoundCommand compoundCommand = CCommandFactory.eINSTANCE.createCompoundCommand();
		compoundCommand.setType(EMFCommandType.COMPOUND);

		RemoveCommand removeCommand = createRemoveESuperTypeCommand(modelState, eClass, eSuperType);
		compoundCommand.getCommands().add(RemoveCommandContribution.clientCommand(removeCommand));

		RemoveCommand removeNotationElementCommand = createRemoveNotationElementCommand(modelState,
				getNotationElement(modelState, inheritanceEdgeId));
		compoundCommand.getCommands().add(RemoveCommandContribution.clientCommand(removeNotationElementCommand));

		return this.edit(compoundCommand);
	}

	private boolean edit(CCommand command) {
		try {
			return this.modelServerClient.edit(getSemanticURI(), command, FORMAT_XMI).thenApply(res -> res.body())
					.get();
		} catch (InterruptedException | ExecutionException e) {
			return false;
		}
	}

	public boolean save() {
		try {
			return this.modelServerClient.save(getSemanticURI()).thenApply(res -> res.body()).get();
		} catch (InterruptedException | ExecutionException e) {
			return false;
		}
	}

	public boolean undo() {
		try {
			return this.modelServerClient.undo(getSemanticURI()).thenApply(res -> res.body()).get();
		} catch (InterruptedException | ExecutionException e) {
			return false;
		}
	}

	public boolean redo() {
		try {
			return this.modelServerClient.redo(getSemanticURI()).thenApply(res -> res.body()).get();
		} catch (InterruptedException | ExecutionException e) {
			return false;
		}
	}

	public EObject createEcoreNotation() {
		try {
			return this.modelServerClient.createEcoreNotation(getSemanticURI(), FORMAT_XMI).thenApply(res -> res.body())
					.get();
		} catch (InterruptedException | ExecutionException e) {
			LOGGER.error(e);
			throw new GLSPServerException("Error during enotation creation", e);
		}
	}

}
