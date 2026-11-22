package com.github.karmadeb.locklogin.neo.common;

import com.github.karmadeb.locklogin.neo.api.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.agnostic.ServerPlatform;

@SuppressWarnings("unused")
public record NeoServerPlatform(NamespacedIdentity id, Class<?> provider) implements ServerPlatform {
}
