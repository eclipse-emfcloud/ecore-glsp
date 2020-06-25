package org.eclipse.emfcloud.ecore.glsp.palette;

import java.util.List;
import java.util.Map;

import org.eclipse.emfcloud.ecore.glsp.util.EcoreConfig.Types;
import org.eclipse.glsp.api.action.kind.TriggerEdgeCreationAction;
import org.eclipse.glsp.api.action.kind.TriggerNodeCreationAction;
import org.eclipse.glsp.api.model.GraphicalModelState;
import org.eclipse.glsp.api.provider.ToolPaletteItemProvider;
import org.eclipse.glsp.api.types.PaletteItem;

import com.google.common.collect.Lists;

public class EcoreToolPaletteItemProvider implements ToolPaletteItemProvider {

	@Override
	public List<PaletteItem> getItems(Map<String, String> args, GraphicalModelState modelState) {
		System.err.println("Create palette");
		return Lists.newArrayList(classifiers(), relations(), features());
	}

	private PaletteItem classifiers() {
		PaletteItem createEClass = node(Types.ECLASS, "Class");
		PaletteItem createAbstract = node(Types.ABSTRACT, "Abstract");
		PaletteItem createInterface = node(Types.INTERFACE, "Interface");
		PaletteItem createEnum = node(Types.ENUM, "Enum");
		PaletteItem createDataType = node(Types.DATATYPE, "DataType");

		List<PaletteItem> classifiers = Lists.newArrayList(createEClass, createAbstract, createInterface, createEnum,
				createDataType);
		return PaletteItem.createPaletteGroup("ecore.classifier", "Classifier", classifiers);
	}

	private PaletteItem relations() {
		PaletteItem createEcoreEdge = edge(Types.REFERENCE, "Reference");
		PaletteItem createComposition = edge(Types.COMPOSITION, "Containment");
		PaletteItem createInheritance = edge(Types.INHERITANCE, "Inheritance");
		PaletteItem createBiReference = edge(Types.BIDIRECTIONAL_REFERENCE, "Bi-Directional Reference");
		PaletteItem createBiComposition = edge(Types.BIDIRECTIONAL_COMPOSITION, "Bi-Directional Containment");

		List<PaletteItem> edges = Lists.newArrayList(createEcoreEdge, createComposition, createInheritance,
				createBiReference, createBiComposition);
		return PaletteItem.createPaletteGroup("ecore.relation", "Relation", edges);
	}

	private PaletteItem features() {
		PaletteItem createAttributeOperation = node(Types.ATTRIBUTE, "Attribute");
		PaletteItem createEnumLiteral = node(Types.ENUMLITERAL, "Literal");

		List<PaletteItem> features = Lists.newArrayList(createAttributeOperation, createEnumLiteral);
		
		return PaletteItem.createPaletteGroup("ecore.feature", "Feature", features);
	}

	private PaletteItem node(String elementTypeId, String label) {
		return new PaletteItem(elementTypeId, label, new TriggerNodeCreationAction(elementTypeId));
	}

	private PaletteItem edge(String elementTypeId, String label) {
		return new PaletteItem(elementTypeId, label, new TriggerEdgeCreationAction(elementTypeId));
	}
}
