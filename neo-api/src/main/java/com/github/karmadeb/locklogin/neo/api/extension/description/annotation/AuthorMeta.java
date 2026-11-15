package com.github.karmadeb.locklogin.neo.api.extension.description.annotation;

import java.lang.annotation.*;

/**
 * Extension description author
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthorMeta {

	/**
	 * Author username
	 * @return the username
	 */
	String username();

	/**
	 * Author email address
	 * @return the address
	 */
	String address() default "";

	/**
	 * Author website
	 * @return the website
	 */
	String website() default "";
}
