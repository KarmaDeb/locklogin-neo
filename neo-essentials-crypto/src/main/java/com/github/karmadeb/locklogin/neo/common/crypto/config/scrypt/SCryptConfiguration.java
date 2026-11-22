package com.github.karmadeb.locklogin.neo.common.crypto.config.scrypt;

import com.github.karmadeb.locklogin.neo.api.configuration.ConfigReader;
import com.github.karmadeb.locklogin.neo.api.configuration.ConfigWriter;
import com.github.karmadeb.locklogin.neo.api.configuration.part.ConfigPart;
import com.github.karmadeb.locklogin.neo.api.configuration.part.ConfigPath;
import org.jetbrains.annotations.NotNull;

public class SCryptConfiguration extends ConfigPart {

	public static final int DEFAULT_COST = 1 << 17;
	public static final int DEFAULT_BLOCK_SIZE = 8;
	public static final int DEFAULT_PARALLELISM = 1;
	public static final int MAX_POWER_OF_TWO = 1024;

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
		return ConfigPath.of("hash", "scrypt");
	}

	public int cost() {
		return this.reader.getInt("cost").orElse(DEFAULT_COST);
	}

	public int block() {
		return this.reader.getInt("block").orElse(DEFAULT_BLOCK_SIZE);
	}

    public int parallelism() {
		return this.reader.getInt("parallelism").orElse(DEFAULT_PARALLELISM);
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
				.with("cost", "SCrypt parameter cost")
				.intNumber(value -> value
						.required(DEFAULT_COST)
						.validator(SCryptConfiguration::isValidCost))
				.with("block", "SCrypt block size")
				.intNumber(value -> value
						.required(DEFAULT_BLOCK_SIZE)
						.validator(SCryptConfiguration::isValidPositiveInt))
				.with("parallelism", "SCrypt parallelism")
				.intNumber(value -> value
						.required(DEFAULT_PARALLELISM)
						.validator(SCryptConfiguration::isValidPositiveInt))
				.build();
	}

	public static boolean isValidCost(final Object obj) {
		Integer cost = extractStrictInt(obj);
		if (cost == null || cost <= 1) {
			return false;
		}

		return (cost & (cost - 1)) == 0 && cost <= (1 << 24);
	}

	public static boolean isValidPositiveInt(final Object obj) {
		Integer val = extractStrictInt(obj);
		return val != null && val >= 1 && val <= SCryptConfiguration.MAX_POWER_OF_TWO;
	}

	private static Integer extractStrictInt(final Object obj) {
		if (obj instanceof Integer i) {
			return i;
		}

		if (obj instanceof Number n) {
			long asLong = n.longValue();
			double asDouble = n.doubleValue();
			if (asLong == asDouble && asLong >= Integer.MIN_VALUE && asLong <= Integer.MAX_VALUE) {
				return (int) asLong;
			}
		}
		return null;
	}
}
