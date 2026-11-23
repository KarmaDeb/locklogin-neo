package com.github.karmadeb.locklogin.neo.api;

/**
 * The multi-single function allows executing the same
 * parameter function but as an array, returning an array
 * too. MultiSingleFunction extends from SneakyFunction, meaning
 * it has the same advantages of a simple {@link SneakyFunction}
 * @param <P> the parameter type
 * @param <V> the value type
 */
public interface MultiSingleFunction<P, V> extends SneakyFunction<P, V> {

	/**
	 * Execute the function
	 * @param param the function parameter
	 * @return the function result
	 * @throws Exception if the function fails to execute
	 */
	@Override
	V apply(final P param) throws Exception;

	/**
	 * Execute the function
	 * @param params the function parameters
	 * @return the function result
	 * @throws Exception if the function fails to execute
	 */
	V[] apply(final P[] params) throws Exception;
}
