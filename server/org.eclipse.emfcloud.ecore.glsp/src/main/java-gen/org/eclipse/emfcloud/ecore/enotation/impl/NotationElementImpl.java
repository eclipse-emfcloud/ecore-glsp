/**
 * Copyright (c) 2019-2020 EclipseSource and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 */
package org.eclipse.emfcloud.ecore.enotation.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emfcloud.ecore.enotation.EnotationPackage;
import org.eclipse.emfcloud.ecore.enotation.NotationElement;
import org.eclipse.emfcloud.ecore.enotation.SemanticProxy;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Notation Element</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.emfcloud.ecore.enotation.impl.NotationElementImpl#getSemanticElement <em>Semantic Element</em>}</li>
 *   <li>{@link org.eclipse.emfcloud.ecore.enotation.impl.NotationElementImpl#getGraphicId <em>Graphic Id</em>}</li>
 * </ul>
 *
 * @generated
 */
public abstract class NotationElementImpl extends MinimalEObjectImpl.Container implements NotationElement {
	/**
	 * The cached value of the '{@link #getSemanticElement() <em>Semantic Element</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSemanticElement()
	 * @generated
	 * @ordered
	 */
	protected SemanticProxy semanticElement;

	/**
	 * The default value of the '{@link #getGraphicId() <em>Graphic Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGraphicId()
	 * @generated
	 * @ordered
	 */
	protected static final String GRAPHIC_ID_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getGraphicId() <em>Graphic Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGraphicId()
	 * @generated
	 * @ordered
	 */
	protected String graphicId = GRAPHIC_ID_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected NotationElementImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return EnotationPackage.Literals.NOTATION_ELEMENT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SemanticProxy getSemanticElement() {
		return semanticElement;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetSemanticElement(SemanticProxy newSemanticElement, NotificationChain msgs) {
		SemanticProxy oldSemanticElement = semanticElement;
		semanticElement = newSemanticElement;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, EnotationPackage.NOTATION_ELEMENT__SEMANTIC_ELEMENT, oldSemanticElement, newSemanticElement);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSemanticElement(SemanticProxy newSemanticElement) {
		if (newSemanticElement != semanticElement) {
			NotificationChain msgs = null;
			if (semanticElement != null)
				msgs = ((InternalEObject)semanticElement).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - EnotationPackage.NOTATION_ELEMENT__SEMANTIC_ELEMENT, null, msgs);
			if (newSemanticElement != null)
				msgs = ((InternalEObject)newSemanticElement).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - EnotationPackage.NOTATION_ELEMENT__SEMANTIC_ELEMENT, null, msgs);
			msgs = basicSetSemanticElement(newSemanticElement, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, EnotationPackage.NOTATION_ELEMENT__SEMANTIC_ELEMENT, newSemanticElement, newSemanticElement));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getGraphicId() {
		return graphicId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setGraphicId(String newGraphicId) {
		String oldGraphicId = graphicId;
		graphicId = newGraphicId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, EnotationPackage.NOTATION_ELEMENT__GRAPHIC_ID, oldGraphicId, graphicId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case EnotationPackage.NOTATION_ELEMENT__SEMANTIC_ELEMENT:
				return basicSetSemanticElement(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case EnotationPackage.NOTATION_ELEMENT__SEMANTIC_ELEMENT:
				return getSemanticElement();
			case EnotationPackage.NOTATION_ELEMENT__GRAPHIC_ID:
				return getGraphicId();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case EnotationPackage.NOTATION_ELEMENT__SEMANTIC_ELEMENT:
				setSemanticElement((SemanticProxy)newValue);
				return;
			case EnotationPackage.NOTATION_ELEMENT__GRAPHIC_ID:
				setGraphicId((String)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case EnotationPackage.NOTATION_ELEMENT__SEMANTIC_ELEMENT:
				setSemanticElement((SemanticProxy)null);
				return;
			case EnotationPackage.NOTATION_ELEMENT__GRAPHIC_ID:
				setGraphicId(GRAPHIC_ID_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case EnotationPackage.NOTATION_ELEMENT__SEMANTIC_ELEMENT:
				return semanticElement != null;
			case EnotationPackage.NOTATION_ELEMENT__GRAPHIC_ID:
				return GRAPHIC_ID_EDEFAULT == null ? graphicId != null : !GRAPHIC_ID_EDEFAULT.equals(graphicId);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (graphicId: ");
		result.append(graphicId);
		result.append(')');
		return result.toString();
	}

} //NotationElementImpl
