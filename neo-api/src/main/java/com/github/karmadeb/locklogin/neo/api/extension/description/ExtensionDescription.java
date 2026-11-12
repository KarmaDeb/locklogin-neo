package com.github.karmadeb.locklogin.neo.api.extension.description;

import java.util.Set;

/**
 * The extension description acts as a wrapper for a LockLogin
 * extension description
 * @param id the extension id
 * @param name the extension name
 * @param description the extension description (a bit redundant name)
 * @param version the extension version
 * @param authors the extension authors
 * @param dependsOn the extensions this one depends on
 * @param runBefore the extensions this one must load before
 *                  of. If the target extension defines the current
 *                  extension as a dependency, this option is ignored.
 *                  The same happens if the extensions defines the "run
 *                  before" extension as a "depends on" too
 */
public record ExtensionDescription(String id,
								   String name,
								   String description,
								   Version version,
								   Set<Author> authors,
								   Set<String> dependsOn,
								   Set<String> runBefore) {
}