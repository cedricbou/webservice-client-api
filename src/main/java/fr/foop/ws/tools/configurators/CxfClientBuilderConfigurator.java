package fr.foop.ws.tools.configurators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import fr.foop.ws.CxfClientBuilder;

public interface CxfClientBuilderConfigurator {

	public final static Logger LOGGER = LoggerFactory.getLogger(CxfClientBuilderConfigurator.class);
	
	public static abstract class BasicBuilderConfigurator implements
			CxfClientBuilderConfigurator {
		@Override
		public CxfClientBuilder configure(CxfClientBuilder configured,
				Optional<String> propValue) {
			LOGGER.debug("autoconfigure : {} -> {}", this.getClass().getName(), propValue.toString());
			
			if (propValue.isPresent()) {
				return ensurePresentConfigured(configured, propValue.get());
			}
			
			return configured;
		}

		protected abstract CxfClientBuilder ensurePresentConfigured(
				final CxfClientBuilder configured, final String propValue);
	}

	public final static CxfClientBuilderConfigurator NOOP_CONFIGURATOR = new CxfClientBuilderConfigurator() {

		@Override
		public CxfClientBuilder configure(CxfClientBuilder configured,
				Optional<String> propValue) {
			return configured;
		}
	};

	public final static CxfClientBuilderConfigurator ENDPOINT_CONFIGURATOR = new BasicBuilderConfigurator() {
		@Override
		protected CxfClientBuilder ensurePresentConfigured(
				CxfClientBuilder configured, String propValue) {
			return configured.withEndpoint(propValue);
		}
	};

	public final static CxfClientBuilderConfigurator INLOGGER_CONFIGURATOR = new BasicBuilderConfigurator() {
		@Override
		protected CxfClientBuilder ensurePresentConfigured(
				CxfClientBuilder configured, String propValue) {
			return configured.withInLogger(propValue);
		}
	};

	public final static CxfClientBuilderConfigurator OUTLOGGER_CONFIGURATOR = new BasicBuilderConfigurator() {
		@Override
		protected CxfClientBuilder ensurePresentConfigured(
				CxfClientBuilder configured, String propValue) {
			return configured.withOutLogger(propValue);
		}
	};

	public final static CxfClientBuilderConfigurator LOGGER_CONFIGURATOR = new BasicBuilderConfigurator() {
		@Override
		protected CxfClientBuilder ensurePresentConfigured(
				CxfClientBuilder configured, String propValue) {
			return configured.withLogger(propValue);
		}
	};

	public final static CxfClientBuilderConfigurator WSSE_USER_CONFIGURATOR = new BasicBuilderConfigurator() {
		@Override
		protected CxfClientBuilder ensurePresentConfigured(
				CxfClientBuilder configured, String propValue) {
			return configured.withWsseUser(propValue);
		}
	};

	public final static CxfClientBuilderConfigurator WSSE_PWD_CONFIGURATOR = new BasicBuilderConfigurator() {
		@Override
		protected CxfClientBuilder ensurePresentConfigured(
				CxfClientBuilder configured, String propValue) {
			return configured.withWssePwd(propValue);
		}
	};

	public final static CxfClientBuilderConfigurator CONNECTION_TIMEOUT_CONFIGURATOR = new BasicBuilderConfigurator() {
		@Override
		protected CxfClientBuilder ensurePresentConfigured(
				CxfClientBuilder configured, String propValue) {
			return configured.withConnectionTimeout(Long.valueOf(propValue));
		}
	};

	public final static CxfClientBuilderConfigurator RECEIVE_TIMEOUT_CONFIGURATOR = new BasicBuilderConfigurator() {
		@Override
		protected CxfClientBuilder ensurePresentConfigured(
				CxfClientBuilder configured, String propValue) {
			return configured.withReceiveTimeout(Long.valueOf(propValue));
		}
	};

	public final static CxfClientBuilderConfigurator SERVERS_CONFIGURATOR = new BasicBuilderConfigurator() {
		@Override
		protected CxfClientBuilder ensurePresentConfigured(
				CxfClientBuilder configured, String propValue) {
			return configured.withServers(Iterables.toArray(Splitter.on(",")
					.trimResults().omitEmptyStrings().split(propValue),
					String.class));
		}
	};

	public CxfClientBuilder configure(final CxfClientBuilder configured,
			final Optional<String> propValue);
}
