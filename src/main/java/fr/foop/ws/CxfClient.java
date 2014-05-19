package fr.foop.ws;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.apache.cxf.frontend.ClientProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.foop.ws.tools.WebServicePortConfigurer;

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
	 * Instanciate a configured web service port the provided server.
	 * @param server
	 * @return
	 */
	private Port instanciateForServer(final String server) {
		final Port port = newPort(newNoEndpointServiceManager());
		final WebServicePortConfigurer<Port> configurer = new WebServicePortConfigurer<Port>(port);
		
		configurer.configureEndpoint(endpoint, server);
		
		if(config.inLogger.isPresent()) {
			configurer.configureInLogger(config.inLogger.get());
		}

		if(config.outLogger.isPresent()) {
			configurer.configureOutLogger(config.outLogger.get());
		}
		
		if(config.wsseUser.isPresent() && config.wssePwd.isPresent()) {
			configurer.configureWsse(config.wsseUser.get(), config.wssePwd.get());
		}
		
		configurer.configureTimeouts(config.connectionTimeout, config.receiveTimeout);

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

}
