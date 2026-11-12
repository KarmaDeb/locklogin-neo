package com.github.karmadeb.locklogin.neo.api.extension.problem;

import com.github.karmadeb.locklogin.neo.api.Constants;
import com.github.karmadeb.locklogin.neo.api.extension.NeoExtension;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * The extension id collision problem is throw when an extension
 * is registered using an {@link NeoExtension#id() id} that already
 * exists. This might not be always the case, as an extension with the
 * same owner, same id, but different version is not considered an ID
 * collision but rather a version collision which LockLogin can resolve
 * without throwing any exception
 */
public class ExtensionIDCollisionProblem extends RuntimeException {

	/**
	 * Initialize the exception
	 * @param id the extension id
	 * @param jarFiles the jar files involved
	 */
	public ExtensionIDCollisionProblem(String id, final Path... jarFiles) {
		super(String.format("Found clashing extension ID \"%s\" in between: [%s]",
				id,
				Arrays.stream(jarFiles)
						.map(ExtensionIDCollisionProblem::extensionPath)
						.collect(Collectors.joining(", "))));
	}

	private static String extensionPath(final Path path)  {
		Path extensionsDirectory = Constants.WORKING_DIRECTORY.resolve("extensions");
		return extensionsDirectory.relativize(path).normalize().toString();
	}
}
