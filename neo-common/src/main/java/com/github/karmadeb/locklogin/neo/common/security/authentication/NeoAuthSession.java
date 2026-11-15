package com.github.karmadeb.locklogin.neo.common.security.authentication;

import com.github.karmadeb.locklogin.neo.api.extension.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.extension.registry.ExtensionRegistryType;
import com.github.karmadeb.locklogin.neo.api.player.PlayerAdapter;
import com.github.karmadeb.locklogin.neo.api.player.PlayerAdapterFactory;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthLayer;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthLayer.Hook;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthLayer.HookFailure;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthLayer.Result;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthSession;
import com.github.karmadeb.locklogin.neo.common.extension.NeoRegistry;
import com.github.karmadeb.locklogin.neo.common.struct.SortedDuplexMap;
import com.github.karmadeb.locklogin.neo.common.struct.SortedDuplexMap.Walker;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

public final class NeoAuthSession<T> implements AuthSession<T> {

	private static final String KICK_REASON_COMPROMISED = "LockLogin authentication pipe has been compromised";
	private static final String KICK_REASON_BROKEN = "LockLogin authentication pipe has been broken";
	private static final byte CANCEL = 1 << 1;
	private static final byte CANCEL_HARD = 1 << 2;

	private final Class<T> type;

	private final SortedDuplexMap<NamespacedIdentity, AuthLayer<T>> layers = new SortedDuplexMap<>();
	private final Set<NamespacedIdentity> disabled = new HashSet<>();
	private final Set<BiConsumer<Boolean, HookFailure>> handlers = new LinkedHashSet<>();
	private final CompletableFuture<Boolean> futureResult = new CompletableFuture<>();

	private volatile Thread executionThread;
	private boolean running = false;
	private int cancel = 1;

	private Boolean result;
	private HookFailure failure;

	@Contract("null -> fail")
	public NeoAuthSession(final Class<T> type) {
		this.type = Objects.requireNonNull(type);

		ExtensionRegistryType<AuthLayer<?>> registry = NeoRegistry.registry().authLayer();
		List<AuthLayer<?>> collected = new ArrayList<>();
		for (NamespacedIdentity id : registry) {
			AuthLayer<?> layer = registry.get(id).orElse(null);
			if (layer == null || !layer.autoRegister() || (!layer.type().equals(this.type) && !layer.type().equals(PlayerAdapter.class))) {
				continue;
			}

			collected.add(layer);
		}

		for (AuthLayer<?> layer : sortTopologically(collected)) {
			this.add(layer);
		}
	}

	/**
	 * Get the session type. This is essentially required
	 * for in-runtime validation of player object type
	 * before calling to {@link #start(T)}. It's important
	 * to specify the object type correctly, for instance
	 * {@code org.bukkit.entity.Player.class} for Bukkit.
	 * Alternatively, you can use the experimental "neo-api-bridge"
	 * API to have a "centralized" way of interacting with players
	 * and the server object, even though it is not currently recommended
	 *
	 * @return the layer type
	 */
	@Override
	public @NotNull Class<T> type() {
		return this.type;
	}

