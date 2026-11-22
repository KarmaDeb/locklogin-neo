package com.github.karmadeb.locklogin.neo.common.crypto.config.bcrypt;

import com.github.karmadeb.locklogin.neo.api.configuration.part.ConfigPart;
import com.github.karmadeb.locklogin.neo.api.configuration.ConfigReader;
import com.github.karmadeb.locklogin.neo.api.configuration.ConfigWriter;
import com.github.karmadeb.locklogin.neo.api.configuration.part.ConfigPath;
import org.jetbrains.annotations.NotNull;


public class BCryptConfiguration extends ConfigPart {

	public static final int DEFAULT_COST = 12;
	public static final int MIN_BCRYPT_COST = 4;
	public static final int MAX_BCRYPT_COST = 31;

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
		return ConfigPath.of("hash", "bcrypt");
	}

	public int cost() {
		return this.reader.getInt("cost").orElse(DEFAULT_COST);
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

		writer.schema()
				.with("cost", "Bcrypt parameter cost")
				.intNumber(value -> value
						.required(DEFAULT_COST)
						.validator(BCryptConfiguration::isValidBcryptCost))
				.build();
	}

	public static boolean isValidBcryptCost(final Object obj) {
		if (obj instanceof Integer cost) {
			return cost >= MIN_BCRYPT_COST && cost <= MAX_BCRYPT_COST;
		}
		if (obj instanceof Number n) {
			long asLong = n.longValue();
			double asDouble = n.doubleValue();
			return asLong == asDouble && asLong >= MIN_BCRYPT_COST && asLong <= MAX_BCRYPT_COST;
		}
		return false;
	}
}
