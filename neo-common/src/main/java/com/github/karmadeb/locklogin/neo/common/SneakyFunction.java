package com.github.karmadeb.locklogin.neo.common;

@FunctionalInterface
public interface SneakyFunction<P, V> {

	V apply(final P param) throws Exception;
}
