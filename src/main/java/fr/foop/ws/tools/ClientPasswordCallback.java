package fr.foop.ws.tools;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.ws.security.WSPasswordCallback;

class ClientPasswordCallback implements CallbackHandler {
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
