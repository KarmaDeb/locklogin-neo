package com.github.karmadeb.locklogin.neo.common.config.plugin.sql;

import com.github.karmadeb.locklogin.neo.api.configuration.ConfigReader;
import com.github.karmadeb.locklogin.neo.api.configuration.ConfigWriter;
import com.github.karmadeb.locklogin.neo.api.configuration.part.ConfigPart;
import com.github.karmadeb.locklogin.neo.api.configuration.part.ConfigPath;
import com.github.karmadeb.locklogin.neo.api.configuration.schema.SchemaWriter;
import org.apache.commons.validator.routines.DomainValidator;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.jetbrains.annotations.NotNull;

public final class SQLConfiguration extends ConfigPart {

	public static final String DEFAULT_HOST = "127.0.0.1";
	public static final int DEFAULT_PORT = 3306;
	public static final String DEFAULT_DATABASE = "locklogin";
	public static final int DEFAULT_MIN_POOL_SIZE = 4;
	public static final int DEFAULT_MAX_POOL_SIZE = 10;
	public static final long DEFAULT_CONN_TIMEOUT = 30000;
	public static final long DEFAULT_IDLE_TIMEOUT = 600000;
	public static final long DEFAULT_MAX_LIFETIME = 1800000;
	public static final long DEFAULT_KEEP_ALIVE = Math.round(DEFAULT_MAX_LIFETIME / 4d);
	public static final long DEFAULT_VALIDATION = 5000;

	private ConfigReader reader;

	/**
	 * Create a new configuration part
	 */
	public SQLConfiguration() {}

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
	public ConfigPath path() {
		return ConfigPath.of("persistence");
	}

	public String host() {
		return this.reader.getString("host").orElse(DEFAULT_HOST);
	}

	public int port() {
		return this.reader.getInt("port").orElse(DEFAULT_PORT);
	}

	public String username() {
		return this.reader.getString("user").orElse("");
	}

	public String password() {
		return this.reader.getString("password").orElse("");
	}

	public int minPoolSize() {
		return this.reader.getSection("pooling.size").flatMap(
				pooling -> pooling.getInt("minimum"))
				.orElse(DEFAULT_MIN_POOL_SIZE);
	}

	public int maxPoolSize() {
		return this.reader.getSection("pooling.size").flatMap(
				pooling -> pooling.getInt("maximum"))
				.orElse(DEFAULT_MAX_POOL_SIZE);
	}

	public long connectionTimeout() {
		return this.reader.getSection("pooling.health").flatMap(
						pooling -> pooling.getLong("connection"))
				.orElse(DEFAULT_CONN_TIMEOUT);
	}

	public long idleTimeout() {
		return this.reader.getSection("pooling.health").flatMap(
						pooling -> pooling.getLong("idle"))
				.orElse(DEFAULT_IDLE_TIMEOUT);
	}

	public long lifetime() {
		return this.reader.getSection("pooling.health").flatMap(
						pooling -> pooling.getLong("lifetime"))
				.orElse(DEFAULT_MAX_LIFETIME);
	}

	public long keepAlive() {
		return this.reader.getSection("pooling.health").flatMap(
						pooling -> pooling.getLong("keep-alive"))
				.orElse(DEFAULT_KEEP_ALIVE);
	}

