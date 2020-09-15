package org.eclipse.emfcloud.ecore.glsp.actions;

import org.eclipse.emfcloud.ecore.glsp.properties.EcoreProperties;
import org.eclipse.glsp.api.action.kind.ResponseAction;

public class SetEcorePropertiesAction extends ResponseAction {

	private EcoreProperties ecoreProperties;

	public SetEcorePropertiesAction() {
		super(ActionKind.SET_ECORE_PROPERTIES);
	}

	public SetEcorePropertiesAction(final EcoreProperties ecoreProperties) {
		this();
		this.ecoreProperties = ecoreProperties;
	}

	public EcoreProperties getEcoreProperties() {
		return ecoreProperties;
	}

	public void setEcoreProperties(final EcoreProperties ecoreProperties) {
		this.ecoreProperties = ecoreProperties;
	}
	
}
