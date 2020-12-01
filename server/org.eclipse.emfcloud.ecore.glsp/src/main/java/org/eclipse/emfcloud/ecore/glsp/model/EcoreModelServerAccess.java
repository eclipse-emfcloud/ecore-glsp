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
import org.eclipse.emf.ecore.EClassifier;
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
		return this.add(modelState, getEPackage(modelState), EcorePackage.Literals.EPACKAGE__ECLASSIFIERS,
				newEClassifier);
	}

	private boolean add(EcoreModelState modelState, EObject owner, EReference feature, EObject element) {
		Command addCommand = AddCommand.create(
				EcoreModelState.getEditorContext(modelState).getResourceManager().getEditingDomain(), owner, feature,
				element);
		return this.edit(addCommand);
	}

	public boolean removeEReference(EcoreModelState modelState, EReference eReference) {
		EReference eOpposite = eReference.getEOpposite();
		EReference feature = EcorePackage.Literals.ECLASS__ESTRUCTURAL_FEATURES;
		if (eOpposite != null) {
			Command removeCommand = createRemoveCommand(modelState, eReference, feature);
			Command removeOppositeCommand = createRemoveCommand(modelState, eOpposite, feature);
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
		return this.remove(modelState, eReference, feature);
	}

	public boolean removeEClassifier(EcoreModelState modelState, EClassifier eClassifier) {
		Command removeEClassifierCommand = createRemoveCommand(modelState, eClassifier,
				EcorePackage.Literals.EPACKAGE__ECLASSIFIERS);

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
				if (setting.getEStructuralFeature().isChangeable() && eObject instanceof EStructuralFeature) {
					Command removeCommand = createRemoveCommand(modelState, eObject,
							EcorePackage.Literals.ECLASS__ESTRUCTURAL_FEATURES);
					try {
						compoundCommand.getCommands().add(getCommandCodec().encode(removeCommand));
					} catch (EncodingException e) {
						return false;
					}
				} else if (setting.getEStructuralFeature().isChangeable() && eObject instanceof EGenericType) {
					Command removeCommand = createRemoveCommand(modelState, eObject,
							EcorePackage.Literals.ECLASS__ESUPER_TYPES);
					try {
						compoundCommand.getCommands().add(getCommandCodec().encode(removeCommand));
					} catch (EncodingException e) {
						return false;
					}
				}
			}
			return this.editCompound(compoundCommand);
		} else {
			return this.edit(removeEClassifierCommand);
		}
	}

	public boolean removeEAttribute(EcoreModelState modelState, EAttribute eAttribute) {
		return this.remove(modelState, eAttribute, EcorePackage.Literals.ECLASS__ESTRUCTURAL_FEATURES);
	}

	public boolean removeEEnumLiteral(EcoreModelState modelState, EEnumLiteral eEnumLiteral) {
		return this.remove(modelState, eEnumLiteral, EcorePackage.Literals.EENUM__ELITERALS);
	}

	private boolean remove(EcoreModelState modelState, EObject element, EReference feature) {
		return this.edit(createRemoveCommand(modelState, element, feature));
	}

	private Command createRemoveCommand(EcoreModelState modelState, EObject element, EReference feature) {
		EditingDomain editingDomain = EcoreModelState.getEditorContext(modelState).getResourceManager()
				.getEditingDomain();
		int index = element.eContainer().eContents().indexOf(element);
		return RemoveCommand.create(editingDomain, element.eContainer(), feature, index);
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
