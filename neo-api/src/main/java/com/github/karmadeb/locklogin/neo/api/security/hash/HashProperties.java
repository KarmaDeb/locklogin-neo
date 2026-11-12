package com.github.karmadeb.locklogin.neo.api.security.hash;

import org.jetbrains.annotations.Contract;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An object which can hold any primitive data
 * as part of metadata for a hash. This might be used
 * by some algorithms in order to provide information
 * when re-hashing. Please note, hash properties
 * are strict type bound, which means if you set
 * an int with key "A", getting a long with key "A"
 * will not return the int value, but instead return a
 * default value (0 if not provided)
 */
public final class HashProperties {

	private static final Map<Class<?>, Character> TYPE_MAPPINGS = new HashMap<>(){
		{
			put(Boolean.TYPE, 'B');
			put(Byte.TYPE, 'b');
			put(Short.TYPE, 's');
			put(Integer.TYPE, 'i');
			put(Long.TYPE, 'l');
			put(Float.TYPE, 'f');
			put(Double.TYPE, 'd');
			put(BigInteger.class, 'I');
			put(BigDecimal.class, 'D');
			put(Character.TYPE, 'c');
			put(String.class, 'S');
		}
	};

	private static class Value {
		private final Class<?> type;
		private final Object raw;

		public Value(final boolean value) {
			this(value, Boolean.TYPE);
		}

		public Value(final byte value) {
			this(value, Byte.TYPE);
		}

		public Value(final short value) {
			this(value, Short.TYPE);
		}

		public Value(final int value) {
			this(value, Integer.TYPE);
		}

		public Value(final long value) {
			this(value, Long.TYPE);
		}

		public Value(final float value) {
			this(value, Float.TYPE);
		}

		public Value(final double value) {
			this(value, Double.TYPE);
		}

		public Value(final BigInteger value) {
			this(value, BigInteger.class);
		}

		public Value(final BigDecimal value) {
			this(value, BigDecimal.class);
		}

		public Value(final char value) {
			this(value, Character.TYPE);
		}

		public Value(final String value) {
			this(value, String.class);
		}

		private Value(final Object obj, final Class<?> type) {
			this.raw = obj;
			this.type = type;
		}

		public Object get() {
			return this.raw;
		}

		public Class<?> type() {
			return this.type;
		}
	}

	private final SequencedMap<String, Value> values;

	/**
	 * Create a new mutable instance of hash
	 * properties
	 */
	public HashProperties() {
		 this.values = new LinkedHashMap<>();
	}

	/**
	 * Create a new read-only instance of hash
	 * properties
	 * @param properties the properties
	 */
	@Contract("null -> fail")
	public HashProperties(final HashProperties properties) {
		this.values = Collections.unmodifiableSequencedMap(Objects.requireNonNull(properties).values);
	}

	/**
	 * Remove a value
	 * @param key the value key
	 */
	public void unset(final String key) {
		if (key == null) {
			return;
		}

		this.values.remove(key);
	}

	/**
	 * Set a value
	 * @param key the key
	 * @param value the value
	 */
	public void set(final String key, final boolean value) {
		if (key == null) {
			return;
		}

		Value v = new Value(value);
		this.values.put(key, v);
	}

	/**
	 * Set a value
	 * @param key the key
	 * @param value the value
	 */
	public void set(final String key, final byte value) {
		if (key == null) {
			return;
		}

		Value v = new Value(value);
		this.values.put(key, v);
	}

	/**
	 * Set a value
	 * @param key the key
	 * @param value the value
	 */
	public void set(final String key, final short value) {
		if (key == null) {
			return;
		}

		Value v = new Value(value);
		this.values.put(key, v);
	}

	/**
	 * Set a value
	 * @param key the key
	 * @param value the value
	 */
	public void set(final String key, final int value) {
		if (key == null) {
			return;
		}

		Value v = new Value(value);
		this.values.put(key, v);
	}

	/**
	 * Set a value
	 * @param key the key
	 * @param value the value
	 */
	public void set(final String key, final long value) {
		if (key == null) {
			return;
		}

		Value v = new Value(value);
		this.values.put(key, v);
	}

	/**
	 * Set a value
	 * @param key the key
	 * @param value the value
	 */
	public void set(final String key, final float value) {
		if (key == null) {
			return;
		}

		Value v = new Value(value);
		this.values.put(key, v);
	}

