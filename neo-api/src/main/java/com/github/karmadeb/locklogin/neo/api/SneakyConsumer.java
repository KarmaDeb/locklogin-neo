package com.github.karmadeb.locklogin.neo.api;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * The sneaky consumer allows to execute a consumer which
 * at the moment of execution might or might not throw an
 * exception. The behavior is very similar to what a {@link Consumer}
 * allows to do, but it just also allows to throw exceptions
 * @param <P> the parameter type
 */
@FunctionalInterface
public interface SneakyConsumer<P> {

	/**
	 * Execute the consumer
	 * @param param the consumer parameter
	 * @throws Exception if the consumer fails to execute
	 */
	void accept(final P param) throws Exception;

	/**
	 * Returns a composed consumer that first applies the {@code before}
	 * consumer to its input, and then applies this consumer to the result.
	 * If evaluation of either consumer throws an exception, it is relayed to
	 * the caller of the composed consumer.
	 *
	 * @param <C> the type of input to the {@code before} consumer, and to the
	 *           composed consumer
	 * @param composer the consumer to apply before this consumer is applied
	 * @return a composed consumer that first applies the {@code before}
	 * consumer and then applies this consumer
	 * @throws NullPointerException if before is null
	 */
	@Contract("null -> fail") @NotNull
	default <C> SneakyConsumer<C> compose(final SneakyFunction<? super C, ? extends P> composer) {
		Objects.requireNonNull(composer);
		return v -> accept(composer.apply(v));
	}

	/**
	 * Returns a composed {@code Consumer} that performs, in sequence, this
	 * operation followed by the {@code after} operation. If performing either
	 * operation throws an exception, it is relayed to the caller of the
	 * composed operation.  If performing this operation throws an exception,
	 * the {@code after} operation will not be performed.
	 *
	 * @param after the operation to perform after this operation
	 * @return a composed {@code Consumer} that performs in sequence this
	 * operation followed by the {@code after} operation
	 * @throws NullPointerException if {@code after} is null
	 */
	@Contract("null -> fail") @NotNull
	default SneakyConsumer<P> andThen(final SneakyConsumer<? super P> after) {
		Objects.requireNonNull(after);
		return p -> {
			accept(p);
			after.accept(p);
		};
	}

	/**
	 * Normalize the sneaky consumer into a
	 * java's compatible consumer
	 * @return the normalized consumer
	 */
	@NotNull
	default Consumer<P> normalize() {
		return p -> {
			try {
				accept(p);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		};
	}
}
