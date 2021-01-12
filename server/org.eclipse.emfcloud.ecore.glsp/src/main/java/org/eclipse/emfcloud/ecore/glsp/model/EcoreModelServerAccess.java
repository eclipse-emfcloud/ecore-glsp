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
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil.UsageCrossReferencer;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emfcloud.ecore.glsp.EcoreFacade;
import org.eclipse.emfcloud.modelserver.client.ModelServerClient;
import org.eclipse.emfcloud.modelserver.client.NotificationSubscriptionListener;
import org.eclipse.emfcloud.modelserver.command.CCommand;
import org.eclipse.emfcloud.modelserver.command.CCommandFactory;
import org.eclipse.emfcloud.modelserver.command.CCompoundCommand;
import org.eclipse.emfcloud.modelserver.command.CommandKind;
import org.eclipse.emfcloud.modelserver.common.codecs.EncodingException;
import org.eclipse.emfcloud.modelserver.edit.CommandCodec;
import org.eclipse.glsp.server.model.GModelState;

import com.google.common.base.Preconditions;

public class EcoreModelServerAccess {

	private static Logger LOGGER = Logger.getLogger(EcoreModelServerAccess.class);

	private static final String FORMAT_XMI = "xmi";

	private String modelUri;

	private ModelServerClient modelServerClient;
	private NotificationSubscriptionListener<EObject> subscriptionListener;
	private CommandCodec commandCodec;

	public EcoreModelServerAccess(final String modelUri, final ModelServerClient modelServerClient,
			final CommandCodec commandCodec) {
		Preconditions.checkNotNull(modelServerClient);
		this.modelUri = modelUri;
		this.modelServerClient = modelServerClient;
		this.commandCodec = commandCodec;
	}

	public CommandCodec getCommandCodec() {
		return commandCodec;
	}

	public ModelServerClient getModelServerClient() {
		return modelServerClient;
	}

	public void update(GModelState graphicalModelState) {
		EcoreFacade facade = EcoreModelState.getEcoreFacade(graphicalModelState);
		EObject root = facade.getSemanticResource().getContents().get(0);
		// trigger full model update
		modelServerClient.update(modelUri, root, FORMAT_XMI);
	}

	public void subscribe(NotificationSubscriptionListener<EObject> subscriptionListener) {
		LOGGER.debug("EcoreModelServerAccess - subscribe");
		this.subscriptionListener = subscriptionListener;
		this.modelServerClient.subscribe(modelUri, subscriptionListener, FORMAT_XMI);
	}

	public void unsubscribe() {
		LOGGER.debug("EcoreModelServerAccess - unsubscribe");
		if (subscriptionListener != null) {
			this.modelServerClient.unsubscribe(modelUri);
		}
	}

	private EPackage getEPackage(EcoreModelState modelState) {
		EcoreFacade facade = EcoreModelState.getEcoreFacade(modelState);
		return facade.getEPackage();
	}

	public boolean addEClassifier(EcoreModelState modelState, EClassifier newEClassifier) {
		return this.add(modelState, getEPackage(modelState), EcorePackage.eINSTANCE.getEPackage_EClassifiers(), newEClassifier);
	}
	
	public boolean addSuperType(EcoreModelState modelState, EClassifier superType, EClassifier parent) {
		return this.add(modelState, parent, EcorePackage.eINSTANCE.getEClass_ESuperTypes(), superType);
	}
	
	public boolean addEReference(EcoreModelState modelState, EReference newEReference, EClassifier parent) {
		return this.add(modelState, parent, EcorePackage.eINSTANCE.getEClass_EStructuralFeatures(), newEReference);
	}
	
	public boolean addEAttribute(EcoreModelState modelState, EAttribute newEAttribute, EClassifier parent) {
		return this.add(modelState, parent, EcorePackage.eINSTANCE.getEClass_EStructuralFeatures(), newEAttribute);
	}
	
	public boolean addEEnumLiteral(EcoreModelState modelState, EEnumLiteral newEEnumLiteral, EEnum parent) {
		return this.add(modelState, parent, EcorePackage.eINSTANCE.getEEnum_ELiterals(), newEEnumLiteral);
	}
	