	/**
	 * Set a value
	 * @param key the key
	 * @param value the value
	 */
	public void set(final String key, final double value) {
		if (key == null) {
			return;
		}

		Value v = new Value(value);
		this.values.put(key, v);
	}

	/**
	 * Set a value
	 * @param key the key
	 * @param value the value
	 */
	public void set(final String key, final BigInteger value) {
		if (key == null || value == null) {
			return;
		}

		Value v = new Value(value);
		this.values.put(key, v);
	}

	/**
	 * Set a value
	 * @param key the key
	 * @param value the value
	 */
	public void set(final String key, final BigDecimal value) {
		if (key == null || value == null) {
			return;
		}

		Value v = new Value(value);
		this.values.put(key, v);
	}

	/**
	 * Set a value
	 * @param key the key
	 * @param value the value
	 */
	public void set(final String key, final char value) {
		if (key == null) {
			return;
		}

		Value v = new Value(value);
		this.values.put(key, v);
	}

	/**
	 * Set a value
	 * @param key the key
	 * @param value the value
	 */
	public void set(final String key, final String value) {
		if (key == null || value == null) {
			return;
		}

		Value v = new Value(value);
		this.values.put(key, v);
	}

	/**
	 * Get a value
	 * @param key the key
	 * @return the value
	 */
	public boolean getBoolean(final String key) {
		return this.getBoolean(key, false);
	}

	/**
	 * Get a value
	 * @param key the key
	 * @return the value
	 */
	public byte getByte(final String key) {
		return this.getByte(key, (byte) 0);
	}

	/**
	 * Get a value
	 * @param key the key
	 * @return the value
	 */
	public short getShort(final String key) {
		return this.getShort(key, (short) 0);
	}

	/**
	 * Get a value
	 * @param key the key
	 * @return the value
	 */
	public int getInt(final String key) {
		return this.getInt(key,  0);
	}

	/**
	 * Get a value
	 * @param key the key
	 * @return the value
	 */
	public long getLong(final String key) {
		return this.getLong(key, 0L);
	}

	/**
	 * Get a value
	 * @param key the key
	 * @return the value
	 */
	public float getFloat(final String key) {
		return this.getFloat(key, 0F);
	}

	/**
	 * Get a value
	 * @param key the key
	 * @return the value
	 */
	public double getDouble(final String key) {
		return this.getDouble(key, 0D);
	}

	/**
	 * Get a value
	 * @param key the key
	 * @return the value
	 */
	public BigInteger getBigInt(final String key) {
		return this.getBigInt(key, BigInteger.ZERO);
	}

	/**
	 * Get a value
	 * @param key the key
	 * @return the value
	 */
	public BigDecimal getBigDecimal(final String key) {
		return this.getBigDecimal(key, BigDecimal.ZERO);
	}

	/**
	 * Get a value
	 * @param key the key
	 * @return the value
	 */
	public char getChar(final String key) {
		return this.getChar(key, '\0');
	}

	/**
	 * Get a value
	 * @param key the key
	 * @return the value
	 */
	public String getString(final String key) {
		return this.getString(key, "");
	}

	/**
	 * Get a value
	 * @param key the key
	 * @param def the default value
	 * @return the value
	 */
	public boolean getBoolean(final String key, final boolean def) {
		return this.get(key, Boolean.TYPE, def);
	}

	/**
	 * Get a value
	 * @param key the key
	 * @param def the default value
	 * @return the value
	 */
	public byte getByte(final String key, final byte def) {
		return this.get(key, Byte.TYPE, def);
	}

	/**
	 * Get a value
	 * @param key the key
	 * @param def the default value
	 * @return the value
	 */
	public short getShort(final String key, final short def) {
		return this.get(key, Short.TYPE, def);
	}

	/**
	 * Get a value
	 * @param key the key
	 * @param def the default value
	 * @return the value
	 */
	public int getInt(final String key, final int def) {
		return this.get(key, Integer.TYPE, def);
	}

	/**
	 * Get a value
	 * @param key the key
	 * @param def the default value
	 * @return the value
	 */
	public long getLong(final String key, final long def) {
		return this.get(key, Long.TYPE, def);
	}

