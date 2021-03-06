package fr.foop.ws;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import fr.foop.ws.tools.configurators.CxfClientBuilderConfigurator;
import fr.foop.ws.tools.configurators.PropertyMeta;

public class CxfClientBuilder {

	public final Optional<String> endpoint;

	public final Optional<String> wsseUser;

	public final Optional<String> wssePwd;

	public final long connectionTimeout;

	public final long receiveTimeout;

	public final ImmutableList<String> servers;

	public final Optional<Logger> inLogger;

	public final Optional<Logger> outLogger;
	
	public final Optional<Object> mockedPort;
	
	public final boolean useMock;

	private final static String[] propNames = new String[] { "endpoint",
			"wsseUser", "wssePwd", "connectionTimeout", "receiveTimeout",
			"inLogger", "outLogger", "logger", "mockedPort", "useMock", "servers" };
	
	private final static ImmutableMap<String, PropertyMeta> propMetas = ImmutableMap
			.<String, PropertyMeta> builder()
			.put("endpoint",
					new PropertyMeta(
							"The web service endpoint, can contain the variable {{server}}, in this case it would be replaced by one of the server defined in servers.",
							CxfClientBuilderConfigurator.ENDPOINT_CONFIGURATOR))
			.put("wsseUser",
					new PropertyMeta(
							"The user to pass as WSSE user credential", CxfClientBuilderConfigurator.WSSE_USER_CONFIGURATOR))
			.put("wssePwd",
					new PropertyMeta(
							"The password to pass as the WSSE password credential (only PasswordText supported for now)",
							CxfClientBuilderConfigurator.WSSE_PWD_CONFIGURATOR))
			.put("connectionTimeout",
					new PropertyMeta(
							"the time in millis before timeout during connection",
							CxfClientBuilderConfigurator.CONNECTION_TIMEOUT_CONFIGURATOR))
			.put("receiveTimeout",
					new PropertyMeta(
							"The time in millis to wait before timeout while not receiving data",
							CxfClientBuilderConfigurator.RECEIVE_TIMEOUT_CONFIGURATOR))
			.put("inLogger",
					new PropertyMeta(
							"The logger name to use for client request sent to the server (seen from server side)",
							CxfClientBuilderConfigurator.INLOGGER_CONFIGURATOR))
			.put("outLogger",
					new PropertyMeta(
							"The logger name to log server response sent to the client (seen from server side)",
							CxfClientBuilderConfigurator.OUTLOGGER_CONFIGURATOR))
			.put("logger",
					new PropertyMeta(
							"The logger name to use for both in/out logger. It will override in/out logger settings",
							CxfClientBuilderConfigurator.LOGGER_CONFIGURATOR))
			.put("mockedPort",
					new PropertyMeta(
							"The mocked object to use as port, it must implements the CXF generated port interface",
							CxfClientBuilderConfigurator.MOCKED_PORT_CONFIGURATOR))
			.put("useMock",
					new PropertyMeta(
							"A flag to enable or disable the use of the mocked port object",
							CxfClientBuilderConfigurator.USE_MOCK_CONFIGURATOR))
			.put("servers",
					new PropertyMeta(
							"Comma separated list of server to use in the endpoint, they will be tried turn by turn at service initialisation",
							CxfClientBuilderConfigurator.SERVERS_CONFIGURATOR))
			.build();

	public CxfClientBuilder() {
		this(Optional.<String> absent(), Optional.<String> absent(), Optional
				.<String> absent(), 1000, 3000, Optional.<Logger> absent(),
				Optional.<Logger> absent(), Optional.<Object> absent(), false, ImmutableList.<String> of());
	}

	public CxfClientBuilder(final String endPoint, final String... servers) {
		this(Optional.of(endPoint), Optional.<String> absent(), Optional
				.<String> absent(), 1000, 3000, Optional.<Logger> absent(),
				Optional.<Logger> absent(),  Optional.<Object> absent(), false, ImmutableList.copyOf(servers));
	}

	public CxfClientBuilder(final Optional<String> endpoint,
			final Optional<String> username, final Optional<String> password,
			final long connectionTimeout, final long receiveTimeout,
			final Optional<Logger> inLogger, final Optional<Logger> outLogger,
			final Optional<Object> mockedPort,
			final boolean useMock,
			final ImmutableList<String> servers) {
		this.endpoint = endpoint;
		this.wsseUser = username;
		this.wssePwd = password;
		this.connectionTimeout = connectionTimeout;
		this.receiveTimeout = receiveTimeout;
		this.outLogger = outLogger;
		this.inLogger = inLogger;
		this.mockedPort = mockedPort;
		this.useMock = useMock;
		this.servers = ImmutableList.<String> copyOf(servers);
	}

	public CxfClientBuilder withEndpoint(final String endpoint) {
		return new CxfClientBuilder(Optional.of(endpoint), wsseUser, wssePwd,
				connectionTimeout, receiveTimeout, inLogger, outLogger, mockedPort, useMock, servers);
	}

	public CxfClientBuilder withReceiveTimeout(final long receiveTimeout) {
		return new CxfClientBuilder(endpoint, wsseUser, wssePwd,
				connectionTimeout, receiveTimeout, inLogger, outLogger, mockedPort, useMock, servers);
	}

