package com.github.karmadeb.locklogin.neo.api.security.authentication;

import com.github.karmadeb.locklogin.neo.api.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthLayer.HookFailure;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;

/**
 * An authentication session allows for extensions to
 * register, disable or check different session {@link AuthLayer}
 * of a player. Please note disabling is only allowed when there's
 * at least one not-disabled mandatory auth layer left. It's important
 * to note that even though the session extends to {@link Future}, the
 * authentication process is not truly asynchronously but event-based,
 * meaning calling to Bukkit API without a "syncer-thread" is possible
 * and safe
 * @param <T> the player type, for multi-platform purposes
 */
@SuppressWarnings("unused")
public interface AuthSession<T> extends Future<Boolean> {

	/**
	 * Current session stage
	 */
	enum Stage {
		/**
		 * The current session is waiting for a user
		 * input before processing. During this stage,
		 * the session timeout (if any) is still counting
		 * backwards
		 */
		PENDING,
		/**
		 * The current session has received user input and a
		 * layer is processing it. During this stage, time
		 * stops counting backwards and resumes or goes to the
		 * next step if something goes wrong or successful
		 */
		PROCESSING,
		/**
		 * The current session has done processing all its auth
		 * layers and a result is available
		 */
		FINISHED
	}

	/**
	 * Get the current session stage
	 * @return the stage of the session
	 */
	@NotNull
	Stage stage();

	/**
	 * Set the current session stage. Modifying this
	 * value when {@link #isDone()} is true does nothing
	 * as isDone being true forces {@link #stage()} to
	 * return {@link Stage#FINISHED}
	 * @param stage the current stage
	 */
	void stage(final @NotNull Stage stage);

	/**
	 * Get the session type. This is essentially required
	 * for in-runtime validation of player object type
	 * before calling to {@link #start(T)}. It's important
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
	 * Add an authentication layer at the end of
	 * the queue. Order matters, meaning the authentication
	 * challenge is presented to the player
	 * in the same order it was added, skipping
	 * disabled layers. This operation returns false when
	 * the layer was already in the authentication session
	 * or if the layer is not compatible with {@link #type()}
	 * @param layer the layer to add
	 * @return if the layer was added
	 */
	@Contract("null -> fail")
	default boolean add(final AuthLayer<?> layer) {
		return add(NamespacedIdentity.locklogin("top"), Objects.requireNonNull(layer));
	}

	/**
	 * Add an authentication layer to run BEFORE the
	 * specified layer. If the "before" layer is not present,
	 * the authentication layer is put at the end of the queue
	 * instead, regardless the required layer being added later.
	 * Similar to {@link #add(AuthLayer)} this method returns only
	 * false when the layer is already in the session
	 * @param before the layer to add before of
	 * @param layer the layer
	 * @return if the layer was added
	 */
	@Contract("null, _ -> fail; _, null -> fail")
	default boolean add(final String before, final AuthLayer<?> layer) {
		return add(NamespacedIdentity.parse(Objects.requireNonNull(before)), Objects.requireNonNull(layer));
	}

	/**
	 * Add an authentication layer to run BEFORE the
	 * specified layer. If the "before" layer is not present,
	 * the authentication layer is put at the end of the queue
	 * instead, regardless the required layer being added later.
	 * Similar to {@link #add(AuthLayer)} this method returns only
	 * false when the layer is already in the session
	 * @param before the layer to add before of
	 * @param layer the layer
	 * @return if the layer was added
	 */
	@Contract("null, _ -> fail; _, null -> fail")
	default boolean add(final AuthLayer<?> before, final AuthLayer<?> layer) {
		return add(Objects.requireNonNull(before).id(), Objects.requireNonNull(layer));
	}

	/**
	 * Add an authentication layer to run BEFORE the
	 * specified layer. If the "before" layer is not present,
	 * the authentication layer is put at the end of the queue
	 * instead, regardless the required layer being added later.
	 * Similar to {@link #add(AuthLayer)} this method returns only
	 * false when the layer is already in the session
	 * @param before the layer to add before of
	 * @param layer the layer
	 * @return if the layer was added
	 */
	boolean add(final @NotNull NamespacedIdentity before, final @NotNull AuthLayer<?> layer);

	/**
	 * Disables a layer. This method return method can
	 * safely be ignored, as it's purely informative,
	 * it indicates if the layer was already disabled or
	 * not registered (by returning false), and true if it
	 * was. It's important to note that layers cannot be removed
	 * from an auth session once added, but instead it can be
	 * disabled.
	 * @param layer the layer to disable
	 * @return if the layer was disabled
	 */
	@Contract("null -> fail")
	default boolean disable(final AuthLayer<?> layer) {
		return disable(Objects.requireNonNull(layer).id());
	}

	/**
	 * Disables a layer. This method return method can
	 * safely be ignored, as it's purely informative,
	 * it indicates if the layer was already disabled or
	 * not registered (by returning false), and true if it
	 * was. It's important to note that layers cannot be removed
	 * from an auth session once added, but instead it can be
	 * disabled.
	 * @param layer the layer to disable
	 * @return if the layer was disabled
	 */
	@Contract("null -> fail")
	default boolean disable(final String layer) {
		return disable(NamespacedIdentity.parse(Objects.requireNonNull(layer)));
	}

	/**
	 * Disables a layer. This method return method can
	 * safely be ignored, as it's purely informative,
	 * it indicates if the layer was already disabled or
	 * not registered (by returning false), and true if it
	 * was. It's important to note that layers cannot be removed
	 * from an auth session once added, but instead it can be
	 * disabled.
	 * @param layer the layer to disable
	 * @return if the layer was disabled
	 */
	boolean disable(final @NotNull NamespacedIdentity layer);

