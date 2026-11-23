package com.github.karmadeb.locklogin.neo.common.auth.platform.bukkit;

import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthLayer.Hook;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class AuthMetadataValue implements MetadataValue {

	private final Hook hook;
	private final JavaPlugin plugin;

	public AuthMetadataValue(final Hook hook, final JavaPlugin plugin) {
		this.hook = hook;
		this.plugin = plugin;
	}

	@Override
	public @Nullable Hook value() {
		return this.hook;
	}

	@Override
	public int asInt() {
		return 0;
	}

	@Override
	public float asFloat() {
		return 0;
	}

	@Override
	public double asDouble() {
		return 0;
	}

	@Override
	public long asLong() {
		return 0;
	}

	@Override
	public short asShort() {
		return 0;
	}

	@Override
	public byte asByte() {
		return 0;
	}

	@Override
	public boolean asBoolean() {
		return false;
	}

	@Override
	public @NonNull String asString() {
		return "";
	}

	@Override
	public @Nullable Plugin getOwningPlugin() {
		return this.plugin;
	}

	@Override
	public void invalidate() {
	}
}
