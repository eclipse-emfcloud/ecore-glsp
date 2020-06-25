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
package org.eclipse.emfcloud.ecore.glsp.util;

public final class EcoreConfig {

	public static final class Types {

		public static final String LABEL_NAME = "label:name";
		public static final String LABEL_TEXT = "label:text";
		public static final String LABEL_EDGE_NAME = "label:edge-name";
		public static final String LABEL_EDGE_MULTIPLICITY = "label:edge-multiplicity";
		public static final String COMP = "comp:comp";
		public static final String COMP_HEADER = "comp:header";
		public static final String ICON_CLASS = "icon:class";
		public static final String ICON_ABSTRACT = "icon:abstract";
		public static final String ICON_INTERFACE = "icon:interface";
		public static final String ICON_ENUM = "icon:enum";
		public static final String ICON_DATATYPE = "icon:datatype";
		public static final String LABEL_ICON = "label:icon";
		public static final String ECLASS = "node:class";
		public static final String ENUM = "node:enum";
		public static final String DATATYPE = "node:datatype";
		public static final String REFERENCE = "edge:reference";
		public static final String BIDIRECTIONAL_REFERENCE = "edge:bidirectional_reference";
		public static final String ATTRIBUTE = "node:attribute";
		public static final String OPERATION = "node:operation";
		public static final String ENUMLITERAL = "node:enumliteral";
		public static final String COMPOSITION = "edge:composition";
		public static final String BIDIRECTIONAL_COMPOSITION = "edge:bidirectional_composition";
		public static final String INHERITANCE = "edge:inheritance";
		public static final String ABSTRACT = "node:class:abstract";
		public static final String INTERFACE = "node:class:interface";
		public static final String LABEL_INSTANCE = "label:instancename";

		private Types() {
		};
	}

	public static final class CSS {

		public static final String NODE = "ecore-node";
		public static final String FOREIGN_PACKAGE = "foreign-package";
		public static final String ABSTRACT = "abstract";
		public static final String INTERFACE = "interface";
		public static final String COMPOSITION = "composition";
		public static final String ECORE_EDGE = "ecore-edge";
		public static final String INHERITANCE = "inheritance";
		public static final String ITALIC = "italic";

		private CSS() {
		};
	}

	private EcoreConfig() {
	};
}
