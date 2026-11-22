package com.github.karmadeb.locklogin.neo.common.config.registry;

import com.github.karmadeb.locklogin.neo.api.configuration.schema.SchemaWriter;
import com.github.karmadeb.locklogin.neo.api.configuration.schema.ValueWriter;
import com.github.karmadeb.locklogin.neo.api.configuration.schema.ValueWriter.Type;
import com.github.karmadeb.locklogin.neo.common.config.schema.ConfigurationSchemaWriter;
import com.github.karmadeb.locklogin.neo.common.config.schema.ConfigurationValueWriter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Helper class that centralizes all schema merge logic.
 * Takes two schemas and produces a merged map of values,
 * leaving both originals unchanged.
 */
public final class SchemaMerger {

	private SchemaMerger() {
	}

	/**
	 * Merge two schemas into a new map of ConfigurationValueWriters.
	 * @param thisSchema the current schema writer
	 * @param other the other schema to merge with
	 * @return a map of merged values
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, ConfigurationValueWriter<?>> merge(final ConfigurationSchemaWriter thisSchema,
														  final SchemaWriter other) {
		Objects.requireNonNull(other);
		Map<String, ConfigurationValueWriter<?>> valueMap = new LinkedHashMap<>();

		Map<String, ? extends ValueWriter<?>> others = other.values();
		for (Map.Entry<String, ConfigurationValueWriter<?>> entry : thisSchema.values.entrySet()) {
			String key = entry.getKey();
			ConfigurationValueWriter<?> value = entry.getValue();
			ValueWriter<?> otherValue = others.get(key);

			if (otherValue == null) {
				valueMap.put(key, new ConfigurationValueWriter<>(value));
				continue;
			}

			if (otherValue.constant().isPresent()) {
				valueMap.put(key, new ConfigurationValueWriter<>(otherValue));
				continue;
			}

			if (value.type().equals(Type.MAPPING)) {
				if (otherValue.type().equals(Type.MAPPING_LIST)) {
					valueMap.put(key, mergeSingleToList(
							(ValueWriter<SchemaWriter>) value,
							(ValueWriter<List<SchemaWriter>>) otherValue));
				} else if (otherValue.type().equals(Type.MAPPING)) {
					valueMap.put(key, mergeMappingWithMapping(
							(ValueWriter<SchemaWriter>) value,
							(ValueWriter<SchemaWriter>) otherValue));
				} else {
					valueMap.put(key, new ConfigurationValueWriter<>(otherValue));
				}
			} else if (value.type().equals(Type.MAPPING_LIST)) {
				if (otherValue.type().equals(Type.MAPPING)) {
					valueMap.put(key, mergeListToSingle(
							(ValueWriter<List<SchemaWriter>>) value,
							(ValueWriter<SchemaWriter>) otherValue));
				} else if (otherValue.type().equals(Type.MAPPING_LIST)) {
					valueMap.put(key, mergeListWithList(
							(ValueWriter<List<SchemaWriter>>) value,
							(ValueWriter<List<SchemaWriter>>) otherValue));
				} else {
					valueMap.put(key, new ConfigurationValueWriter<>(otherValue));
				}
			} else if (value.type().equals(Type.STRING_LIST)) {
				if (otherValue.type().equals(Type.STRING_LIST)) {
					valueMap.put(key, mergeListWithList(
							(ValueWriter<List<String>>) value,
							(ValueWriter<List<String>>) otherValue));
				} else {
					valueMap.put(key, new ConfigurationValueWriter<>(otherValue));
				}
			} else if (otherValue.type().equals(Type.STRING_LIST) && value.type().equals(Type.STRING)) {
				valueMap.put(key, mergeSingleToList(
						(ValueWriter<String>) value,
						(ValueWriter<List<String>>) otherValue));
			} else if (value.type().equals(otherValue.type())) {
				valueMap.put(key, new ConfigurationValueWriter<>(otherValue));
			} else {
				valueMap.put(key, new ConfigurationValueWriter<>(otherValue));
			}
		}

		for (Map.Entry<String, ? extends ValueWriter<?>> entry : other.values().entrySet()) {
			String key = entry.getKey();
			if (valueMap.containsKey(key)) {
				continue;
			}
			valueMap.put(key, new ConfigurationValueWriter<>(entry.getValue()));
		}

		return valueMap;
	}

	/**
	 * Merge two MAPPING values by recursively merging their nested schemas.
	 */
	private static ConfigurationValueWriter<SchemaWriter> mergeMappingWithMapping(final ValueWriter<SchemaWriter> thisValue,
																				  final ValueWriter<SchemaWriter> otherValue) {
		SchemaWriter thisSchema = thisValue.with().map(Supplier::get).orElse(null);
		SchemaWriter otherSchema = otherValue.with().map(Supplier::get).orElse(null);

		SchemaWriter mergedSchema;
		if (thisSchema instanceof ConfigurationSchemaWriter && otherSchema instanceof ConfigurationSchemaWriter) {
			mergedSchema = thisSchema.merge(otherSchema);
		} else if (otherSchema != null) {
			mergedSchema = otherSchema;
		} else {
			mergedSchema = thisSchema;
		}

		ConfigurationValueWriter<SchemaWriter> merged = new ConfigurationValueWriter<>(otherValue);
		if (mergedSchema != null) {
			merged.with(() -> mergedSchema);
		}
		return merged;
	}

