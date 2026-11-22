package com.github.karmadeb.locklogin.neo.api.essentials;

import java.math.BigInteger;
import java.util.function.Predicate;

/**
 * LockLogin essentials feature management. Please note features
 * can only be disabled, as enabling one would usually require a
 * fresh server startup.
 */
public final class EssentialFeatures implements Predicate<Integer> {

	public static final int FEATURE_JOOQ = 1 << 1;
	public static final int FEATURE_CRYPTO = 1 << 2;
	private static BigInteger FEATURE_SET = BigInteger.ZERO.setBit(FEATURE_JOOQ).setBit(FEATURE_CRYPTO);

	private EssentialFeatures() {
		throw new UnsupportedOperationException("Cannot instantiate features manager");
	}

	/**
	 * Disable a feature
	 * @param feature the feature to turn off
	 */
	public static void disable(final int feature) {
		FEATURE_SET = FEATURE_SET.clearBit(feature);
	}

	/**
	 * Evaluates this predicate on the given argument.
	 *
	 * @param integer the input argument
	 * @return {@code true} if the input argument matches the predicate,
	 * otherwise {@code false}
	 */
	@Override
	public boolean test(final Integer integer) {
		if (integer == null) {
			return false;
		}

		return FEATURE_SET.testBit(integer);
	}
}
