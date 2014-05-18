package fr.foop.ws;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
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

public abstract class CxfClient<Port, ServiceManager> {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CxfClient.class);

	private final CxfClientBuilder config;
	private final Port port;
	private final Class<ServiceManager> smClazz;
	private final String endpoint;

	protected CxfClient(final CxfClientBuilder config,
			final Class<ServiceManager> smClazz) {
		this.config = config;
		this.smClazz = smClazz;
		this.endpoint = detectEndpoint();
		this.port = electPort();
	}

	protected CxfClientBuilder config() {
		return config;
	}

	public abstract void checkIfPortUp(final Port port) throws Exception;

	public abstract Port newPort(final ServiceManager serviceManager);

	private String detectEndpoint() {
		if (config.endpoint.isPresent()) {
			return config.endpoint.get();
		} else {
			return ClientProxy.getClient(newPort(newDefaultServiceManager()))
					.getEndpoint().getEndpointInfo().getAddress();
		}
	}

	private ServiceManager newNoEndpointServiceManager() {
		try {
			return smClazz.getConstructor(URL.class).newInstance((URL) null);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(
					"failed to instanciate new service manager", e);
		}
	}

	private ServiceManager newDefaultServiceManager() {
		try {
			return smClazz.newInstance();
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | SecurityException e) {
			throw new RuntimeException(
					"failed to instanciate new service manager", e);
		}
	}

	/**
	 * Configure the given Web Service Port according the CxfClientBuilder configuration.
	 * It configures Port endpoint (and replace the server name in the endpoint),
	 * WSSE if any, times out, loggers if required, ...
	 * @param port
	 * @param server
	 */
	private void configure(final Port port, final String server) {
		final Client client = ClientProxy.getClient(port);
		HTTPConduit http = (HTTPConduit) client.getConduit();

		BindingProvider provider = (BindingProvider) port;

		final String endpoint = this.endpoint.replaceFirst(
				"\\{\\{server\\}\\}", server);

		provider.getRequestContext().put(
				BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);

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

	/**
	 * Instanciate a configured web service port the provided server.
	 * @param server
	 * @return
	 */
	private Port instanciateForServer(final String server) {
		final Port port = newPort(newNoEndpointServiceManager());
		configure(port, server);
		return port;
	}

	/**
	 * Try to find a working Web Service Port for the given server list.
	 * @return
	 */
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
