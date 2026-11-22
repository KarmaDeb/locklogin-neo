package com.github.karmadeb.locklogin.neo.common.auth.config;

import com.github.karmadeb.locklogin.neo.api.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.configuration.ConfigReader;
import com.github.karmadeb.locklogin.neo.api.configuration.ConfigWriter;
import com.github.karmadeb.locklogin.neo.api.configuration.part.ConfigPart;
import com.github.karmadeb.locklogin.neo.api.configuration.part.ConfigPath;
import com.github.karmadeb.locklogin.neo.api.configuration.schema.SchemaWriter;
import com.github.karmadeb.locklogin.neo.common.crypto.algorithms.argon.Argon2HashMethod;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class RegistrationConfig extends ConfigPart {

	public static final String DEFAULT_ALGORITHM = Argon2HashMethod.ID.resource();
	public static final boolean DEFAULT_REGISTER_CONFIRM = true;
	public static final int DEFAULT_REGISTER_TIMEOUT = 120;
	public static final int DEFAULT_LOGIN_TIMEOUT = 60;

	private ConfigReader reader;

	/**
	 * Get the part path. This is used internally
	 * when generating the {@link ConfigWriter}. Consider
	 * it the "scope". Using {@link ConfigPath#empty()} will write
	 * in the ROOT of the document, and using an existing
	 * section key will allow to overwrite its contents
	 *
	 * @return the config part
	 */
	@Override
	public @NotNull ConfigPath path() {
		return ConfigPath.of("authentication");
	}

	public NamespacedIdentity algorithm() {
		String algorithm = this.reader.getString("algorithm").orElse(DEFAULT_ALGORITHM).toLowerCase();
		if (!algorithm.contains(":")) {
			return NamespacedIdentity.locklogin(algorithm);
		}

		return NamespacedIdentity.parse(algorithm);
	}

	public Optional<Integer> registerTimeout() {
		int timeout = this.reader.getInt("registration.timeout").orElse(DEFAULT_REGISTER_TIMEOUT);
		if (timeout <= 0) {
			return Optional.empty();
		}

		return Optional.of(timeout);
	}

	public boolean confirmPassword() {
		return this.reader.getBool("registration.confirm").orElse(DEFAULT_REGISTER_CONFIRM);
	}

	public Optional<Integer> loginTimeout() {
		int timeout = this.reader.getInt("login").orElse(DEFAULT_LOGIN_TIMEOUT);
		if (timeout <= 0) {
			return Optional.empty();
		}

		return Optional.of(timeout);
	}

	/**
	 * Initialize the configuration
	 *
	 * @param reader the reader
	 * @param writer the writer
	 */
	@Override
	public void init(final @NotNull ConfigReader reader, final @NotNull ConfigWriter writer) {
		this.reader = reader;

		writer.schema().with("algorithm",
				"The password hashing algorithm. The default is argon2 (provided by",
				"locklogin essentials). But this can be replaced by any other provided through",
				"the locklogin API"
		).string(
				config -> config.required(DEFAULT_ALGORITHM)
						.validator(raw -> raw instanceof String str &&
								str.toLowerCase().matches("^[a-z-_0-9]+(:[a-z-_0-9]+)?$")
						)
		).with("registration",
				"Registration process configuration"
		).section((gen, value) -> {
			SchemaWriter schema = gen.get().with("timeout",
					"The amount of time (in seconds) the client has to create a new",
					"account. Usually the amount of time is longer than login. A value of zero or",
					"lower disables timeout and lets the client take as much time as he needs to",
					"register"
			).intNumber(config -> config.required(DEFAULT_REGISTER_TIMEOUT)
			).with("confirm",
					"Specifies if during registration, the client has to type his password",
					"twice for the operation to complete. This is recommended to avoid mistyping the",
					"password at once"
			).bool(config -> config.required(DEFAULT_REGISTER_CONFIRM));

			value.required(schema);
		}).with("login",
				"The amount of time (in seconds) the client has to login into his",
				"existing account. Usually the amount of time is less than register. A value of zero",
				"or lower disables timeout and lets the client take as much time as he needs to",
				"sign in"
		).intNumber(
				config -> config.required(DEFAULT_LOGIN_TIMEOUT)
		).build();
	}
}