	/**
	 * Add an authentication layer to run BEFORE the
	 * specified layer. If the "before" layer is not present,
	 * the authentication layer is put at the end of the queue
	 * instead, regardless the required layer being added later.
	 * Similar to {@link #add(AuthLayer)} this method returns only
	 * false when the layer is already in the session
	 *
	 * @param before the layer to add before of
	 * @param layer  the layer
	 * @return if the layer was added
	 */
	@Override
	public boolean add(final @NotNull NamespacedIdentity before, final @NotNull AuthLayer<?> layer) {
		if (running || this.result != null || this.layers.contains(layer.id()) || (!layer.type().equals(this.type) &&
				!layer.type().equals(PlayerAdapter.class)) ||
				NeoRegistry.registry().authLayer().get(layer.id()).isEmpty()) {
			return false;
		}

		if (layer.type().equals(PlayerAdapter.class)) {
			AuthLayer<T> proxy = new AuthLayer<>() {
				@Override
				public @NotNull Class<T> type() {
					return NeoAuthSession.this.type;
				}

				@Override
				public @NotNull Hook challenge(final @NotNull T player) {
					PlayerAdapter adapter = PlayerAdapterFactory.adapt(player).orElse(null);
					if (adapter == null) {
						return Hook.completed(Result.SILENT.reason("Failed to adapt client"));
					}

					// noinspection unchecked
					return ((AuthLayer<PlayerAdapter>) layer).challenge(adapter);
				}

				@Override
				public @NotNull NamespacedIdentity id() {
					return layer.id();
				}
			};

			layers.insertBefore(before, layer.id(), proxy);
			return true;
		}

		// noinspection unchecked
		layers.insertBefore(before, layer.id(), (AuthLayer<T>) layer);
		return true;
	}

	/**
	 * Disables a layer. This method return method can
	 * safely be ignored, as it's purely informative,
	 * it indicates if the layer was already disabled or
	 * not registered (by returning false), and true if it
	 * was. It's important to note that layers cannot be removed
	 * from an auth session once added, but instead it can be
	 * disabled.
	 *
	 * @param layer the layer to disable
	 * @return if the layer was disabled
	 */
	@Override
	public boolean disable(final @NotNull NamespacedIdentity layer) {
		if (!this.layers.contains(layer)) {
			return false;
		}

		AuthLayer<?> authLayer = this.layers.get(layer);
		assert authLayer != null;

		if (authLayer.required()) {
			long requiredCount = this.layers.values().filter(AuthLayer::required).count();
			if (requiredCount == 1) {
				return false;
			}
		}

		return this.disabled.add(layer);
	}

	/**
	 * Enables a layer. This method return method can
	 * safely be ignored, as it's purely informative,
	 * it indicates if the layer was already enabled or
	 * not registered (by returning false), and true if it
	 * was not.
	 *
	 * @param layer the layer to enable
	 * @return if the layer was disabled
	 */
	@Override
	public boolean enable(final @NotNull NamespacedIdentity layer) {
		if (this.running || !this.layers.contains(layer)) {
			return false;
		}

		return this.disabled.remove(layer);
	}

	/**
	 * Get if a layer is currently disabled in this
	 * auth session. This method returns true regardless
	 * if the auth layer is not registered. For checking if a
	 * layer is unregistered see {@link #contains(NamespacedIdentity)}
	 *
	 * @param layer the layer to check for
	 * @return if the layer is disabled
	 */
	@Override
	public boolean disabled(final @NotNull NamespacedIdentity layer) {
		return !this.layers.contains(layer) || this.disabled.contains(layer);
	}

	/**
	 * Get if a layer is currently registered in this
	 * auth session. This method returns true regardless
	 * if the auth layer was disabled. For checking if a
	 * layer is disabled see {@link #disabled(NamespacedIdentity)}
	 *
	 * @param layer the layer to check for
	 * @return if the layer is registered
	 */
	@Override
	public boolean contains(final @NotNull NamespacedIdentity layer) {
		return this.layers.contains(layer);
	}

	/**
	 * Start the authentication process
	 *
	 * @param player the player to start the process for
	 */
	@Override
	public void start(final @NotNull T player) {
		if (this.running || this.result != null) {
			return;
		}

		this.executionThread = Thread.currentThread();
		Iterator<AuthLayer<T>> iterator = this.layers.values().iterator();
		if (!iterator.hasNext()) {
			return;
		}

		Hook master = new Hook()
				.onSuccess(() -> {
					this.result = true;
				})
				.onFailure(failure -> {
					this.result = false;
					this.failure = failure;
				}).always(() -> {
					try {
						for (BiConsumer<Boolean, HookFailure> handler : this.handlers) {
							handler.accept(this.result, this.failure);
						}
					} finally {
						this.handlers.clear();
						this.running = false;
						this.futureResult.complete(this.result);
					}
				});

		Walker<AuthLayer<T>> walker = this.layers.navigate();
		if (walker == null) {
			master.failure(Result.KICK.reason(KICK_REASON_COMPROMISED));
			return;
		}

		resolveWalker(player, walker, master);
	}

