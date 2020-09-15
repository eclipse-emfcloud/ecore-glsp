package org.eclipse.emfcloud.ecore.glsp.actions;

import org.eclipse.emfcloud.ecore.glsp.properties.EcoreProperties;
import org.eclipse.glsp.api.operation.Operation;

public class EditEcorePropertiesOperation extends Operation {
	private EcoreProperties properties;

	public EditEcorePropertiesOperation() {
		super(ActionKind.EDIT_ECORE_PROPERTIES);
	}

	public EditEcorePropertiesOperation(final EcoreProperties properties) {
		this();
		this.properties = properties;
	}

	public EcoreProperties getProperties() {
		return properties;
	}

	public void setProperties(final EcoreProperties properties) {
		this.properties = properties;
	}

}
