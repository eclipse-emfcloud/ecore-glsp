/********************************************************************************
 * Copyright (c) 2020 EclipseSource and others.
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
import org.eclipse.emf.common.command.Command;
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
import org.eclipse.emfcloud.modelserver.client.ModelServerClientApi;
import org.eclipse.emfcloud.modelserver.client.NotificationSubscriptionListener;
import org.eclipse.emfcloud.modelserver.command.CCommand;
import org.eclipse.emfcloud.modelserver.command.CCommandFactory;
import org.eclipse.emfcloud.modelserver.command.CCompoundCommand;
import org.eclipse.emfcloud.modelserver.command.CommandKind;
import org.eclipse.emfcloud.modelserver.common.codecs.EncodingException;
import org.eclipse.emfcloud.modelserver.edit.CommandCodec;
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

	private ModelServerClientApi<EObject> modelServerClient;
	private NotificationSubscriptionListener<EObject> subscriptionListener;
	private CommandCodec commandCodec;

	public EcoreModelServerAccess(final String sourceURI, final ModelServerClientApi<EObject> modelServerClient,
			final CommandCodec commandCodec) {
		Preconditions.checkNotNull(modelServerClient);
		this.baseSourceUri = sourceURI.substring(0, sourceURI.lastIndexOf('.'));
		this.modelServerClient = modelServerClient;
		this.commandCodec = commandCodec;
	}

	public String getSemanticURI() {
		return baseSourceUri + ECORE_EXTENSION;
	}

	public String getNotationURI() {
		return baseSourceUri + NOTATION_EXTENSION;
	}

	public CommandCodec getCommandCodec() {
		return commandCodec;
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
			throw new GLSPServerException("Error during model loading", e);
		}
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
		compoundCommand.setType(CommandKind.COMPOUND);
		try {
			Command addEClassifier = createAddCommand(modelState, getEPackage(modelState),
					EcorePackage.Literals.EPACKAGE__ECLASSIFIERS, newEClassifier);
			Command addShape = createAddCommand(modelState, getDiagram(modelState),
					EnotationPackage.Literals.DIAGRAM__ELEMENTS, newShape);

			compoundCommand.getCommands().add(getCommandCodec().encode(addEClassifier));
			compoundCommand.getCommands().add(getCommandCodec().encode(addShape));
		} catch (EncodingException e) {
			return false;
		}
		return this.editCompound(compoundCommand);
	}

	public boolean addEReference(EcoreModelState modelState, EReference newEReference, EClassifier source,
			Edge newEdge) {
		CCompoundCommand compoundCommand = CCommandFactory.eINSTANCE.createCompoundCommand();
		compoundCommand.setType(CommandKind.COMPOUND);
		try {
			Command addEReference = createAddCommand(modelState, source,
					EcorePackage.Literals.ECLASS__ESTRUCTURAL_FEATURES, newEReference);
			Command addEdge = createAddCommand(modelState, getDiagram(modelState),
					EnotationPackage.Literals.DIAGRAM__ELEMENTS, newEdge);

			compoundCommand.getCommands().add(getCommandCodec().encode(addEReference));
			compoundCommand.getCommands().add(getCommandCodec().encode(addEdge));
		} catch (EncodingException e) {
			return false;
		}
		return this.editCompound(compoundCommand);
	}

	public boolean addEReferenceBidirectional(EcoreModelState modelState, EReference newEReference,
			EReference newOpposite, EClassifier source, EClassifier target, Edge newEReferenceEdge,
			Edge newOppositeEdge, boolean setOpposites) {

		CCompoundCommand compoundCommand = CCommandFactory.eINSTANCE.createCompoundCommand();
		compoundCommand.setType(CommandKind.COMPOUND);
		try {
			Command addEReference = createAddCommand(modelState, source,
					EcorePackage.Literals.ECLASS__ESTRUCTURAL_FEATURES, newEReference);
			Command addOpposite = createAddCommand(modelState, target,
					EcorePackage.Literals.ECLASS__ESTRUCTURAL_FEATURES, newOpposite);
			Command addReferenceEdge = createAddCommand(modelState, getDiagram(modelState),
					EnotationPackage.Literals.DIAGRAM__ELEMENTS, newEReferenceEdge);
			Command addOppositeEdge = createAddCommand(modelState, getDiagram(modelState),
					EnotationPackage.Literals.DIAGRAM__ELEMENTS, newOppositeEdge);

			compoundCommand.getCommands().add(getCommandCodec().encode(addEReference));
			compoundCommand.getCommands().add(getCommandCodec().encode(addOpposite));
			compoundCommand.getCommands().add(getCommandCodec().encode(addReferenceEdge));
			compoundCommand.getCommands().add(getCommandCodec().encode(addOppositeEdge));

			if (setOpposites) {
				Command setReferenceOpposite = createSetCommand(modelState, newEReference,
						EcorePackage.eINSTANCE.getEReference_EOpposite(), newOpposite);
				Command setOppositeOpposite = createSetCommand(modelState, newOpposite,
						EcorePackage.eINSTANCE.getEReference_EOpposite(), newEReference);

				compoundCommand.getCommands().add(getCommandCodec().encode(setReferenceOpposite));
				compoundCommand.getCommands().add(getCommandCodec().encode(setOppositeOpposite));
			}

		} catch (EncodingException e) {
			return false;
		}
		return this.editCompound(compoundCommand);
	}

	public boolean addESuperType(EcoreModelState modelState, EClassifier newESuperType, EClassifier parent,
			Edge newEdge) {
		CCompoundCommand compoundCommand = CCommandFactory.eINSTANCE.createCompoundCommand();
		compoundCommand.setType(CommandKind.COMPOUND);
		try {
			Command addESuperType = createAddCommand(modelState, parent, EcorePackage.Literals.ECLASS__ESUPER_TYPES,
					newESuperType);
			Command addEdge = createAddCommand(modelState, getDiagram(modelState),
					EnotationPackage.Literals.DIAGRAM__ELEMENTS, newEdge);

			compoundCommand.getCommands().add(getCommandCodec().encode(addESuperType));
			compoundCommand.getCommands().add(getCommandCodec().encode(addEdge));
		} catch (EncodingException e) {
			return false;
		}
		return this.editCompound(compoundCommand);
	}

	private Command createAddAttributeCommand(EcoreModelState modelState, EAttribute newEAttribute, EClass parent) {
		return createAddCommand(modelState, parent, EcorePackage.Literals.ECLASS__ESTRUCTURAL_FEATURES, newEAttribute);
	}

	public boolean addEAttribute(EcoreModelState modelState, EAttribute newEAttribute, EClass parent) {
		return this.edit(createAddAttributeCommand(modelState, newEAttribute, parent));
	}

	public boolean addEEnumLiteral(EcoreModelState modelState, EEnumLiteral newEEnumLiteral, EEnum parent) {
		return this.add(modelState, parent, EcorePackage.Literals.EENUM__ELITERALS, newEEnumLiteral);
	}

	private Command createAddOperationCommand(EcoreModelState modelState, EOperation newEOperation, EClass parent) {
		return createAddCommand(modelState, parent, EcorePackage.Literals.ECLASS__EOPERATIONS, newEOperation);
	}

	public boolean addEOperation(EcoreModelState modelState, EOperation newEOperation, EClass parent) {
		return this.edit(createAddOperationCommand(modelState, newEOperation, parent));
	}

	private Command createAddCommand(EcoreModelState modelState, EObject owner, EReference feature, EObject addObject) {
		return AddCommand.create(EcoreModelState.getEditorContext(modelState).getResourceManager().getEditingDomain(),
				owner, feature, addObject);
	}

	private boolean add(EcoreModelState modelState, EObject owner, EReference feature, EObject addObject) {
		return this.edit(createAddCommand(modelState, owner, feature, addObject));
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
		compoundCommand.setType(CommandKind.COMPOUND);
		try {
			Command setEClassifierName = createSetCommand(modelState, eClassifier,
					EcorePackage.Literals.ENAMED_ELEMENT__NAME, newName);

			Shape shape = (Shape) getNotationElement(modelState, eClassifier);
			Command removeOldShape = createRemoveNotationElementCommand(modelState, shape);
			Shape newShape = EnotationFactory.eINSTANCE.createShape();
			newShape.setPosition(shape.getPosition());
			newShape.setSemanticElement(createProxyFromOldElement(shape, eClassifier.getName(), newName));
			Command addNewShape = createAddCommand(modelState, getDiagram(modelState),
					EnotationPackage.Literals.DIAGRAM__ELEMENTS, newShape);

			compoundCommand.getCommands().add(getCommandCodec().encode(setEClassifierName));
			compoundCommand.getCommands().add(getCommandCodec().encode(removeOldShape));
			compoundCommand.getCommands().add(getCommandCodec().encode(addNewShape));

			// Update notation elements for EStructuralFeatures and ESuperTypes
			if (eClassifier instanceof EClass) {
				EClass eClass = ((EClass) eClassifier);
				for (EStructuralFeature eStructuralFeature : eClass.getEStructuralFeatures()) {
					NotationElement notationElement = getNotationElement(modelState, eStructuralFeature);
					if (notationElement != null) {
						Command setSemanticProxyCommand = createSetCommand(modelState, notationElement,
								EnotationPackage.Literals.NOTATION_ELEMENT__SEMANTIC_ELEMENT,
								createProxyFromOldElement(notationElement, eClassifier.getName(), newName));
						compoundCommand.getCommands().add(getCommandCodec().encode(setSemanticProxyCommand));
						Command setEdgeSourceCommand = createSetCommand(modelState, notationElement,
								EnotationPackage.Literals.EDGE__SOURCE, newShape);
						compoundCommand.getCommands().add(getCommandCodec().encode(setEdgeSourceCommand));
					}
				}
				for (EClass eSuperType : eClass.getESuperTypes()) {
					NotationElement notationElement = getNotationElement(modelState, eClass, eSuperType);
					if (notationElement != null) {
						Command setEdgeSourceCommand = createSetCommand(modelState, notationElement,
								EnotationPackage.Literals.EDGE__SOURCE, newShape);
						compoundCommand.getCommands().add(getCommandCodec().encode(setEdgeSourceCommand));
					}
				}
			}

			// Update usages
			Collection<Setting> usages = UsageCrossReferencer.find(eClassifier,
					eClassifier.eResource().getResourceSet());
			if (!usages.isEmpty()) {
				for (Setting setting : usages) {
					EObject eObject = setting.getEObject();
					if (setting.getEStructuralFeature().isChangeable() && eObject.eContainer() instanceof EClass) {
						if (eObject instanceof EStructuralFeature) {
							try {
								NotationElement notationElement = getNotationElement(modelState,
										(EStructuralFeature) eObject);
								if (notationElement != null) {
									Command setEdgeSourceCommand = createSetCommand(modelState, notationElement,
											EnotationPackage.Literals.EDGE__TARGET, newShape);
									compoundCommand.getCommands().add(getCommandCodec().encode(setEdgeSourceCommand));
								}
							} catch (EncodingException e) {
								return false;
							}
						} else if (eObject instanceof EGenericType
								&& ((EGenericType) eObject).getEClassifier() instanceof EClass) {
							try {
								EClass eClass = (EClass) eObject.eContainer();
								EClass eSuperType = (EClass) ((EGenericType) eObject).getEClassifier();
								NotationElement notationElement = getNotationElement(modelState, eClass, eSuperType);
								if (notationElement != null) {
									Command setEdgeSourceCommand = createSetCommand(modelState, notationElement,
											EnotationPackage.Literals.EDGE__TARGET, newShape);
									compoundCommand.getCommands().add(getCommandCodec().encode(setEdgeSourceCommand));
								}
							} catch (EncodingException e) {
								return false;
							}
						}
					}
				}
			}

		} catch (EncodingException e) {
			return false;
		}
		return this.editCompound(compoundCommand);
	}

	private Command createSetAttributeTypeCommand(EcoreModelState modelState, EAttribute eAttribute,
			EDataType newType) {
		return createSetCommand(modelState, eAttribute, EcorePackage.Literals.ETYPED_ELEMENT__ETYPE, newType);
	}

	public boolean setAttribute(EcoreModelState modelState, EAttribute eAttribute, String newName, EDataType newType) {
		CCompoundCommand compoundCommand = CCommandFactory.eINSTANCE.createCompoundCommand();
		compoundCommand.setType(CommandKind.COMPOUND);
		try {
			if (newName != null) {
				Command removeEAttributeNameCommand = createRemoveEStructuralFeatureCommand(modelState, eAttribute);
				compoundCommand.getCommands().add(getCommandCodec().encode(removeEAttributeNameCommand));

				EAttribute newEAttribute = EcoreFactory.eINSTANCE.createEAttribute();
				newEAttribute.setName(newName);

				Command addEAttributeCommand = createAddAttributeCommand(modelState, newEAttribute,
						eAttribute.getEContainingClass());
				compoundCommand.getCommands().add(getCommandCodec().encode(addEAttributeCommand));

				Command setEAttributeTypeCommand = createSetAttributeTypeCommand(modelState, newEAttribute, newType);
				compoundCommand.getCommands().add(getCommandCodec().encode(setEAttributeTypeCommand));
			} else {
				Command setEAttributeTypeCommand = createSetAttributeTypeCommand(modelState, eAttribute, newType);
				compoundCommand.getCommands().add(getCommandCodec().encode(setEAttributeTypeCommand));
			}

		} catch (EncodingException e) {
			return false;
		}
		if (compoundCommand.getCommands().isEmpty()) {
			return false;
		}
		return this.editCompound(compoundCommand);
	}

	public boolean setLiteralName(EcoreModelState modelState, EEnumLiteral eEnumLiteral, String newName) {
		return this.set(modelState, eEnumLiteral, EcorePackage.Literals.ENAMED_ELEMENT__NAME, newName);
	}

	private Command createSetOperationTypeCommand(EcoreModelState modelState, EOperation eOperation,
			EDataType newType) {
		return createSetCommand(modelState, eOperation, EcorePackage.Literals.ETYPED_ELEMENT__ETYPE, newType);
	}

	public boolean setOperation(EcoreModelState modelState, EOperation eOperation, String newName, EDataType newType) {
		CCompoundCommand compoundCommand = CCommandFactory.eINSTANCE.createCompoundCommand();
		compoundCommand.setType(CommandKind.COMPOUND);
		try {
			if (newName != null) {
				Command removeEAttributeNameCommand = createRemoveEOperationCommand(modelState, eOperation);
				compoundCommand.getCommands().add(getCommandCodec().encode(removeEAttributeNameCommand));

				EOperation newEOperation = EcoreFactory.eINSTANCE.createEOperation();
				newEOperation.setName(newName);

				Command addEAttributeCommand = createAddOperationCommand(modelState, newEOperation,
						eOperation.getEContainingClass());
				compoundCommand.getCommands().add(getCommandCodec().encode(addEAttributeCommand));

				Command setEAttributeTypeCommand = createSetOperationTypeCommand(modelState, newEOperation, newType);
				compoundCommand.getCommands().add(getCommandCodec().encode(setEAttributeTypeCommand));
			} else {
				Command setEAttributeTypeCommand = createSetOperationTypeCommand(modelState, eOperation, newType);
				compoundCommand.getCommands().add(getCommandCodec().encode(setEAttributeTypeCommand));
			}

		} catch (EncodingException e) {
			return false;
		}
		if (compoundCommand.getCommands().isEmpty()) {
			return false;
		}
		return this.editCompound(compoundCommand);
	}

	public boolean setEdgeName(EcoreModelState modelState, EReference eReference, String newName) {
		CCompoundCommand compoundCommand = CCommandFactory.eINSTANCE.createCompoundCommand();
		compoundCommand.setType(CommandKind.COMPOUND);
		try {
			Command setEReferenceName = createSetCommand(modelState, eReference,
					EcorePackage.Literals.ENAMED_ELEMENT__NAME, newName);

			Edge edge = (Edge) getNotationElement(modelState, eReference);
			Command removeOldEdge = createRemoveNotationElementCommand(modelState, edge);
			Edge newEdge = EnotationFactory.eINSTANCE.createEdge();
			newEdge.getBendPoints().addAll(edge.getBendPoints());
			newEdge.setSemanticElement(createProxyFromOldElement(edge, eReference.getName(), newName));
			newEdge.setSource(edge.getSource());
			newEdge.setTarget(edge.getTarget());
			Command addNewEdge = createAddCommand(modelState, getDiagram(modelState),
					EnotationPackage.Literals.DIAGRAM__ELEMENTS, newEdge);

			compoundCommand.getCommands().add(getCommandCodec().encode(setEReferenceName));
			compoundCommand.getCommands().add(getCommandCodec().encode(removeOldEdge));
			compoundCommand.getCommands().add(getCommandCodec().encode(addNewEdge));
		} catch (EncodingException e) {
			return false;
		}
		return this.editCompound(compoundCommand);
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

	private Command createSetCommand(EcoreModelState modelState, EObject owner, EStructuralFeature feature,
			Object setObject) {
		EditingDomain editingDomain = EcoreModelState.getEditorContext(modelState).getResourceManager()
				.getEditingDomain();
		return SetCommand.create(editingDomain, owner, feature, setObject);
	}

	private boolean set(EcoreModelState modelState, EObject owner, EStructuralFeature feature, Object setObject) {
		return this.edit(createSetCommand(modelState, owner, feature, setObject));
	}

	public boolean setBounds(EcoreModelState modelState, Map<Shape, ElementAndBounds> changeBoundsMap) {
		CCompoundCommand compoundCommand = CCommandFactory.eINSTANCE.createCompoundCommand();
		compoundCommand.setType(CommandKind.COMPOUND);
		changeBoundsMap.forEach((Shape shape, ElementAndBounds newBounds) -> {
			try {
				if (newBounds.getNewPosition() != null) {
					Command setXPosition = createSetCommand(modelState, shape.getPosition(),
							GraphPackage.Literals.GPOINT__X, newBounds.getNewPosition().getX());
					Command setYPosition = createSetCommand(modelState, shape.getPosition(),
							GraphPackage.Literals.GPOINT__Y, newBounds.getNewPosition().getY());
					compoundCommand.getCommands().add(getCommandCodec().encode(setXPosition));
					compoundCommand.getCommands().add(getCommandCodec().encode(setYPosition));
				}

				if (newBounds.getNewSize() != null) {
					Command setHeight = createSetCommand(modelState, shape.getSize(),
							GraphPackage.Literals.GBOUNDS__HEIGHT, newBounds.getNewSize().getHeight());
					Command setWidth = createSetCommand(modelState, shape.getSize(),
							GraphPackage.Literals.GBOUNDS__WIDTH, newBounds.getNewSize().getWidth());
					compoundCommand.getCommands().add(getCommandCodec().encode(setHeight));
					compoundCommand.getCommands().add(getCommandCodec().encode(setWidth));
				}
			} catch (EncodingException e) {
				// return false;
			}
		});
		if (compoundCommand.getCommands().isEmpty()) {
			return false;
		}
		return this.editCompound(compoundCommand);
	}

	public boolean setBendPoints(EcoreModelState modelState, Map<Edge, ElementAndRoutingPoints> changeBendPointsMap) {
		CCompoundCommand compoundCommand = CCommandFactory.eINSTANCE.createCompoundCommand();
		compoundCommand.setType(CommandKind.COMPOUND);
		changeBendPointsMap.forEach((Edge edge, ElementAndRoutingPoints newRoutingPoints) -> {
			// clear old bend points first
			for (GPoint bendPoint : edge.getBendPoints()) {
				try {
					Command removeBendPoint = createRemoveCommand(modelState, edge,
							EnotationPackage.Literals.EDGE__BEND_POINTS, edge.getBendPoints().indexOf(bendPoint));
					compoundCommand.getCommands().add(getCommandCodec().encode(removeBendPoint));
				} catch (EncodingException e) {
					// return false;
				}
			}

			// add all new points
			newRoutingPoints.getNewRoutingPoints().forEach((GPoint newPoint) -> {
				try {
					Command addBendPoint = createAddCommand(modelState, edge,
							EnotationPackage.Literals.EDGE__BEND_POINTS, newPoint);
					compoundCommand.getCommands().add(getCommandCodec().encode(addBendPoint));
				} catch (EncodingException e) {
					// return false;
				}
			});

		});
		if (compoundCommand.getCommands().isEmpty()) {
			return false;
		}
		return this.editCompound(compoundCommand);
	}

	private Command createRemoveCommand(EcoreModelState modelState, EObject owner, EStructuralFeature feature,
			int index) {
		EditingDomain editingDomain = EcoreModelState.getEditorContext(modelState).getResourceManager()
				.getEditingDomain();
		return RemoveCommand.create(editingDomain, owner, feature, index);
	}

	private Command createRemoveCommand(EcoreModelState modelState, EObject owner, EStructuralFeature feature,
			EObject element) {
		EditingDomain editingDomain = EcoreModelState.getEditorContext(modelState).getResourceManager()
				.getEditingDomain();
		return RemoveCommand.create(editingDomain, owner, feature, List.of(element));
	}

	private NotationElement getNotationElement(EcoreModelState modelState, EObject semanticElement) {
		return modelState.getIndex().getNotation(semanticElement).orElse(null);
	}

	private Command createRemoveEStructuralFeatureCommand(EcoreModelState modelState,
			EStructuralFeature eStructuralFeature) {
		return createRemoveCommand(modelState, eStructuralFeature.getEContainingClass(),
				EcorePackage.Literals.ECLASS__ESTRUCTURAL_FEATURES, eStructuralFeature);
	}

	public boolean removeEAttribute(EcoreModelState modelState, EAttribute eAttribute) {
		return this.edit(createRemoveEStructuralFeatureCommand(modelState, eAttribute));
	}

	public boolean removeEReference(EcoreModelState modelState, EReference eReference) {
		CCompoundCommand compoundCommand = CCommandFactory.eINSTANCE.createCompoundCommand();
		compoundCommand.setType(CommandKind.COMPOUND);

		try {
			Command removeCommand = createRemoveEStructuralFeatureCommand(modelState, eReference);
			compoundCommand.getCommands().add(getCommandCodec().encode(removeCommand));

			Command removeNotationElementCommand = createRemoveNotationElementCommand(modelState,
					getNotationElement(modelState, eReference));
			compoundCommand.getCommands().add(getCommandCodec().encode(removeNotationElementCommand));

			if (eReference.getEOpposite() != null) {
				EReference eOpposite = eReference.getEOpposite();
				Command removeOppositeCommand = createRemoveEStructuralFeatureCommand(modelState, eOpposite);
				compoundCommand.getCommands().add(getCommandCodec().encode(removeOppositeCommand));

				Command removeOppositeNotationElementCommand = createRemoveNotationElementCommand(modelState,
						getNotationElement(modelState, eOpposite));
				compoundCommand.getCommands().add(getCommandCodec().encode(removeOppositeNotationElementCommand));
			}
		} catch (EncodingException e) {
			return false;
		}
		return this.editCompound(compoundCommand);
	}

	private Command createRemoveEClassifierCommand(EcoreModelState modelState, EClassifier eClassifier) {
		return createRemoveCommand(modelState, eClassifier.getEPackage(), EcorePackage.Literals.EPACKAGE__ECLASSIFIERS,
				eClassifier);
	}

	private Command createRemoveNotationElementCommand(EcoreModelState modelState, NotationElement notationElement) {
		return createRemoveCommand(modelState, getDiagram(modelState), EnotationPackage.Literals.DIAGRAM__ELEMENTS,
				notationElement);
	}

	public boolean removeEClassifier(EcoreModelState modelState, EClassifier eClassifier) {
		Command removeEClassifierCommand = createRemoveEClassifierCommand(modelState, eClassifier);

		Collection<Setting> usages = UsageCrossReferencer.find(eClassifier, eClassifier.eResource().getResourceSet());
		if (!usages.isEmpty()) {
			CCompoundCommand compoundCommand = CCommandFactory.eINSTANCE.createCompoundCommand();
			compoundCommand.setType(CommandKind.COMPOUND);

			try {
				compoundCommand.getCommands().add(getCommandCodec().encode(removeEClassifierCommand));
				Command removeNotationElementCommand = createRemoveNotationElementCommand(modelState,
						getNotationElement(modelState, eClassifier));
				compoundCommand.getCommands().add(getCommandCodec().encode(removeNotationElementCommand));

				// Remove notation elements for EStructuralFeatures and ESuperTypes
				if (eClassifier instanceof EClass) {
					EClass eClass = ((EClass) eClassifier);
					for (EStructuralFeature eStructuralFeature : eClass.getEStructuralFeatures()) {
						NotationElement notationElement = getNotationElement(modelState, eStructuralFeature);
						if (notationElement != null) {
							Command removeNotationCommand = createRemoveNotationElementCommand(modelState,
									notationElement);
							compoundCommand.getCommands().add(getCommandCodec().encode(removeNotationCommand));
						}
					}
					for (EClass eSuperType : eClass.getESuperTypes()) {
						NotationElement notationElement = getNotationElement(modelState, eClass, eSuperType);
						if (notationElement != null) {
							Command removeNotationCommand = createRemoveNotationElementCommand(modelState,
									notationElement);
							compoundCommand.getCommands().add(getCommandCodec().encode(removeNotationCommand));
						}
					}
				}
			} catch (EncodingException e) {
				return false;
			}

			// Remove usages
			for (Setting setting : usages) {
				EObject eObject = setting.getEObject();
				if (setting.getEStructuralFeature().isChangeable() && eObject.eContainer() instanceof EClass) {
					if (eObject instanceof EStructuralFeature) {
						try {
							Command removeCommand = createRemoveEStructuralFeatureCommand(modelState,
									(EStructuralFeature) eObject);
							compoundCommand.getCommands().add(getCommandCodec().encode(removeCommand));
							NotationElement notationElement = getNotationElement(modelState, eObject);
							if (notationElement != null) {
								Command removeNotationCommand = createRemoveNotationElementCommand(modelState,
										notationElement);
								compoundCommand.getCommands().add(getCommandCodec().encode(removeNotationCommand));
							}
						} catch (EncodingException e) {
							return false;
						}
					} else if (eObject instanceof EGenericType
							&& ((EGenericType) eObject).getEClassifier() instanceof EClass) {
						try {
							EClass eClass = (EClass) eObject.eContainer();
							EClass eSuperType = (EClass) ((EGenericType) eObject).getEClassifier();
							Command removeCommand = createRemoveESuperTypeCommand(modelState, eClass, eSuperType);
							compoundCommand.getCommands().add(getCommandCodec().encode(removeCommand));
							NotationElement notationElement = getNotationElement(modelState, eClass, eSuperType);
							if (notationElement != null) {
								Command removeNotationCommand = createRemoveNotationElementCommand(modelState,
										notationElement);
								compoundCommand.getCommands().add(getCommandCodec().encode(removeNotationCommand));
							}
						} catch (EncodingException e) {
							return false;
						}
					}
				}
			}
			return this.editCompound(compoundCommand);
		} else {
			return this.edit(removeEClassifierCommand);
		}
	}

	private Command createRemoveEEnumLiteralCommand(EcoreModelState modelState, EEnumLiteral eEnumLiteral) {
		return createRemoveCommand(modelState, eEnumLiteral.getEEnum(), EcorePackage.Literals.EENUM__ELITERALS,
				eEnumLiteral);
	}

	public boolean removeEEnumLiteral(EcoreModelState modelState, EEnumLiteral eEnumLiteral) {
		return this.edit(createRemoveEEnumLiteralCommand(modelState, eEnumLiteral));
	}

	private Command createRemoveEOperationCommand(EcoreModelState modelState, EOperation eOperation) {
		return createRemoveCommand(modelState, eOperation.getEContainingClass(),
				EcorePackage.Literals.ECLASS__EOPERATIONS, eOperation);
	}

	public boolean removeEOperation(EcoreModelState modelState, EOperation eOperation) {
		return this.edit(createRemoveEOperationCommand(modelState, eOperation));
	}

	private Command createRemoveESuperTypeCommand(EcoreModelState modelState, EClass eClass, EClass eSuperType) {
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
		compoundCommand.setType(CommandKind.COMPOUND);

		try {
			Command removeCommand = createRemoveESuperTypeCommand(modelState, eClass, eSuperType);
			compoundCommand.getCommands().add(getCommandCodec().encode(removeCommand));

			Command removeNotationElementCommand = createRemoveNotationElementCommand(modelState,
					getNotationElement(modelState, inheritanceEdgeId));
			compoundCommand.getCommands().add(getCommandCodec().encode(removeNotationElementCommand));

		} catch (EncodingException e) {
			return false;
		}
		return this.editCompound(compoundCommand);
	}

	private boolean editCompound(CCompoundCommand compoundCommand) {
		try {
			return this.modelServerClient.edit(getSemanticURI(), compoundCommand, FORMAT_XMI)
					.thenApply(res -> res.body()).get();
		} catch (InterruptedException | ExecutionException e) {
			return false;
		}
	}

	private boolean edit(Command command) {
		try {
			CCommand ccommand = getCommandCodec().encode(command);
			return this.modelServerClient.edit(getSemanticURI(), ccommand, FORMAT_XMI).thenApply(res -> res.body())
					.get();
		} catch (InterruptedException | ExecutionException | EncodingException e) {
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

}
