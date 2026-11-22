package com.github.karmadeb.locklogin.neo.api.extension.description.annotation;

import java.lang.annotation.*;

/**
 * Extension description
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExtensionMeta {

	/**
	 * Extension id
	 * @return the id
	 */
	String id();

	/**
	 * Extension name
	 * @return the name
	 */
	String name();

	/**
	 * Extension description
	 * @return the description
	 */
	String description();

	/**
	 * Extension version
	 * @return the version
	 */
	VersionMeta version();

	/**
	 * Extension authors
	 * @return the authors
	 */
	AuthorMeta[] authors();

	/**
	 * Get all the dependencies of this
	 * extension
	 * @return the dependencies
	 */
	String[] dependsOn() default {};

	/**
	 * Get all the extensions which must
	 * load after this
	 * @return the extension to load after
	 */
	String[] runBefore() default {};

	/**
	 * When auto discover is set to true, the extension
	 * is provided "as is", without a parent plugin to
	 * load it. Those extensions are a special kind intended
	 * to be placed inside an "extensions" directory
	 * in LockLogin plugin.
	 * @return if the extension has auto-discovery features
	 */
	boolean autoDiscover() default false;
}