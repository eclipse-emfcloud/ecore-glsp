package org.eclipse.emfcloud.ecore.glsp.actions;

import org.eclipse.glsp.server.actions.ResponseAction;

public class SetSemanticUriAction extends ResponseAction {

	private String modelUri;
	private String semanticUri;
	private String elementEClass;

	public SetSemanticUriAction() {
		super(ActionKind.SET_SEMANTIC_URI);
	}

	public SetSemanticUriAction(final String modelUri, final String semanticUri, final String elementEClass) {
		this();
		this.modelUri = modelUri;
		this.semanticUri = semanticUri;
		this.elementEClass = elementEClass;
	}

	public String getModelUri() {
		return modelUri;
	}

	public void setModelUri(String modelUri) {
		this.modelUri = modelUri;
	}

	public String getSemanticUri() {
		return semanticUri;
	}

	public void setSemanticUri(final String semanticUri) {
		this.semanticUri = semanticUri;
	}

	public String getElementEClass() {
		return elementEClass;
	}

	public void setElementEClass(String elementEClass) {
		this.elementEClass = elementEClass;
	}

}
