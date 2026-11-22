package com.github.karmadeb.locklogin.neo.common.auth.platform.bukkit;

import com.github.karmadeb.locklogin.neo.api.LockLogin;
import com.github.karmadeb.locklogin.neo.common.auth.config.RegistrationConfig;
import com.github.karmadeb.locklogin.neo.common.auth.platform.bukkit.command.BukkitLoginCommand;
import com.github.karmadeb.locklogin.neo.common.auth.platform.bukkit.command.BukkitRegisterCommand;
import com.github.karmadeb.locklogin.neo.common.auth.platform.bukkit.layer.PasswordLoginLayer;
import com.github.karmadeb.locklogin.neo.common.auth.platform.bukkit.layer.PasswordRegisterLayer;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unused")
public final class BukkitPlatform {

	private final RegistrationConfig config;

	public BukkitPlatform(final RegistrationConfig config) {
		this.config = config;
	}

	public void init(final JavaPlugin plugin) {
		LockLogin<Player> api = LockLogin.get();

		api.authLayers().register(new PasswordRegisterLayer(plugin, this.config));
		api.authLayers().register(new PasswordLoginLayer(plugin, this.config));

		PluginCommand register = plugin.getCommand("register");
		if (register != null) {
			register.setExecutor(new BukkitRegisterCommand(this.config));
		}

		PluginCommand login = plugin.getCommand("login");
		if (login != null) {
			login.setExecutor(new BukkitLoginCommand(this.config));
		}
	}
}
