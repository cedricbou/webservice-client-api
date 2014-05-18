package fr.foop.ws;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class CxfClientBuilder {

	public final String endpoint;

	public final Optional<String> wsseUser;

	public final Optional<String> wssePwd;

	public final long connectionTimeout;

	public final long receiveTimeout;

	public final ImmutableList<String> servers;

	public final Optional<Logger> inLogger;

	public final Optional<Logger> outLogger;

	public CxfClientBuilder(final String endPoint, final String... servers) {
		this(endPoint, Optional.<String> absent(), Optional.<String> absent(),
				1000, 3000, Optional.<Logger> absent(), Optional
						.<Logger> absent(), ImmutableList.copyOf(servers));
	}

	public CxfClientBuilder(final String endpoint,
			final Optional<String> username, final Optional<String> password,
			final long connectionTimeout, final long receiveTimeout,
			final Optional<Logger> inLogger, final Optional<Logger> outLogger,
			final ImmutableList<String> servers) {
		this.endpoint = endpoint;
		this.wsseUser = username;
		this.wssePwd = password;
		this.connectionTimeout = connectionTimeout;
		this.receiveTimeout = receiveTimeout;
		this.outLogger = outLogger;
		this.inLogger = inLogger;
		this.servers = ImmutableList.<String>copyOf(servers);
	}

	public CxfClientBuilder withEndpoint(final String endpoint) {
		return new CxfClientBuilder(endpoint, wsseUser, wssePwd,
				connectionTimeout, receiveTimeout, inLogger, outLogger, servers);
	}

	public CxfClientBuilder withReceiveTimeout(final long receiveTimeout) {
		return new CxfClientBuilder(endpoint, wsseUser, wssePwd,
				connectionTimeout, receiveTimeout, inLogger, outLogger, servers);
	}

	public CxfClientBuilder withConnectionTimeout(final long connectionTimeout) {
		return new CxfClientBuilder(endpoint, wsseUser, wssePwd,
				connectionTimeout, receiveTimeout, inLogger, outLogger, servers);
	}

	public CxfClientBuilder withCredentials(final String username,
			final String password) {
		return new CxfClientBuilder(endpoint, Optional.fromNullable(username),
				Optional.fromNullable(password), connectionTimeout,
				receiveTimeout, inLogger, outLogger, servers);
	}

	public CxfClientBuilder withInLogger(final Logger logger) {
		return new CxfClientBuilder(endpoint, wsseUser, wssePwd,
				connectionTimeout, receiveTimeout, Optional.fromNullable(logger), outLogger, servers);
	}

	public CxfClientBuilder withOutLogger(final Logger logger) {
		return new CxfClientBuilder(endpoint, wsseUser, wssePwd,
				connectionTimeout, receiveTimeout, inLogger, Optional.fromNullable(logger), servers);
	}

	public CxfClientBuilder withInOutLogger(final Logger logger) {
		return new CxfClientBuilder(endpoint, wsseUser, wssePwd,
				connectionTimeout, receiveTimeout, Optional.fromNullable(logger), Optional.fromNullable(logger), servers);
	}

	public CxfClientBuilder withServers(final String... servers) {
		return new CxfClientBuilder(endpoint, wsseUser, wssePwd,
				connectionTimeout, receiveTimeout, inLogger, outLogger, ImmutableList.copyOf(servers));
	}

	public <Port, Client extends CxfClient<Port>> Client build(
			Class<Client> clazz) {
		try {
			return clazz.getConstructor(CxfClientBuilder.class).newInstance(
					this);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException(
					"failed to build CxfClient with class : "
							+ clazz.toString());
		}
	}

}