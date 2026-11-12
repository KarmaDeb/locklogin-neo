package com.github.karmadeb.locklogin.neo.api.extension.registry;

import com.github.karmadeb.locklogin.neo.api.extension.NamespacedIdentity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;


/**
 * An extension registry type represents a registry
 * specific object.
 * @param <T> the specific object
 */
public abstract class ExtensionRegistryType<T extends RegistryItem> implements Iterable<NamespacedIdentity> {

	protected final ExtensionRegistry registry;
	protected final Map<NamespacedIdentity, T> elements = new LinkedHashMap<>();

	/**
	 * Create a new extension registry
	 * @param registry the registry
	 */
	@Contract("null -> fail")
	public ExtensionRegistryType(final ExtensionRegistry registry) {
		this.registry = Objects.requireNonNull(registry);
	}

	/**
	 * Get the registry type
	 * @return the type
	 */
	@NotNull
	public abstract Class<T> type();

	/**
	 * Get the registry type ID
	 * @return the ID
	 */
	@NotNull
	public abstract NamespacedIdentity id();

	/**
	 * Get an element by ID. It is highly recommended to
	 * use {@link #get(NamespacedIdentity) get with id} instead
	 * @param id the id
	 * @return the element
	 */
	@NotNull
	@Contract("null -> fail")
	public final Optional<T> get(final String id)  {
		NamespacedIdentity idInstance = NamespacedIdentity.parse(Objects.requireNonNull(id));
		return this.get(idInstance);
	}

	/**
	 * Get an element by ID
	 * @param id the element ID
	 * @return the element instance
	 */
	@NotNull
	public Optional<T> get(final NamespacedIdentity id) {
		return Optional.ofNullable(this.elements.get(id));
	}

	/**
	 * Register a new element
	 * @param element the element to register
	 * @throws Conflict if there's any conflict
	 */
	public void register(final T element) throws Conflict {
		NamespacedIdentity id = element.id();
		NamespacedIdentity res = this.registry.extension().id(id.resource());

		if (!id.equals(res)) {
			throw new IllegalStateException(String.format(
					"Element id does not match registry's namespace (%s <> %s)",
					id,
					res
			));
		}

		if (!this.registry.occupy(id)) {
			for (ExtensionRegistryType<?> entry : this.registry) {
				if (entry.equals(this)) {
					continue;
				}

				RegistryItem item = entry.get(id).orElse(null);
				if (item != null) {
					throw new Conflict(id, item, element);
				}
			}
		}

		this.elements.put(id, element);
	}

	/**
	 * Get all the keys registered in this registry. The collection
	 * cannot be modified
	 * @return the registered resource keys
	 */
	@NotNull @Unmodifiable
	public Collection<NamespacedIdentity> keys() {
		return Collections.unmodifiableCollection(this.elements.keySet());
	}

	/**
	 * Returns an iterator over elements of type {@code T}.
	 *
	 * @return an Iterator.
	 */
	@Override
	public @NotNull Iterator<NamespacedIdentity> iterator() {
		return new HashMap<>(this.elements).keySet().iterator();
	}

	/**
	 * The conflict exception is thrown and only thrown when two identical
	 * IDs register a different resource each other. Even though the ExtensionRegistryType
	 * is bound to a specific object, the ID (and by so, it's resource) is
	 * not and should never be shared across different resources
	 */
	public static final class Conflict extends Exception {

		private final NamespacedIdentity id;
		private final RegistryItem registered;
		private final RegistryItem item;

		/**
		 * Create a new exception
		 * @param id the ID which caused the exception
		 * @param registered the currently registered item
		 * @param item the item tried to be registered
		 */
		@Contract("null, _, _ -> fail; _, null, _ -> fail; _, _, null -> fail")
		public Conflict(final NamespacedIdentity id, final RegistryItem registered, final RegistryItem item) {
			super(String.format("""
					Failed to register registry item %s (%s) as a different resource \
					on same extension (%s) has been already registered
					""",
					Objects.requireNonNull(registered).getClass().getSimpleName(),
					Objects.requireNonNull(id),
					Objects.requireNonNull(item).getClass().getSimpleName()));

			this.id = id;
			this.registered = registered;
			this.item = item;
		}

		/**
		 * Get the ID
		 * @return the resource ID
		 */
		public @NotNull NamespacedIdentity id() {
			return this.id;
		}

		/**
		 * Get the registered item
		 * @return the registered item
		 */
		public @NotNull RegistryItem registered() {
			return this.registered;
		}

		/**
		 * Get the item to register
		 * @return the item to register
		 */
		public @NotNull RegistryItem item() {
			return this.item;
		}
	}
}