	public long validationTimeout() {
		return this.reader.getSection("pooling.health").flatMap(
						pooling -> pooling.getLong("validation"))
				.orElse(DEFAULT_VALIDATION);
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

		writer.schema().with("host",
				"The database host.",
				"This value is completely ignored by but might",
				"not limit to SQLite driver.",
				"",
				"Developer note: Using domains should be completely fine",
				"but it's still very recommended to provide the IP address",
				"directly of your database sever in order to prevent DNS problems",
				"at runtime, which are unfortunately more common than it should",
				"be."
		).string(config -> config.required(DEFAULT_HOST).validator(value -> {
			if (!(value instanceof String raw)) {
				return false;
			}

			InetAddressValidator addressValidator = InetAddressValidator.getInstance();
			DomainValidator domainValidator = DomainValidator.getInstance(true);

			return addressValidator.isValid(raw) || domainValidator.isValid(raw);
		})).with("port",
				"The database port.",
				"This value is completely ignored by but might",
				"not limit to SQLite driver.",
				"",
				"By [RFC](https://www.ietf.org/archive/id/draft-ietf-tcpm-rfc793bis-25.html), ",
				"the maximum port number allowed is also 2^{16} = 65536"
		).intNumber(config -> config.required(DEFAULT_PORT).validator(value -> {
			if (!(value instanceof Integer port)) {
				return false;
			}

			return port >= 1 && port <= 65535;
		})).with("user",
				"The database username.",
				"This value is completely ignored by but might",
				"not limit to SQLite driver."
		).string(config ->
				config.required("").validator(raw -> raw instanceof String str && !str.isBlank())
		).with("password",
				"The database user password.",
				"This value is completely ignored by but might",
				"not limit to SQLite driver."
		).string(config ->
				config.required("").validator(raw -> raw instanceof String str && !str.isBlank())
		).with("database",
				"The database name. For drivers such as SQLite",
				"this will generate a <database>.db file locally in your system",
				"",
				"Developer note: Using SQLite is completely fine, even for medium servers, ",
				"but you have to make sure you have enough space in disk to allocate that",
				"data."
		).string(config -> config.required(DEFAULT_DATABASE)
				.validator(raw -> raw instanceof String str && !str.isBlank())
		).with("pooling",
				"The database pooling configuration, allows to customize how",
				"HikariCP (the pooling engine LockLogin uses) allocates connections to",
				"the database.",
				"",
				"Pooling cannot be disabled (and you should not), except for SQLite drivers or",
				"those who might (by code) choose to not do pooling."
		).section((pooling, poolingValue) -> {
			SchemaWriter schema = pooling.get()
					.with("size",
							"Allows configuring the pool size.",
							"",
							"Values too high can cause the database to stop accepting",
							"further connections, and values too low can cause the pool",
							"to be useless.")
					.section((poolingSize, poolingSizeValue) -> {
						SchemaWriter poolSize = poolingSize.get()
								.with("minimum",
										"The minimum amount of connections",
										"to keep in the pool"
								).intNumber(config ->
										config.required(DEFAULT_MIN_POOL_SIZE).validator(raw ->
												raw instanceof Integer size && size >= DEFAULT_MIN_POOL_SIZE &&
												size <= reader.getInt("pooling.size.maximum").orElse(DEFAULT_MAX_POOL_SIZE))
								).with("maximum",
										"The maximum amount of connections",
										"to keep in the pool"
								).intNumber(config ->
										config.required(DEFAULT_MAX_POOL_SIZE).validator(raw ->
												raw instanceof Integer size &&
												size > reader.getInt("pooling.size.minimum").orElse(DEFAULT_MIN_POOL_SIZE))
								);

						poolingSizeValue.constant(poolSize);
					}).with("health",
							"Configure lifetime and health check parameters")
					.section((poolingHealth, poolingHealthValue) -> {
						SchemaWriter poolHealth = poolingHealth.get()
								.with("connection",
										"Pool connections timeout, in milliseconds.",
										"Minimum value is 250"
								).longNumber(config ->
										config.required(DEFAULT_CONN_TIMEOUT).validator(raw ->
												raw instanceof Long conn && conn >= 250)
								).with("idle",
										"Pool idle connections timeout, in milliseconds.",
										"Minimum value is 30000"
								).longNumber(config ->
										config.required(DEFAULT_IDLE_TIMEOUT).validator(raw ->
												raw instanceof Long idle && idle >= 30000)
								).with("lifetime",
										"Max lifetime for a connection in the pool, in",
										"milliseconds.",
										"",
										"Minimum value is double of pooling.health.idle"
								).longNumber(config ->
										config.required(DEFAULT_MAX_LIFETIME).validator(raw ->
												raw instanceof Long life &&
												life >= reader.getLong("pooling.health.idle").orElse(DEFAULT_IDLE_TIMEOUT))
								).with("keep-alive",
										"Frequency (in milliseconds) in which HikariCP makes a",
										"ping to the database (for connection) to test if the connection is",
										"still alive. A value of zero or lower disables it")
								.longNumber(config ->
										config.with(DEFAULT_KEEP_ALIVE).validator(raw ->
												raw == null || raw instanceof Long)
								).with("validation",
										"Pool connection timeout for validating a connection is",
										"still alive (in milliseconds). Minimum value of 250"
								).longNumber(config -> config.required(DEFAULT_VALIDATION).validator(raw ->
										raw instanceof Long valid && valid >= 250)
								);

						poolingHealthValue.constant(poolHealth);
					});

			poolingValue.constant(schema);
		});
	}
}
