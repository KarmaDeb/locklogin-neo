package com.github.karmadeb.locklogin.neo.common.crypto.algorithms.bcrypt.exception;

import com.github.karmadeb.locklogin.neo.api.security.hash.HashProperties;
import org.jetbrains.annotations.Contract;

import java.util.Objects;

/**
 * This exception is thrown when an operation which requires a bcrypt's
 * {@link HashProperties properties} does not receive a bcrypt's hash properties.
 */
public final class NotBCryptHashException extends RuntimeException {

	/**
	 * Initialize the exception
	 * @param message the exception message
	 */
	@Contract("null -> fail")
	public NotBCryptHashException(final String message) {
		super(Objects.requireNonNull(message));
	}
}
