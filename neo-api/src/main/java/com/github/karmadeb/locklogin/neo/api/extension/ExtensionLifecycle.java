package com.github.karmadeb.locklogin.neo.api.extension;

/**
 * LockLogin extension lifecycle
 */
public enum ExtensionLifecycle {
	/**
	 * Lifecycle origin by plugin loading
	 */
	PLUGIN_LOAD,
	/**
	 * Lifecycle origin by plugin unloading
	 */
	PLUGIN_UNLOAD,
	/**
	 * Lifecycle origin by manual operation
	 */
	MANUAL
}
