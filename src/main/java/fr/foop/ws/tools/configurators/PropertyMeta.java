package fr.foop.ws.tools.configurators;

public class PropertyMeta {

	public final String doc;
	public final CxfClientBuilderConfigurator configurator;
	
	public PropertyMeta(final String doc, final CxfClientBuilderConfigurator configurator) {
		this.doc = doc;
		this.configurator = configurator;
	}
}