	public boolean setOpposite(EcoreModelState modelState, EReference eReference, EReference opposite) {
		return this.set(modelState, eReference, EcorePackage.eINSTANCE.getEReference_EOpposite(), opposite);
	}
	
	public boolean setInstanceName(EcoreModelState modelState, EClassifier eClassifier, String name) {
		return this.set(modelState, eClassifier, EcorePackage.eINSTANCE.getEClassifier_InstanceClassName(), name);
	}
	
	public boolean setName(EcoreModelState modelState, EClassifier eClassifier, String name) {
		return this.set(modelState, eClassifier, EcorePackage.eINSTANCE.getENamedElement_Name(), name);
	}
	
	public boolean setAttributeName(EcoreModelState modelState, EAttribute eAttribute, String name) {
		return this.set(modelState, eAttribute, EcorePackage.eINSTANCE.getENamedElement_Name(), name);
	}
	
	public boolean setAttributeType(EcoreModelState modelState, EAttribute eAttribute, EClassifier type) {
		return this.set(modelState, eAttribute, EcorePackage.eINSTANCE.getEAttribute_EAttributeType(), type);
	}
	
	public boolean setLiteralName(EcoreModelState modelState, EEnumLiteral eEnumLiteral, String literal) {
		return this.set(modelState, eEnumLiteral, EcorePackage.eINSTANCE.getENamedElement_Name(), literal);
	}
	
	public boolean setAttributeType(EcoreModelState modelState, EAttribute eAttribute, EAttribute type) {
		return this.set(modelState, eAttribute, EcorePackage.eINSTANCE.getEAttribute_EAttributeType(), type);
	}
	
	public boolean setEdgeName(EcoreModelState modelState, EReference eReference, String name) {
		return this.set(modelState, eReference, EcorePackage.eINSTANCE.getENamedElement_Name(), name);
	}
	
	public boolean setLowerMultiplicity(EcoreModelState modelState, EReference eReference, int multiplicity) {
		return this.set(modelState, eReference, EcorePackage.eINSTANCE.getETypedElement_LowerBound(), multiplicity);
	}
	
	public boolean setUpperMultiplicity(EcoreModelState modelState, EReference eReference, int multiplicity) {
		return this.set(modelState, eReference, EcorePackage.eINSTANCE.getETypedElement_UpperBound(), multiplicity);
	}

	private boolean add(EcoreModelState modelState, EObject owner, EReference feature, Object addObject) {
		Command addCommand = AddCommand.create(
				EcoreModelState.getEditorContext(modelState).getResourceManager().getEditingDomain(), owner, feature,
				addObject);
		return this.edit(addCommand);
	}
	
	private boolean set(EcoreModelState modelState, EObject owner, EStructuralFeature feature, Object setObject) {
		Command setCommand = SetCommand.create(
				EcoreModelState.getEditorContext(modelState).getResourceManager().getEditingDomain(), owner, feature,
				setObject);
		return this.edit(setCommand);
	}

	private Command createRemoveCommand(EcoreModelState modelState, EObject owner, EStructuralFeature feature,
			int index) {
		EditingDomain editingDomain = EcoreModelState.getEditorContext(modelState).getResourceManager()
				.getEditingDomain();
		return RemoveCommand.create(editingDomain, owner, feature, index);
	}

	private Command createRemoveEStructuralFeatureCommand(EcoreModelState modelState,
			EStructuralFeature eStructuralFeature) {
		int index = eStructuralFeature.getEContainingClass().getEStructuralFeatures().indexOf(eStructuralFeature);
		return createRemoveCommand(modelState, eStructuralFeature.getEContainingClass(),
				EcorePackage.Literals.ECLASS__ESTRUCTURAL_FEATURES, index);
	}

	public boolean removeEAttribute(EcoreModelState modelState, EAttribute eAttribute) {
		return this.edit(createRemoveEStructuralFeatureCommand(modelState, eAttribute));
	}