	private void resolveWalker(final T player, final Walker<AuthLayer<T>> walker, final Hook master) {
		AuthLayer<T> layer = walker.from();
		Walker<AuthLayer<T>> target = walker.to();

		// Disabling while running the walker is possible, as the result of a layer A can cause the disable of layer B
		if (this.disabled.contains(layer.id())) {
			if (target == null) {
				master.failure(Result.KICK.reason(KICK_REASON_COMPROMISED));
				return;
			}

			resolveWalker(player, target, master);
			return;
		}

		Hook await = layer.challenge(player);
		await.onSuccess(() -> {
			if (target == null) {
				master.successful();
				return;
			}

			resolveWalker(player, target, master);
		}).onFailure(master::failure);
	}

	/**
	 * Add a handler for when the session completes the
	 * authentication. This method completes instantly if a
	 * result was already provided
	 *
	 * @param handler the handler
	 */
	@Override
	public NeoAuthSession<T> then(final @NotNull BiConsumer<Boolean, HookFailure> handler) {
		if (this.result != null) {
			wrapSafely(handler).accept(this.result, this.failure);
			return this;
		}

		this.handlers.add(wrapSafely(handler));
		return this;
	}

	/**
	 * Get the failure reason. This can be null
	 * in two cases:
	 * 1 - The session has not yet been run
	 * 2 - The session completed successful
	 *
	 * @return the failure reason
	 */
	@Override
	public @NotNull Optional<HookFailure> reason() {
		return Optional.ofNullable(this.failure);
	}

	/**
	 * Attempts to cancel execution of this task.  This method has no
	 * effect if the task is already completed or cancelled, or could
	 * not be cancelled for some other reason.  Otherwise, if this
	 * task has not started when {@code cancel} is called, this task
	 * should never run.  If the task has already started, then the
	 * {@code mayInterruptIfRunning} parameter determines whether the
	 * thread executing this task (when known by the implementation)
	 * is interrupted in an attempt to stop the task.
	 *
	 * <p>The return value from this method does not necessarily
	 * indicate whether the task is now cancelled; use {@link
	 * #isCancelled}.
	 *
	 * @param mayInterruptIfRunning {@code true} if the thread
	 *                              executing this task should be interrupted (if the thread is
	 *                              known to the implementation); otherwise, in-progress tasks are
	 *                              allowed to complete
	 * @return {@code false} if the task could not be cancelled,
	 * typically because it has already completed; {@code true}
	 * otherwise. If two or more threads cause a task to be cancelled,
	 * then at least one of them returns {@code true}. Implementations
	 * may provide stronger guarantees.
	 */
	@Override
	public boolean cancel(final boolean mayInterruptIfRunning) {
		if (this.isDone() || this.isCancelled()) {
			return false;
		}

		if (mayInterruptIfRunning) {
			this.cancel |= CANCEL_HARD;
			if (this.executionThread != null) {
				this.executionThread.interrupt();
			}
		} else {
			this.cancel |= CANCEL;
		}

		if (this.result == null) {
			this.result = false;
			this.failure = Result.KICK.reason(KICK_REASON_BROKEN);
		}

		return true;
	}

	/**
	 * Returns {@code true} if this task was cancelled before it completed
	 * normally.
	 *
	 * @return {@code true} if this task was cancelled before it completed
	 */
	@Override
	public boolean isCancelled() {
		return (this.cancel & CANCEL) != 0 || (this.cancel & CANCEL_HARD) != 0;
	}