	/**
	 * Merge a single value (this) with a list value (other).
	 * Result: [this single, *other list] — other's list items come first, then this single appended.
	 * Used for: STRING + STRING_LIST
	 */
	private static <T> ConfigurationValueWriter<List<T>> mergeSingleToList(final ValueWriter<T> thisValue,
																		   final ValueWriter<List<T>> otherValue) {
		List<T> combined = new ArrayList<>();

		otherValue.with().ifPresent(sup -> {
			List<T> list = sup.get();
			if (list != null) combined.addAll(list);
		});
		otherValue.required().ifPresent(sup -> {
			List<T> list = sup.get();
			if (list != null) combined.addAll(list);
		});

		thisValue.with().ifPresent(sup -> {
			T s = sup.get();
			if (s != null) combined.add(s);
		});
		thisValue.required().ifPresent(sup -> {
			T s = sup.get();
			if (s != null && !combined.contains(s)) combined.add(s);
		});

		ConfigurationValueWriter<List<T>> merged = new ConfigurationValueWriter<>(otherValue);
		if (!combined.isEmpty()) {
			merged.with(() -> combined);
		}
		return merged;
	}

	/**
	 * Merge a list value (this) with a single value (other).
	 * Result: [*this list, other single] — this list items come first, then other single appended.
	 * Used for: MAPPING_LIST + MAPPING
	 */
	private static <T> ConfigurationValueWriter<List<T>> mergeListToSingle(final ValueWriter<List<T>> thisValue,
																		   final ValueWriter<T> otherValue) {
		List<T> combined = new ArrayList<>();

		thisValue.with().ifPresent(sup -> {
			List<T> list = sup.get();
			if (list != null) combined.addAll(list);
		});
		thisValue.required().ifPresent(sup -> {
			List<T> list = sup.get();
			if (list != null) combined.addAll(list);
		});

		otherValue.with().ifPresent(sup -> {
			T s = sup.get();
			if (s != null) combined.add(s);
		});
		otherValue.required().ifPresent(sup -> {
			T s = sup.get();
			if (s != null && !combined.contains(s)) combined.add(s);
		});

		ConfigurationValueWriter<List<T>> merged = new ConfigurationValueWriter<>(thisValue);
		if (!combined.isEmpty()) {
			merged.with(() -> combined);
		}
		otherValue.validator().ifPresent(merged::validator);
		return merged;
	}

	/**
	 * Merge two list values.
	 * Result: [*this list, *other list] — combined in order.
	 * Used for: MAPPING_LIST + MAPPING_LIST, STRING_LIST + STRING_LIST
	 */
	private static <T> ConfigurationValueWriter<List<T>> mergeListWithList(final ValueWriter<List<T>> thisValue,
																		   final ValueWriter<List<T>> otherValue) {
		List<T> combined = new ArrayList<>();

		thisValue.with().ifPresent(sup -> {
			List<T> list = sup.get();
			if (list != null) combined.addAll(list);
		});
		thisValue.required().ifPresent(sup -> {
			List<T> list = sup.get();
			if (list != null) combined.addAll(list);
		});
		otherValue.with().ifPresent(sup -> {
			List<T> list = sup.get();
			if (list != null) combined.addAll(list);
		});
		otherValue.required().ifPresent(sup -> {
			List<T> list = sup.get();
			if (list != null) combined.addAll(list);
		});

		ConfigurationValueWriter<List<T>> merged = new ConfigurationValueWriter<>(thisValue);
		if (!combined.isEmpty()) {
			merged.with(() -> combined);
		}
		otherValue.validator().ifPresent(merged::validator);
		return merged;
	}
}
