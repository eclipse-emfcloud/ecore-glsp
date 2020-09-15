package org.eclipse.emfcloud.ecore.glsp.actions;

import org.eclipse.glsp.api.action.kind.RequestAction;

public class RequestEcorePropertiesAction extends RequestAction<SetEcorePropertiesAction> {

	private String graphicElementId;

	public RequestEcorePropertiesAction() {
		super(ActionKind.REQUEST_ECORE_PROPERTIES);
	}

	public RequestEcorePropertiesAction(final String graphicElementId) {
		this();
		this.graphicElementId = graphicElementId;
	}

	public String getGraphicElementId() {
		return graphicElementId;
	}

	public void setGraphicElementId(final String graphicElementId) {
		this.graphicElementId = graphicElementId;
	}

}
