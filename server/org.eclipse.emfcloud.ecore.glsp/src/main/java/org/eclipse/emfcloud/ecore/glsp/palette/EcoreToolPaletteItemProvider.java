/********************************************************************************
 * Copyright (c) 2021 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the MIT License which is
 * available at https://opensource.org/licenses/MIT.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT
 ********************************************************************************/
package org.eclipse.emfcloud.ecore.glsp.palette;

import java.util.List;
import java.util.Map;

import org.eclipse.emfcloud.ecore.glsp.util.EcoreConfig.Types;
import org.eclipse.glsp.server.actions.TriggerEdgeCreationAction;
import org.eclipse.glsp.server.actions.TriggerNodeCreationAction;
import org.eclipse.glsp.server.features.toolpalette.PaletteItem;
import org.eclipse.glsp.server.features.toolpalette.ToolPaletteItemProvider;
import org.eclipse.glsp.server.model.GModelState;

import com.google.common.collect.Lists;

public class EcoreToolPaletteItemProvider implements ToolPaletteItemProvider {

	@Override
	public List<PaletteItem> getItems(Map<String, String> args, GModelState modelState) {
		System.err.println("Create palette");
		return Lists.newArrayList(classifiers(), relations(), features());
	}

	private PaletteItem classifiers() {
		PaletteItem createEClass = node(Types.ECLASS, "Class", "eclass");
		PaletteItem createAbstract = node(Types.ABSTRACT, "Abstract", "eclassabstract");
		PaletteItem createInterface = node(Types.INTERFACE, "Interface", "eclassinterface");
		PaletteItem createEnum = node(Types.ENUM, "Enum", "eenum");
		PaletteItem createDataType = node(Types.DATATYPE, "DataType", "edatatype");

		List<PaletteItem> classifiers = Lists.newArrayList(createEClass, createAbstract, createInterface, createEnum,
				createDataType);
		return PaletteItem.createPaletteGroup("ecore.classifier", "Classifier", classifiers, "symbol-property");
	}

	private PaletteItem relations() {
		PaletteItem createEcoreEdge = edge(Types.REFERENCE, "Reference", "ereference");
		PaletteItem createComposition = edge(Types.COMPOSITION, "Containment", "ereference");
		PaletteItem createInheritance = edge(Types.INHERITANCE, "Inheritance", "egenericsupertype");
		PaletteItem createBiReference = edge(Types.BIDIRECTIONAL_REFERENCE, "Bi-Directional Reference", "ereference");
		PaletteItem createBiComposition = edge(Types.BIDIRECTIONAL_COMPOSITION, "Bi-Directional Containment", "ereference");

		List<PaletteItem> edges = Lists.newArrayList(createEcoreEdge, createComposition, createInheritance,
				createBiReference, createBiComposition);
		return PaletteItem.createPaletteGroup("ecore.relation", "Relation", edges, "symbol-property");
	}

	private PaletteItem features() {
		PaletteItem createAttribute = node(Types.ATTRIBUTE, "Attribute", "eattribute");
		PaletteItem createEnumLiteral = node(Types.ENUMLITERAL, "Literal", "eenumliteral");
		PaletteItem createOperation = node(Types.OPERATION, "Operation", "eoperation");

		List<PaletteItem> features = Lists.newArrayList(createAttribute, createEnumLiteral, createOperation);
		
		return PaletteItem.createPaletteGroup("ecore.feature", "Feature", features, "symbol-property");
	}

	private PaletteItem node(String elementTypeId, String label, String icon) {
		return new PaletteItem(elementTypeId, label, new TriggerNodeCreationAction(elementTypeId), icon);
	}

	private PaletteItem edge(String elementTypeId, String label, String icon) {
		return new PaletteItem(elementTypeId, label, new TriggerEdgeCreationAction(elementTypeId), icon);
	}
}
