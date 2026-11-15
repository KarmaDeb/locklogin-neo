package com.github.karmadeb.locklogin.neo.api;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

/**
 * The sneaky function allows to execute a function which
 * at the moment of execution might or might not throw an
 * exception. The behavior is very similar to what a {@link java.util.function.Function}
 * allows to do, but it just also allows to throw exceptions
 * @param <P> the parameter type
 * @param <V> the value type
 */
@FunctionalInterface
public interface SneakyFunction<P, V> {

	/**
	 * Execute the function
	 * @param param the function parameter
	 * @return the function result
	 * @throws Exception if the function fails to execute
	 */
	V apply(final P param) throws Exception;

	/**
	 * Returns a composed function that first applies the {@code before}
	 * function to its input, and then applies this function to the result.
	 * If evaluation of either function throws an exception, it is relayed to
	 * the caller of the composed function.
	 *
	 * @param <C> the type of input to the {@code before} function, and to the
	 *           composed function
	 * @param composer the function to apply before this function is applied
	 * @return a composed function that first applies the {@code before}
	 * function and then applies this function
	 * @throws NullPointerException if before is null
	 */
	@Contract("null -> fail") @NotNull
	default <C> SneakyFunction<C, V> compose(final SneakyFunction<? super C, ? extends P> composer) {
		Objects.requireNonNull(composer);
		return v -> apply(composer.apply(v));
	}

	/**
	 * Returns a composed function that first applies this function to
	 * its input, and then applies the {@code after} function to the result.
	 * If evaluation of either function throws an exception, it is relayed to
	 * the caller of the composed function.
	 *
	 * @param <C> the type of output of the {@code after} function, and of the
	 *           composed function
	 * @param after the function to apply after this function is applied
	 * @return a composed function that first applies this function and then
	 * applies the {@code after} function
	 * @throws NullPointerException if after is null
	 */
	@Contract("null -> fail") @NotNull
	default <C> SneakyFunction<P, C> andThen(final SneakyFunction<? super V, ? extends C> after) {
		Objects.requireNonNull(after);
		return p -> after.apply(apply(p));
	}

	/**
	 * Normalize the sneaky function into a
	 * java's compatible function
	 * @return the normalized function
	 */
	@NotNull
	default Function<P, V> normalize() {
		return p -> {
			try {
				return apply(p);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		};
	}

	/**
	 * Returns a function that always returns its input argument.
	 *
	 * @param <P> the type of the input and output objects to the function
	 * @return a function that always returns its input argument
	 */
	@NotNull
	static <P> SneakyFunction<P, P> identity() {
		return p -> p;
	}
}
