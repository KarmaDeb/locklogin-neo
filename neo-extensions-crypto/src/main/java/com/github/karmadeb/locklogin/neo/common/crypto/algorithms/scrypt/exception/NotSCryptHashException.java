package com.github.karmadeb.locklogin.neo.common.crypto.algorithms.scrypt.exception;

import com.github.karmadeb.locklogin.neo.api.security.hash.HashProperties;
import org.jetbrains.annotations.Contract;

import java.util.Objects;

/**
 * This exception is thrown when an operation which requires a scrypt's
 * {@link HashProperties properties} does not receive a scrypt's hash properties.
 */
public final class NotSCryptHashException extends RuntimeException {

	/**
	 * Initialize the exception
	 * @param message the exception message
	 */
	@Contract("null -> fail")
	public NotSCryptHashException(final String message) {
		super(Objects.requireNonNull(message));
	}
}