	/**
	 * Get a value
	 * @param key the key
	 * @param def the default value
	 * @return the value
	 */
	public float getFloat(final String key, final float def) {
		return this.get(key, Float.TYPE, def);
	}

	/**
	 * Get a value
	 * @param key the key
	 * @param def the default value
	 * @return the value
	 */
	public double getDouble(final String key, final double def) {
		return this.get(key, Double.TYPE, def);
	}

	/**
	 * Get a value
	 * @param key the key
	 * @param def the default value
	 * @return the value
	 */
	public BigInteger getBigInt(final String key, final BigInteger def) {
		return this.get(key, BigInteger.class, def);
	}

	/**
	 * Get a value
	 * @param key the key
	 * @param def the default value
	 * @return the value
	 */
	public BigDecimal getBigDecimal(final String key, final BigDecimal def) {
		return this.get(key, BigDecimal.class, def);
	}

	/**
	 * Get a value
	 * @param key the key
	 * @param def the default value
	 * @return the value
	 */
	public char getChar(final String key, final char def) {
		return this.get(key, Character.TYPE, def);
	}

	/**
	 * Get a value
	 * @param key the key
	 * @param def the default value
	 * @return the value
	 */
	public String getString(final String key, final String def) {
		return this.get(key, String.class, def);
	}

	/**
	 * Get the string representation of the hash properties
	 * @return the string representation
	 */
	public String toString() {
		StringBuilder builder = new StringBuilder();

		for (Map.Entry<String, Value> entry : this.values.entrySet()) {
			String key = entry.getKey();
			Value value = entry.getValue();

			String raw = String.valueOf(value).replace("&", "\\&");
			char type = TYPE_MAPPINGS.get(value.type());

			builder.append(key)
					.append("=")
					.append(type)
					.append(raw)
					.append("&");
		}
		if (builder.isEmpty()) {
			return "";
		}

		return builder.substring(0, builder.length() - 1);
	}

	private <T> T get(final String key, final Class<T> type, final T def) {
		if (key == null || type == null) {
			return def;
		}

		Value v = this.values.get(key);
		if (v == null || !v.type().equals(type)) {
			return def;
		}

		return type.cast(v.get());
	}

	public static HashProperties fromString(final String data) {
		HashProperties properties = new HashProperties();

		List<String> parts = extractStringData(data);
		for (String element : parts) {
			String[] entry = element.split("=", 2);

			String key = entry[0];
			String rawValue = entry[1].substring(1);
			char rawType = entry[1].charAt(0);

			switch (rawType) {
				case 'B' -> properties.set(key, rawValue.equals("true"));
				case 'b' -> properties.set(key, Byte.parseByte(rawValue));
				case 's' -> properties.set(key, Short.parseShort(rawValue));
				case 'i' -> properties.set(key, Integer.parseInt(rawValue));
				case 'l' -> properties.set(key, Long.parseLong(rawValue));
				case 'f' -> properties.set(key, Float.parseFloat(rawValue));
				case 'd' -> properties.set(key, Double.parseDouble(rawValue));
				case 'I' -> properties.set(key, new BigInteger(rawValue));
				case 'D' -> properties.set(key, new BigDecimal(rawValue));
				case 'c' -> properties.set(key, rawValue.charAt(0));
				case 'S' -> properties.set(key, rawValue);
			}
		}

		return properties;
	}

	private static List<String> extractStringData(final String data) {
		List<String> parts = new ArrayList<>();
		int lastIndex = 0;

		Matcher matcher = Pattern.compile("&").matcher(data);

		while (matcher.find()) {
			int ampIndex = matcher.start();

			int barCounter = 0;
			int i = ampIndex - 1;
			while (i >= 0 && data.charAt(i) == '\\') {
				barCounter++;
				i--;
			}

			if (barCounter % 2 == 0) {
				parts.add(data.substring(lastIndex, ampIndex));
				lastIndex = matcher.end();
			}
		}

		parts.add(data.substring(lastIndex));
		return parts;
	}

	/**
	 * Creates a clone of the specified properties. Unlike
	 * the {@link HashProperties(HashProperties) constructor}, this method
	 * returns a mutable instance of hash properties
	 * @param properties the properties
	 * @return the cloned properties
	 */
	public static HashProperties cloneOf(final HashProperties properties) {
		HashProperties clone = new HashProperties();
		clone.values.putAll(properties.values);

		return clone;
	}
}