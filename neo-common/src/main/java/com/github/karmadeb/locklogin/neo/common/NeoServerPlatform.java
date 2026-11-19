package com.github.karmadeb.locklogin.neo.common;

import com.github.karmadeb.locklogin.neo.api.agnostic.ServerPlatform;
import com.github.karmadeb.locklogin.neo.api.extension.NamespacedIdentity;

public record NeoServerPlatform(NamespacedIdentity id, Class<?> provider) implements ServerPlatform {
}
