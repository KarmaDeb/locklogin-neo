package com.github.karmadeb.locklogin.neo.api.annotation;

import java.lang.annotation.*;

/**
 * Represents a class/method which, does not implement abstract
 * but, it's logically considered as abstract. Implementation of the
 * class methods or methods annotated with TrueAbstract is not mandatory.
 * However, is highly recommended as if it's annotated with this,
 * it's also considered part of the object usual lifecycle
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface TrueAbstract {

}
