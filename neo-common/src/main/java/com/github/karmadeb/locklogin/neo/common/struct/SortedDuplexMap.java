package com.github.karmadeb.locklogin.neo.common.struct;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public final class SortedDuplexMap<K, V> {

	private static final class Node<K, V> {

		K key;
		V value;
		Node<K, V> prev;
		Node<K, V> next;

		@Contract("null, _ -> fail; _, null -> fail")
		public Node(final K key, final V value) {
			this.key = Objects.requireNonNull(key);
			this.value = Objects.requireNonNull(value);
		}
	}

	private final Map<K, Node<K, V>> map = new LinkedHashMap<>();
	private Node<K, V> head = null;
	private Node<K, V> tail = null;

	public boolean contains(final K key) {
		if (key == null) {
			return false;
		}

		return this.map.containsKey(key);
	}

	@Contract("null, _, _ -> fail; _, null, _ -> fail; _, _, null -> fail")
	public void insertBefore(final K targetKey, final K newKey, final V newValue) {
		Objects.requireNonNull(targetKey);
		Objects.requireNonNull(newKey);
		Objects.requireNonNull(newValue);

		if (!map.containsKey(targetKey)) {
			this.put(newKey, newValue);
		}

		Node<K, V> targetNode = map.get(targetKey);
		Node<K, V> newNode = new Node<>(newKey, newValue);

		newNode.next = targetNode;
		newNode.prev = targetNode.prev;

		if (targetNode.prev == null) {
			head = newNode;
		} else {
			targetNode.prev.next = newNode;
		}
		targetNode.prev = newNode;

		map.put(newKey, newNode);
	}

	@Contract("null, _ -> fail; _, null -> fail")
	public void put(final K key, final V value) {
		Objects.requireNonNull(key);
		Objects.requireNonNull(value);

		if (map.containsKey(key)) {
			map.get(key).value = value;
			return;
		}
		Node<K, V> newNode = new Node<>(key, value);
		if (head == null) {
			head = tail = newNode;
		} else {
			tail.next = newNode;
			newNode.prev = tail;
			tail = newNode;
		}
		map.put(key, newNode);
	}

	@Contract("null -> fail")
	public V get(final K key) {
		Node<K, V> node = map.get(Objects.requireNonNull(key));
		return node == null ? null : node.value;
	}

	public Walker<V> navigate() {
		if (head == null) {
			return null;
		}

		Walker<V> walker = null;
		Node<K, V> current = tail;
		while (current != null) {
			walker = new Walker<>(current.value, walker);
			current = current.prev;
		}

		return walker;
	}

	public Stream<K> keys() {
		return new ArrayList<K>(this.map.keySet()).stream();
	}

	public Stream<V> values() {
		return new ArrayList<>(this.map.values())
				.stream()
				.map(n -> n.value);
	}

	public record Walker<V> (V from, Walker<V> to) {}
}
