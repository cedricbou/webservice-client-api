package fr.foop.ws;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
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
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CxfClient<Port> {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CxfClient.class);

	private final CxfClientBuilder config;
	private final Port port;

	protected CxfClient(final CxfClientBuilder config) {
		this.config = config;
		this.port = electPort();
	}

	protected CxfClientBuilder config() {
		return config;
	}

	public abstract void checkIfPortUp(final Port port) throws Exception;

	public abstract Port newPort();

	private void configure(final Port port, final String server) {
		final Client client = ClientProxy.getClient(port);
		HTTPConduit http = (HTTPConduit) client.getConduit();

		BindingProvider provider = (BindingProvider) port;

		if (config.endpoint.isPresent()) {
			final String endpoint = config.endpoint.get().replaceFirst(
					"\\{\\{server\\}\\}", server);

			provider.getRequestContext().put(
					BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
		}

		final Endpoint cxfEndpoint = client.getEndpoint();

		if (config.wsseUser.isPresent() && config.wssePwd.isPresent()) {
			Map<String, Object> outProps = new HashMap<String, Object>();
			outProps.put(WSHandlerConstants.ACTION,
					WSHandlerConstants.USERNAME_TOKEN);
			outProps.put(WSHandlerConstants.USER, config.wsseUser.get());
			outProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
			outProps.put(WSHandlerConstants.PW_CALLBACK_REF,
					new ClientPasswordCallback(config.wsseUser.get(),
							config.wssePwd.get()));

			WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
			cxfEndpoint.getOutInterceptors().add(wssOut);
		}

		if (config.inLogger.isPresent()) {
			cxfEndpoint.getInInterceptors().add(new LoggingInInterceptor() {

				@Override
				protected void log(java.util.logging.Logger arg0, String arg1) {
					config.inLogger.get().info(arg1);
				}
			});
		}

		if (config.outLogger.isPresent()) {
			cxfEndpoint.getInInterceptors().add(new LoggingOutInterceptor() {

				@Override
				protected void log(java.util.logging.Logger arg0, String arg1) {
					config.outLogger.get().info(arg1);
				}
			});
		}

		final HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
		httpClientPolicy.setConnectionTimeout(config.connectionTimeout);
		httpClientPolicy.setAllowChunking(false);
		httpClientPolicy.setReceiveTimeout(config.receiveTimeout);
		http.setClient(httpClientPolicy);
	}

	private Port instanciateForServer(final String server) {
		final Port port = newPort();
		configure(port, server);
		return port;
	}

	private Port electPort() {
		if (config.servers.size() == 0) {
			return instanciateForServer("");
		} else {
			int i = 0;
			while (i < config.servers.size()) {
				final Port port = instanciateForServer(config.servers.get(i++));

				try {
					checkIfPortUp(port);
					return port;
				} catch (Exception e) {
					LOGGER.warn(
							"Failed to instanciate Service [{}] : {}",
							config.endpoint + " <-- "
									+ config.servers.get(i - 1), e.getMessage());
					LOGGER.debug("Failed to instanciate Service", e);
				}
			}
		}

		return null;
	}

	public Port service() {
		return port;
	}

	private static class ClientPasswordCallback implements CallbackHandler {
		final String username;
		final String password;

		public ClientPasswordCallback(final String username,
				final String password) {
			this.username = username;
			this.password = password;
		}

		@Override
		public void handle(Callback[] callbacks) throws IOException,
				UnsupportedCallbackException {
			WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];

			if (pc.getIdentifier().equals(username)) {
				pc.setPassword(password);
			}
		}
	}

}
