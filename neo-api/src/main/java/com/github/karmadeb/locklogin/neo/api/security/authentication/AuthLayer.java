package com.github.karmadeb.locklogin.neo.api.security.authentication;

import com.github.karmadeb.locklogin.neo.api.NamespacedIdentity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * A layer defines a specific behavior for a player to
 * proceed to its authentication. A layer can be mandatory
 * or non-mandatory (AKA: optional). When a layer is mandatory
 * it means the auth layer is strictly required and can only
 * be disabled if there's at least one mandatory authentication
 * layer. However, when an authentication layer is only optional, it
 * can be disabled regardless the mandatory statement, and disabling
 * will always succeed. This is kind of because the previous constraint
 * as the contracts ensures that at least one auth layer is present in.
 * the {@link AuthSession<T>}
 * @param <T> the player type, for multi-platform purposes
 */
@SuppressWarnings("unused")
public interface AuthLayer<T> {

	/**
	 * Get the layer id
	 * @return the ID of the layer
	 */
	@NotNull
	NamespacedIdentity id();

	/**
	 * Get the layer type. This is essentially required
	 * for in-runtime validation of player object type
	 * before calling to {@link #challenge(Object)}. It's important
	 * to specify the object type correctly, for instance
	 * {@code org.bukkit.entity.Player.class} for Bukkit.
	 * Alternatively, you can use the experimental "neo-api-bridge"
	 * API to have a "centralized" way of interacting with players
	 * and the server object, even though it is not currently recommended
	 * @return the layer type
	 */
	@NotNull
	Class<T> type();

	/**
	 * Determines if this layer is registered
	 * automatically
	 * @return if the layer is registered
	 */
	default boolean autoRegister() {
		return true;
	}

	/**
	 * Determines if this layer is required. When false
	 * it means that regardless of {@link Result#KICK kick}
	 * failure, the user receives a message in chat in case
	 * of fail, unless reason is {@link Result#SILENT silent}
	 * or success
	 * @return if the layer is required
	 */
	default boolean required() {
		return true;
	}

	/**
	 * Get an array of IDs to register this layer
	 * before on. This is ignored unless registered
	 * through the registry
	 * @return the IDs to register the layer before
	 */
	default NamespacedIdentity @NotNull[] before() {
		return new NamespacedIdentity[]{};
	}

	/**
	 * Get the timeout for the player to complete
	 * the challenge. If not provided, no timeout
	 * is enabled
	 * @return the layer authentication timeout
	 */
	@NotNull
	default Optional<Integer> timeout() {
		return Optional.empty();
	}

	/**
	 * Challenge the player. When the method returns true, the
	 * authentication passes, otherwise it fails. It's important to not
	 * misbehave this method in returning true instantly if the player
	 * does not apply for the challenge (i.e: an auth layer which is for
	 * staff members only). Instead, the {@link AuthSession#disable(NamespacedIdentity)}
	 * should be used in order to prevent dirtying the auditor logs.
	 * @param player the player to challenge
	 * @return the challenge result
	 */
	@NotNull
	Hook challenge(final @NotNull T player);

	/**
	 * A hook is a special object which allows for different commands
	 * or events to fire it as required. The hook is a one-time use object
	 * meaning after either {@link #successful()} or {@link #failure(HookFailure)}
	 * are called, further calls will usually get ignored
	 */
	final class Hook implements Callable<Void> {

		private boolean processed = false;
		private HookFailure result;

		private final Set<Runnable> onSuccess = new LinkedHashSet<>();
		private final Set<Consumer<HookFailure>> onFailure = new LinkedHashSet<>();
		private final Set<Runnable> always = new LinkedHashSet<>();

		/**
		 * Create a completed hook
		 * @return the hook
		 */
		public static Hook completed() {
			Hook hook = new Hook();
			hook.successful();

			return hook;
		}

		/**
		 * Create a completed hook
		 * @param reason the hook failure reason
		 * @return the hook
		 */
		@Contract("null -> fail")
		public static Hook completed(final HookFailure reason) {
			Hook hook = new Hook();
			hook.failure(reason);

			return hook;
		}

		/**
		 * Complete the hook successfully
		 */
		public void successful() {
			if (this.processed) {
				return;
			}

			try {
				for (Runnable op : this.onSuccess) {
					op.run();
				}
			} finally {
				for (Runnable op : this.always) {
					op.run();
				}

				this.onSuccess.clear();
				this.always.clear();
			}
		}

