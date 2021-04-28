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
package org.eclipse.emfcloud.ecore.glsp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emfcloud.ecore.glsp.util.EcoreConfig.Types;
import org.eclipse.glsp.graph.DefaultTypes;
import org.eclipse.glsp.graph.GraphPackage;
import org.eclipse.glsp.server.diagram.DiagramConfiguration;
import org.eclipse.glsp.server.diagram.EdgeTypeHint;
import org.eclipse.glsp.server.diagram.ShapeTypeHint;
import org.eclipse.glsp.server.layout.ServerLayoutKind;

import com.google.common.collect.Lists;

public class EcoreDiagramConfiguration implements DiagramConfiguration {

	@Override
	public String getDiagramType() {
		return "ecorediagram";
	}

	@Override
	public List<EdgeTypeHint> getEdgeTypeHints() {
		return Lists.newArrayList(createDefaultEdgeTypeHint(Types.REFERENCE),
				createDefaultEdgeTypeHint(Types.COMPOSITION),
				createDefaultEdgeTypeHint(Types.INHERITANCE),
				createDefaultEdgeTypeHint(Types.BIDIRECTIONAL_REFERENCE),
				createDefaultEdgeTypeHint(Types.BIDIRECTIONAL_COMPOSITION));
	}

	@Override
	public EdgeTypeHint createDefaultEdgeTypeHint(String elementId) {
		List<String> allowed = Lists.newArrayList(Types.ECLASS, Types.INTERFACE, Types.ABSTRACT);
		return new EdgeTypeHint(elementId, true, true, true, allowed, allowed);
	}

	@Override
	public List<ShapeTypeHint> getNodeTypeHints() {
		List<ShapeTypeHint> hints = new ArrayList<>();
		hints.add(new ShapeTypeHint(DefaultTypes.GRAPH, false, false, false, false,
				List.of(Types.ECLASS, Types.ABSTRACT, Types.INTERFACE, Types.ENUM, Types.DATATYPE)));
		hints.add(new ShapeTypeHint(Types.ECLASS, true, true, false, false, List.of(Types.ATTRIBUTE, Types.OPERATION)));
		hints.add(new ShapeTypeHint(Types.ENUM, true, true, false, false, List.of(Types.ENUMLITERAL)));
		hints.add(new ShapeTypeHint(Types.DATATYPE, true, true, false, true));
		hints.add(new ShapeTypeHint(Types.ATTRIBUTE, false, true, false, true));
		hints.add(new ShapeTypeHint(Types.OPERATION, false, true, false, true));
		hints.add(new ShapeTypeHint(Types.ENUMLITERAL, false, true, false, true));
		return hints;
	}

	@Override
	public Map<String, EClass> getTypeMappings() {
		Map<String, EClass> mappings = DefaultTypes.getDefaultTypeMappings();

		mappings.put(Types.LABEL_NAME, GraphPackage.Literals.GLABEL);
		mappings.put(Types.LABEL_TEXT, GraphPackage.Literals.GLABEL);
		mappings.put(Types.LABEL_EDGE_NAME, GraphPackage.Literals.GLABEL);
		mappings.put(Types.LABEL_EDGE_MULTIPLICITY, GraphPackage.Literals.GLABEL);
		mappings.put(Types.COMP, GraphPackage.Literals.GCOMPARTMENT);
		mappings.put(Types.COMP_HEADER, GraphPackage.Literals.GCOMPARTMENT);
		mappings.put(Types.LABEL_ICON, GraphPackage.Literals.GCOMPARTMENT);
		mappings.put(Types.ICON_CLASS, GraphPackage.Literals.GCOMPARTMENT);
		mappings.put(Types.ICON_ABSTRACT, GraphPackage.Literals.GCOMPARTMENT);
		mappings.put(Types.ICON_INTERFACE, GraphPackage.Literals.GCOMPARTMENT);
		mappings.put(Types.ICON_ENUM, GraphPackage.Literals.GCOMPARTMENT);
		mappings.put(Types.ICON_DATATYPE, GraphPackage.Literals.GCOMPARTMENT);

		// ecore stuff
		mappings.put(Types.ECLASS, GraphPackage.Literals.GNODE);
		mappings.put(Types.ENUM, GraphPackage.Literals.GNODE);
		mappings.put(Types.DATATYPE, GraphPackage.Literals.GNODE);
		mappings.put(Types.REFERENCE, GraphPackage.Literals.GEDGE);
		mappings.put(Types.INHERITANCE, GraphPackage.Literals.GEDGE);
		mappings.put(Types.COMPOSITION, GraphPackage.Literals.GEDGE);
		mappings.put(Types.BIDIRECTIONAL_REFERENCE, GraphPackage.Literals.GEDGE);
		mappings.put(Types.BIDIRECTIONAL_COMPOSITION, GraphPackage.Literals.GEDGE);
		mappings.put(Types.ATTRIBUTE, GraphPackage.Literals.GLABEL);
		mappings.put(Types.OPERATION, GraphPackage.Literals.GLABEL);
		mappings.put(Types.ENUMLITERAL, GraphPackage.Literals.GLABEL);
		mappings.put(Types.LABEL_INSTANCE, GraphPackage.Literals.GLABEL);
		return mappings;
	}

	@Override
	public ServerLayoutKind getLayoutKind() {
		return ServerLayoutKind.MANUAL;
	}

}
