package com.github.karmadeb.locklogin.neo.common;

public final class YamlPrettyFormatter {

	private YamlPrettyFormatter() {
		throw new UnsupportedOperationException("Cannot instantiate yaml pretty formatter");
	}

	/**
	 * Insert blank lines between sibling keys in a YAML document.
	 * A sibling is a key or list item that appears at the same indentation level
	 * as its predecessor.
	 */
	public static String format(final String yaml) {
		String[] lines = yaml.split("\n", -1);
		boolean[] insertBefore = new boolean[lines.length];

		for (int i = 0; i < lines.length; i++) {
			String trimmed = lines[i].trim();

			if (trimmed.isEmpty()) {
				continue;
			}

			int prevIdx = i - 1;
			while (prevIdx >= 0) {
				String prevTrim = lines[prevIdx].trim();
				if (!prevTrim.isEmpty() && !prevTrim.startsWith("#")) {
					break;
				}
				prevIdx--;
			}
			if (prevIdx < 0) {
				continue;
			}

			String prevNonEmpty = lines[prevIdx];
			String prevTrimmed = prevNonEmpty.trim();

			int prevIndent = getIndent(prevNonEmpty);
			int currIndent = getIndent(lines[i]);

			if (currIndent <= prevIndent && currIndent >= 0) {
				boolean currIsList = trimmed.startsWith("- ") || trimmed.startsWith("-'") || trimmed.startsWith("-\"") || trimmed.equals("-");
				boolean prevIsList = prevTrimmed.startsWith("- ") || prevTrimmed.startsWith("-'") || prevTrimmed.startsWith("-\"") || prevTrimmed.equals("-");

				if (!(currIsList && prevIsList && currIndent == prevIndent)) {
					if (currIsList || trimmed.contains(":")) {
						boolean hasBlankBetween = false;
						for (int b = prevIdx + 1; b < i; b++) {
							if (lines[b].trim().isEmpty()) {
								hasBlankBetween = true;
								break;
							}
						}
						if (!hasBlankBetween) {
							int markIdx = getMarkIdx(i, trimmed, lines);
							insertBefore[markIdx] = true;
						}
					}
				}
			}
		}

		StringBuilder result = new StringBuilder(yaml.length() * 2);
		for (int i = 0; i < lines.length; i++) {
			if (insertBefore[i]) {
				result.append("\n");
			}
			if (i > 0) {
				result.append("\n");
			}
			result.append(lines[i]);
		}

		return result.toString();
	}

	private static int getMarkIdx(int i, String trimmed, String[] lines) {
		int markIdx = i;
		if (trimmed.startsWith("#")) {
			while (markIdx > 0 && lines[markIdx - 1].trim().startsWith("#")) {
				markIdx--;
			}
		} else {
			int beforeIdx = i - 1;
			while (beforeIdx >= 0 && lines[beforeIdx].trim().isEmpty()) {
				beforeIdx--;
			}
			if (beforeIdx >= 0 && lines[beforeIdx].trim().startsWith("#")) {
				while (beforeIdx > 0 && lines[beforeIdx - 1].trim().startsWith("#")) {
					beforeIdx--;
				}
				markIdx = beforeIdx;
			}
		}
		return markIdx;
	}

	/**
	 * Returns the indentation level (number of leading spaces) of a line.
	 * Returns -1 for empty lines.
	 */
	private static int getIndent(final String line) {
		if (line.trim().isEmpty()) {
			return -1;
		}
		int indent = 0;
		for (int i = 0; i < line.length(); i++) {
			if (line.charAt(i) == ' ') {
				indent++;
			} else {
				break;
			}
		}
		return indent;
	}
}
