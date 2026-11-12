package com.github.karmadeb.locklogin.neo.common.security.methods.argon;

import com.github.karmadeb.locklogin.neo.api.configuration.part.ConfigPart;
import com.github.karmadeb.locklogin.neo.api.configuration.ConfigReader;
import com.github.karmadeb.locklogin.neo.api.configuration.ConfigWriter;
import com.github.karmadeb.locklogin.neo.api.configuration.part.ConfigPath;
import com.github.karmadeb.locklogin.neo.common.security.methods.argon.Argon2Hash.Parameter;
import com.github.karmadeb.locklogin.neo.common.security.methods.argon.Argon2Hash.Version;
import org.jetbrains.annotations.NotNull;

public final class Argon2Configuration extends ConfigPart {

	Argon2Configuration(final @NotNull ConfigReader reader) {
		super(reader);
	}

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
		return ConfigPath.of("hash", "argon");
	}

	public Parameter parameter() {
		String value = this.reader.getString("parameter").orElse("argon2id")
				.toLowerCase();
		if (value.equalsIgnoreCase("argon2i")) {
			return Parameter.ARGON2_I;
		} else if (value.equalsIgnoreCase("argon2d")) {
			return Parameter.ARGON2_D;
		}

		return Parameter.ARGON2_ID;
	}

	public Argon2Hash.Version version() {
		int value = this.reader.getInt("version").orElse(Version.VERSION_13.version());
		return Version.get(value);
	}

	public int iterations() {
		return this.reader.getInt("iterations").orElse(Argon2Hash.DEFAULT_ITERATIONS);
	}

	public int memory() {
		return this.reader.getInt("memory").orElse(Argon2Hash.DEFAULT_MEMORY);
	}

	public int parallelism() {
		return this.reader.getInt("parallelism").orElse(Argon2Hash.DEFAULT_PARALLELISM);
	}

	@Override
	public void init(final @NotNull ConfigWriter writer) {
		writer.schema()
				.with("parameter",
						"Argon2 parameter spec. Valid options are:",
						"- argon2i",
						"- argon2d",
						"- argon2id"
				).string(value -> value.required("argon2id")
						.validator(obj -> {
							String raw = String.valueOf(obj);
							return raw.equalsIgnoreCase("argon2i") ||
									raw.equalsIgnoreCase("argon2d") ||
									raw.equalsIgnoreCase("argon2id");
						})
				).with("version",
						"Argon2 version. Valid options are:",
						"- 10",
						"- 13")
				.intNumber(value -> value.required(13)
						.validator(obj -> {
							if (!(obj instanceof Number n)) return false;
							int val = n.intValue();

							return val == 10 || val == 13;
						}))
				.with("iterations",
						"Argon2 algorithm iteration count. Must be a non-zero positive int")
				.intNumber(value -> value.required(Argon2Hash.DEFAULT_ITERATIONS)
						.validator(obj -> {
							if (!(obj instanceof Number n)) return false;
							int val = n.intValue();

							return val >= Argon2Hash.DEFAULT_ITERATIONS;
						}))
				.with("memory",
						"Argon2 algorithm memory cost in a power of 2 (2^16KiB = 64 MiB). Must be a non-zero positive int")
				.intNumber(value -> value.required(Argon2Hash.DEFAULT_MEMORY)
						.validator(obj -> {
							if (!(obj instanceof Number n)) return false;
							int val = n.intValue();

							return val >= Argon2Hash.DEFAULT_MEMORY;
						}))
				.with("parallelism",
						"Argon2 algorithm parallelism. Must be a non-zero positive int")
				.intNumber(value -> value.required(Argon2Hash.DEFAULT_PARALLELISM)
						.validator(obj -> {
							if (!(obj instanceof Number n)) return false;
							int val = n.intValue();

							return val >= Argon2Hash.DEFAULT_PARALLELISM;
						}))
				.build();
	}
}
