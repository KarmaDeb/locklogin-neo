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
}