package fr.foop.ws.tools;

import java.util.HashMap;
import java.util.Map;

import javax.xml.ws.BindingProvider;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.slf4j.Logger;

public class WebServicePortConfigurer<Port> {

	private final Client client;
	private final HTTPConduit http;
	private final BindingProvider provider;
	private final Endpoint cxfEndpoint;

	public WebServicePortConfigurer(final Port port) {
		this.client = ClientProxy.getClient(port);
		this.http = (HTTPConduit) client.getConduit();
		this.provider = (BindingProvider) port;
		this.cxfEndpoint = client.getEndpoint();
	}

	public void configureEndpoint(final String endpoint, final String server) {
		final String concreteEndpoint = endpoint.replaceFirst(
				"\\{\\{server\\}\\}", server);

		provider.getRequestContext().put(
				BindingProvider.ENDPOINT_ADDRESS_PROPERTY, concreteEndpoint);
	}

	public void configureWsse(final String wsseUser, final String wssePwd) {
		Map<String, Object> outProps = new HashMap<String, Object>();
		outProps.put(WSHandlerConstants.ACTION,
				WSHandlerConstants.USERNAME_TOKEN);
		outProps.put(WSHandlerConstants.USER, wsseUser);
		outProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
		outProps.put(WSHandlerConstants.PW_CALLBACK_REF,
				new ClientPasswordCallback(wsseUser, wssePwd));

		WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
		cxfEndpoint.getOutInterceptors().add(wssOut);
	}

	public void configureInLogger(final Logger logger) {
		cxfEndpoint.getInInterceptors().add(new LoggingInInterceptor() {

			@Override
			protected void log(java.util.logging.Logger arg0, String arg1) {
				logger.info(arg1);
			}
		});
	}

	public void configureOutLogger(final Logger logger) {
		cxfEndpoint.getOutInterceptors().add(new LoggingOutInterceptor() {

			@Override
			protected void log(java.util.logging.Logger arg0, String arg1) {
				logger.info(arg1);
			}
		});
	}
	
	public void configureTimeouts(final long connectionTimeout, final long receiveTimeout) {
		final HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
		httpClientPolicy.setConnectionTimeout(connectionTimeout);
		httpClientPolicy.setAllowChunking(false);
		httpClientPolicy.setReceiveTimeout(receiveTimeout);
		http.setClient(httpClientPolicy);		
	}

}