	/**
	 * Enables a layer. This method return method can
	 * safely be ignored, as it's purely informative,
	 * it indicates if the layer was already enabled or
	 * not registered (by returning false), and true if it
	 * was not.
	 * @param layer the layer to enable
	 * @return if the layer was disabled
	 */
	@Contract("null -> fail")
	default boolean enable(final AuthLayer<?> layer) {
		return enable(Objects.requireNonNull(layer).id());
	}

	/**
	 * Enables a layer. This method return method can
	 * safely be ignored, as it's purely informative,
	 * it indicates if the layer was already enabled or
	 * not registered (by returning false), and true if it
	 * was not.
	 * @param layer the layer to enable
	 * @return if the layer was disabled
	 */
	@Contract("null -> fail")
	default boolean enable(final String layer) {
		return contains(NamespacedIdentity.parse(Objects.requireNonNull(layer)));
	}

	/**
	 * Enables a layer. This method return method can
	 * safely be ignored, as it's purely informative,
	 * it indicates if the layer was already enabled or
	 * not registered (by returning false), and true if it
	 * was not.
	 * @param layer the layer to enable
	 * @return if the layer was disabled
	 */
	boolean enable(final @NotNull NamespacedIdentity layer);

	/**
	 * Get if a layer is currently disabled in this
	 * auth session. This method returns true regardless
	 * if the auth layer is not registered. For checking if a
	 * layer is unregistered see {@link #contains(NamespacedIdentity)}
	 * @param layer the layer to check for
	 * @return if the layer is disabled
	 */
	@Contract("null -> fail")
	default boolean disabled(final AuthLayer<?> layer) {
		return disabled(Objects.requireNonNull(layer).id());
	}

	/**
	 * Get if a layer is currently disabled in this
	 * auth session. This method returns true regardless
	 * if the auth layer is not registered. For checking if a
	 * layer is unregistered see {@link #contains(NamespacedIdentity)}
	 * @param layer the layer to check for
	 * @return if the layer is disabled
	 */
	@Contract("null -> fail")
	default boolean disabled(final String layer) {
		return disabled(NamespacedIdentity.parse(Objects.requireNonNull(layer)));
	}

	/**
	 * Get if a layer is currently disabled in this
	 * auth session. This method returns true regardless
	 * if the auth layer is not registered. For checking if a
	 * layer is unregistered see {@link #contains(NamespacedIdentity)}
	 * @param layer the layer to check for
	 * @return if the layer is disabled
	 */
	boolean disabled(final @NotNull NamespacedIdentity layer);

	/**
	 * Get if a layer is currently enabled in this
	 * auth session. For checking if a layer is
	 * registered see {@link #contains(NamespacedIdentity)}
	 * @param layer the layer to check for
	 * @return if the layer is enabled
	 */
	@Contract("null -> fail")
	default boolean enabled(final AuthLayer<?> layer) {
		return disabled(Objects.requireNonNull(layer).id());
	}

	/**
	 * Get if a layer is currently enabled in this
	 * auth session. For checking if a layer is
	 * registered see {@link #contains(NamespacedIdentity)}
	 * @param layer the layer to check for
	 * @return if the layer is enabled
	 */
	@Contract("null -> fail")
	default boolean enabled(final String layer) {
		return disabled(NamespacedIdentity.parse(Objects.requireNonNull(layer)));
	}

	/**
	 * Get if a layer is currently enabled in this
	 * auth session. For checking if a layer is
	 * registered see {@link #contains(NamespacedIdentity)}
	 * @param layer the layer to check for
	 * @return if the layer is enabled
	 */
	@Contract("null -> fail")
	default boolean enabled(final NamespacedIdentity layer) {
		return !disabled(Objects.requireNonNull(layer));
	}

	/**
	 * Get if a layer is currently registered in this
	 * auth session. This method returns true regardless
	 * if the auth layer was disabled. For checking if a
	 * layer is disabled see {@link #disabled(NamespacedIdentity)}
	 * @param layer the layer to check for
	 * @return if the layer is registered
	 */
	@Contract("null -> fail")
	default boolean contains(final AuthLayer<?> layer) {
		return contains(Objects.requireNonNull(layer).id());
	}

	/**
	 * Get if a layer is currently registered in this
	 * auth session. This method returns true regardless
	 * if the auth layer was disabled. For checking if a
	 * layer is disabled see {@link #disabled(NamespacedIdentity)}
	 * @param layer the layer to check for
	 * @return if the layer is registered
	 */
	@Contract("null -> fail")
	default boolean contains(final String layer) {
		return contains(NamespacedIdentity.parse(Objects.requireNonNull(layer)));
	}

	/**
	 * Get if a layer is currently registered in this
	 * auth session. This method returns true regardless
	 * if the auth layer was disabled. For checking if a
	 * layer is disabled see {@link #disabled(NamespacedIdentity)}
	 * @param layer the layer to check for
	 * @return if the layer is registered
	 */
	boolean contains(final @NotNull NamespacedIdentity layer);

	/**
	 * Start the authentication process
	 * @param player the player to start the process for
	 */
	void start(final @NotNull T player);

	/**
	 * Add a handler for when the session completes the
	 * authentication. This method completes instantly if a
	 * result was already provided
	 * @param handler the handler
	 * @return the session
	 */
	AuthSession<T> then(final @NotNull BiConsumer<Boolean, HookFailure> handler);

	/**
	 * Get the failure reason. This can be null
	 * in two cases:
	 * 1 - The session has not yet been run
	 * 2 - The session completed successful
	 * @return the failure reason
	 */
	@NotNull
	Optional<HookFailure> reason();
}
