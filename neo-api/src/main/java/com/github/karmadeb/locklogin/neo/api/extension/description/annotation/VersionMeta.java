package com.github.karmadeb.locklogin.neo.api.extension.description.annotation;

import java.lang.annotation.*;

/**
 * Extension description version
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VersionMeta {

	/**
	 * Get the version major
	 * @return the major
	 */
	int major();

	/**
	 * Get the version minor
	 * @return the minor
	 */
	int minor();

	/**
	 * Get the version patch. Any value
	 * equal or over zero is considered
	 * valid
	 * @return the version patch
	 */
	int patch() default -1;

	/**
	 * Get the version data
	 * @return the data
	 */
	String data() default "";
}
