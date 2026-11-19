package com.github.karmadeb.locklogin.neo.common.jooq.persistence.manager.bukkit;

import com.github.karmadeb.locklogin.neo.api.security.authentication.AuthSession;
import com.github.karmadeb.locklogin.neo.api.user.CacheAccountManager;
import com.github.karmadeb.locklogin.neo.api.user.UserManager;
import com.github.karmadeb.locklogin.neo.api.user.account.Account;
import com.github.karmadeb.locklogin.neo.common.jooq.orm.Tables;
import com.github.karmadeb.locklogin.neo.common.jooq.orm.tables.records.UsersRecord;
import com.github.karmadeb.locklogin.neo.common.jooq.persistence.driver.JooQDriver;
import com.github.karmadeb.locklogin.neo.common.jooq.persistence.user.NeoUserAccount;
import com.github.karmadeb.locklogin.neo.common.security.authentication.NeoAuthSession;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jooq.JSON;
import org.jspecify.annotations.NonNull;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

// TODO: Implement cache
public final class BukkitUserManager implements UserManager<Player> {

	private final JooQDriver driver;
	private final Map<UUID, NeoAuthSession<Player>> sessions = new ConcurrentHashMap<>();

	@Contract("null -> fail")
	public BukkitUserManager(final JooQDriver driver) {
		this.driver = Objects.requireNonNull(driver);
	}

	/**
	 * Get the existing session for the specified
	 * player. If for any reason the player does not yet
	 * have a session, this method will return an empty
	 * optional
	 *
	 * @param player the player
	 * @return the player session
	 */
	@Override
	public @NotNull Optional<? extends AuthSession<Player>> session(final @NotNull Player player) {
		return Optional.of(this.sessions.computeIfAbsent(player.getUniqueId(),
				_ -> new NeoAuthSession<>(Player.class)));
	}

	/**
	 * Get the existing account for the specified
	 * player. If the player never connected to the
	 * server this method creates a new account instance
	 * and returns it, implementations should use the
	 * player instance to fill the required fields.
	 *
	 * @param player the player
	 * @return the player account fetch task
	 */
	@Override
	public @NotNull CompletableFuture<Optional<? extends Account>> account(@NonNull Player player) {
		UUID neoId = UUID.nameUUIDFromBytes(("NeoAuthPlayer:" + player.getUniqueId()).getBytes(StandardCharsets.UTF_8));

		return this.driver.execute(context -> {
			UsersRecord record = context.selectFrom(Tables.USERS)
					.where(Tables.USERS.UUID.eq(neoId))
					.fetchOne();

			if (record == null) {
				LocalDateTime now = LocalDateTime.now(Clock.systemUTC());

				record = context.insertInto(Tables.USERS)
						.set(Tables.USERS.UUID, neoId)
						.set(Tables.USERS.USERNAME, player.getName())
						.set(Tables.USERS.FIRST_SEEN_AT, now)
						.set(Tables.USERS.LAST_SEEN_AT, now)
						.set(Tables.USERS.SECRETS, JSON.json("{}"))
						.returning()
						.fetchOne();
			}

			NeoUserAccount account = new NeoUserAccount(this.driver, record);
			return Optional.of(account);
		});
	}

	/**
	 * Get an existing account by its ID. If no record
	 * with ID is present, this method return an empty
	 * optional. The core difference between this and
	 * {@link #account(Player)} is that this method does not
	 * create a new account instance as required.
	 *
	 * @param id the account id
	 * @return the player account fetch task
	 */
	@Override
	public @NotNull CompletableFuture<Optional<? extends Account>> account(UUID id) {
		UUID neoId = UUID.nameUUIDFromBytes(("NeoAuthPlayer:" + id).getBytes(StandardCharsets.UTF_8));

		return this.driver.execute(context -> {
			UsersRecord record = context.selectFrom(Tables.USERS)
					.where(Tables.USERS.UUID.eq(neoId))
					.fetchOne();

			if (record == null) {
				return Optional.empty();
			}

			NeoUserAccount account = new NeoUserAccount(this.driver, record);
			return Optional.of(account);
		});
	}

	/**
	 * Get the cache-bound user manager
	 *
	 * @return the manager
	 */
	@Override
	public @NotNull CacheAccountManager<Player> cache() {
		return null;
	}
}
