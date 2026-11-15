package com.github.karmadeb.locklogin.neo.api.persistence.exception;

import com.github.karmadeb.locklogin.neo.api.persistence.driver.PersistenceDriver;
import org.jetbrains.annotations.Contract;

import java.util.Objects;

/**
 * The unsupported driver exception is thrown when a
 * migration between two drivers fails because none of the
 * supports each others protocol
 */
public class UnsupportedDriverException extends RuntimeException {

	/**
	 * Create a new exception
	 * @param driver the driver
	 */
	@Contract("null -> fail")
	public UnsupportedDriverException(final PersistenceDriver<?, ?> driver) {
		Objects.requireNonNull(driver);
		super(String.format("Failed to execute migrate operation for driver %s (%s)",
				driver.name(), driver.provider().getCanonicalName()));
	}
}
