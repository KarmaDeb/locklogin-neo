package com.github.karmadeb.locklogin.neo.common.crypto.algorithms.argon.exception;

import com.github.karmadeb.locklogin.neo.api.security.hash.HashProperties;
import org.jetbrains.annotations.Contract;

import java.util.Objects;

/**
 * This exception is thrown when an operation which requires an argon's
 * {@link HashProperties properties} does not receive an argon's hash properties.
 */
public final class NotArgonHashException extends RuntimeException {

	/**
	 * Initialize the exception
	 * @param message the exception message
	 */
	@Contract("null -> fail")
	public NotArgonHashException(final String message) {
		super(Objects.requireNonNull(message));
	}
}
