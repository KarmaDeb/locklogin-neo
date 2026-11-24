package com.github.karmadeb.locklogin.neo.spigot;

import com.github.karmadeb.locklogin.neo.api.LockLogin.Stage;
import com.github.karmadeb.locklogin.neo.api.persistence.PersistenceManager;
import com.github.karmadeb.locklogin.neo.bukkit.BukkitLockLogin;
import com.github.karmadeb.locklogin.neo.bukkit.EssentialsLoader;
import com.github.karmadeb.locklogin.neo.bukkit.events.PlayerChatListener;
import com.github.karmadeb.locklogin.neo.bukkit.events.PlayerMoveListener;
import com.github.karmadeb.locklogin.neo.spigot.event.PlayerSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public final class LockLoginPlugin extends JavaPlugin {

	private final BukkitLockLogin api = new BukkitLockLogin("spigot");
	private final EssentialsLoader loader;

	public LockLoginPlugin() {
		api.registerAsProvider();
		this.loader = new EssentialsLoader(this);
	}

	@Override
	public void onLoad() {
		api.setStage(Stage.CONFIGURATION);
		loader.configure();
	}

	@Override
	public void onEnable() {
		api.configurations().configure().thenCompose(_ -> {
			api.setStage(Stage.DRIVERS);
			loader.setup();

			CompletableFuture<Void> awaiter = new CompletableFuture<>();
			Bukkit.getScheduler().runTask(this, () -> {
				PersistenceManager<Player> manager = api.persistence();
				// noinspection ConstantValue
				if (manager == null) {
					Bukkit.getPluginManager().disablePlugin(this);
					awaiter.completeExceptionally(new IllegalStateException("No persistence driver has been configured"));
					return;
				}

				manager.driver().connect().whenComplete((_, subProblem) -> {
					if (subProblem == null) {
						awaiter.complete(null);
						return;
					}

					awaiter.completeExceptionally(subProblem);
					api.setStage(Stage.LOADED);
				});
			});

			return awaiter;
		}).thenAccept(_ -> {
			PluginManager manager = Bukkit.getPluginManager();

			// TODO: Make this not a hardcoded set
			manager.registerEvents(new PlayerChatListener(Set.of("login", "register")), this);
			manager.registerEvents(new PlayerMoveListener(), this);
			manager.registerEvents(new PlayerSpawnEvent(this), this);

			loader.postSetup();
		}).exceptionally(problem -> {
			this.getLogger().log(Level.SEVERE, problem, () -> "Problem during LockLogin setup");
			Bukkit.getPluginManager().disablePlugin(this);

			return null;
		});
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
	}
}
