package com.github.karmadeb.locklogin.neo.common.security.methods.bcrypt;

import com.github.karmadeb.locklogin.neo.api.configuration.part.ConfigPart;
import com.github.karmadeb.locklogin.neo.api.configuration.ConfigReader;
import com.github.karmadeb.locklogin.neo.api.configuration.ConfigWriter;
import com.github.karmadeb.locklogin.neo.api.configuration.part.ConfigPath;
import org.jetbrains.annotations.NotNull;

public class BCryptConfiguration extends ConfigPart {

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
	public ConfigPath path() {
		return ConfigPath.of("hash", "bcrypt");
	}

	public int cost() {
		return this.reader.getInt("cost").orElse(BcryptHash.DEFAULT_COST);
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

		writer.schema().with("cost", "Bcrypt parameter cost")
				.intNumber(value -> value.required(BcryptHash.DEFAULT_COST)
						.validator(obj -> obj instanceof Number n && n.intValue() >= BcryptHash.DEFAULT_COST))
				.build();
	}
}
