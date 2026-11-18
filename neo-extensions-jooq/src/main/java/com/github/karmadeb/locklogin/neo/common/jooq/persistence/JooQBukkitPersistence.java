package com.github.karmadeb.locklogin.neo.common.jooq.persistence;

import com.github.karmadeb.locklogin.neo.api.persistence.PersistenceManager;
import com.github.karmadeb.locklogin.neo.api.persistence.driver.PersistenceDriver;
import com.github.karmadeb.locklogin.neo.api.user.UserManager;
import org.jetbrains.annotations.NotNull;

// TODO: Implement
public final class JooQBukkitPersistence extends PersistenceManager {

	/**
	 * Create a new persistence manager
	 *
	 * @param driver the driver
	 */
	public JooQBukkitPersistence(final PersistenceDriver driver) {
		super(driver);
	}

	/**
	 * Get the user manager
	 *
	 * @return the user manager
	 */
	@Override
	public @NotNull UserManager users() {
		return null;
	}
}