		/**
		 * Complete the hook as a failure
		 * @param reason the failure reason
		 */
		@Contract("null -> fail")
		public void failure(final HookFailure reason) {
			if (this.processed) {
				return;
			}

			this.result = Objects.requireNonNull(reason);
			processed = true;

			try {
				for (Consumer<HookFailure> op : this.onFailure) {
					op.accept(reason);
				}
			} finally {
				for (Runnable op : this.always) {
					op.run();
				}

				this.onFailure.clear();
				this.always.clear();
			}
		}

		/**
		 * Add a listener for successful operations
		 * @param op the operation
		 * @return the hook
		 */
		@Contract("null -> fail")
		public Hook onSuccess(final Runnable op) {
			Objects.requireNonNull(op);

			if (this.processed && this.result == null) {
				wrapSafely(op).run();
				return this;
			}

			this.onSuccess.add(wrapSafely(op));

			return this;
		}

		/**
		 * Add a listener for failed operations
		 * @param op the operation
		 * @return the hook
		 */
		@Contract("null -> fail")
		public Hook onFailure(final Runnable op) {
			Objects.requireNonNull(op);
			return this.onFailure(_ -> op.run());
		}

		/**
		 * Add a listener for failed operations
		 * @param op the operation
		 * @return the hook
		 */
		@Contract("null -> fail")
		public Hook onFailure(final Consumer<HookFailure> op) {
			Objects.requireNonNull(op);

			if (this.processed && this.result != null) {
				wrapSafely(op).accept(this.result);
				return this;
			}

			this.onFailure.add(wrapSafely(op));
			return this;
		}

		/**
		 * Add a listener after each operation regardless
		 * if successful or not
		 * @param op the operation
		 * @return the hook
		 */
		@Contract("null -> fail")
		public Hook always(final Runnable op) {
			Objects.requireNonNull(op);

			if (this.processed && this.result == null) {
				wrapSafely(op).run();
				return this;
			}

			this.always.add(wrapSafely(op));
			return this;
		}

		private Runnable wrapSafely(final Runnable operation) {
			return () -> {
				try {
					operation.run();
				} catch (Throwable ex) {
					throw new RuntimeException(ex);
				}
			};
		}

		private Consumer<HookFailure> wrapSafely(final Consumer<HookFailure> operation) {
			return rs -> {
				try {
					operation.accept(rs);
				} catch (Throwable ex) {
					throw new RuntimeException(ex);
				}
			};
		}

		/**
		 * Computes a result, or throws an exception if unable to do so.
		 *
		 * @return computed result
		 */
		@Override
		public Void call() {
			this.successful();
			return null;
		}
	}

	/**
	 * Failure results
	 */
	enum Result {
		/**
		 * Message to the player
		 */
		MESSAGE,
		/**
		 * Do not notify the player, but the system
		 */
		SILENT,
		/**
		 * Kick the player
		 */
		KICK;

		/**
		 * Create a new hook failure reason
		 * @param reason the reason
		 * @return the hook failure
		 */
		@Contract("null -> fail")
		public HookFailure reason(final String reason) {
			return new HookFailure(this, reason);
		}
	}

	/**
	 * The hook failure represents when a hook
	 * completes as a failure. Depending on the layer
	 * implementation, the hook failure can be a
	 * simple message, an "silent" (which logs in audit
	 * but does not notify the user) or "kick". It's
	 * always recommended to use HookFailure kick when
	 * failure results in kick as it allows the audit log
	 * to properly log the operation
	 */
	final class HookFailure {

		private final Result result;
		private final String reason;

		/**
		 * Create a new hook failure
		 * @param result the result
		 * @param reason the reason
		 */
		@Contract("null, _ -> fail; _, null -> fail")
		private HookFailure(final Result result, final String reason) {
			this.result = Objects.requireNonNull(result);
			this.reason = Objects.requireNonNull(reason);
		}

		/**
		 * Get the result
		 * @return the result
		 */
		@NotNull
		public Result result() {
			return this.result;
		}

		/**
		 * Get the reason
		 * @return the reason
		 */
		@NotNull
		public String reason() {
			return this.reason;
		}
	}
}