	public CxfClientBuilder withConnectionTimeout(final long connectionTimeout) {
		return new CxfClientBuilder(endpoint, wsseUser, wssePwd,
				connectionTimeout, receiveTimeout, inLogger, outLogger, mockedPort, useMock, servers);
	}

	public CxfClientBuilder withWsseUser(final String username) {
		return new CxfClientBuilder(endpoint, Optional.fromNullable(username),
				wssePwd, connectionTimeout, receiveTimeout, inLogger,
				outLogger, mockedPort, useMock, servers);
	}

	public CxfClientBuilder withWssePwd(final String password) {
		return new CxfClientBuilder(endpoint, wsseUser,
				Optional.fromNullable(password), connectionTimeout,
				receiveTimeout, inLogger, outLogger, mockedPort, useMock, servers);
	}

	public CxfClientBuilder withWsseCredentials(final String username,
			final String password) {
		return this.withWsseUser(username).withWssePwd(password);
	}

	public CxfClientBuilder withInLogger(final Logger logger) {
		return new CxfClientBuilder(endpoint, wsseUser, wssePwd,
				connectionTimeout, receiveTimeout,
				Optional.fromNullable(logger), outLogger, mockedPort, useMock, servers);
	}

	public CxfClientBuilder withInLogger(final String loggerName) {
		return new CxfClientBuilder(endpoint, wsseUser, wssePwd,
				connectionTimeout, receiveTimeout, Optional.of(LoggerFactory
						.getLogger(loggerName)), outLogger, mockedPort, useMock, servers);
	}

	public CxfClientBuilder withOutLogger(final Logger logger) {
		return new CxfClientBuilder(endpoint, wsseUser, wssePwd,
				connectionTimeout, receiveTimeout, inLogger,
				Optional.fromNullable(logger), mockedPort, useMock, servers);
	}

	public CxfClientBuilder withOutLogger(final String loggerName) {
		return new CxfClientBuilder(endpoint, wsseUser, wssePwd,
				connectionTimeout, receiveTimeout, inLogger,
				Optional.of(LoggerFactory.getLogger(loggerName)), mockedPort, useMock, servers);
	}

	public CxfClientBuilder withLogger(final Logger logger) {
		return this.withInLogger(logger).withOutLogger(logger);
	}

	public CxfClientBuilder withLogger(final String loggerName) {
		return this.withInLogger(loggerName).withOutLogger(loggerName);
	}

	public CxfClientBuilder withInOutLogger(final Logger logger) {
		return new CxfClientBuilder(endpoint, wsseUser, wssePwd,
				connectionTimeout, receiveTimeout,
				Optional.fromNullable(logger), Optional.fromNullable(logger),
				mockedPort, useMock, servers);
	}

	public CxfClientBuilder withServers(final String... servers) {
		return new CxfClientBuilder(endpoint, wsseUser, wssePwd,
				connectionTimeout, receiveTimeout, inLogger, outLogger, mockedPort, useMock,
				ImmutableList.copyOf(servers));
	}

	public CxfClientBuilder withProperties(final String rootKey,
			final Properties props) {
		CxfClientBuilder configured = this;

		final boolean rootKeyEndsWithDot = rootKey.endsWith(".");
		
		for (final String property : propNames) {
			final String rootedProperty = Joiner.on(rootKeyEndsWithDot?"":".").join(rootKey, property);
			configured = Optional
					.fromNullable(propMetas.get(property).configurator)
					.or(CxfClientBuilderConfigurator.NOOP_CONFIGURATOR)
					.configure(configured,
							Optional.fromNullable((String) props.get(rootedProperty)));
		}

		return configured;
	}
	
	public <Port> CxfClientBuilder withMockedPort(final Port port) {
		return new CxfClientBuilder(endpoint, wsseUser, wssePwd,
				connectionTimeout, receiveTimeout, inLogger, outLogger, Optional.<Object>of((Object)port), useMock,
				servers);
	}
	
	public <Port> CxfClientBuilder withMockedPort(final Class<Port> portClass) {
		try {
			return withMockedPort(portClass.newInstance());
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("unable to instianciate mocked port class : " + portClass.getName(), e);
		}
	}
	
	public CxfClientBuilder enableMocking() {
		return new CxfClientBuilder(endpoint, wsseUser, wssePwd,
				connectionTimeout, receiveTimeout, inLogger, outLogger, mockedPort, true,
				servers);
	}

	public CxfClientBuilder disableMocking() {
		return new CxfClientBuilder(endpoint, wsseUser, wssePwd,
				connectionTimeout, receiveTimeout, inLogger, outLogger, mockedPort, false,
				servers);
	}

	public CxfClientBuilder withMockedPort(final String portClassName) {
		try {
			return withMockedPort(Class.forName(portClassName));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("unable to find class for named mocked port : " + portClassName, e);
		}
	}

	public <Port, Client extends CxfClient<Port, ?>> Client build(
			Class<Client> clazz) {
		try {
			return clazz.getConstructor(CxfClientBuilder.class).newInstance(
					this);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException(
					"failed to build CxfClient with class : "
							+ clazz.toString(), e);
		}
	}

}