	/**
	 * Returns {@code true} if this task completed.
	 * <p>
	 * Completion may be due to normal termination, an exception, or
	 * cancellation -- in all of these cases, this method will return
	 * {@code true}.
	 *
	 * @return {@code true} if this task completed
	 */
	@Override
	public boolean isDone() {
		return this.result != null;
	}

	/**
	 * Waits if necessary for the computation to complete, and then
	 * retrieves its result.
	 *
	 * @return the computed result
	 * @throws CancellationException if the computation was cancelled
	 * @throws ExecutionException    if the computation threw an
	 *                               exception
	 * @throws InterruptedException  if the current thread was interrupted
	 *                               while waiting
	 */
	@Override
	public Boolean get() throws InterruptedException, ExecutionException {
		if (this.result != null) {
			return this.result;
		}

		return this.futureResult.get();
	}

	/**
	 * Waits if necessary for at most the given time for the computation
	 * to complete, and then retrieves its result, if available.
	 *
	 * @param timeout the maximum time to wait
	 * @param unit    the time unit of the timeout argument
	 * @return the computed result
	 * @throws CancellationException if the computation was cancelled
	 * @throws ExecutionException    if the computation threw an
	 *                               exception
	 * @throws InterruptedException  if the current thread was interrupted
	 *                               while waiting
	 * @throws TimeoutException      if the wait timed out
	 */
	@Override
	public Boolean get(final long timeout, final @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		if (this.result != null) {
			return this.result;
		}

		return this.futureResult.get(timeout, unit);
	}

	private static List<AuthLayer<?>> sortTopologically(final Collection<AuthLayer<?>> layers) {
		List<AuthLayer<?>> originalList = new ArrayList<>(layers);

		Map<NamespacedIdentity, AuthLayer<?>> layerMap = new LinkedHashMap<>();
		for (AuthLayer<?> layer : originalList) {
			layerMap.put(layer.id(), layer);
		}

		Map<NamespacedIdentity, Integer> inDegrees = new HashMap<>();
		Map<NamespacedIdentity, List<NamespacedIdentity>> graph = new HashMap<>();

		for (AuthLayer<?> layer : originalList) {
			inDegrees.put(layer.id(), 0);
		}

		for (AuthLayer<?> layer : originalList) {
			NamespacedIdentity uId = layer.id();
			for (NamespacedIdentity vId : layer.before()) {
				if (layerMap.containsKey(vId)) {
					graph.computeIfAbsent(uId, _ -> new ArrayList<>()).add(vId);
					inDegrees.put(vId, inDegrees.get(vId) + 1);
				}
			}
		}

		List<AuthLayer<?>> sortedResult = new ArrayList<>();
		Set<NamespacedIdentity> visited = new HashSet<>();

		while (sortedResult.size() < originalList.size()) {
			AuthLayer<?> nextNode = null;
			for (AuthLayer<?> layer : originalList) {
				NamespacedIdentity id = layer.id();
				if (!visited.contains(id) && inDegrees.getOrDefault(id, 0) == 0) {
					nextNode = layer;
					break;
				}
			}

			if (nextNode == null) {
				for (AuthLayer<?> layer : originalList) {
					if (!visited.contains(layer.id())) {
						sortedResult.add(layer);
						visited.add(layer.id());
					}
				}
				break;
			}

			sortedResult.add(nextNode);
			visited.add(nextNode.id());

			List<NamespacedIdentity> neighbors = graph.get(nextNode.id());
			if (neighbors != null) {
				for (NamespacedIdentity neighborId : neighbors) {
					inDegrees.put(neighborId, inDegrees.get(neighborId) - 1);
				}
			}
		}

		return sortedResult;
	}

	private static BiConsumer<Boolean, HookFailure> wrapSafely(final BiConsumer<Boolean, HookFailure> handler) {
		return (b, r) -> {
			try {
				handler.accept(b, r);
			} catch (Throwable ex) {
				throw new RuntimeException(ex);
			}
		};
	}
}
