package com.github.karmadeb.locklogin.neo.common.jooq.persistence;

import com.github.karmadeb.locklogin.neo.api.persistence.PersistenceManager;
import com.github.karmadeb.locklogin.neo.api.user.UserManager;
import com.github.karmadeb.locklogin.neo.common.jooq.persistence.driver.JooQDriver;
import com.github.karmadeb.locklogin.neo.common.jooq.persistence.manager.bukkit.BukkitUserManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class JooQBukkitPersistence extends PersistenceManager<Player> {

	private final BukkitUserManager manager;

	/**
	 * Create a new persistence manager
	 *
	 * @param driver the driver
	 */
	@Contract("null -> fail")
	public JooQBukkitPersistence(final JooQDriver driver) {
		super(driver);
		this.manager = new BukkitUserManager(driver);
	}

	/**
	 * Get the user manager
	 *
	 * @return the user manager
	 */
	@Override
	public @NotNull UserManager<Player> users() {
		return this.manager;
	}
}