	public boolean removeEReference(EcoreModelState modelState, EReference eReference) {
		EReference eOpposite = eReference.getEOpposite();
		if (eOpposite != null) {
			Command removeCommand = createRemoveEStructuralFeatureCommand(modelState, eReference);
			Command removeOppositeCommand = createRemoveEStructuralFeatureCommand(modelState, eOpposite);
			CCompoundCommand compoundCommand = CCommandFactory.eINSTANCE.createCompoundCommand();
			compoundCommand.setType(CommandKind.COMPOUND);
			try {
				compoundCommand.getCommands().add(getCommandCodec().encode(removeCommand));
				compoundCommand.getCommands().add(getCommandCodec().encode(removeOppositeCommand));
			} catch (EncodingException e) {
				return false;
			}
			return this.editCompound(compoundCommand);
		}
		return this.edit(createRemoveEStructuralFeatureCommand(modelState, eReference));
	}

	private Command createRemoveEClassifierCommand(EcoreModelState modelState, EClassifier eClassifier) {
		int index = eClassifier.getEPackage().getEClassifiers().indexOf(eClassifier);
		return createRemoveCommand(modelState, eClassifier.getEPackage(), EcorePackage.Literals.EPACKAGE__ECLASSIFIERS,
				index);
	}

	public boolean removeEClassifier(EcoreModelState modelState, EClassifier eClassifier) {
		Command removeEClassifierCommand = createRemoveEClassifierCommand(modelState, eClassifier);

		Collection<Setting> usages = UsageCrossReferencer.find(eClassifier, eClassifier.eResource().getResourceSet());
		if (!usages.isEmpty()) {
			CCompoundCommand compoundCommand = CCommandFactory.eINSTANCE.createCompoundCommand();
			compoundCommand.setType(CommandKind.COMPOUND);

			try {
				compoundCommand.getCommands().add(getCommandCodec().encode(removeEClassifierCommand));
			} catch (EncodingException e) {
				return false;
			}

			for (Setting setting : usages) {
				EObject eObject = setting.getEObject();
				if (setting.getEStructuralFeature().isChangeable() && eObject.eContainer() instanceof EClass) {
					if (eObject instanceof EStructuralFeature) {
						Command removeCommand = createRemoveEStructuralFeatureCommand(modelState,
								(EStructuralFeature) eObject);
						try {
							compoundCommand.getCommands().add(getCommandCodec().encode(removeCommand));
						} catch (EncodingException e) {
							return false;
						}
					} else if (eObject instanceof EGenericType && ((EGenericType) eObject).getEClassifier() instanceof EClass) {
						Command removeCommand = createRemoveESuperTypeCommand(modelState, (EClass) ((EGenericType) eObject).getEClassifier(),
								(EClass) eObject.eContainer());
						try {
							compoundCommand.getCommands().add(getCommandCodec().encode(removeCommand));
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
		int index = eEnumLiteral.getEEnum().getELiterals().indexOf(eEnumLiteral);
		return createRemoveCommand(modelState, eEnumLiteral.getEEnum(), EcorePackage.Literals.EENUM__ELITERALS, index);
	}

	public boolean removeEEnumLiteral(EcoreModelState modelState, EEnumLiteral eEnumLiteral) {
		return this.edit(createRemoveEEnumLiteralCommand(modelState, eEnumLiteral));
	}

	private Command createRemoveESuperTypeCommand(EcoreModelState modelState, EClass eSuperType, EClass eClass) {
		int index = eClass.getESuperTypes().indexOf(eSuperType);
		return createRemoveCommand(modelState, eClass, EcorePackage.Literals.ECLASS__ESUPER_TYPES, index);
	}

	public boolean removeESuperType(EcoreModelState modelState, EClass eSuperType, EClass eClass) {
		return this.edit(createRemoveESuperTypeCommand(modelState, eSuperType, eClass));
	}

	private boolean editCompound(CCompoundCommand compoundCommand) {
		try {
			return this.modelServerClient.edit(modelUri, compoundCommand, FORMAT_XMI).thenApply(res -> res.body())
					.get();
		} catch (InterruptedException | ExecutionException e) {
			return false;
		}
	}

	private boolean edit(Command command) {
		try {
			CCommand ccommand = getCommandCodec().encode(command);
			return this.modelServerClient.edit(modelUri, ccommand, FORMAT_XMI).thenApply(res -> res.body()).get();
		} catch (InterruptedException | ExecutionException | EncodingException e) {
			return false;
		}
	}

	public boolean save() {
		try {
			return this.modelServerClient.save(modelUri).thenApply(res -> res.body()).get();
		} catch (InterruptedException | ExecutionException e) {
			return false;
		}
	}

}
