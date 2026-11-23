package com.github.karmadeb.locklogin.neo.common.auth.platform.bukkit.command;

import com.github.karmadeb.locklogin.neo.api.Constants;
import com.github.karmadeb.locklogin.neo.api.LockLogin;
import com.github.karmadeb.locklogin.neo.api.NamespacedIdentity;
import com.github.karmadeb.locklogin.neo.api.security.HashManager.Purpose;
import com.github.karmadeb.locklogin.neo.api.security.HashMethod;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthLayer.Hook;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthLayer.Result;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthSession;
import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthSession.Stage;
import com.github.karmadeb.locklogin.neo.api.security.hash.Hash;
import com.github.karmadeb.locklogin.neo.api.user.UserManager;
import com.github.karmadeb.locklogin.neo.api.user.account.Account;
import com.github.karmadeb.locklogin.neo.common.auth.AuthMetadataValue;
import com.github.karmadeb.locklogin.neo.common.auth.config.RegistrationConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.concurrent.Callable;

import static com.github.karmadeb.locklogin.neo.common.auth.AuthEntrypoint.CHALLENGE_METADATA_KEY;

public final class BukkitLoginCommand implements CommandExecutor {

	private final JavaPlugin plugin;
	private final RegistrationConfig config;

	public BukkitLoginCommand(final JavaPlugin plugin, final RegistrationConfig config) {
		this.plugin = plugin;
		this.config = config;
	}

	/**
	 * Executes the given command, returning its success.
	 * <br>
	 * If false is returned, then the "usage" plugin.yml entry for this command
	 * (if defined) will be sent to the player.
	 *
	 * @param sender  Source of the command
	 * @param command Command which was executed
	 * @param label   Alias of the command which was used
	 * @param args    Passed command arguments
	 * @return true if a valid command, otherwise false
	 */
	@Override
	public boolean onCommand(final @NonNull CommandSender sender,
							 final @NonNull Command command,
							 final @NonNull String label,
							 final @NonNull String[] args) {
		// TODO: Make messages not hardcoded, which will require us to implement a true translation system

		if (!(sender instanceof Player player)) {
			sender.sendMessage(ChatColor.RED + "This command is intended for players");
			return true;
		}

		Hook hook = player.getMetadata(CHALLENGE_METADATA_KEY).stream()
				.filter(v -> v instanceof AuthMetadataValue m && m.value() != null)
				.map(AuthMetadataValue.class::cast)
				.findAny()
				.map(AuthMetadataValue::value)
				.orElse(null);

		if (hook == null) {
			player.sendMessage(ChatColor.RED + "You have not pending authentication");
			return true;
		}

		if (args.length < 1) {
			player.sendMessage(ChatColor.RED + "Please type /login <password>");
			return true;
		}

		String password = args[0];
		LockLogin<Player> api = LockLogin.get();
		UserManager<Player> manager = api.persistence().users();

		AuthSession<Player> session = manager.session(player).orElseThrow();
		session.stage(Stage.PROCESSING);

		manager.account(player).whenComplete((result, throwable) -> {
			if (!player.isOnline()) {
				return;
			}

			if (throwable != null || result.isEmpty()) {
				// TODO: Log exception

				session.stage(Stage.PENDING);
				player.sendMessage(ChatColor.RED + "Something went wrong while logging into your account. Please try again");
				return;
			}

			Account account = result.get();
			Optional<String> secret = account.get(Account.SECRET_PASSWORD);
			if (secret.isEmpty()) {
				session.stage(Stage.PENDING);
				player.sendMessage(ChatColor.RED + "You are not registered. Please tpe /register " + "<password>"
						.repeat(this.config.confirmPassword() ? 2 : 1));
				return;
			}

			String rawHash = secret.get();
			Hash hash = Hash.fromString(rawHash);
			NamespacedIdentity algorithm = NamespacedIdentity.create(
					hash.properties().getString(Constants.HASH_PROP_NAMESPACE_ID),
					hash.properties().getString(Constants.HASH_PROP_RESOURCE_ID)
			);

			Optional<HashMethod> algorithmMethod = api.hashes().get(algorithm, Purpose.PASSWORD);
			if (algorithmMethod.isEmpty()) {
				Bukkit.getScheduler().callSyncMethod(this.plugin, () -> {
					hook.failure(Result.KICK.reason("Could not continue with logging: Password algorithm is not available"));
					return null;
				});

				return;
			}

			HashMethod method = algorithmMethod.get();
			if (!method.verify(hash, password)) {
				session.stage(Stage.PENDING);
				player.sendMessage(ChatColor.RED + "Wrong password. Please try again");

				return;
			}

			if (method.requiresUpdate(hash)) {
				Hash newHash = method.hash(password);
				account.put(Account.SECRET_PASSWORD, newHash.toString());

				account.save();
			}

			player.sendMessage(ChatColor.GREEN + "Successfully logged in");
			Bukkit.getScheduler().callSyncMethod(this.plugin, hook);
		});

		return true;
	}
}
