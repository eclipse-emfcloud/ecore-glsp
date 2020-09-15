package org.eclipse.emfcloud.ecore.glsp.properties;

public class EcoreProperties {

	private final String graphicId;
	private final String name;
	private final String nsPrefix;
	private final String nsUri;
	private final String instanceClassName;
	private final String instanceClassType;
	private final boolean isAbstract;
	private final boolean isInterface;
	private final boolean hasSuperTypes;

	public EcoreProperties(final String graphicId, final String name, final String nsPrefix, final String nsUri, final String instanceClassName,
			final String instanceClassType, final boolean isAbstract, final boolean isInterface,
			final boolean hasSuperTypes) {
		super();
		this.graphicId = graphicId;
		this.name = name;
		this.nsPrefix = nsPrefix;
		this.nsUri = nsUri;
		this.instanceClassName = instanceClassName;
		this.instanceClassType = instanceClassType;
		this.isAbstract = isAbstract;
		this.isInterface = isInterface;
		this.hasSuperTypes = hasSuperTypes;
	}

	public EcoreProperties(final String graphicId, final String name, final String nsPrefix, final String nsUri) {
		this(graphicId, name, nsPrefix, nsUri, "", "", false, false, false);
	}
	
	public EcoreProperties(final String graphicId, final String name, final String instanceClassName,
			final String instanceClassType, final boolean isAbstract, final boolean isInterface,
			final boolean hasSuperTypes) {
		this(graphicId, name, "", "", instanceClassName, instanceClassType, isAbstract, isInterface, hasSuperTypes);
	}
	
	public String getGraphicId() {
		return graphicId;
	}

	public String getName() {
		return name;
	}

	public String getNsPrefix() {
		return nsPrefix;
	}

	public String getNsUri() {
		return nsUri;
	}

	public String getInstanceClassName() {
		return instanceClassName;
	}

	public String getInstanceClassType() {
		return instanceClassType;
	}

	public boolean isAbstract() {
		return isAbstract;
	}

	public boolean isInterface() {
		return isInterface;
	}

	public boolean hasSuperTypes() {
		return hasSuperTypes;
	}

}
