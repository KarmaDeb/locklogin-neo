package com.github.karmadeb.locklogin.neo.api;

import java.net.URL;
import java.nio.file.Path;
import java.security.SecureRandom;

/**
 * The constants class is a utility class
 * used to provide context data from the API
 */
public class Constants {

	public static final String LOCKLOGIN_EXTENSION_ID = "locklogin";
	public static final String HASH_PROP_EXT_ID = "lei";
	public static final String HASH_PROP_HASH_ID = "lhi";

	public static final String RESOURCE_HASH = "hashes";
	public static final String RESOURCE_AUTH_LAYER = "auth-layers";

	public static final SecureRandom RANDOM = new SecureRandom();
	public static final Path WORKING_DIRECTORY;

	static {
		URL location = Constants.class.getProtectionDomain().getCodeSource().getLocation();
		try {
			WORKING_DIRECTORY = Path.of(location.toURI()).resolve("LockLogin");
		} catch (Throwable ex) {
			throw new RuntimeException(ex);
		}
	}

	private Constants() {
		throw new UnsupportedOperationException("Cannot initialize utility class \"Constants\"");
	}
}
