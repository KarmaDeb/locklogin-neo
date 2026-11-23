package com.github.karmadeb.locklogin.neo.common.i18n.bundle;

import com.github.karmadeb.locklogin.neo.api.i18n.bundle.LocaleBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.*;

public final class LocalizationBundle implements LocaleBundle {

	private final Map<Locale, LocalizationWriter> localeMap = new LinkedHashMap<>();

	public LocalizationBundle(final Map<Locale, LocalizationReader> reader) {
		this.mapReaders(reader);
	}

	private void mapReaders(final Map<Locale, LocalizationReader> readers) {
		for (Map.Entry<Locale, LocalizationReader> entry : readers.entrySet()) {
			Locale locale = entry.getKey();
			MappingNode node = entry.getValue().yaml();

			localeMap.put(locale, new LocalizationWriter(locale, node));
		}
	}

	/**
	 * Get the bundle supported locales
	 *
	 * @return the locales
	 */
	@Override
	public @NotNull @Unmodifiable Collection<Locale> locales() {
		return Collections.unmodifiableCollection(this.localeMap.keySet());
	}

	/**
	 * Get the bundle reader
	 *
	 * @param locale the locale
	 * @return the reader
	 */
	@Override
	public @NotNull LocalizationReader reader(final @NotNull Locale locale) {
		return this.writer(locale);
	}

	/**
	 * Get the bundle writer
	 *
	 * @param locale the locale
	 * @return the writer
	 */
	@Override
	public @NotNull LocalizationWriter writer(final @NotNull Locale locale) {
		return this.localeMap.computeIfAbsent(locale, _ -> new LocalizationWriter(locale, new MappingNode(
				Tag.MAP, new ArrayList<>(), FlowStyle.AUTO
		)));
	}

	public void compose() {
		Collection<LocalizationWriter> writers = this.localeMap.values();
		for (LocalizationWriter source : writers) {
			for (LocalizationWriter target : writers) {
				if (source != target) {
					source.copyTo(target);
				}
			}
		}

		LocalizationWriter reference = this.localeMap.values().stream().findFirst().orElse(null);
		if (reference != null) {
			for (LocalizationWriter writer : writers) {
				if (writer != reference) {
					writer.reorderKeys(reference);
				}
			}
		}
	}
}
