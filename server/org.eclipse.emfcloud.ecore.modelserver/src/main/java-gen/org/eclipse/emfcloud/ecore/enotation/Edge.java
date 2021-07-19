/**
 * Copyright (c) 2019-2021 EclipseSource and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 */
package org.eclipse.emfcloud.ecore.enotation;

import org.eclipse.emf.common.util.EList;

import org.eclipse.glsp.graph.GPoint;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Edge</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.emfcloud.ecore.enotation.Edge#getBendPoints <em>Bend Points</em>}</li>
 *   <li>{@link org.eclipse.emfcloud.ecore.enotation.Edge#getSource <em>Source</em>}</li>
 *   <li>{@link org.eclipse.emfcloud.ecore.enotation.Edge#getTarget <em>Target</em>}</li>
 * </ul>
 *
 * @see org.eclipse.emfcloud.ecore.enotation.EnotationPackage#getEdge()
 * @model
 * @generated
 */
public interface Edge extends NotationElement {
	/**
	 * Returns the value of the '<em><b>Bend Points</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.glsp.graph.GPoint}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Bend Points</em>' containment reference list.
	 * @see org.eclipse.emfcloud.ecore.enotation.EnotationPackage#getEdge_BendPoints()
	 * @model containment="true"
	 * @generated
	 */
	EList<GPoint> getBendPoints();

	/**
	 * Returns the value of the '<em><b>Source</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Source</em>' reference.
	 * @see #setSource(NotationElement)
	 * @see org.eclipse.emfcloud.ecore.enotation.EnotationPackage#getEdge_Source()
	 * @model
	 * @generated
	 */
	NotationElement getSource();

	/**
	 * Sets the value of the '{@link org.eclipse.emfcloud.ecore.enotation.Edge#getSource <em>Source</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Source</em>' reference.
	 * @see #getSource()
	 * @generated
	 */
	void setSource(NotationElement value);

	/**
	 * Returns the value of the '<em><b>Target</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Target</em>' reference.
	 * @see #setTarget(NotationElement)
	 * @see org.eclipse.emfcloud.ecore.enotation.EnotationPackage#getEdge_Target()
	 * @model
	 * @generated
	 */
	NotationElement getTarget();

	/**
	 * Sets the value of the '{@link org.eclipse.emfcloud.ecore.enotation.Edge#getTarget <em>Target</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Target</em>' reference.
	 * @see #getTarget()
	 * @generated
	 */
	void setTarget(NotationElement value);

} // Edge
